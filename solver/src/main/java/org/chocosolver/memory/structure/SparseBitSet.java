package org.chocosolver.memory.structure;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateBitSet;

import java.util.Arrays;

/**
 * A backtrackable bitset optimised for compaction. This implementation saves
 * memory when the content is sparse, typically when it contains a few bit
 * sets or clustered bitsets. The implementation relies on an allocation per
 * block: setting a bit on a high index will only lead to the allocation of
 * the corresponding block, contrary to {@link S64BitSet} that will allocate
 * memory up to the desired index whatever the actual need.
 * <p>
 * In terms of memory efficiency, the benefits will depend on the density of the
 * bit sets and the block size. When some memory regions are set then
 * cleared, the memory is not reclaimed. Accordingly, if the bitset is full of 1
 * early and cleared over the time, then no savings are possible.
 * {@link #equals(Object)} and {@link #hashCode()} supports with any kind of
 * {@link IStateBitSet} implementation but they must not be used in any
 * performance sensitive context.
 */
public class SparseBitSet implements IStateBitSet {

  /** Block size in bits. */
  private final int blockSize;

  /**
   * The index declares the opened blocks in the current world. This is not
   * required for correctness but speed up iterations to only browse
   * meaningful blocks.
   */
  private final S64BitSet index;

  /** The blocks. */
  private IStateBitSet[] blocks;

  /**
   * The environment to use to create internal backtrackable variables.
   */
  private final IEnvironment env;

  /**
   * @param env       backtracking environment.
   * @param blockSize block size in bits.
   */
  public SparseBitSet(final IEnvironment env, final int blockSize) {
    this.env = env;
    if (blockSize <= 0) {
      throw new IllegalArgumentException(
          "Block size must be > 0. Got " + blockSize);
    }
    this.blockSize = blockSize;
    blocks = new IStateBitSet[0];
    index = new S64BitSet(env);
  }

