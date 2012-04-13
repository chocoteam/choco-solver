package solver.constraints.propagators.nary.alldifferent.proba;

import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import solver.Cause;
import solver.ICause;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.recorders.coarse.CoarseEventRecorderWithCondition;
import solver.recorders.conditions.ICondition;
import solver.search.loop.AbstractSearchLoop;
import solver.variables.EventType;
import solver.variables.IVariableMonitor;
import solver.variables.IntVar;
import solver.variables.delta.IDeltaMonitor;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 22/11/11
 */
public class CondAllDiffBCProba implements IVariableMonitor<IntVar>, ICondition<CoarseEventRecorderWithCondition> {

    public static final double[] fact = fact(26);

    IEnvironment environment;
    protected final RemProc rem_proc;
    protected final MinMaxProc minMax_proc;
    Random rand = new Random();

    protected Union unionset; // the union of the domains
    protected IntVar[] vars;

    IStateInt nbNotInstVar;
    double proba;

    //////
    protected final TIntObjectHashMap<IDeltaMonitor> deltamon; // delta monitoring -- can be NONE
    protected final TIntIntHashMap idxVs; // index of this within the variables structure -- mutable
    protected TIntLongHashMap timestamps; // a timestamp lazy clear the event structures


    public CondAllDiffBCProba(IEnvironment environment, IntVar[] vars) {
        this.rem_proc = new RemProc(this);
        this.minMax_proc = new MinMaxProc();
        this.environment = environment;
        this.vars = vars;
        this.nbNotInstVar = environment.makeInt(vars.length);
        this.deltamon = new TIntObjectHashMap<IDeltaMonitor>(vars.length);
        this.idxVs = new TIntIntHashMap(vars.length, (float) 0.5, -2, -2);
        this.timestamps = new TIntLongHashMap(vars.length, (float) 0.5, -2, -2);
        for (int i = 0; i < vars.length; i++) {
            IntVar v = vars[i];
            v.analyseAndAdapt(EventType.REMOVE.mask); // to be sure delta is created and maintained
            int vid = v.getId();
            deltamon.put(vid, v.getDelta().getMonitor(Cause.Null));
            v.addMonitor(this); // attach this as a variable monitor
            timestamps.put(vid, -1);

        }
    }

