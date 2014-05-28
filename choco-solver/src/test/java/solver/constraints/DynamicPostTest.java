/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.reification.PropConditionnal;
import solver.propagation.PropagationEngineFactory;
import solver.search.loop.monitors.IMonitorOpenNode;
import solver.search.loop.monitors.IMonitorSolution;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VF;
import solver.variables.VariableFactory;
import util.ESat;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/12/12
 */
public class DynamicPostTest {

    PropagationEngineFactory engine;

    public DynamicPostTest(PropagationEngineFactory engine) {
        this.engine = engine;
    }

    public DynamicPostTest() {
        this(PropagationEngineFactory.DEFAULT);
    }

    @Test(groups = "1s")
    public void test0() {
        final Solver solver = new Solver();
        final IntVar X = VariableFactory.enumerated("X", 1, 2, solver);
        final IntVar Y = VariableFactory.enumerated("Y", 1, 2, solver);
        final IntVar Z = VariableFactory.enumerated("Z", 1, 2, solver);
        solver.set(engine.make(solver));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 8);
    }


    @Test(groups = "1s")
    public void test1() {
        final Solver solver = new Solver();
        final IntVar X = VariableFactory.enumerated("X", 1, 2, solver);
        final IntVar Y = VariableFactory.enumerated("Y", 1, 2, solver);
        final IntVar Z = VariableFactory.enumerated("Z", 1, 2, solver);

        solver.post(new Constraint("Conditionnal",
                new PropConditionnal(new IntVar[]{X, Y, Z},
                        new Constraint[]{IntConstraintFactory.arithm(X, "=", Y), IntConstraintFactory.arithm(Y, "=", Z)},
                        new Constraint[]{}) {
                    @Override
                    public ESat checkCondition() {
                        int nbNode = (int) solver.getMeasures().getNodeCount();
                        switch (nbNode) {
                            case 0:
                            case 1:
                                return ESat.UNDEFINED;
                            case 2:
                                return ESat.TRUE;
                            default:
                                return ESat.FALSE;
                        }

                    }
                }));
        solver.set(engine.make(solver));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 7);
    }

    @Test(groups = "1s")
    public void test2() {
        final Solver solver = new Solver();
        final IntVar X = VariableFactory.enumerated("X", 1, 2, solver);
        final IntVar Y = VariableFactory.enumerated("Y", 1, 2, solver);
        final IntVar Z = VariableFactory.enumerated("Z", 1, 2, solver);
        solver.getSearchLoop().plugSearchMonitor(new IMonitorOpenNode() {
            @Override
            public void beforeOpenNode() {
            }

            @Override
            public void afterOpenNode() {
                if (solver.getMeasures().getNodeCount() == 1) {
                    solver.post(IntConstraintFactory.arithm(X, "=", Y));
                    solver.post(IntConstraintFactory.arithm(Y, "=", Z));
                }
            }
        });
        solver.set(engine.make(solver));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups = "1s")
    public void test3() {
        final Solver solver = new Solver();
        final IntVar X = VariableFactory.enumerated("X", 1, 2, solver);
        final IntVar Y = VariableFactory.enumerated("Y", 1, 2, solver);
        final IntVar Z = VariableFactory.enumerated("Z", 1, 2, solver);
        Constraint c1 = IntConstraintFactory.arithm(X, "=", Y);
        Constraint c2 = IntConstraintFactory.arithm(X, "=", Z);
        solver.post(c1);
        solver.post(c2);
        solver.unpost(c2);
        solver.unpost(c1);
        solver.set(engine.make(solver));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 8);
        Assert.assertEquals(solver.getNbCstrs(), 0);
    }

    @Test(groups = "1s")
    public void test4() {
        final Solver solver = new Solver();
        final IntVar X = VariableFactory.enumerated("X", 1, 2, solver);
        final IntVar Y = VariableFactory.enumerated("Y", 1, 2, solver);
        final IntVar Z = VariableFactory.enumerated("Z", 1, 2, solver);
        final Constraint c1 = IntConstraintFactory.arithm(X, "=", Y);
        final Constraint c2 = IntConstraintFactory.arithm(X, "=", Z);
        solver.post(c1);
        solver.post(c2);
        solver.plugMonitor(new IMonitorSolution() {
            @Override
            public void onSolution() {
                solver.unpost(c1);
                solver.unpost(c2);
            }
        });
        solver.set(engine.make(solver));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 5);
        Assert.assertEquals(solver.getNbCstrs(), 0);
    }

    private static void popAll(List<Constraint> stack, Solver solver) {
        for (Constraint constraint : stack) {
            solver.unpost(constraint);
        }
    }

    private static void push(Constraint constraint, List<Constraint> stack, Solver solver) {
        stack.add(constraint);
        solver.post(constraint);
    }

    @Test(groups = "1s")
    public void testJLpareto() {
        // Objectives are to maximize "a" and maximize "b".
        Solver solver = new Solver();
        IntVar a = VF.enumerated("a", 0, 2, solver);
        IntVar b = VF.enumerated("b", 0, 2, solver);
        IntVar c = VF.enumerated("c", 0, 2, solver);

        solver.post(ICF.arithm(a, "+", b, "<", 3));

        // START extra variables/constraints for guided improvement algorithm
        List<Constraint> stack = new ArrayList<>();
        IntVar lbA = VF.enumerated("lbA", 0, 2, solver);
        IntVar lbB = VF.enumerated("lbB", 0, 2, solver);
        BoolVar aSBetter = ICF.arithm(a, ">", lbA).reif();
        BoolVar bSBetter = ICF.arithm(b, ">", lbB).reif();
        BoolVar aBetter = ICF.arithm(a, ">=", lbA).reif();
        BoolVar bBetter = ICF.arithm(b, ">=", lbB).reif();
        push(ICF.arithm(lbA, "=", a), stack, solver);
        push(ICF.arithm(lbB, "=", b), stack, solver);
        Constraint strictlyBetter
                = LCF.or(
                LCF.and(aSBetter, bBetter),
                LCF.and(aBetter, bSBetter));
        // END extra variables/constraints for guided improvement algorithm
        int nbSolution = 0;
        while (solver.findSolution()) {
            int bestA;
            int bestB;
            do {
                bestA = a.getValue();
                bestB = b.getValue();

                popAll(stack, solver);
                push(ICF.arithm(lbA, "=", bestA), stack, solver);
                push(ICF.arithm(lbB, "=", bestB), stack, solver);
                push(strictlyBetter, stack, solver);
            } while (solver.nextSolution());

            popAll(stack, solver);

            push(ICF.arithm(a, "=", bestA), stack, solver);
            push(ICF.arithm(b, "=", bestB), stack, solver);
            push(ICF.arithm(lbA, "=", bestA), stack, solver);
            push(ICF.arithm(lbB, "=", bestB), stack, solver);

            solver.getEngine().flush();
            solver.getSearchLoop().reset();

            if (solver.findSolution()) {
                do {
                    //System.out.println("Found pareto optimal solution: " + a + ", " + b + ", " + c);
                    nbSolution++;
                } while (solver.nextSolution());
            }

            popAll(stack, solver);

            solver.getEngine().flush();
            solver.getSearchLoop().reset();

            solver.post(LCF.or(
                    ICF.arithm(a, ">", bestA),
                    ICF.arithm(b, ">", bestB)));
        }
        Assert.assertEquals(9, nbSolution);
    }
}
