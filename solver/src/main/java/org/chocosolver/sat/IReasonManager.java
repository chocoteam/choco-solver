/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2026, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;

import java.lang.ref.SoftReference;
import java.util.Arrays;

/**
 * An interface to manage the way reason of a propagation or a conflict is represented.
 * <br/>
 *
 * @author Charles Prud'homme
 */
public interface IReasonManager {

    /**
     * @return a shared IndexBasedReason.ReasonClause to avoid creating new objects
     * when extracting the conflict clause from an IndexBasedReason.
     */
    IndexBasedReason.ReasonClause getSharedIndexBasedClause();

    /**
     * Create a reason from a single literal
     *
     * @param d1 a literal
     * @return a reason
     */
    default Reason r(int d1) {
        return new Reason.Reason1().set(d1);
    }

    /**
     * Create a reason from two literals
     *
     * @param d1 a literal
     * @param d2 a literal
     * @return a reason
     */
    default Reason r(int d1, int d2) {
        return new Reason.Reason2().set(d1, d2);
    }

    /**
     * Create a reason from one or more literals.
     * <p>If more than 2 literals are given, the literal at index 0 should be left empty for the asserting literal.
     * </p>
     *
     * @param ds other literals
     * @return a reason
     * @implSpec if length of ps is strictly greater than 2,
     * then the literal at index 0 should be left empty for the asserting literal
     */
    Reason r(int... ds);

    /**
     * Create a reason from one or more literals.
     * <p>If more than 2 literals are given, the literal at index 0 should be left empty for the asserting literal.
     * </p>
     *
     * @param ds other literals
     * @return a reason
     * @implSpec if length of ps is strictly greater than 2,
     * then the literal at index 0 should be left empty for the asserting literal
     */
    Reason r(TIntArrayList ds);

    /**
     * Gather a reason with a new literal.
     *
     * @param r a reason
     * @param p a literal
     * @return a new reason with p added to the literals of r
     */
    Reason gather(Reason r, int p);

    /**
     * @param environment the environment to use for stateful integer management (if needed by the manager implementation)
     * @param version     of the manager to create:
     *                    <ul>
     *                        <li>0: no manager, high GC, low RAM</li>
     *                        <li>1: reason manager, low GC, high RAM</li>
     *                        <li>2: reason manager with chunks, (should be) low GC, low RAM</li>
     *                    </ul>
     * @return the created manager
     * @implSpec if version is 0, the returned manager will not store reasons,
     * and will create new reason objects on the fly for each call to r() or gather(),
     * which may lead to high GC overhead but low RAM usage.
     * If version is 1, the returned manager will store reasons in a flat integer buffer,
     * and reasons will be represented by their index in this buffer,
     * which may lead to low GC overhead but high RAM usage.
     * If version is 2, the returned manager will store reasons in a chunked integer buffer,
     * and reasons will be represented by their index in this buffer,
     * which may lead to low GC overhead and low RAM usage.
     * If version is not 0, 1, or 2, the returned manager will be a no-op manager
     * that does not store reasons and creates new reason objects on the fly, similar to version 0.
     */
    static IReasonManager makeManager(IEnvironment environment, int version) {
        switch (version) {
            case 1:
                return new ReasonManager(environment);
            case 2:
                return new ReasonManagerChunk(environment);
            default:
                return new NoManager();
        }
    }

    final class NoManager implements IReasonManager {

        @Override
        public IndexBasedReason.ReasonClause getSharedIndexBasedClause() {
            return null;
        }

        @Override
        public Reason r(int... ps) {
            if (ps.length == 1) {
                return r(ps[0]);
            } else if (ps.length == 2) {
                return r(ps[0], ps[1]);
            } else if (ps.length > 2) {
                assert ps[0] == 0 : "The first literal should be left empty for the asserting literal";
                return new ArrayClause(ps);
            } else {
                return Reason.UNDEF;
            }
        }

        @Override
        public Reason r(TIntArrayList ps) {
            if (ps.size() == 1) {
                return r(ps.getQuick(0));
            } else if (ps.size() == 2) {
                return r(ps.getQuick(0), ps.getQuick(1));
            } else if (ps.size() > 2) {
                assert ps.getQuick(0) == 0 : "The first literal should be left empty for the asserting literal";
                return new ArrayClause(ps);
            } else {
                return Reason.UNDEF;
            }
        }

