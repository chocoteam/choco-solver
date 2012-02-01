package solver.recorders.conditions;

import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import solver.constraints.probabilistic.propagators.nary.Union;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.recorders.fine.ArcEventRecorderWithCondition;
import solver.variables.EventType;
import solver.variables.IntVar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static solver.recorders.conditions.CondAllDiffBCProba.Distribution.DIRAC;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 22/11/11
 */
public class CondAllDiffBCProba extends CondAllDiffBC {

    public static String tabf = "/functions/f.txt";
    public static String tabg = "/functions/g.txt";

    public static enum Distribution {
        NONE, UNIFORM, DIRAC
    }

    /**
     * incremental data structure for the union of the domains
     */
    protected Union unionset;
    protected final RemProc rem_proc;
    protected Random rand;

    /**
     * record of the number of variables not yet instantiated
     */
    protected IStateInt nbFreeVars;

    /**
     * record of the domains' size sum, for non-instantiated domains
     */
    protected IStateInt sumDomSize;

    /**
     * only for evaluations
     */
    boolean active;

    final Distribution dist;

    private double[] f;
    private double[] g;
    static final int nbValues = 950;

    public CondAllDiffBCProba(IEnvironment environment, IntVar[] vars, boolean active, Distribution dist) throws IOException {
        super(environment, vars);
        this.unionset = new Union(vars, environment);
        this.rand = new Random();
        this.sumDomSize = environment.makeInt(0);
        rem_proc = new RemProc(this, this.sumDomSize);
        int k = vars.length;
        for (IntVar v : vars) {
            if (v.instantiated()) {
                k--;
            } else {
                this.sumDomSize.add(v.getDomainSize());
            }
        }
        this.nbFreeVars = environment.makeInt(k);
        this.active = active;
        this.dist = dist;
        if (dist.equals(DIRAC)) {
            // initialiser f et g a partir des fichiers fournis
            InputStream isF = CondAllDiffBCProba.class.getResourceAsStream(tabf);
            BufferedReader readf = new BufferedReader(new InputStreamReader(isF));
            InputStream isG = CondAllDiffBCProba.class.getResourceAsStream(tabg);
            BufferedReader readg = new BufferedReader(new InputStreamReader(isG));
            this.f = new double[nbValues];
            this.g = new double[nbValues];
            for (int i = 0; i < nbValues; i++) {
                String fi = readf.readLine();
                String gi = readg.readLine();
                String[] tfi = fi.split("\\*\\^");
                /*for (String s : tfi) {
                    System.out.print(s + "  --  ");
                }
                System.out.println();*/
                String[] tgi = gi.split("\\*\\^");
                if (tfi.length > 2 || tgi.length > 2) {
                    System.out.println(tfi.length + ", " + tgi.length);
                    throw new UnsupportedOperationException();
                }
                if (tfi.length == 1) {
                    this.f[i] = Double.parseDouble(tfi[0]);
                } else {
                    this.f[i] = Double.parseDouble(tfi[0]) * Math.pow(10, Double.parseDouble(tfi[1]));
                }
                if (tgi.length == 1) {
                    this.g[i] = Double.parseDouble(tgi[0]);
                } else {
                    this.g[i] = Double.parseDouble(tgi[0]) * Math.pow(10, Double.parseDouble(tgi[1]));
                }

            }
        }
    }

    @Override
    boolean isValid() {
        return !active || rand.nextDouble() >= proba();
    }


    @Override
    void update(ArcEventRecorderWithCondition recorder, EventType event) {
        if (EventType.isInstantiate(event.mask)) {
            this.nbFreeVars.add(-1);
        }
        // WARNING: Initially, the paper proposes to only react to variable assignment... But in practice, a value in
        // the union can be removed only by a propagation which is not induced by an assignment.
        try {
            recorder.getDeltaMonitor(recorder.getVariables()[0]).forEach(rem_proc, EventType.REMOVE);
        } catch (ContradictionException e) {
            throw new SolverException("CondAllDiffBCProba#update encounters an exception");
        }
    }


