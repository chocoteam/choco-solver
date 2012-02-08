package solver.recorders.conditions;

import choco.kernel.memory.IEnvironment;
import solver.constraints.propagators.Propagator;
import solver.recorders.IEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 3 nov. 2011
 */
public class CondAllDiffBC<R extends IEventRecorder> extends AbstractCondition<R> {

    protected IntVar[] vars;

    public CondAllDiffBC(IEnvironment environment, IntVar[] vars) {
        super(environment);
        this.vars = vars;
        next = new CompletlyInstantiated(environment, vars.length);
    }


    @Override
    boolean isValid() {
        return true;
    }

    @Override
    boolean alwaysValid() {
        return false; // the condition has te be evaluated at each waking up
    }

    @Override
    void update(R recorder, Propagator propagator, EventType event) {}
}
