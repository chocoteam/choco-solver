package choco.proba;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.propagators.nary.alldifferent.proba.CondAllDiffBCProba;
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
    public static int TIMELIMIT = 20000;

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
    CondAllDiffBCProba.Distribution dist; // kind of distribution considered for the probability

    // output data
    private int nbTests; // number of tests executed
    private long nbSolutions;
    private long nbNodes;
    private long nbBcks;
    private long nbHeavyPropag;
    private long nbLigthPropag;
    private float time;

    // output averages
    private long avgSolutions;
    private long avgNodes;
    private long avgBcks;
    private long avgHeavyPropag;
    private long avgLigthPropag;
    private long avgTime;

    AbstractBenchProbas(Solver solver, int size, AllDifferent.Type type, int frequency,
                        boolean active, CondAllDiffBCProba.Distribution dist, BufferedWriter out, int seed) {
        this.solver = solver;
        this.type = type;
        //this.mode = mode;
        this.out = out;
        this.seed = seed;
        this.frequency = frequency;
        this.active = active;
        this.dist = dist;
        this.size = size;
    }

    abstract void buildProblem(int size, boolean proba);

    void restartProblem(int size, int seed) {
        this.solver = new Solver();
        this.seed = seed;
        this.size = size;
    }

    void recordResults() throws IOException {
        IMeasures mes = this.solver.getMeasures();
        this.nbSolutions = mes.getSolutionCount();
        this.nbNodes = mes.getNodeCount();
        this.nbBcks = mes.getBackTrackCount();
        this.nbHeavyPropag = mes.getPropagationsCount();
        this.nbLigthPropag = mes.getEventsCount();
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
        s += ((double) this.avgSolutions / this.nbTests) + "\t";
        s += ((double) this.avgNodes / this.nbTests) + "\t";
        s += ((double) this.avgBcks / this.nbTests) + "\t";
        s += ((double) this.avgHeavyPropag / this.nbTests) + "\t";
        s += ((double) this.avgLigthPropag / this.nbTests) + "\t";
        s += ((double) this.avgTime / this.nbTests) + "\t";
        s += "-" + "\t";
        results.write(s);
        results.flush();
    }

    private void incrAverage() {
        this.avgSolutions += this.nbSolutions;
        this.avgNodes += this.nbNodes;
        this.avgBcks += this.nbBcks;
        this.avgHeavyPropag += this.nbHeavyPropag;
        this.avgLigthPropag += this.nbLigthPropag;
        this.avgTime += this.time;
        this.nbTests++;
    }

    private void writeResults() throws IOException {
        String s = "";
        s += this.nbSolutions + "\t";
        s += this.nbNodes + "\t";
        s += this.nbBcks + "\t";
        s += this.nbHeavyPropag + "\t";
        s += this.nbLigthPropag + "\t";
        if (this.time == 0) {
            s += "NaN" + "\t";
        } else {
            s += this.time + "\t";
        }
        s += "-" + "\t";
        this.out.write(s);
        this.out.flush();
    }

    void configSearchStrategy() {
        this.solver.set(StrategyFactory.random(this.vars, this.solver.getEnvironment(), this.seed));
    }

    void solveProcess() {
        this.solver.findSolution();
    }

    void execute() throws IOException {
        this.buildProblem(size, false);
        this.solver.post(this.cstrs);
//        solver.set(PropagationStrategies.TWO_QUEUES_WITH_ARCS.make(solver));
        this.solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
        this.configSearchStrategy();
        this.solveProcess();
    }

    public String toString() {
        return "" + type + "-" + this.dist;
    }


}
