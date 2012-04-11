package solver.constraints.propagators.nary.alldifferent.proba;

import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.IStateDouble;
import choco.kernel.memory.IStateInt;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import solver.Cause;
import solver.ICause;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.probabilities.ProbaUtils;
import solver.variables.EventType;
import solver.variables.IVariableMonitor;
import solver.variables.IntVar;
import solver.variables.delta.IDeltaMonitor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 22/11/11
 */
public class CondAllDiffBCProba implements IVariableMonitor<IntVar> {

    public static double[] fact = {
            ProbaUtils.fact(1),
            ProbaUtils.fact(2),
            ProbaUtils.fact(3),
            ProbaUtils.fact(4),
            ProbaUtils.fact(5),
            ProbaUtils.fact(6),
            ProbaUtils.fact(7),
            ProbaUtils.fact(8),
            ProbaUtils.fact(9),
            ProbaUtils.fact(10),
            ProbaUtils.fact(11),
            ProbaUtils.fact(12),
            ProbaUtils.fact(13),
            ProbaUtils.fact(14),
            ProbaUtils.fact(15),
            ProbaUtils.fact(16),
            ProbaUtils.fact(17),
            ProbaUtils.fact(18),
            ProbaUtils.fact(19),
            ProbaUtils.fact(20),
            ProbaUtils.fact(21),
            ProbaUtils.fact(22),
            ProbaUtils.fact(23),
            ProbaUtils.fact(24),
            ProbaUtils.fact(25),
            ProbaUtils.fact(26)
    };

    IEnvironment environment;
    protected final RemProc rem_proc;
    protected final MinMaxProc minMax_proc;
    Random rand = new Random();

    protected Union unionset; // the union of the domains
    protected IntVar[] vars;
    protected IStateInt lastInstVal; // the value of the last instanciated variable
    protected IStateInt lastInstLow; // the lower bound of the value of the last instanciated variable
    protected IStateInt lastInstUpp; // the upper bound of the value of the last instanciated variable

    IStateBool asInst;
    IStateDouble proba;
    IStateInt n; // number of variables not yet instanciated before the last instanciation
    IStateInt m; // size of unionset before the last instanciation
    IStateInt v; // position (in unionset) of lastInstVal
    IStateInt al; // position (in unionset) of lastInstLow
    IStateInt be; // position (in unionset) of lastInstUpp

    //////
    protected final TIntObjectHashMap<IDeltaMonitor> deltamon; // delta monitoring -- can be NONE
    protected final TIntIntHashMap idxVs; // index of this within the variables structure -- mutable


    public CondAllDiffBCProba(IEnvironment environment, IntVar[] vars) {
        this.rem_proc = new RemProc(this);
        this.minMax_proc = new MinMaxProc();
        this.environment = environment;
        this.vars = vars;
        this.asInst = environment.makeBool(false);
        this.lastInstVal = environment.makeInt(-1);
        this.lastInstLow = environment.makeInt(-1);
        this.lastInstUpp = environment.makeInt(-1);
        this.proba = environment.makeFloat();
        this.n = environment.makeInt(vars.length);
        this.v = environment.makeInt(-1);
        this.al = environment.makeInt(-1);
        this.be = environment.makeInt(-1);

        this.deltamon = new TIntObjectHashMap<IDeltaMonitor>(vars.length);
        this.idxVs = new TIntIntHashMap(vars.length, (float) 0.5, -2, -2);

        for (int i = 0; i < vars.length; i++) {
            IntVar v = vars[i];
            if (v.instantiated()) {
                asInst.set(true);
                n.add(-1);
            }
            deltamon.put(i, v.getDelta().getMonitor(Cause.Null));
            v.addMonitor(this); // attach this as a variable monitor
            v.analyseAndAdapt(EventType.REMOVE.mask); // to be sure delta is created and maintained
        }
        this.unionset = new Union(vars, environment);
        this.m = this.unionset.getSize();
    }

    boolean isValid() {
        double cut = rand.nextGaussian();
        return (1 - proba.get() > cut);  // ici appeler le calcul de la proba : on retourne vrai avec une chance de 1-proba ?
        //return true;
    }

