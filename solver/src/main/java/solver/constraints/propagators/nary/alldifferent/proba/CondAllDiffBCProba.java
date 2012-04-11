package solver.constraints.propagators.nary.alldifferent.proba;

import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.IStateDouble;
import choco.kernel.memory.IStateInt;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.probabilities.ProbaUtils;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 22/11/11
 */
public class CondAllDiffBCProba {

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
        for (IntVar v : vars) {
            if (v.instantiated()) {
                asInst.set(true);
                n.add(-1);
            }
        }
        this.unionset = new Union(vars, environment);
        this.m = this.unionset.getSize();
    }

    boolean isValid() {
        double cut = rand.nextGaussian();
        return (1 - proba.get() > cut);  // ici appeler le calcul de la proba : on retourne vrai avec une chance de 1-proba ?
        //return true;
    }

    void update(AbstractFineEventRecorder recorder, Propagator propagator, int evtmask) {
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
            lastInst = (IntVar)recorder.getVariables()[0];
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
    }

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

//    protected IntVar[] vars;
//
//    public static String tabf = "/functions/f.txt";
//    public static String tabg = "/functions/g.txt";
//
//    public static enum Distribution {
//        NONE, UNIFORM, DIRAC
//    }
//
//    /**
//     * incremental data structure for the union of the domains
//     */
//    protected Union unionset;
//    protected final RemProc rem_proc;
//    protected Random rand;
//
//    /**
//     * record of the number of variables not yet instantiated
//     */
//    protected IStateInt nbFreeVars;
//
//    /**
//     * record of the domains' size sum, for non-instantiated domains
//     */
//    protected IStateInt sumDomSize;
//
//    final Distribution dist;
//
//    private double[] f;
//    private double[] g;
//    static final int nbValues = 950;
//
//    public CondAllDiffBCProba(IEnvironment environment, IntVar[] vars, Distribution dist) {
//        this.vars = vars;
//        this.unionset = new Union(vars, environment);
//        this.rand = new Random();
//        this.sumDomSize = environment.makeInt(0);
//        rem_proc = new RemProc(this, this.sumDomSize);
//        int k = vars.length;
//        for (IntVar v : vars) {
//            if (v.instantiated()) {
//                k--;
//            } else {
//                this.sumDomSize.add(v.getDomainSize());
//            }
//        }
//        this.nbFreeVars = environment.makeInt(k);
//        this.dist = dist;
//        if (dist.equals(DIRAC)) {
//            try {
//                // initialiser f et g a partir des fichiers fournis
//                InputStream isF = CondAllDiffBCProba.class.getResourceAsStream(tabf);
//                BufferedReader readf = new BufferedReader(new InputStreamReader(isF));
//                InputStream isG = CondAllDiffBCProba.class.getResourceAsStream(tabg);
//                BufferedReader readg = new BufferedReader(new InputStreamReader(isG));
//                this.f = new double[nbValues];
//                this.g = new double[nbValues];
//                for (int i = 0; i < nbValues; i++) {
//                    String fi = null;
//
//                    fi = readf.readLine();
//                    String gi = readg.readLine();
//                    String[] tfi = fi.split("\\*\\^");
//                    /*for (String s : tfi) {
//                        System.out.print(s + "  --  ");
//                    }
//                    System.out.println();*/
//                    String[] tgi = gi.split("\\*\\^");
//                    if (tfi.length > 2 || tgi.length > 2) {
//                        System.out.println(tfi.length + ", " + tgi.length);
//                        throw new UnsupportedOperationException();
//                    }
//                    if (tfi.length == 1) {
//                        this.f[i] = Double.parseDouble(tfi[0]);
//                    } else {
//                        this.f[i] = Double.parseDouble(tfi[0]) * Math.pow(10, Double.parseDouble(tfi[1]));
//                    }
//                    if (tgi.length == 1) {
//                        this.g[i] = Double.parseDouble(tgi[0]);
//                    } else {
//                        this.g[i] = Double.parseDouble(tgi[0]) * Math.pow(10, Double.parseDouble(tgi[1]));
//                    }
//                }
//            } catch (IOException e) {
//                throw new SolverException("Encounter a problem while reading functions");
//            }
//        }
//    }
//
//    boolean isValid() {
//        return rand.nextDouble() >= proba();
//    }
//    /*boolean isValid() {
//        return rand.nextDouble() >= 0.5;
//    }*/
//
//
//    void update(AbstractFineEventRecorder recorder, Propagator propagator, int evtmask) {
//        if (EventType.isInstantiate(evtmask)) {
//            this.nbFreeVars.add(-1);
//        }
//        // WARNING: Initially, the paper proposes to only react to variable assignment... But in practice, a value in
//        // the union can be removed only by a propagation which is not induced by an assignment.
//        try {
//            recorder.getDeltaMonitor(propagator, recorder.getVariables()[0]).forEach(rem_proc, EventType.REMOVE);
//        } catch (ContradictionException e) {
//            throw new SolverException("CondAllDiffBCProba#update encounters an exception");
//        }
//    }
//
//
//    private static class RemProc implements IntProcedure {
//        private final CondAllDiffBCProba p;
//        private IStateInt sumDomSize;
//
//        public RemProc(CondAllDiffBCProba p, IStateInt sumDomSize) {
//            this.p = p;
//            this.sumDomSize = sumDomSize;
//        }
//
//        @Override
//        public void execute(int i) throws ContradictionException {
//            p.unionset.remove(i);
//            this.sumDomSize.add(-1);
//        }
//    }
//
//    private double proba() {
//        switch (dist) {
//            case DIRAC:
//                return probaDirac();
//            case UNIFORM:
//                return probaUniform();
//            default:
//                throw new UnsupportedOperationException();
//        }
//    }
//
//    // Computing the probability of remaining bound consistent with dirac domain distribution
//    private double probaDirac() {
//        double res;
//        int n = this.nbFreeVars.get();
//        int m = unionset.getSize();
//        int v = unionset.getPositionLastRemVal();
//        double d = (double) sumDomSize.get() / n;
//        double rho = (double) n / m;
//        double nu = (double) v / (m - 1);
//        double mu = d / m;
//        /*if (mu < 0) {
//            System.out.println("union = " + m);
//            System.out.println("free vars = " + nbFreeVars);
//            System.out.println("sum dom = " + sumDomSize);
//            System.exit(0);
//        }*/
//        if (nu == 0 || nu == 1 || mu >= rho) {
//            res = 1.0;    // case 1: theo. 2 p.10
//        } else {
//            if (rho < 1) {
//                res = 1.0; // case 2: theo. 2 p.10
//            } else {
//                if (mu < 0.05) {
//                    res = 0; // allow to avoid to find a bad case in the table representing values of function tauNuMu
//                } else {
//                    if (rho == 1) {
//                        res = 1 - (tauNuMu(nu, mu) / (m * psiNuMu(nu, mu)));  // case 3: theo. 2 p.10
//                    } else {
//                        res = 0; // cas rho > 1 ? couvert par mu >= rho ?
//                    }
//                }
//            }
//        }
//        return res;
//    }
//
//    private double tauNuMu(double nu, double mu) {
//        int valmu = (int) Math.floor(1000 * mu - 49);
//        /*if (valmu < 0) {
//            System.out.println("mu = " + mu);
//            System.out.println("valmu = " + valmu);
//            System.exit(0);
//        }*/
//        //System.out.println(f.length);
//        double fmu = this.f[valmu];
//        double gmu = this.g[valmu];
//        if (nu > 1 - mu) {
//            if (nu < mu) {
//                return fmu;  // case 1: theo. 2 p.10
//            } else {
//                return (fmu / 2) + (kron(nu, mu) * ((fmu / 2) - ((1 - mu) * gmu))); //case 2: theo. 2 p.10
//            }
//        } else {
//            if (nu >= mu) {
//                return ((fmu / 2) - ((1 - mu) * gmu)) * (kron(nu, mu) + kron(nu, 1 - mu)); // case 3: theo. 2 p.10
//            } else {
//                return (fmu / 2) + (kron(nu, 1 - mu) * ((fmu / 2) - ((1 - mu) * gmu))); // case 4: theo. 2 p.10
//            }
//        }
//    }
//
//    private int kron(double nu, double mu) {
//        if (nu == mu) {
//            return 1;
//        } else {
//            return 0;
//        }
//    }
//
//    private double psiNuMu(double nu, double mu) {
//        return Math.min(nu, 1 - mu) - Math.max(0, nu - mu);
//    }
//
//    // Computing the probability of remaining bound consistent with uniform domain distribution
//    private double probaUniform() {
//        double res;
//        int n = this.nbFreeVars.get();
//        int m = unionset.getSize();
//        int v = unionset.getPositionLastRemVal();
//        double rho = (double) n / m;
//        double nu = (double) v / (m - 1);
//        if (nu == 0 || nu == 1) {
//            if (rho == 1) {
//                res = 1 - ((2 * rho * (1 - Math.pow(Math.E, -4)) + 1.94264) / m); // case 3: theo. 1 p.9
//            } else { // rho is obviously >0
//                res = 1 - ((2 * rho * (1 - Math.pow(Math.E, -4 * rho))) / m); // case 2: theo. 1 p.9
//            }
//        } else { // nu is obviously >0
//            if (rho == 1) {
//                res = 1 - ((4 + (11.9359 / (2 * nu * (1 - nu)))) / m); // case 4: theo. 1 p.9
//            } else { // rho is obviously >0
//                res = 1 - (4 * rho / m); // case 1: theo. 1 p.9
//            }
//        }
//        return res;
//    }
//
//
//    /**
//     * test for unionset => has to be executed in the search loop at the beginning of downBranch method
//     *
//     * @return true if unionset is ok
//     */
//    public boolean checkUnion() {
//        int[] toCheck = unionset.getValues();
//        Arrays.sort(toCheck);
//        int[] computed = computeUnion();
//        Arrays.sort(computed);
//        if (toCheck.length != computed.length) {
//            System.out.println(printTab("incr", toCheck));
//            System.out.println("--------------------");
//            System.out.println(printTab("comp", computed));
//            return false;
//        } else {
//            int i = 0;
//            while (i < toCheck.length && toCheck[i] == computed[i]) {
//                i++;
//            }
//            if (i != toCheck.length) {
//                System.out.println(printTab("incr", toCheck));
//                System.out.println("--------------------");
//                System.out.println(printTab("comp", computed));
//                return false;
//            } else {
//                return true;
//            }
//        }
//    }
//
//    private int[] computeUnion() {
//        Set<Integer> vals = new HashSet<Integer>();
//        for (IntVar var : vars) {
//            int ub = var.getUB();
//            for (int i = var.getLB(); i <= ub; i = var.nextValue(i)) {
//                vals.add(i);
//            }
//        }
//        int[] res = new int[vals.size()];
//        int j = 0;
//        for (Integer i : vals) {
//            res[j++] = i;
//        }
//        return res;
//    }
//
//    private String printTab(String s, int[] tab) {
//        String res = s + " : [";
//        for (int aTab : tab) {
//            res += aTab + ", ";
//        }
//        res = res.substring(0, res.length() - 2);
//        res += "]";
//        return res;
//    }


}