  /**
   * Check that the given index is strictly positive.
   *
   * @param index the index
   * @throws IndexOutOfBoundsException if the index is negative
   */
  private static void requirePositiveIndex(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(
          "Positive index expected. Got " + index);
    }
  }

  /**
   * Check the validity of a range of indices.
   * Both indices must be strictly positive and the second one must be greater
   * than the first one.
   *
   * @param from lower bound.
   * @param to   upper bound.
   * @throws IndexOutOfBoundsException if the range is invalid.
   */
  private static void validIndexRange(final int from, final int to) {
    requirePositiveIndex(from);
    requirePositiveIndex(to);
    if (from > to) {
      throw new IndexOutOfBoundsException(
          "Invalid range: [" + from + ", " + to + ")");
    }
  }

  /**
   * Get the block index for a given bit.
   * This does not ensure that the block exists or that the index is big enough.
   *
   * @param bit the bit
   * @return the block index.
   */
  private int blockIndex(final int bit) {
    return bit / blockSize;
  }

  /**
   * Get the bitset-level index value from a block-level index.
   *
   * @param blockIdx   the block index.
   * @param localIndex the block-level index.
   * @return the absolute index.
   */
  private int absIndex(final int blockIdx, final int localIndex) {
    return blockIdx * blockSize + localIndex;
  }

  /**
   * Get the block-level index from a bitset-level one. This does
   * not indicate which block must be used (see {@link #blockIndex(int)}).
   *
   * @param absIndex the bit.
   * @return the block-level index to use.
   */
  private int localIndex(final int absIndex) {
    return absIndex % blockSize;
  }

  /**
   * Ensure that the index is big enough.
   * The index is growth to the given value if needed but no blocks are however
   * created.
   *
   * @param size the desired index size.
   */
  private void ensureIndexCapacity(final int size) {
    if (size >= blocks.length) {
      blocks = Arrays.copyOf(blocks, size + 1);
    }
  }

  /**
   * If needed, create the block at the given block index.
   * The index is considered to be big enough.
   *
   * @param blockIndex the block index.
   * @return the block at this index.
   */
  private IStateBitSet ensureBlock(final int blockIndex) {
    if (blocks[blockIndex] == null) {
      // Create the block and register it.
      blocks[blockIndex] = new S64BitSet(env, 64);
    }
    index.set(blockIndex);
    return blocks[blockIndex];
  }

  @Override
  public void set(final int bit) {
    requirePositiveIndex(bit);
    // Block index.
    final int bIdx = blockIndex(bit);
    // Ensure the index is big enough.
    ensureIndexCapacity(bIdx);
    // Set the right offset in the block.
    ensureBlock(bIdx).set(localIndex(bit));
  }

  @Override
  public void clear(final int bit) {
    requirePositiveIndex(bit);
    // Which block.
    final int bIdx = blockIndex(bit);
    if (!index.get(bIdx)) {
      // The block is not registered in this world. Nothing to clear.
      return;
    }
    // The block exists and is registered. Clear at the offset.
    blocks[bIdx].clear(localIndex(bit));
  }

  @Override
  public void set(final int bit, final boolean flag) {
    requirePositiveIndex(bit);
    if (flag) {
      set(bit);
    } else {
      clear(bit);
    }
  }

  @Override
  public boolean get(final int bit) {
    requirePositiveIndex(bit);
    // Which block.
    final int bIdx = blockIndex(bit);
    if (!index.get(bIdx)) {
      // Un-registered block.
      return false;
    }
    return blocks[bIdx].get(localIndex(bit));
  }

  @Override
  public int size() {
    return index.size() * blockSize;
  }

  /**
   * Get the number of bits actually used to store data.
   * This accounts for the blocks and the index sizes.
   * @return a positive amount
   */
  public long memorySize() {
    long size = index.size();
    for (int bIdx = index.nextSetBit(0); bIdx >= 0;
         bIdx = index.nextSetBit(bIdx + 1)) {
      final IStateBitSet bs = blocks[bIdx];
      assert bs != null;
      size += bs.size();
    }
    return size;
  }

  @Override
  public int cardinality() {
    int sum = 0;
    for (int bIdx = index.nextSetBit(0); bIdx >= 0;
         bIdx = index.nextSetBit(bIdx + 1)) {
      final IStateBitSet bs = blocks[bIdx];
      assert bs != null;
      sum += bs.cardinality();
    }
    return sum;
  }

  @Override
  public void clear() {
    // Clear the content and the index.
    for (int bIdx = index.nextSetBit(0); bIdx >= 0;
         bIdx = index.nextSetBit(bIdx + 1)) {
      blocks[bIdx].clear();
    }
    index.clear();
  }

  @Override
  public boolean isEmpty() {
    if (index.isEmpty()) {
      // No blocks so for sure an empty bitset.
      return true;
    }
    // One non-empty block is sufficient.
    for (int bIdx = index.nextSetBit(0); bIdx >= 0;
         bIdx = index.nextSetBit(bIdx + 1)) {
      if (!blocks[bIdx].isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void clear(final int from, final int to) {
    validIndexRange(from, to);
    if (from == to) {
      return;
    }
    // Go over every impacted blocks. The first and the last may be partially
    // set.
    for (int bIdx = blockIndex(from); bIdx <= blockIndex(to); bIdx++) {
      if (bIdx >= blocks.length) {
        // The block is passed the index size. Thus for sure everything here is
        // cleared.
        return;
      }
      if (!this.index.get(bIdx)) {
        // No block allocated here, nothing to clear;
        continue;
      }
      final int st;
      if (bIdx == blockIndex(from)) {
        st = from;
      } else {
        st = 0;
      }
      // local end is the block end for all the block, except the last one
      final int ed;
      if (bIdx == blockIndex(to)) {
        ed = localIndex(to);
      } else {
        ed = blockSize;
      }
      ensureBlock(bIdx).clear(st, ed);
    }
  }

  @Override
  public void set(final int from, final int to) {
    validIndexRange(from, to);
    if (from == to) {
      return;
    }
    // Grows the index.
    ensureIndexCapacity(blockIndex(to));
    // Go over every impacted blocks. The first and the last may be partially
    // set.
    final int firstBlock = blockIndex(from);
    final int lastBlock = blockIndex(to);
    for (int bIdx = firstBlock; bIdx <= lastBlock; bIdx++) {
      // local start is offset(from) for the first block only, otherwise 0.
      final int st;
      if (bIdx == firstBlock) {
        st = localIndex(from);
      } else {
        st = 0;
      }
      // local end is the block end for all the block, except the last one
      final int ed;
      if (bIdx == lastBlock) {
        ed = localIndex(to);
      } else {
        ed = blockSize;
      }
      ensureBlock(bIdx).set(st, ed);
    }
  }

  @Override
  public int nextSetBit(final int fromIndex) {
    requirePositiveIndex(fromIndex);
    final int startingBlock = blockIndex(fromIndex);

    // Iterate over all the blocks starting from the current index to pick the
    // first bit set.
    for (int bIdx = index.nextSetBit(startingBlock); bIdx >= 0;
         bIdx = index.nextSetBit(bIdx + 1)) {
      // For the current block, the offset is the one associated to fromIndex
      // but at the moment the next blocks are browsed, the offset is 0 to grab
      // the first bit set.
      final int offset;
      if (bIdx > startingBlock) {
        offset = 0;
      } else {
        offset = localIndex(fromIndex);
      }
      int bit = blocks[bIdx].nextSetBit(offset);
      if (bit >= 0) {
        // Found it.
        return absIndex(bIdx, bit);
      }
    }
    return -1;
  }

  @Override
  public int prevSetBit(final int fromIndex) {
    requirePositiveIndex(fromIndex);
    final int lastBlockIdx = blockIndex(fromIndex);
    // Iterate over all the blocks backward, starting from the current index to
    // pick the first bit set.
    for (int bIdx = index.prevSetBit(lastBlockIdx); bIdx >= 0;
         bIdx = index.prevSetBit(bIdx - 1)) {
      // For the current block, the offset is the one associated to fromIndex
      // but at the moment the previous blocks are browsed, the offset is
      // 'blockSize' to grab the first bit set from the end.
      int offset = localIndex(fromIndex);
      if (bIdx < lastBlockIdx) {
        offset = blockSize;
      }
      int bit = blocks[bIdx].prevSetBit(offset);
      if (bit >= 0) {
        // Found it.
        return absIndex(bIdx, bit);
      }
    }
    return -1;
  }

  @Override
  public int nextClearBit(final int fromIndex) {
    requirePositiveIndex(fromIndex);
    final int fromBlock = blockIndex(fromIndex);
    int curBlock = fromBlock;
    while (curBlock < blocks.length) {
      if (blocks[curBlock] == null || !index.get(curBlock)) {
        // null block. fromIndex is then clear for sure.
        // In case the block is not null, check  the index in case we have set
        // bits but a cleared block that only clear the index.
        return fromIndex;
      }
      // local offset depending on the block under inspection.
      final int localOff;
      if (curBlock > fromBlock) {
        // Intermediate block, start at 0.
        localOff = 0;
      } else {
        // First block, start from fromIndex.
        localOff = localIndex(fromIndex);
      }
      final int nextClear = blocks[curBlock].nextClearBit(localOff);
      if (nextClear != blockSize) {
        // Not all the bits are set, the first clear bit is here.
        return absIndex(curBlock, nextClear);
      }
      // All the bits are set, check the next block.
      curBlock++;
    }
    return fromIndex;
  }

  @Override
  public int prevClearBit(final int fromIndex) {
    requirePositiveIndex(fromIndex);
    final int fromBlock = blockIndex(fromIndex);
    if (fromBlock >= index.length()) {
      // Outside the current index. For sure there is a cleared bit at fromIndex.
      return fromIndex;
    }
    int curBlock = fromBlock;
    while (curBlock >= 0) {
      if (!index.get(curBlock)) {
        // null block. fromIndex is then clear for sure. Possibly also a cleared
        // block.
        return fromIndex;
      }
      // local offset depending on the block under inspection.
      final int localOff;
      if (curBlock < fromBlock) {
        // Intermediate block, start at blockSize.
        localOff = blockSize - 1;
      } else {
        // First block, start from fromIndex.
        localOff = localIndex(fromIndex);
      }
      final int prevClear = blocks[curBlock].prevClearBit(localOff);
      if (prevClear >= 0) {
        // Not all the bits are set, the first clear bit is here.
        return absIndex(curBlock, prevClear);
      }
      // All the bits are set, check the previous block.
      curBlock--;
    }
    // No cleared bit in any block.
    return -1;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IStateBitSet)) {
      return false;
    }

    final IStateBitSet that = (IStateBitSet) o;
    // Fail fast.
    if (this.cardinality() != that.cardinality()) {
      return false;
    }
    // Same cardinality. Iterate over the bit sets. Those must be sets in 'that'
    // as well.
    for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i + 1)) {
      if (!that.get(i)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;
    for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i + 1)) {
      hashCode = hashCode * 31 + i;
    }
    return hashCode;
  }

  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder();
    b.append('{');
    int i = nextSetBit(0);
    if (i != -1) {
      b.append(i);
      for (i = nextSetBit(i + 1); i >= 0; i = nextSetBit(i + 1)) {
        b.append(", ").append(i);
      }
    }
    b.append('}');
    return b.toString();
  }
}