    /*void update(AbstractFineEventRecorder recorder, Propagator propagator, int evtmask) {
        System.out.println(recorder);
        //-------------------------------------------------------
        try {
            recorder.getDeltaMonitor(propagator, recorder.getVariables()[0]).forEach(minMax_proc, EventType.REMOVE);
        } catch (ContradictionException e) {
            throw new SolverException("CondAllDiffBCProba#update encounters an exception");
        }
        int low = minMax_proc.getMin();
        int upp = minMax_proc.getMax();
        IntVar lastInst = null;
        if (EventType.isInstantiate(evtmask)) {
            lastInst = (IntVar) recorder.getVariables()[0];
            lastInstLow.set(low);
            lastInstUpp.set(upp);
            lastInstVal.set(lastInst.getValue());
        }

        // si une variable au moins a ŽtŽ inst OU on vient de bck OU acas divers
        long m = this.m.get();
        long n = this.n.get();
        long v = this.v.get();
        long al = this.al.get();
        long be = this.be.get();
        if (!this.asInst.get() || v > m - 1 || al > m - 1 || be > m - 1) {
            this.proba.set(0); // propage
        } else {
            this.proba.set(proba(m, n, v, al, be)); // je calcule la proba avant de prendre en compte les changements courrant
        }


        //-------------------------------------------------------

        // WARNING: Initially, the paper proposes to only react to variable assignment... But in practice, a value in
        // the union can be removed only by a propagation which is not induced by an assignment.
        try {
            recorder.getDeltaMonitor(propagator, recorder.getVariables()[0]).forEach(rem_proc, EventType.REMOVE);
        } catch (ContradictionException e) {
            throw new SolverException("CondAllDiffBCProba#update encounters an exception");
        }

        if (EventType.isInstantiate(evtmask)) {
            this.asInst.set(true);
            this.n.add(-1);
            //unionset.forceRemove(lastInstVal.get());
        }
        this.v.set(unionset.getPosition(lastInstVal.get()));
        this.al.set(unionset.getPosition(lastInstLow.get()));
        this.be.set(unionset.getPosition(lastInstUpp.get()));
        this.m = unionset.getSize();
        this.minMax_proc.setMax();
        this.minMax_proc.setMin();
        assert checkUnion();
    }*/

    private static double proba(long m, long n, long v, long al, long be) {
        //assert m>0:""+n+","+m;
        //assert n>=0:""+n+","+m;
        //System.out.println("("+n+","+m+")");
        return (m - n < (2 * Math.sqrt(m))) ? probaCase2(m, m - n, v, al, be) : probaCase1(m, n / m, v, al, be);
    }

    private static double probaCase1(long m, long l, long v, long al, long be) {
        return 1 - fi(m, 1, v, al, be) * ((2 * l * (1 - Math.exp(-4 * l))) / m);
    }

    private static double probaCase2(long m, long l, long v, long al, long be) {
        long sum1 = 0;
        for (int j = 1; j <= 26; j++) {
            sum1 += fi(m, m - l - j - 1, v, al, be) * Math.log(1 - f(l, j));
        }
        long sum2 = 0;
        for (int j = 1; j <= 26; j++) {
            sum2 += fi(m, m - l - j - 1, v, al, be) * ((g(l, j)) / (1 - f(l, j)));
        }
        return Math.exp(sum1) * (1 - (1 / m) * (fi(m, 1, v, al, be) * 2 * (1 - Math.exp(-4)) + sum2));
    }

    private static int heavyiside(long x) {
        return x >= 0 ? 1 : 0;
    }

    private static double f(long i, int j) {
        double val = (Math.pow((i + j), j) * Math.pow(2, j) * Math.exp(-(2 * (i + j)))) / fact[j - 1];
        double val2 = (1 + (j / (2 * (i + j))));
        return val * val2;
    }

    private static double g(long i, int j) {
        double val = (Math.pow((i + j), j) * Math.pow(2, j) * Math.exp(-(2 * (i + j)))) / fact[j - 1];
        double val2 = ((i + 1) * ((2 * i) + j - 1) * j) / (4 * (i + j));
        double val3 = (2 * i * (i + 1) + j * (i + 2)) / 2;
        return val * (val2 + val3);
    }

