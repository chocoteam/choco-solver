package solver.probabilities;

import memory.IEnvironment;
import memory.structure.S64BitSet;
import memory.trailing.EnvironmentTrailing;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 */
public class DedicatedS64BitSet extends S64BitSet {


    public DedicatedS64BitSet(IEnvironment environment) {
        super(environment);
    }

    public DedicatedS64BitSet(IEnvironment environment, int nbits) {
        super(environment, nbits);
    }

    public int cardinality(int toIndex) {
        int sum = 0;
        int wordOfToIndex = wordIndex(toIndex);
        long lastWord = words[wordOfToIndex].get();
        if (wordOfToIndex == 0) {
            if (toIndex + 1 != BITS_PER_WORD) {
                long firstWordMask = WORD_MASK << toIndex + 1;
                long lastWordMask = WORD_MASK >>> -BITS_PER_WORD;
                lastWord = lastWord & ~(firstWordMask & lastWordMask);
            }
        } else {
            for (int i = wordOfToIndex - 1; i >= 0; i--) {
                sum += Long.bitCount(words[i].get());
            }
            int toIndexInWord = toIndex - (wordOfToIndex * BITS_PER_WORD);
            if (toIndexInWord + 1 != BITS_PER_WORD) {
                long firstWordMask = WORD_MASK << toIndexInWord + 1;
                long lastWordMask = WORD_MASK >>> -BITS_PER_WORD;
                lastWord = lastWord & ~(firstWordMask & lastWordMask);
            }
        }
        //assert lastWord >=0 : toIndex + " - " + this;
        sum += Long.bitCount(lastWord);
        return sum;
    }

    public static void main(String[] args) {
        int size = 134;
        IEnvironment env = new EnvironmentTrailing();
        DedicatedS64BitSet bs = new DedicatedS64BitSet(env, size);
        for (int i = 28; i < 67; i++) {
            bs.set(i, true);
        }
        bs.clear(65);
        System.out.println(bs.cardinality());
        System.out.println(bs.cardinality(63));
        System.out.println(bs.cardinality(size - 1));
    }

}
