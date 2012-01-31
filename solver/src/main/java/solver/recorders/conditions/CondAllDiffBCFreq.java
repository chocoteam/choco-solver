package solver.recorders.conditions;

import choco.kernel.memory.IEnvironment;
import solver.variables.IntVar;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 22/11/11
 */
public class CondAllDiffBCFreq extends CondAllDiffBC {

    final int frequency;
    int count;

    public CondAllDiffBCFreq(IEnvironment environment, IntVar[] vars, int frequency) {
        super(environment, vars);
        this.frequency = frequency;
        this.count = 0;
    }

    @Override
    boolean isValid() {
        count++;
        return count%frequency==0;
    }

}