    private static double fi(long m, long l, long v, long al, long be) {
        return Math.min(v, m - l - 1) - Math.max(0, v - l) + 1 - heavyiside(l - (be - al)) * (Math.min(al, m - l - 1) - Math.max(0, be - l) + 1);
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
            for (IntVar vs : vars) {
                System.out.println(vs);
            }
            System.out.println(printTab("incr", toCheck));
            System.out.println("--------------------");
            System.out.println(printTab("comp1", computed));
            return false;
        } else {
            int i = 0;
            while (i < toCheck.length && toCheck[i] == computed[i]) {
                i++;
            }
            if (i != toCheck.length) {
                for (IntVar vs : vars) {
                    System.out.println(vs);
                }
                System.out.println(printTab("incr", toCheck));
                System.out.println("--------------------");
                System.out.println(printTab("comp2", computed));
                return false;
            } else {
                return true;
            }
        }
    }

    private int[] computeUnion() {
        Set<Integer> vals = new HashSet<Integer>();
        for (IntVar var : vars) {
            if (!var.instantiated()) {
                int ub = var.getUB();
                for (int i = var.getLB(); i <= ub; i = var.nextValue(i)) {
                    vals.add(i);
                }
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

    public void activate() {
        for(int i = 0; i < vars.length; i++){
            vars[i].activate(this);
        }
    }

    private static class RemProc implements IntProcedure {
        private final CondAllDiffBCProba p;

        public RemProc(CondAllDiffBCProba p) {
            this.p = p;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            p.unionset.remove(i);
        }
    }

    private static class MinMaxProc implements IntProcedure {
        private int min;
        private int max;

        public MinMaxProc() {
            this.min = Integer.MAX_VALUE;
            this.max = Integer.MIN_VALUE;
        }

        public int getMin() {
            return min;
        }

        public void setMin() {
            this.min = Integer.MAX_VALUE;
        }

        public int getMax() {
            return max;
        }

        public void setMax() {
            this.max = Integer.MIN_VALUE;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            if (i < min) {
                this.min = i;
            } else {
                if (i > max) {
                    this.max = i;
                }
            }
        }
    }

    //////////////////////////////////////////


    @Override
    public void afterUpdate(IntVar var, EventType evt, ICause cause) {
        IDeltaMonitor dm = deltamon.get(var.getId());
        System.out.printf("CND : %s on %s\n", var, evt);
        //-------------------------------------------------------
        try {
            dm.forEach(minMax_proc, EventType.REMOVE);
        } catch (ContradictionException e) {
            throw new SolverException("CondAllDiffBCProba#update encounters an exception");
        }
        int low = minMax_proc.getMin();
        int upp = minMax_proc.getMax();
        if (EventType.isInstantiate(evt.mask)) {
            lastInstLow.set(low);
            lastInstUpp.set(upp);
            lastInstVal.set(var.getValue());
        }

        // si une variable au moins a ŽtŽ inst OU on vient de bck OU acas divers
        long m = this.m.get();
        long n = this.n.get();
        long v = this.v.get();
        long al = this.al.get();
        long be = this.be.get();
        if (!this.asInst.get() || v > m - 1 || al > m - 1 || be > m - 1) {
            this.proba.set(0); // propage
        } else {
            this.proba.set(proba(m, n, v, al, be)); // je calcule la proba avant de prendre en compte les changements courrant
        }

        //-------------------------------------------------------

        // WARNING: Initially, the paper proposes to only react to variable assignment... But in practice, a value in
        // the union can be removed only by a propagation which is not induced by an assignment.
        try {
            dm.forEach(rem_proc, EventType.REMOVE);
        } catch (ContradictionException e) {
            throw new SolverException("CondAllDiffBCProba#update encounters an exception");
        }

        if (EventType.isInstantiate(evt.mask)) {
            this.asInst.set(true);
            this.n.add(-1);
            //unionset.forceRemove(lastInstVal.get());
        }
        this.v.set(unionset.getPosition(lastInstVal.get()));
        this.al.set(unionset.getPosition(lastInstLow.get()));
        this.be.set(unionset.getPosition(lastInstUpp.get()));
        this.m = unionset.getSize();
        this.minMax_proc.setMax();
        this.minMax_proc.setMin();
        assert checkUnion();

        dm.clear();
    }

    @Override
    public int getIdxInV(IntVar variable) {
        return idxVs.get(variable.getId());
    }

    @Override
    public void setIdxInV(IntVar variable, int idx) {
        idxVs.put(variable.getId(), idx);
    }

    @Override
    public void beforeUpdate(IntVar var, EventType evt, ICause cause) {
    }

    @Override
    public void contradict(IntVar var, EventType evt, ICause cause) {
    }
}