        @Override
        public Reason gather(Reason r, int p) {
            if (r instanceof Clause) {
                Clause cl = (Clause) r;
                int[] ps = new int[cl.size() + 1];
                for (int i = 0; i < cl.size(); i++) {
                    ps[i] = cl._g(i);
                }
                ps[cl.size()] = p;
                return r(ps);
            } else if (r instanceof Reason.Reason1) {
                return r(((Reason.Reason1) r).d1, p);
            } else if (r instanceof Reason.Reason2) {
                int[] ps = new int[4];
                ps[0] = 0;// leave space for the asserting literal
                ps[1] = ((Reason.Reason2) r).d1;
                ps[2] = ((Reason.Reason2) r).d2;
                ps[3] = p;
                return r(ps);
            } else if (r instanceof IndexBasedReason) {
                throw new UnsupportedOperationException("Cannot gather a reason of type " + r.getClass().getSimpleName() + " with this ReasonManager");
            } else {
                return r(p);
            }
        }
    }

    /**
     * An IReasonManager that stores reasons in an integer buffer, and reasons are represented by their index in this buffer.
     */
    interface IndexBasedManager extends IReasonManager {

        int getVal(int at);

        void setVal(int at, int value);

        Reason r(IndexBasedReason reason, int p);
    }

    /**
     * An IndexBasedManager that stores reasons in a flat integer buffer,
     * and reasons are represented by their index in this buffer.
     */
    final class ReasonManager implements IndexBasedManager {
        private final IndexBasedReason.ReasonClause sharedClause = new IndexBasedReason.ReasonClause();
        private final IStateInt currentIndex;
        private final IStateInt currentReason;
        private int[] buffer = new int[1024];
        private IndexBasedReason[] reasons = new IndexBasedReason[256];

        public ReasonManager(IEnvironment environment) {
            this.currentIndex = environment.makeInt(0);
            this.currentReason = environment.makeInt(0);
        }

        @Override
        public int getVal(int at) {
            return buffer[at];
        }

        @Override
        public void setVal(int at, int value) {
            buffer[at] = value;
        }

        private Reason getOrMakeReason(int ci) {
            int cr = currentReason.get();
            if (cr >= reasons.length) {
                reasons = Arrays.copyOf(reasons, (int) (reasons.length * 1.5));
            }
            IndexBasedReason reason = reasons[cr];
            if (reason == null) {
                reason = new IndexBasedReason();
                reasons[cr] = reason;
            }
            reason.setUp(ci, this);
            currentReason.set(cr + 1);
            return reason;
        }

        private void ensureCapacity(int startIndex, int size) {
            int current = buffer.length;
            int required = startIndex + size;
            if (required <= buffer.length) {
                return;
            }
            int newLen = current + (current >> 1);
            if (newLen < required) {
                newLen = required;
            }
            buffer = Arrays.copyOf(buffer, newLen);
        }

        @Override
        public IndexBasedReason.ReasonClause getSharedIndexBasedClause() {
            return sharedClause;
        }

        /*
        This method is left here as a comment to illustrate how the chunked manager can be implemented without using array-based manager,
        but it is less efficient than the default implementation.
        @Override
        public Reason r(int d1) {
            int ci = currentIndex.get();
            ensureCapacity(ci, 3);
            buffer[ci] = 2;
            buffer[ci + 1] = 0;
            buffer[ci + 2] = d1;
            currentIndex.add(3);

            return getOrMakeReason(ci);
        }*/

        /*
        This method is left here as a comment to illustrate how the chunked manager can be implemented without using array-based manager,
        but it is less efficient than the default implementation.
        @Override
        public Reason r(int d1, int d2) {
            int ci = currentIndex.get();
            ensureCapacity(ci, 4);
            buffer[ci] = 3;
            buffer[ci + 1] = 0;
            buffer[ci + 2] = d1;
            buffer[ci + 3] = d2;
            currentIndex.add(4);

            return getOrMakeReason(ci);
        }*/

