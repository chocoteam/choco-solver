/*
 * Created by IntelliJ IDEA.
 * User: sofdem - sophie.demassey{at}emn.fr
 * Date: Jul 27, 2010 - 1:45:04 PM
 */

package org.chocosolver.samples.nsp;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.ArrayUtils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Sophie Demassey
 */
public class NurseScheduling {

    enum BranchingStrategy {


        /*IMPACT {
            AbstractStrategy getGoal(Solver s, IntVar[] vars) {
                return new ImpactBasedBranching(s, vars);
            }
        },*/
        INC_DOMWDEG {
            AbstractStrategy getGoal(Solver s, IntVar[] vars) {
                return IntStrategyFactory.domOverWDeg(vars, System.currentTimeMillis());
            }
        },
        FORCE_INPUT {
            AbstractStrategy getGoal(Solver s, IntVar[] vars) {
                return IntStrategyFactory.minDom_LB(cast(s.getVars()));
            }
        },
        FORCE_DOMWDEG {
            AbstractStrategy getGoal(Solver s, IntVar[] vars) {
                return IntStrategyFactory.domOverWDeg(cast(s.getVars()), System.currentTimeMillis());
            }
        },
        /*DOMWDEG {
            AbstractStrategy getGoal(Solver s, IntVar[] vars) {
                return StrategyFactory.domWDeg(s, vars, new IncreasingDomain());
            }
        },*/
        MIN_DOM {
            AbstractStrategy getGoal(Solver s, IntVar[] vars) {
                return IntStrategyFactory.minDom_LB(vars);
            }
        },

        /*LEX {
            AbstractStrategy getGoal(Solver s, IntVar[] vars) {
                return StrategyFactory.lexicographic(s, vars);
            }
        },*/
        RAND {
            AbstractStrategy getGoal(Solver s, IntVar[] vars) {
                return IntStrategyFactory.random_bound(vars, 0);
            }
        };


        //	DOMDDEG { AbstractIntBranchingStrategy getGoal(Solver s, IntVar[] vars) { return BranchingFactory.domDDeg(s, vars, new IncreasingDomain()); }},
        AbstractStrategy getGoal(Solver s, IntVar[] vars) {
            return null;
        }

        private static IntVar[] cast(Variable[] vars) {
            IntVar[] ivars = new IntVar[vars.length];
            for (int i = 0; i < ivars.length; i++) {
                ivars[i] = (IntVar) vars[i];
            }
            return ivars;
        }
    }

//    private AbstractStrategy<IntVar> buildStrategy(Solver solver, IntVar[][] shifts, CostRegular[][] cregs) {
//        int[] days = new int[shifts.length * shifts[0].length];
//        IntVar[] flatten = new IntVar[shifts.length * shifts[0].length];
//        for (int e = 0, k = 0; e < shifts.length; e++) {
//            for (int d = 0; d < shifts[e].length; d++, k++) {
//                days[k] = d;
//                flatten[k] = shifts[e][d];
//            }
//        }
//        IMetric<IntVar> shift2day = new Map<IntVar>(flatten, days);
//
//        final THashMap<IntVar, CostRegular> regmap = new THashMap<IntVar, CostRegular>();
//        int act = 0; //  day
//        for (int e = 0; e < shifts.length; e++) {
//            for (int d = 0; d < shifts[e].length; d++) {
//                regmap.put(shifts[e][d], cregs[e][act]);
//            }
//        }
//
//        IMetric<IntVar> shift2reg = new IMetric<IntVar>() {
//            @Override
//            public int eval(IntVar var) {
//                return regmap.get(var).getMetric("CR_COST").eval(var);
//            }
//        };
//
//        AbstractSorter<IntVar> seq = new Seq<IntVar>(
//                new Incr<IntVar>(shift2day),
//                new Incr<IntVar>(shift2reg)
//        );
//        return StrategyVarValAssign.dyn(flatten, seq,
//                ValidatorFactory.instanciated, solver.getEnvironment());
//    }


    public static void runOne(NSData data) {
        NSCPModelConstrained.ConstraintOptions basisOptions = NSCPModelConstrained.ConstraintOptions.BASIC;
//        NSCPModelConstrained.ConstraintOptions patternOptions = NSCPModelConstrained.ConstraintOptions.WITH_REG;
        NSCPModelConstrained.ConstraintOptions patternOptions = NSCPModelConstrained.ConstraintOptions.WITH_MCR;

        BranchingStrategy strategy = BranchingStrategy.FORCE_DOMWDEG;

        Solver solver = new Solver();
        NurseSchedulingProblem m = new NSCPModelConstrained(data, basisOptions, patternOptions, solver);
        SearchMonitorFactory.limitTime(solver, 180000);
        IntVar[] vars = ArrayUtils.flatten(ArrayUtils.transpose(m.getShifts()));
        solver.set(strategy.getGoal(solver, vars));
        if (Boolean.TRUE == solver.findSolution()) {
            m.printSolution(solver);
            NSChecker checker = new NSChecker(data);
            if (checker.checkSolution(m.getSolution(solver)))
                System.out.println("Solution checked.");
        }
        String content =
                solver.getMeasures().getTimeCount() + " ms,\t " + solver.getMeasures().getNodeCount() + " nodes,\t "
                        + solver.getMeasures().getBackTrackCount() + " bks,\t "
                        + strategy.name() + "\t " + patternOptions.name() + "\t "
                        + m.getDescription() + "\n";
        System.out.println(content);
    }

