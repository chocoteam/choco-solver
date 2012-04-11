package choco.proba;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.nary.alldifferent.CounterProba;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.measure.IMeasures;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: xavier lorca
 */
public abstract class AbstractBenchProbas {
    public static final String sep =";";
    public static int TIMELIMIT = 20000;

    public enum Distribution {
        NONE, UNIFORM
    }

    BufferedWriter out;

    int frequency; // only for the case of a CondAllDiffBCFreq case
    int seed;
    boolean active; // true iff the probability is effectively applied

    //boolean mode; // true iff we search for all the solutions
    int size;
    Solver solver;
    IntVar[] allVars; // all the vairables of the problem
    IntVar[] vars; // decision variables for the problem
    Constraint[] cstrs; // all the cstrs involved in the problem (including alldiff)
    AllDifferent.Type type;
    Distribution dist; // kind of distribution considered for the probability
    CounterProba count;

    // output data
    private int nbTests; // number of tests executed
    private long nbSolutions;
    private long nbNodes;
    private long nbBcks;
    private long nbPropag; // number of propagation for the meta-propagator of alldiff
    private long nbAlldiffProp;  // number of time the real algorithm (AC or BC or RC) has been executed
    private long nbNeqsProp;  // number of time the neq algorithm has been executed
    private float time;

    // output averages
    private long avgSolutions;
    private long avgNodes;
    private long avgBcks;
    private long avgPropag;
    private long avgNbAlldiffProp;
    private long avgNbNeqsProp;
    private long avgTime;

    AbstractBenchProbas(Solver solver, int size, AllDifferent.Type type, int frequency,
                        boolean active, Distribution dist, BufferedWriter out, int seed) {
        this.solver = solver;
        this.type = type;
        //this.mode = mode;
        this.out = out;
        this.seed = seed;
        this.frequency = frequency;
        this.active = active;
        this.dist = dist;
        this.size = size;
        this.count = new CounterProba();
    }

    abstract void buildProblem(int size, boolean proba);

    void restartProblem(int size, int seed) {
        //System.out.println("---------------- new instance -------------");
        this.solver = new Solver();
        this.seed = seed;
        this.size = size;
        this.count = new CounterProba();
    }

    void recordResults() throws IOException {
        IMeasures mes = this.solver.getMeasures();
        this.nbSolutions = mes.getSolutionCount();
        this.nbNodes = mes.getNodeCount();
        this.nbBcks = mes.getBackTrackCount();
        if (this.dist.equals(Distribution.NONE)) {
            this.nbPropag = mes.getPropagationsCount()+mes.getEventsCount();
        } else {
            this.nbPropag = count.getNbProp();
        }
        this.nbAlldiffProp = count.getNbAllDiff();
        this.nbNeqsProp = count.getNbNeq();
        if (this.solver.getMeasures().getTimeCount() < TIMELIMIT) {
            this.time = mes.getTimeCount();
        } else {
            this.time = 0;
        }
        this.incrAverage();
        this.writeResults();
    }

    void recordAverage(BufferedWriter results) throws IOException {
        String s = "";
        s += ((double) this.avgSolutions / this.nbTests) + sep;
        s += ((double) this.avgNodes / this.nbTests) + sep;
        s += ((double) this.avgBcks / this.nbTests) + sep;
        s += ((double) this.avgPropag / this.nbTests) + sep;
        s += ((double) this.avgNbAlldiffProp / this.nbTests) + sep;
        s += ((double) this.avgNbNeqsProp / this.nbTests) + sep;
        s += ((double) this.avgTime / this.nbTests) + sep;
        s += "-" + sep;
        results.write(s);
        results.flush();
    }

    private void incrAverage() {
        this.avgSolutions += this.nbSolutions;
        this.avgNodes += this.nbNodes;
        this.avgBcks += this.nbBcks;
        this.avgPropag += this.nbPropag;
        this.avgNbAlldiffProp += this.nbAlldiffProp;
        this.avgNbNeqsProp += this.nbNeqsProp;
        this.avgTime += this.time;
        this.nbTests++;
    }

    private void writeResults() throws IOException {
        String s = "";
        s += this.nbSolutions + sep;
        s += this.nbNodes + sep;
        s += this.nbBcks + sep;
        s += this.nbPropag + sep;
        s += this.nbAlldiffProp + sep;
        s += this.nbNeqsProp + sep;
        if (this.time == 0) {
            s += "NaN" + sep;
        } else {
            s += this.time + sep;
        }
        s += "-" + sep;
        this.out.write(s);
        this.out.flush();
    }

    void configSearchStrategy() {
        //this.solver.set(StrategyFactory.random(this.vars, this.solver.getEnvironment(), this.seed));
        //this.solver.set(StrategyFactory.domwdegMindom(this.vars, this.solver));
        this.solver.set(StrategyFactory.minDomMinVal(this.vars, this.solver.getEnvironment()));
    }

    void solveProcess() {
        this.solver.findSolution();
    }

    void execute() throws IOException {
        if (this.dist.equals(Distribution.NONE)) {
            //System.out.println("cas non proba");
            this.buildProblem(size, false);
        } else {
            //System.out.println("cas avec proba");
            this.buildProblem(size, true);
        }
        this.solver.post(this.cstrs);
//        solver.set(PropagationStrategies.TWO_QUEUES_WITH_ARCS.make(solver));
        SearchMonitorFactory.log(this.solver, true, true);
        this.solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
        this.configSearchStrategy();
        this.solveProcess();
    }

    public String toString() {
        return "" + type + "-" + this.dist;
    }


}