        @Override
        public Reason r(int[] ds) {
            assert ds[0] == 0;
            int ci = currentIndex.get();
            ensureCapacity(ci, ds.length + 1);
            buffer[ci] = ds.length;
            //buffer[ci + 1] = 0; useless since ds[0] == 0
            System.arraycopy(ds, 0, buffer, ci + 1, ds.length);
            currentIndex.add(ds.length + 1);

            return getOrMakeReason(ci);
        }

        @Override
        public Reason r(TIntArrayList ds) {
            int ci = currentIndex.get();
            ensureCapacity(ci, ds.size() + 1);
            buffer[ci] = ds.size();
            //buffer[ci + 1] = 0; useless since ds[0] == 0
            for (int i = 0; i < ds.size(); i++) {
                buffer[ci + 1 + i] = ds.getQuick(i);
            }
            currentIndex.add(ds.size() + 1);

            return getOrMakeReason(ci);
        }

        @Override
        public Reason gather(Reason r, int p) {
            if (r instanceof Clause) {
                Clause cl = (Clause) r;
                int[] ps = new int[cl.size() + 1];
                for (int i = 0; i < cl.size(); i++) {
                    ps[i] = cl._g(i);
                }
                ps[cl.size()] = p;
                return r(ps);
            } else if (r instanceof IndexBasedReason) {
                return r((IndexBasedReason) r, p);
            } else {
                throw new UnsupportedOperationException("Cannot gather a reason of type " + r.getClass().getSimpleName() + " with this ReasonManager");
            }
        }

        @Override
        public Reason r(IndexBasedReason reason, int p) {
            int idx = reason.getIndex();
            int size = buffer[idx];
            int ci = currentIndex.get();
            ensureCapacity(ci, size + 1);

            buffer[ci] = size + 1;
            System.arraycopy(buffer, idx + 1, buffer, ci + 1, size);
            buffer[ci + 1 + size] = p;
            currentIndex.add(size + 2);
            return getOrMakeReason(ci);
        }
    }

    /**
     * An IndexBasedManager that stores reasons in a chunked integer buffer,
     * and reasons are represented by their index in this buffer.
     */
    final class ReasonManagerChunk implements IndexBasedManager {
        private final IndexBasedReason.ReasonClause sharedClause = new IndexBasedReason.ReasonClause();

        private final int chunckShift;
        private final int chunkSize;
        private final int chunkMask;
        private final IStateInt currentIndex;
        private int[][] buffer = new int[16][];
        private int chunksCount = 0;
        private SoftReference<Object> canary = new SoftReference<>(new Object());

        private final IStateInt currentReason;
        private IndexBasedReason[] reasons = new IndexBasedReason[256];

        public ReasonManagerChunk(IEnvironment environment) {
            chunckShift = Integer.getInteger("reason.manager.chunk.shift", 16); // 65536 integers per block
            chunkSize = 1 << chunckShift;
            chunkMask = chunkSize - 1;

            this.currentIndex = environment.makeInt(0);
            addChunk();
            this.currentReason = environment.makeInt(0);
        }

        private void addChunk() {
            if (chunksCount == buffer.length) {
                buffer = Arrays.copyOf(buffer, (int) (buffer.length * 1.5));
            }
            buffer[chunksCount++] = new int[chunkSize];
        }

        private int ensureCapacity(int sizeNeeded) {
            //assert sizeNeeded < CHUNK_SIZE : "cannot allocate more than " + CHUNK_SIZE + ", but " + sizeNeeded + " required";
            int ci = currentIndex.get();
            // 1. OPTIONAL ALIGNMENT
            // We only attempt to align if the reason can fit within ONE chunk.
            // If it is larger, alignment is pointless (it will be fragmented anyway).
            if (sizeNeeded <= chunkSize) {
                if ((ci >> chunckShift) != (ci + sizeNeeded - 1) >> chunckShift) {
                    // Overlap error: move to the beginning of the next block
                    ci = (ci + chunkSize) & ~chunkMask;
                    // Update the solver so we don't lose this space
                    currentIndex.set(ci);
                } else if (canary.get() == null) {
                    // current buffer used:
                    int bi = (ci >> chunckShift);
                    Arrays.fill(buffer, bi + 1, chunksCount, null);
                    chunksCount = bi + 1;
                    canary = new SoftReference<>(new Object());
                }
            }

            // 3. Check if we have enough physical blocks allocated
            // lastIndex is the position of the last integer we are going to write
            int lastIndex = ci + sizeNeeded - 1;

            // While the required index exceeds total capacity (number of blocks * block size)
            while ((lastIndex >> chunckShift) >= chunksCount) {
                addChunk();
            }
            return ci;
        }