    private void init() {
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].instantiated()) {
                nbNotInstVar.add(-1);
            }
        }
        this.unionset = new Union(vars, environment);
        assert checkUnion();
    }

    private static double probaAfterOther(long m, long n) {
        return (m - n < (2 * Math.sqrt(m))) ? 0 : 1;
    }

    private static double probaAfterInst(long m, long n, long v, long al, long be) {
        if (m < n || v < 0 || al < 0 || be < 0 || v > m - 1 || al > m - 1 || be > m - 1) {
            return 0; // propage
        } else {
            return (m - n < (2 * Math.sqrt(m))) ? probaCase2(m, m - n, v, al, be) : probaCase1(m, n / m, v, al, be);
        }
    }

    private static double probaCase1(long m, long l, long v, long al, long be) {
        return 1 - fi(m, 1, v, al, be) * ((2 * l * (1 - Math.exp(-4 * l))) / m);
    }

    private static double probaCase2(long m, long l, long v, long al, long be) {
        long sum1 = 0;
        long max = Math.min((m - l - 1), 26);
        for (int j = 1; j <= max; j++) {
            sum1 += fi(m, m - l - j - 1, v, al, be) * Math.log(1 - f(l, j));
        }
        long sum2 = 0;
        for (int j = 1; j <= max; j++) {
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

    public void activate() {
        for (int i = 0; i < vars.length; i++) {
            vars[i].activate(this);
        }
        init();
    }

    private static class RemProc implements IntProcedure {
        private final CondAllDiffBCProba p;

        public RemProc(CondAllDiffBCProba p) {
            this.p = p;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            //System.out.println("traitement du retrait de " + i);
            p.unionset.remove(i);
            assert p.checkOcc(i);
        }
    }

    private static class MinMaxProc implements IntProcedure {
        private int min;
        private int max;

        public MinMaxProc() {
            this.min = Integer.MAX_VALUE;
            this.max = Integer.MIN_VALUE;
        }

        public MinMaxProc init() {
            this.min = Integer.MAX_VALUE;
            this.max = Integer.MIN_VALUE;
            return this;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            if (i < min) {
                this.min = i;
            }
            if (i > max) {
                this.max = i;
            }
        }
    }

    @Override
    public void afterUpdate(IntVar var, EventType evt, ICause cause) {
        /*System.out.printf("\n\nCND : %s on %s\n", var, evt);
        for (IntVar vs : vars) {
            System.out.println(vs);
        }//*/
        ////////////////////// Sauce a Charles pour utiliser le delta domaine de var
        IDeltaMonitor dm = deltamon.get(var.getId());
        int vid = var.getId();
        long t = timestamps.get(vid);
        if (t - AbstractSearchLoop.timeStamp != 0) {
            deltamon.get(vid).clear();
            timestamps.adjustValue(vid, AbstractSearchLoop.timeStamp - t);
        }
        dm.freeze();
        //////////////////////////////////
        int m = unionset.getSize();
        int n = nbNotInstVar.get();
        if (EventType.isInstantiate(evt.mask)) { // la proba sur l'instantiation
            nbNotInstVar.add(-1);
            /*try {
                dm.forEach(minMax_proc.init(), EventType.REMOVE);
            } catch (ContradictionException e) {
                throw new SolverException("CondAllDiffBCProba#update encounters an exception");
            }
            int low = minMax_proc.getMin();
            int upp = minMax_proc.getMax();
            unionset.instantiatedValue(var.getValue(), low, upp);
            unionset.instantiatedValue(var.getValue(), -1, -1);
            int v = unionset.getLastInstValuePos();
            int al = unionset.getLastLowValuePos();
            int be = unionset.getLastUppValuePos(); //*/
            try {
                //System.out.print("****** INST debut retrait sur ");
                //System.out.println(var + ": ");
                dm.forEach(rem_proc, EventType.REMOVE);
                //System.out.println("fin retrait ******");
            } catch (ContradictionException e) {
                throw new SolverException("CondAllDiffBCProba#update encounters an exception");
            }
            //this.proba = probaAfterInst(m, n, v, al, be); // je calcule la proba avant de prendre en compte les changements courrant */
            this.proba = probaAfterOther(m, n);
        } else {   // la proba sur un autre type d'evenement
            try {
                //System.out.print("****** REM debut retrait sur ");
                //System.out.println(var + ": ");
                dm.forEach(rem_proc, EventType.REMOVE);
                //System.out.println("fin retrait ******");
            } catch (ContradictionException e) {
                throw new SolverException("CondAllDiffBCProba#update encounters an exception");
            }
            this.proba = probaAfterOther(m, n);
        }

        ////////////////////////// liberation de l'iterateur sur le delta
        dm.unfreeze();
        assert checkUnion();
    }

    @Override
    public boolean validateScheduling(CoarseEventRecorderWithCondition recorder, Propagator propagator, EventType event) {
        double cut = rand.nextGaussian();
        return (1 - proba > cut);         // todo tester car on economise pas tant que ça de proba !
//        return true;
    }

    @Override
    public ICondition next() {
        return null;
    }

    @Override
    public void linkRecorder(CoarseEventRecorderWithCondition recorder) {
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

    public static double[] fact(int n) {
        double[] tabRes = new double[n];
        tabRes[0] = 1;
        int idx = 1;
        while (idx < n) {
            tabRes[idx] = tabRes[idx - 1] * (idx + 1);
            idx++;
        }
        return tabRes;
    }

    /**
     * test for unionset => has to be executed in the search loop at the beginning of downBranch method
     *
     * @return true if unionset is ok
     */
    public boolean checkUnion() {
        //System.out.print("Union from sctrach: ");
        Set<Integer> allVals = computeUnion();
        for (Integer value : allVals) {
            if (!checkOcc(value)) {
                return false;
            }
        }
        //System.out.println();
        return true;
    }

    public boolean checkOcc(int value) {
        int occ = 0;
        for (IntVar v : vars) {
            if (v.contains(value) && !v.instantiated()) {
                occ++;
            }
        }
        //System.out.print("[" + value + "," + occ + "];");
        if (occ != unionset.getOccOf(value)) {
            /*System.out.println("value " + value + ": " + occ + " VS " + unionset.getOccOf(value));
            for (IntVar v : vars) {
                System.out.println(v);
            } //*/
            return false;
        } else {
            return true;
        }
    }

    private Set<Integer> computeUnion() {
        //System.out.println("*********** calcul manuel de l'union");
        Set<Integer> vals = new HashSet<Integer>();
        //System.out.println(instVals);
        for (IntVar var : vars) {
            int ub = var.getUB();
            for (int i = var.getLB(); i <= ub; i = var.nextValue(i)) {
                vals.add(i);
            }
        }
        //System.out.println("calcul manuel de l'union ***********");
        return vals;
    }
}