    private static class RemProc implements IntProcedure {
        private final CondAllDiffBCProba p;
        private IStateInt sumDomSize;

        public RemProc(CondAllDiffBCProba p, IStateInt sumDomSize) {
            this.p = p;
            this.sumDomSize = sumDomSize;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            p.unionset.remove(i);
            this.sumDomSize.add(-1);
        }
    }

    private double proba() {
        switch (dist) {
            case DIRAC:
                return probaDirac();
            case UNIFORM:
                return probaUniform();
            default:
                throw new UnsupportedOperationException();
        }
    }

    // Computing the probability of remaining bound consistent with dirac domain distribution
    private double probaDirac() {
        double res;
        int n = this.nbFreeVars.get();
        int m = unionset.getSize();
        int v = unionset.getPositionLastRemVal();
        double d = (double) sumDomSize.get() / nbFreeVars.get();
        double rho = (double) n / m;
        double nu = (double) v / (m - 1);
        double mu = d / m;
        if (nu == 0 || nu == 1 || mu >= rho) {
            res = 1.0;    // case 1: theo. 2 p.10
        } else {
            if (rho < 1) {
                res = 1.0; // case 2: theo. 2 p.10
            } else {
                if (rho == 1) {
                    res = 1 - (tauNuMu(nu, mu) / (m * psiNuMu(nu, mu)));  // case 3: theo. 2 p.10
                } else {
                    throw new UnsupportedOperationException(); // cas rho > 1 ? couvert par mu >= rho ?
                }
            }
        }
        return res;
    }

    private double tauNuMu(double nu, double mu) {
        int valmu = (int) Math.floor(1000 * mu - 49);
        double fmu = this.f[valmu];
        double gmu = this.g[(int) Math.floor(1000 * mu - 49)];
        if (nu > 1 - mu) {
            if (nu < mu) {
                return fmu;  // case 1: theo. 2 p.10
            } else {
                return (fmu / 2) + (kron(nu, mu) * ((fmu / 2) - ((1 - mu) * gmu))); //case 2: theo. 2 p.10
            }
        } else {
            if (nu >= mu) {
                return ((fmu / 2) - ((1 - mu) * gmu)) * (kron(nu, mu) + kron(nu, 1 - mu)); // case 3: theo. 2 p.10
            } else {
                return (fmu / 2) + (kron(nu, 1 - mu) * ((fmu / 2) - ((1 - mu) * gmu))); // case 4: theo. 2 p.10
            }
        }
    }

    private int kron(double nu, double mu) {
        if (nu == mu) {
            return 1;
        } else {
            return 0;
        }
    }

    private double psiNuMu(double nu, double mu) {
        return Math.min(nu, 1 - mu) - Math.max(0, nu - mu);
    }

    // Computing the probability of remaining bound consistent with uniform domain distribution
    private double probaUniform() {
        double res;
        int n = this.nbFreeVars.get();
        int m = unionset.getSize();
        int v = unionset.getPositionLastRemVal();
        double rho = (double) n / m;
        double nu = (double) v / (m - 1);
        if (nu == 0 || nu == 1) {
            if (rho == 1) {
                res = 1 - ((2 * rho * (1 - Math.pow(Math.E, -4)) + 1.94264) / m); // case 3: theo. 1 p.9
            } else { // rho is obviously >0
                res = 1 - ((2 * rho * (1 - Math.pow(Math.E, -4 * rho))) / m); // case 2: theo. 1 p.9
            }
        } else { // nu is obviously >0
            if (rho == 1) {
                res = 1 - ((4 + (11.9359 / (2 * nu * (1 - nu)))) / m); // case 4: theo. 1 p.9
            } else { // rho is obviously >0
                res = 1 - (4 * rho / m); // case 1: theo. 1 p.9
            }
        }
        return res;
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
