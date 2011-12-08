package solver.recorders.conditions;

import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import solver.constraints.probabilistic.propagators.nary.Union;
import solver.exception.ContradictionException;
import solver.recorders.fine.ArcEventRecorderWithCondition;
import solver.variables.EventType;
import solver.variables.IntVar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 3 nov. 2011
 */
public class CondAllDiffBC extends AbstractCondition {

    protected Union unionset;
    protected IntVar[] vars;
    protected IStateInt[] fromDelta;
    protected final RemProc rem_proc;

    boolean exec;

    public CondAllDiffBC(IEnvironment environment, IntVar[] vars) {
        super(environment);
        this.vars = vars;
        fromDelta = new IStateInt[vars.length];
        for (int i = 0; i < vars.length; i++) {
            fromDelta[i] = environment.makeInt();
        }
        this.unionset = new Union(vars, environment);
        rem_proc = new RemProc(this);
        this.exec = false;
        this.next = new CompletlyInstantiated(environment, vars.length);
    }


    @Override
    boolean isValid() {
        exec ^= true;
        return exec;
    }

    @Override
    boolean alwaysValid() {
        return false; // on doit appeler la condition a chaque pas
    }

    @Override
    void update(ArcEventRecorderWithCondition request, EventType event) {
//        if (EventType.isRemove(evtMask)) {
        /*int last = request.getLast();
        try {
            request.forEach(rem_proc,
                    fromDelta[request.getIndex(IRequest.VAR_IN_PROP)].get(),
                    last);
        } catch (ContradictionException e) {
            throw new SolverException("CondAllDiffBC#update encounters an exception");
        }
        fromDelta[request.getIndex(IRequest.VAR_IN_PROP)].set(last);*/
        /*if (request.getPropagator().getNbPendingER() == 0
                && !checkUnion()) {
            throw new SolverException("CondAllDiffBC#checkUnion is not valid");
        }*/
    }

    private static class RemProc implements IntProcedure {

        private final CondAllDiffBC p;

        public RemProc(CondAllDiffBC p) {
            this.p = p;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            p.unionset.remove(i);
//            LoggerFactory.getLogger("solver").info("{} remove from {}", i, p.vars[idxVar]);
        }
    }

    /**
     * test for unionset => has to be executed in the search loop at the beginning of downBranch method
     *
     * @return true if unionset is ok
     */
    public boolean checkUnion() {
        int[] toCheck = unionset.getValues();
        Arrays.sort(toCheck);
        int[] computed = computeUnion();
        Arrays.sort(computed);
        if (toCheck.length != computed.length) {
            System.out.println(printTab("incr", toCheck));
            System.out.println("--------------------");
            System.out.println(printTab("comp", computed));
            return false;
        } else {
            int i = 0;
            while (i < toCheck.length && toCheck[i] == computed[i]) {
                i++;
            }
            if (i != toCheck.length) {
                System.out.println(printTab("incr", toCheck));
                System.out.println("--------------------");
                System.out.println(printTab("comp", computed));
                return false;
            } else {
                return true;
            }
        }
    }

    private int[] computeUnion() {
        Set<Integer> vals = new HashSet<Integer>();
        for (IntVar var : vars) {
            int ub = var.getUB();
            for (int i = var.getLB(); i <= ub; i = var.nextValue(i)) {
                vals.add(i);
            }
        }
        int[] res = new int[vals.size()];
        int j = 0;
        for (Integer i : vals) {
            res[j++] = i;
        }
        return res;
    }

    private String printTab(String s, int[] tab) {
        String res = s + " : [";
        for (int aTab : tab) {
            res += aTab + ", ";
        }
        res = res.substring(0, res.length() - 2);
        res += "]";
        return res;
    }
}
