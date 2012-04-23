package solver.constraints.propagators.nary.alldifferent.proba;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import gnu.trove.map.hash.TIntObjectHashMap;
import solver.probabilities.DedicatedS64BitSet;
import solver.variables.IntVar;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 */
public class BitSetUnion {

    private TIntObjectHashMap<IStateInt[]> bounds;

    private DedicatedS64BitSet values;
    private IStateInt[] occurrences;
    private int[] positions;
    private int offset;

    public BitSetUnion(IntVar[] variables, IEnvironment environment) {
        this.bounds = new TIntObjectHashMap<IStateInt[]>(variables.length);
        offset = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        Set<Integer> vals = new HashSet<Integer>();
        for (IntVar var : variables) {
            int ub = var.getUB();
            int lb = var.getLB();
            if (offset > lb) {
                offset = lb;
            }
            if (max < ub) {
                max = ub;
            }
            for (int value = lb; value <= ub; value = var.nextValue(value)) {
                vals.add(value);
            }
        }
        int size = max-offset+1;
        values = new DedicatedS64BitSet(environment,size);//environment.makeBitSet(size);
        occurrences = new IStateInt[size];
        for (int i = 0; i < size; i++) {
            occurrences[i] = environment.makeInt(0);
        }
        for (IntVar var : variables) {
            int vid = var.getId();
            int ub = var.getUB();
            int lb = var.getLB();
            IStateInt[] iBounds = new IStateInt[]{environment.makeInt(lb - offset), environment.makeInt(ub - offset)};
            bounds.put(vid, iBounds);
            for (int value = lb; value <= ub; value = var.nextValue(value)) {
                values.set(value - offset, true);
                if (!var.instantiated()) {
                    occurrences[value - offset].add(1);
                }
            }
        }
        this.positions = new int[3];
    }

    public void remove(int value, IntVar var) {
        int varid = var.getId();
        IStateInt[] t = bounds.get(varid);
        t[0].set(var.getLB() - offset);
        t[1].set(var.getUB() - offset);
        int idxValue = value - offset;
        if (this.contain(idxValue)) {
            // mise a jour des occurrences
            occurrences[idxValue].add(-1);
            if (occurrences[idxValue].get() <= 0) {
                values.clear(idxValue);
            }
        }
        assert check() : "remove " + idxValue + " on " + var + " // " + toString();
    }

    public int[] instantiatedValue(int value, IntVar var) {
        int vinst = value - offset;
        int varid = var.getId();
        IStateInt[] t = bounds.get(varid);
        int vlow = t[0].get();
        int vupp = t[1].get();
        assert contain(vinst) : var + " instanciated value " + vinst + " does not exist in " + values;
        assert contain(vlow);
        assert contain(vupp);

        positions[0] = values.cardinality(vinst);
        positions[1] = values.cardinality(vlow);
        positions[2] = values.cardinality(vupp);// */

        /*int pos = 0;
        int lastInstValuePos = -1;
        int lastLowValuePos = -1;
        int lastUppValuePos = -1;
        for (int i = values.nextSetBit(0); i >= 0; i = values.nextSetBit(i + 1)) {
            if (i == vlow) {
                lastLowValuePos = pos;
            }
            if (i == vinst) {
                lastInstValuePos = pos;
            }
            if (i == vupp) {
                lastUppValuePos = pos;
            }
            pos++;
        }
        positions[0] = lastInstValuePos;
        positions[1] = lastLowValuePos;
        positions[2] = lastUppValuePos;
        assert lastLowValuePos <= lastInstValuePos && lastInstValuePos <= lastUppValuePos : lastLowValuePos + " - " + lastInstValuePos + " - " + lastUppValuePos + " // " + vlow + " - " + vinst + " - " + vupp + " // " + values + " // " + var; //*/
        occurrences[vinst].add(-1);
        if (!this.contain(vinst)) {
            values.clear(vinst);
        }
        return positions;
    }

    public int getSize() {
        return values.cardinality();
    }

    public int getOccOf(int value) {
        int idxValue = value - offset;
        return occurrences[idxValue].get();
    }

    private boolean contain(int normValue) {
        return occurrences[normValue].get() > 0;
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < occurrences.length; i++) {
            s += "<" + i + "[" + offset + "]," + values.get(i) + "," + occurrences[i].get() + ">;";
        }
        return s;
    }

    private boolean check() {
        for (int i = 0; i < occurrences.length; i++) {
            if (values.get(i) && occurrences[i].get() <= 0) {
                return false;
            }
            if (!values.get(i) && occurrences[i].get() > 0) {
                return false;
            }
        }
        return true;
    }
}
