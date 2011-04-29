package solver.constraints.propagators.nary;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure1;
import choco.kernel.memory.IEnvironment;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.nary.matching.MatchingStructure;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.domain.delta.IntDelta;
import solver.views.IView;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 30 nov. 2010
 */
public class PropAllDiffAC extends Propagator<IntVar> {

    //IntVar var;
    //int idxVar; // index of var in struct
    public MatchingStructure struct;
    protected final RemProc rem_proc;
    protected boolean effect;


    @SuppressWarnings({"unchecked"})
    public PropAllDiffAC(IntVar[] vars, IEnvironment environment, Constraint constraint) {
        super(vars, environment, constraint, PropagatorPriority.CUBIC, true);
        //this.var = var;
        //this.idxVar = idxVar;
        this.struct = new MatchingStructure(vars, vars.length, getValueGap(vars), environment);
        rem_proc = new RemProc(this);
    }

    /**
     * Static method for one parameter constructor
     *
     * @param vars domain variable list
     * @return gap between min and max value
     */
    private static int getValueGap(IntVar[] vars) {
        int minValue = Integer.MAX_VALUE, maxValue = Integer.MIN_VALUE;
        for (IntVar var : vars) {
            minValue = Math.min(var.getLB(), minValue);
            maxValue = Math.max(var.getUB(), maxValue);
        }
        return maxValue - minValue + 1;
    }

    @Override
    public int getPropagationConditions() {
        return EventType.ALL_MASK();
    }

    @Override
    public void propagate() throws ContradictionException {
        // On suppose que la structure struct est deja ete initialisee par la contrainte
        // car elle est partagee entre tous les propagateurs
        struct.removeUselessEdges(this);
    }

    @Override
    public void propagateOnView(IView<IntVar> view, int varIdx, int mask) throws ContradictionException {
        IntVar var = view.getVariable();
        IntDelta delta = var.getDelta();

        if (EventType.isInstantiate(mask)) {
            struct.updateMatchingOnInstantiation(varIdx, var.getValue(), this);
        } else {
            int f = view.fromDelta();
            int l = view.toDelta();
            effect = false;
            delta.forEach(rem_proc.set(varIdx), f, l);
        }
        if (getNbViewEnqued() == 0) {
            struct.removeUselessEdges(this);
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            for (IntVar v : vars) {
                if (v.instantiated()) {
                    int vv = v.getValue();
                    for (IntVar w : vars) {
                        if (w != v) {
                            if (w.instantiated()) {
                                if (vv == w.getValue()) {
                                    return ESat.FALSE;
                                }
                            } else {
                                return ESat.UNDEFINED;
                            }
                        }
                    }
                } else {
                    return ESat.UNDEFINED;
                }
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    private static class RemProc implements IntProcedure1<Integer> {

        private final PropAllDiffAC p;
        private int idxVar;

        public RemProc(PropAllDiffAC p) {
            this.p = p;
        }

        @Override
        public IntProcedure1 set(Integer idxVar) {
            this.idxVar = idxVar;
            return this;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            p.struct.nodes[idxVar].removeEdge(i);
            p.effect |= p.struct.deleteMatch(idxVar, i - p.struct.getMinValue());
        }
    }
}