    public static void runTwo(NSData data) {
        NSCPModelConstrained.ConstraintOptions basisOptions = NSCPModelConstrained.ConstraintOptions.REDUNDANT;
//        NSCPModelConstrained.ConstraintOptions patternOptions = NSCPModelConstrained.ConstraintOptions.WITH_MCRW;
//        NSCPModelConstrained.ConstraintOptions patternOptions = NSCPModelConstrained.ConstraintOptions.WITH_REG;
        NSCPModelConstrained.ConstraintOptions patternOptions = NSCPModelConstrained.ConstraintOptions.WITH_MCR;
        BranchingStrategy strategy = BranchingStrategy.FORCE_DOMWDEG;

        Solver solver = new Solver();
        NurseSchedulingProblem m = new NSCPModelConstrained(data, basisOptions, patternOptions, solver);
        IntVar[] vars = ArrayUtils.flatten(ArrayUtils.transpose(m.getShifts()));

        solver.set(strategy.getGoal(solver, vars));

        System.out.printf("%s\n", solver.toString());
        if (Boolean.TRUE == solver.findSolution()) {
            NSChecker checker = new NSChecker(data);
            if (checker.checkSolution(m.getSolution(solver)))
                System.out.println("Solution checked.");
        }
    }


    public static void runOne(NSData data, BranchingStrategy strategy, NSCPModelConstrained.ConstraintOptions basisOptions, NSCPModelConstrained.ConstraintOptions patternOptions) {
        System.out.println(strategy.name() + "\t " + patternOptions.name() + "\t " + basisOptions.name());
        Solver solver = new Solver();
        NurseSchedulingProblem m = new NSCPModelConstrained(data, basisOptions, patternOptions, solver);
        SearchMonitorFactory.limitTime(solver, 180000);
        IntVar[] vars = ArrayUtils.flatten(ArrayUtils.transpose(m.getShifts()));
        solver.set(strategy.getGoal(solver, vars));
        String solved = "0";
        if (Boolean.TRUE == solver.findSolution()) {
            m.printSolution(solver);
            NSChecker checker = new NSChecker(data);
            if (checker.checkSolution(m.getSolution(solver)))
                System.out.println("Solution checked.");
            solved = "1";
        }
        String content =
                solved + ",\t" + solver.getMeasures().getTimeCount() + " ms,\t "
                        + solver.getMeasures().getNodeCount() + " nodes,\t "
                        + solver.getMeasures().getBackTrackCount() + " bks,\t "
                        + strategy.name() + "\t " + patternOptions.name() + "\t " + basisOptions.name() + "\t "
                        + m.getDescription() + "\n";
        String contentCSV =
                solved + "," + solver.getMeasures().getTimeCount() + ","
                        + solver.getMeasures().getNodeCount() + ","
                        + solver.getMeasures().getBackTrackCount() + ","
                        + strategy.name() + "," + patternOptions.name() + "," + basisOptions.name() + ","
                        + m.getDescription() + "\n";
        System.out.println(content);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("out.txt", true));
            writer.write(contentCSV);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void runAll(NSData data) {
        NSCPModelConstrained.ConstraintOptions basisOptions = NSCPModelConstrained.ConstraintOptions.RED_EQUITY;
        //NSCPModelConstrained.ConstraintOptions patternOptions = NSCPModelConstrained.ConstraintOptions.WITH_MCRW;
        for (BranchingStrategy strategy : BranchingStrategy.values())
            for (NSCPModelConstrained.ConstraintOptions patternOptions : NSCPModelConstrained.ConstraintOptions.values())
                if (patternOptions.isPatternOption()) {
                    NurseScheduling.runOne(data, strategy, basisOptions, patternOptions);
                }
    }

    public static void testDefault() {
        NSData data = NSData.makeDefaultInstance();
        System.out.println("run default instance");
        runTwo(data);
    }

    public static void testLapegue(int numInstance) {
        NSParser.read(numInstance);
        NSData data = NSData.makeInstanceNSP();
        System.out.println("run instance number " + numInstance);
        runTwo(data);
    }

    public static void main(String[] args) {
        testDefault();
//        testLapegue(5);
    }

}
