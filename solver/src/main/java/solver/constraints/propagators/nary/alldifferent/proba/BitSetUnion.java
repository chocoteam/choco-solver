package solver.constraints.propagators.nary.alldifferent.proba;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import solver.variables.IntVar;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 */
public class BitSetUnion implements IUnion {

    IStateBitSet values;
    IStateInt[] occurrences;
    int offset;

    int lastInstValuePos;
    int lastLowValuePos;
    int lastUppValuePos;

    public BitSetUnion(IntVar[] variables, IEnvironment environment) {
        offset = Integer.MAX_VALUE;
        Set<Integer> vals = new HashSet<Integer>();
        for (IntVar var : variables) {
            int ub = var.getUB();
            int lb = var.getLB();
            if(offset > lb) {
                offset = lb;
            }
            for (int value = lb; value <= ub; value = var.nextValue(value)) {
                vals.add(value);
            }
        }
        //System.out.println(vals);
        values = environment.makeBitSet(vals.size());
        occurrences = new IStateInt[vals.size()];
        for (int i = 0; i < vals.size(); i++) {
            occurrences[i] = environment.makeInt(0);
        }
        for (IntVar var : variables) {
            int ub = var.getUB();
            int lb = var.getLB();
            for (int value = lb; value <= ub; value = var.nextValue(value)) {
                values.set(value-offset,true);
                occurrences[value-offset].add(1);
            }
        }
    }

    @Override
    public void remove(int value) {
        int idxValue = value-offset;
        occurrences[idxValue].add(-1);
        if (occurrences[idxValue].get() == 0) {
            values.set(idxValue,false);
        }
    }

    @Override
    public void instantiatedValue(int value, int low, int upp) {
        lastInstValuePos = value-offset; // todo : false
        lastLowValuePos = value-offset; // todo : false
        lastUppValuePos = value-offset; // todo : false
        remove(value);
    }

    @Override
    public int getLastInstValuePos() {
        return lastInstValuePos;
    }

    @Override
    public int getLastLowValuePos() {
        return lastLowValuePos;
    }

    @Override
    public int getLastUppValuePos() {
        return lastUppValuePos;
    }

    @Override
    public int getSize() {
        return values.cardinality();
    }

    @Override
    public int getOccOf(int value) {
        int idxValue = value-offset;
        return occurrences[idxValue].get();
    }
}
