package solver.variables;

import choco.kernel.common.util.procedure.TernaryProcedure;
import com.sun.istack.internal.NotNull;
import solver.ICause;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.delta.IDelta;

public class NotifyProcedure implements TernaryProcedure<IRequest, ICause, EventType, IDelta> {
    ICause cause;
    EventType evt;
    IDelta delta;

    @Override
    public TernaryProcedure set(@NotNull ICause cause, EventType event, IDelta delta) {
        this.cause = cause;
        this.evt = event;
        this.delta = delta;
        return this;
    }

    @Override
    public void execute(IRequest request) throws ContradictionException {
        Propagator<IntVar> prop = request.getPropagator();
        if (prop != cause) {
            request.update(evt);
        }
    }
}