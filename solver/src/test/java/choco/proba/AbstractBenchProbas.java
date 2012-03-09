package choco.proba;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.AllDifferent;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.nary.PropAllDiffBC;
import solver.constraints.propagators.nary.PropCliqueNeq;
import solver.recorders.conditions.CondAllDiffBCProba;
import solver.search.measure.IMeasures;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

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

    boolean mode; // true iff we search for all the solutions
    int size;
    Solver solver;
    IntVar[] allVars; // all the vairables of the problem
    IntVar[] vars; // decision variables for the problem
    Constraint[] cstrs; // all the cstrs involved in the problem (including alldiff)
    int nbAllDiff; // number of alldiff cstrs
    AllDifferent.Type type;
    AllDifferent[] allDiffs; // all the alldiff cstrs involved in the problem
    IntVar[][] allDiffVars; // variables related to each alldiff cstrs : allDiffVars[i] are the variables of the cstr allDiffs[i]
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

    AbstractBenchProbas(Solver solver, boolean mode, int size, AllDifferent.Type type, int frequency,
                        boolean active, CondAllDiffBCProba.Distribution dist, BufferedWriter out, int seed) {
        this.solver = solver;
        this.type = type;
        this.mode = mode;
        this.out = out;
        this.seed = seed;
        this.frequency = frequency;
        this.active = active;
        this.dist = dist;
        this.size = size;
    }

    abstract void buildProblem(int size);

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

    void recordAverage() throws IOException {
        String s = "";
        s += ((double) this.avgSolutions / this.nbTests) + "\t";
        s += ((double) this.avgNodes / this.nbTests) + "\t";
        s += ((double) this.avgBcks / this.nbTests) + "\t";
        s += ((double) this.avgHeavyPropag / this.nbTests) + "\t";
        s += ((double) this.avgLigthPropag / this.nbTests) + "\t";
        s += ((double) this.avgTime / this.nbTests) + "\t";
        s += "-" + "\t";
        this.out.write(s);
        this.out.flush();
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

    void execute() throws IOException {
        this.buildProblem(size);
        this.solver.post(this.cstrs);

        // 1. placer les propagateurs dans diffŽrentes listes
        ArrayList<PropAllDiffBC> hall = new ArrayList<PropAllDiffBC>();
        ArrayList<PropCliqueNeq> clique = new ArrayList<PropCliqueNeq>();
        ArrayList<Propagator> others = new ArrayList<Propagator>();

        Constraint[] constraints = solver.getCstrs();
        for (int i = 0; i < constraints.length; i++) {
            Propagator<IntVar>[] props = constraints[i].propagators;
            for (Propagator<IntVar> p : props) {
                if (p instanceof PropAllDiffBC) {
                    hall.add((PropAllDiffBC) p);
                } else if (p instanceof PropCliqueNeq) {
                    clique.add((PropCliqueNeq) p);
                } else {
                    others.add(p);
                }
            }
        }

        /*Primitive[] primitives = null;
        if (this.frequency >= 0) {
            ICondition condition = null;
            primitives = new Primitive[hall.size() * 2];
            if (this.frequency == 0) {
                for (int i = 0; i < hall.size(); i++) {
                    PropProbaAllDiffBC prop = (PropProbaAllDiffBC) hall.get(i);
                    condition = new CondAllDiffBCProba(this.solver.getEnvironment(), prop.getVars(), this.active, this.dist);
                    primitives[i] = Primitive.arcs(condition, prop);
                    primitives[i + hall.size()] = Primitive.coarses(prop);
                }
            } else {
                for (int i = 0; i < hall.size(); i++) {
                    PropProbaAllDiffBC prop = (PropProbaAllDiffBC) hall.get(i);
                    condition = new CondAllDiffBCFreq(this.solver.getEnvironment(), prop.getVars(), this.frequency);
                    primitives[i] = Primitive.arcs(condition, prop);
                    primitives[i + hall.size()] = Primitive.coarses(prop);
                }
            }
        }*/

        /*Propagator[] _cliques = clique.toArray(new Propagator[clique.size()]);
        PropagationStrategy qneq = Queue.build(Sort.build(Primitive.arcs(_cliques)), Primitive.coarses(_cliques));

        Propagator[] _others = others.toArray(new Propagator[others.size()]);
        PropagationStrategy qothers = Queue.build(Primitive.arcs(_others), Primitive.coarses(_others));

        PropagationStrategy qadbc = null;
        if (primitives == null) {
            Propagator[] _hall = hall.toArray(new Propagator[hall.size()]);
            qadbc = Queue.build(Primitive.arcs(_hall), Primitive.coarses(_hall)).pickOne();
        } else {
            if (primitives.length > 0) {  // patch Xavier
                qadbc = Queue.build(primitives).pickOne();
            }
        }
        if (qadbc != null) {   // patch Xavier
            solver.set(
                    Sort.build(
                            qneq.clearOut(),
                            qothers.clearOut(),
                            qadbc.pickOne()
                    ).clearOut()
            );
        } else {   // patch Xavier
            solver.set(
                    Sort.build(
                            qneq.clearOut(),
                            qothers.clearOut()
                    ).clearOut()
            );
        }*/

        this.solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
        this.solver.set(StrategyFactory.random(this.vars, this.solver.getEnvironment(), this.seed));
        //System.out.println(this.solver);
        //solver.set(StrategyFactory.minDomMinVal(vars, solver.getEnvironment()));
        // SearchMonitorFactory.log(solver, true, true);
        if (this.mode) {
            //System.out.println("\t find all solutions");
            this.solver.findAllSolutions();
        } else {
            //System.out.println("\t find first solution");
            this.solver.findSolution();
        }
    }


}
