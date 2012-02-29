package solver.constraints.propagators.nary;

import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.probabilistic.propagators.nary.PropProbaAllDiffBC;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 27/02/12
 */
public class PropAllDiffRC extends PropProbaAllDiffBC {


    public PropAllDiffRC(IntVar[] vars, Solver solver, IntConstraint constraint) {
        super(vars, solver, constraint);
    }

    protected void initialize() throws ContradictionException {
        Deque<IntVar> modified = new ArrayDeque<IntVar>();
        for (IntVar init : vars) {
            if (init.instantiated()) {
                modified.push(init);
            }
        }
        while (!modified.isEmpty()) {
            IntVar cur = modified.pop();
            int valCur = cur.getValue();
            for (IntVar toCheck : vars) {
                if (toCheck != cur && toCheck.contains(valCur)) {
                    toCheck.removeValue(valCur, this);
                    if (toCheck.instantiated()) {
                        modified.push(toCheck);
                    }
                }
            }
        }
    }

    protected void awakeOnInst(int i) throws ContradictionException {   // Propagation classique
        infBoundModified = true;
        supBoundModified = true;
        Deque<IntVar> modified = new ArrayDeque<IntVar>();
        modified.push(vars[i]);
        while (!modified.isEmpty()) {
            IntVar cur = modified.pop();
            int valCur = cur.getValue();
            for (IntVar toCheck : vars) {
                if (toCheck != cur && toCheck.contains(valCur)) {
                    toCheck.removeValue(valCur, this);
                    if (toCheck.instantiated()) {
                        modified.push(toCheck);
                    }
                }
            }
        }
    }


}
