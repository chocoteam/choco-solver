package choco.proba;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraint;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.propagators.nary.alldifferent.proba.CondAllDiffBCProba;
import solver.propagation.generator.Primitive;
import solver.propagation.generator.Queue;
import solver.recorders.conditions.ICondition;
import solver.search.loop.monitors.ISearchMonitor;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.measure.IMeasures;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: xavier lorca
 */
public abstract class AbstractBenchProbas {
    public static final String sep = ";";
    public static int TIMELIMIT = 60000;

    BufferedWriter out;

    int seed;

    //boolean mode; // true iff we search for all the solutions
    int size;
    Solver solver;
    IntVar[] allVars; // all the vairables of the problem
    IntVar[] vars; // decision variables for the problem
    Constraint[] cstrs; // all the cstrs involved in the problem (including alldiff)
    AllDifferent.Type type;
    boolean isProba;

    // output data
    private int nbTests; // number of tests executed
    private long nbSolutions;
    private long nbNodes;
    private long nbPropag; // number of propagation for the meta-propagator of alldiff
    private double ratioPropagByNodes;
    private float time;

    // output averages
    private long avgSolutions;
    private long avgNodes;
    private long avgPropag;
    private double avgRatio;
    private long avgTime;

    AbstractBenchProbas(Solver solver, int size, AllDifferent.Type type, BufferedWriter out, int seed, boolean isProba) {
        this.solver = solver;
        this.type = type;
        this.out = out;
        this.seed = seed;
        this.size = size;
        this.isProba = isProba;
    }

    abstract void buildProblem(int size, boolean proba);

    void restartProblem(int size, int seed) {
        //System.out.println("---------------- new instance -------------");
        this.solver = new Solver();
        this.seed = seed;
        this.size = size;
    }

    void recordResults() throws IOException {
        IMeasures mes = this.solver.getMeasures();
        //this.nbSolutions = mes.getObjectiveValue();//mes.getSolutionCount();
        this.nbSolutions = mes.getSolutionCount();
        this.nbNodes = mes.getNodeCount();
        this.nbPropag = mes.getPropagationsCount(); //+ mes.getEventsCount();  => on compte juste les propag lourdes
        if (this.nbNodes > 0) {
            this.ratioPropagByNodes = ((double) this.nbPropag) / this.nbNodes;
        } else {
            this.ratioPropagByNodes = this.nbPropag; // on a que le noeud root
        }

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
        s += ((double) this.avgPropag / this.nbTests) + sep;
        s += (this.avgRatio / this.nbTests) + sep;
        double avgTime = (double) this.avgTime / this.nbTests;
        if (avgTime == 0) {
            s += "NaN" + sep;
        } else {
            s += avgTime + sep;
        }
        s += "-" + sep;
        results.write(s);
        results.flush();
    }

    private void incrAverage() {
        this.avgSolutions += this.nbSolutions;
        this.avgNodes += this.nbNodes;
        this.avgPropag += this.nbPropag;
        this.avgRatio += this.ratioPropagByNodes;
        this.avgTime += this.time;
        this.nbTests++;
    }

    private void writeResults() throws IOException {
        String s = "";
        if (this.nbSolutions == Integer.MAX_VALUE) {
            s += "NaN" + sep;
        } else {
            s += this.nbSolutions + sep;
        }
        s += this.nbNodes + sep;
        s += this.nbPropag + sep;
        s += this.ratioPropagByNodes + sep;
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

    void configPropStrategy() {
        Queue arcs = Queue.build(Primitive.arcs(cstrs));
        Queue coarses;
        if (!isProba) {
            coarses = Queue.build(Primitive.coarses(cstrs));
        } else {
            ActivateCond sm = new ActivateCond();
            solver.getSearchLoop().plugSearchMonitor(sm);
            java.util.List<Primitive> coarses_ = new ArrayList<Primitive>();
            for (int i = 0; i < cstrs.length; i++) {
                if (cstrs[i] instanceof AllDifferent) {
                    IntConstraint icstr = (IntConstraint) cstrs[i];
                    IntVar[] myvars = icstr.getVariables();
                    ICondition condition = new CondAllDiffBCProba(solver.getEnvironment(), myvars);
                    sm.add(condition);
                    coarses_.add(Primitive.coarses(condition, cstrs[i]));
                } else {
                    coarses_.add(Primitive.coarses(cstrs[i]));
                }
            }
            coarses = Queue.build(coarses_.toArray(new Primitive[coarses_.size()]));
        }
        solver.set(Queue.build(arcs.clearOut(), coarses.pickOne()).clearOut());
    }

    void solveProcess() {
        this.solver.findSolution();
    }

    void execute() throws IOException {
        this.buildProblem(size, false);
        this.solver.post(this.cstrs);
        this.solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
        this.configSearchStrategy();
        this.configPropStrategy();
        this.solveProcess();
    }

    public String toString() {
        if (!isProba) {
            return "" + type;
        } else {
            return "" + type + "-prob";
        }
    }

    private static class ActivateCond extends VoidSearchMonitor implements ISearchMonitor {

        List<ICondition> conds = new ArrayList<ICondition>();

        public void add(ICondition cond) {
            conds.add(cond);
        }

        @Override
        public void afterInitialize() {
            for (ICondition cond : conds) {
                cond.activate();
            }
        }
    }


}
