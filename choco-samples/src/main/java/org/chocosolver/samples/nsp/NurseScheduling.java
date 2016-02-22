/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * Created by IntelliJ IDEA.
 * User: sofdem - sophie.demassey{at}emn.fr
 * Date: Jul 27, 2010 - 1:45:04 PM
 */

package org.chocosolver.samples.nsp;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.ArrayUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.*;

/**
 * @author Sophie Demassey
 */
public class NurseScheduling {

    enum BranchingStrategy {


        /*IMPACT {
            AbstractStrategy getGoal(Model s, IntVar[] vars) {
                return new ImpactBasedBranching(s, vars);
            }
        },*/
        INC_DOMWDEG {
            AbstractStrategy getGoal(Model s, IntVar[] vars) {
                return domOverWDegSearch(vars);
            }
        },
        FORCE_INPUT {
            AbstractStrategy getGoal(Model s, IntVar[] vars) {
                return minDomLBSearch(cast(s.getVars()));
            }
        },
        FORCE_DOMWDEG {
            AbstractStrategy getGoal(Model s, IntVar[] vars) {
                return domOverWDegSearch(cast(s.getVars()));
            }
        },
        /*DOMWDEG {
            AbstractStrategy getGoal(Model s, IntVar[] vars) {
                return StrategyFactory.domWDeg(s, vars, new IncreasingDomain());
            }
        },*/
        MIN_DOM {
            AbstractStrategy getGoal(Model s, IntVar[] vars) {
                return minDomLBSearch(vars);
            }
        },

        /*LEX {
            AbstractStrategy getGoal(Model s, IntVar[] vars) {
                return StrategyFactory.lexicographic(s, vars);
            }
        },*/
        RAND {
            AbstractStrategy getGoal(Model s, IntVar[] vars) {
                return randomSearch(vars, 0);
            }
        };


        //	DOMDDEG { AbstractIntBranchingStrategy getGoal(Model s, IntVar[] vars) { return BranchingFactory.domDDeg(s, vars, new IncreasingDomain()); }},
        AbstractStrategy getGoal(Model s, IntVar[] vars) {
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

//    private AbstractStrategy<IntVar> buildStrategy(Model solver, IntVar[][] shifts, CostRegular[][] cregs) {
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

        Model model = new Model();
        NurseSchedulingProblem m = new NSCPModelConstrained(data, basisOptions, patternOptions, model);
        model.getSolver().limitTime(180000);
        IntVar[] vars = ArrayUtils.flatten(ArrayUtils.transpose(m.getShifts()));
        model.getSolver().set(strategy.getGoal(model, vars));
        if (Boolean.TRUE == model.solve()) {
            m.printSolution(model);
            NSChecker checker = new NSChecker(data);
            if (checker.checkSolution(m.getSolution(model)))
                System.out.println("Solution checked.");
        }
        String content =
                model.getSolver().getMeasures().getTimeCount() + " ms,\t " + model.getSolver().getMeasures().getNodeCount() + " nodes,\t "
                        + model.getSolver().getMeasures().getBackTrackCount() + " bks,\t "
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

        Model model = new Model();
        NurseSchedulingProblem m = new NSCPModelConstrained(data, basisOptions, patternOptions, model);
        IntVar[] vars = ArrayUtils.flatten(ArrayUtils.transpose(m.getShifts()));

        model.getSolver().set(strategy.getGoal(model, vars));

        System.out.printf("%s\n", model.toString());
        if (Boolean.TRUE == model.solve()) {
            NSChecker checker = new NSChecker(data);
            if (checker.checkSolution(m.getSolution(model)))
                System.out.println("Solution checked.");
        }
    }


    public static void runOne(NSData data, BranchingStrategy strategy, NSCPModelConstrained.ConstraintOptions basisOptions, NSCPModelConstrained.ConstraintOptions patternOptions) {
        System.out.println(strategy.name() + "\t " + patternOptions.name() + "\t " + basisOptions.name());
        Model model = new Model();
        NurseSchedulingProblem m = new NSCPModelConstrained(data, basisOptions, patternOptions, model);
        model.getSolver().limitTime(180000);
        IntVar[] vars = ArrayUtils.flatten(ArrayUtils.transpose(m.getShifts()));
        model.getSolver().set(strategy.getGoal(model, vars));
        String solved = "0";
        if (Boolean.TRUE == model.solve()) {
            m.printSolution(model);
            NSChecker checker = new NSChecker(data);
            if (checker.checkSolution(m.getSolution(model)))
                System.out.println("Solution checked.");
            solved = "1";
        }
        String content =
                solved + ",\t" + model.getSolver().getMeasures().getTimeCount() + " ms,\t "
                        + model.getSolver().getMeasures().getNodeCount() + " nodes,\t "
                        + model.getSolver().getMeasures().getBackTrackCount() + " bks,\t "
                        + strategy.name() + "\t " + patternOptions.name() + "\t " + basisOptions.name() + "\t "
                        + m.getDescription() + "\n";
        String contentCSV =
                solved + "," + model.getSolver().getMeasures().getTimeCount() + ","
                        + model.getSolver().getMeasures().getNodeCount() + ","
                        + model.getSolver().getMeasures().getBackTrackCount() + ","
                        + strategy.name() + "," + patternOptions.name() + "," + basisOptions.name() + ","
                        + m.getDescription() + "\n";
        System.out.println(content);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("out.txt", true));
            writer.write(contentCSV);
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