        @Override
        public IndexBasedReason.ReasonClause getSharedIndexBasedClause() {
            return sharedClause;
        }

        @Override
        public void setVal(int currentIndex, int value) {
            buffer[currentIndex >> chunckShift][currentIndex & chunkMask] = value;
        }

        @Override
        public int getVal(int currentIndex) {
            return buffer[currentIndex >> chunckShift][currentIndex & chunkMask];
        }

        private Reason getOrMakeReason(int ci) {
            int cr = currentReason.get();
            if (cr >= reasons.length) {
                reasons = Arrays.copyOf(reasons, (int) (reasons.length * 1.5));
            }
            IndexBasedReason reason = reasons[cr];
            if (reason == null) {
                reason = new IndexBasedReason();
                reasons[cr] = reason;
            }
            reason.setUp(ci, this);
            currentReason.set(cr + 1);
            return reason;
        }

       /*
       This method is left here as a comment to illustrate how the chunked manager can be implemented without using chunk-based manager,
       but it is less efficient than the default implementation.
        @Override
        public Reason r(int d1) {
            int ci = ensureCapacity(3);
            setVal(ci, 2);                   // size
            setVal(ci + 1, 0);    // reserved
            setVal(ci + 2, d1);         // d1
            currentIndex.add(3);
            return getOrMakeReason(ci);
        }*/


        /*
        This method is left here as a comment to illustrate how the chunked manager can be implemented without using chunk-based manager,
        but it is less efficient than the default implementation.
        @Override
        public Reason r(int d1, int d2) {
            int ci = ensureCapacity(4);
            setVal(ci, 3);                  // size
            setVal(ci + 1, 0);  // reserved
            setVal(ci + 2, d1);       // d1
            setVal(ci + 3, d2);       // d2
            currentIndex.add(4);
            return getOrMakeReason(ci);
        }*/

        @Override
        public Reason r(int... ds) {
            if (ds.length == 1) {
                return r(ds[0]);
            } else if (ds.length == 2) {
                return r(ds[0], ds[1]);
            } else if (ds.length > 2) {
                assert ds[0] == 0;
                int size = ds.length;
                int ci = ensureCapacity(size + 1);
                setVal(ci, size);                     // size
                // setVal(ci + 1, 0);  useless, see assert
                //System.arraycopy(ds, 0, buffer[(ci + 1) >> CHUNK_SHIFT], (ci & CHUNK_MASK) + 1, size);
                writeArray(ci + 1, ds);
                currentIndex.add(size + 1);
                return getOrMakeReason(ci);
            } else {
                return Reason.UNDEF;
            }
        }

        private void writeArray(int targetIndex, int[] ds) {
            int remaining = ds.length;
            int srcPos = 0;
            int currentTarget = targetIndex;

            while (remaining > 0) {
                int blockIdx = currentTarget >> chunckShift;
                int offset = currentTarget & chunkMask;

                // Remaining space in the current chunk
                int spaceInCurrentChunk = chunkSize - offset;

                // Copy either all remaining elements, or as many as fit in the chunk
                int amountToCopy = Math.min(remaining, spaceInCurrentChunk);

                System.arraycopy(ds, srcPos, buffer[blockIdx], offset, amountToCopy);

                remaining -= amountToCopy;
                srcPos += amountToCopy;
                currentTarget += amountToCopy;
            }
        }


        @Override
        public Reason r(TIntArrayList ds) {
            if (ds.size() == 1) {
                return r(ds.getQuick(0));
            } else if (ds.size() == 2) {
                return r(ds.getQuick(0), ds.getQuick(1));
            } else if (ds.size() > 2) {
                assert ds.getQuick(0) == 0;
                int size = ds.size();
                int ci = ensureCapacity(ds.size() + 1);
                setVal(ci, size);                     // size
                // setVal(ci + 1, 0);  useless, see assert
                writeArray(ci + 1, ds);
                currentIndex.add(ds.size() + 1);
                return getOrMakeReason(ci);
            } else {
                return Reason.UNDEF;
            }
        }

        private void writeArray(int targetIndex, TIntArrayList ds) {
            int remaining = ds.size();
            int srcPos = 0;
            int currentTarget = targetIndex;

            while (remaining > 0) {
                int blockIdx = currentTarget >> chunckShift;
                int offset = currentTarget & chunkMask;

                // Remaining space in the current chunk
                int spaceInCurrentChunk = chunkSize - offset;

                // Copy either all remaining elements, or as many as fit in the chunk
                int amountToCopy = Math.min(remaining, spaceInCurrentChunk);
                int[] _buffer = buffer[blockIdx];
                for (int i = 0, j = 0; i < amountToCopy; i++) {
                    _buffer[offset + (j++)] = ds.getQuick(srcPos + i);
                }
                remaining -= amountToCopy;
                srcPos += amountToCopy;
                currentTarget += amountToCopy;
            }
        }

        @Override
        public Reason gather(Reason r, int p) {
            if (r instanceof Clause) {
                Clause cl = (Clause) r;
                int[] ps = new int[cl.size() + 1];
                for (int i = 0; i < cl.size(); i++) {
                    ps[i] = cl._g(i);
                }
                ps[cl.size()] = p;
                return r(ps);
            } else if (r instanceof Reason.Reason1) {
                return r(((Reason.Reason1) r).d1, p);
            } else if (r instanceof Reason.Reason2) {
                int[] ps = new int[4];
                ps[0] = 0;// leave space for the asserting literal
                ps[1] = ((Reason.Reason2) r).d1;
                ps[2] = ((Reason.Reason2) r).d2;
                ps[3] = p;
                return r(ps);
            } else if (r instanceof IndexBasedReason) {
                return r((IndexBasedReason) r, p);
            } else {
                throw new UnsupportedOperationException("Cannot gather a reason of type " + r.getClass().getSimpleName() + " with this ReasonManager");
            }
        }


        /**
         * @implSpec The 0s in the reason of the given IndexBasedReason are ignored and not copied to the new reason.
         * This is because
         */
        @Override
        public Reason r(IndexBasedReason reason, int p) {
            int idx = reason.getIndex();
            int size = getVal(idx);
            int ci = ensureCapacity(size + 2);
            setVal(ci, size + 1); // set size
            // setVal(ci + 1, 0);  useless, it is a copy of a verified reason
            writeArray(ci + 1, idx + 1, size); // copy the old reason
            setVal(ci + 1 + size, p); // add the new literal
            currentIndex.add(size + 2); // +1 to save the size and +1 for the new lit
            return getOrMakeReason(ci);
        }

        private void writeArray(int targetIndex, int srcIndex, int size) {
            int remaining = size;
            int currSrc = srcIndex;
            int currTarget = targetIndex;

            while (remaining > 0) {
                // source location
                int srcBlockIdx = currSrc >> chunckShift;
                int srcOffset = currSrc & chunkMask;
                // destination location
                int dstBlockIdx = currTarget >> chunckShift;
                int dstOffset = currTarget & chunkMask;
                // calculating the available space in each respective chunk
                int spaceInSrcChunk = chunkSize - srcOffset;
                int spaceInDstChunk = chunkSize - dstOffset;
                // smallest of the three space to ensure we never go over the limit
                int amountToCopy = Math.min(remaining,
                        Math.min(spaceInSrcChunk, spaceInDstChunk));
                System.arraycopy(buffer[srcBlockIdx], srcOffset,
                        buffer[dstBlockIdx], dstOffset,
                        amountToCopy);
                // cursor update
                currSrc += amountToCopy;
                currTarget += amountToCopy;
                remaining -= amountToCopy;
            }
        }
    }
}

