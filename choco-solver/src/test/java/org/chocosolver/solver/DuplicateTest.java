/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.constraints.binary.PropScale;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.chocosolver.solver.constraints.nary.circuit.CircuitConf;
import org.chocosolver.solver.constraints.set.SCF;
import org.chocosolver.solver.constraints.ternary.PropTimesNaive;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 29/08/2014
 */
public class DuplicateTest {

    private String[] sort(String output) {
        String[] os = output.split("\n");
        Arrays.sort(os);
        return os;
    }


    @Test(groups = "1s")
    public void test1() {
        Solver solver = new Solver("ocohc");
        Solver copy = solver.duplicateModel();
        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
    }

    @Test(groups = "1s")
    public void test2() {
        Solver solver = new Solver("Choco");
        VF.fixed(-2, solver);
        VF.fixed(0, solver);
        VF.fixed("my cste 3", 3, solver);

        VF.bool("bool", solver);
        VF.bounded("bounded", 2, 4, solver);
        VF.enumerated("enum1", 1, 3, solver);
        VF.enumerated("enum2", new int[]{3, 4, 5}, solver);

        VF.set("set1", 2, 4, solver);
        VF.set("set2", new int[]{1, 2}, solver);
        VF.set("set3", new int[]{1, 2, 3, 4}, new int[]{2, 3}, solver);
        VF.set("set4", new int[]{3, 4, 5, 6}, SetType.BITSET, new int[]{5, 6}, SetType.BOOL_ARRAY, solver);

        VF.real("real", 1.1, 2.2, .001, solver);
        VF.real(VF.bounded("bounded", 2, 4, solver), 0.01);

        Solver copy = solver.duplicateModel();
        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
    }

    @Test(groups = "1s")
    public void test3() {
        Solver solver = new Solver("Choco");
        BoolVar b = VF.bool("b", solver);
        VF.not(b);
        b.not();
        VF.eq(b);
        IntVar e = VF.enumerated("e", 1, 3, solver);
        VF.offset(e, -2);
        VF.scale(e, 3);
        VF.minus(e);
        VF.eq(e);
        VF.task(e, solver.ONE(), VF.offset(e, 1));

        Solver copy = solver.duplicateModel();
        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(sort(copy.toString()), sort(solver.toString()));
    }

    @Test(groups = "1s")
    public void test4() {
        Solver solver = new Solver("Choco");
        solver.post(ICF.TRUE(solver));
        solver.post(ICF.FALSE(solver));
        Solver copy = solver.duplicateModel();
        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
    }

    @Test(groups = "1s")
    public void test5() {
        for (String op : new String[]{"=", "!=", ">", "<", ">=", "<="}) {
            Solver solver = new Solver("Choco");
            IntVar v = VF.enumerated("v", 1, 4, solver);
            solver.post(ICF.arithm(v, op, 3));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();
            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test6() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        ICF.arithm(v, "=", 3).reif();

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test7() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);

        solver.post(ICF.member(v, 2, 3));
        solver.post(ICF.member(v, new int[]{2}));
        solver.post(ICF.not_member(v, 0, 1));
        solver.post(ICF.not_member(v, new int[]{7}));


        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test8() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", -6, 4, solver);
        solver.post(ICF.absolute(v, w));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test9() {
        for (String op : new String[]{"=", "!=", ">", "<", ">=", "<="}) {
            Solver solver = new Solver("Choco");
            IntVar v = VF.enumerated("v", 1, 4, solver);
            IntVar w = VF.enumerated("v", 1, 4, solver);
            solver.post(ICF.arithm(v, op, w));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();
            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test10() {
        for (String op1 : new String[]{"+", "-"}) {
            for (String op2 : new String[]{"=", "!=", ">", "<", ">=", "<="}) {
                Solver solver = new Solver("Choco");
                IntVar v = VF.enumerated("v", 1, 4, solver);
                IntVar w = VF.enumerated("v", 1, 4, solver);
                solver.post(ICF.arithm(v, op1, w, op2, 1));

                Solver copy = solver.duplicateModel();

                solver.findAllSolutions();
                copy.findAllSolutions();
                Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
                Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
                Assert.assertEquals(copy.toString(), solver.toString());
                Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
            }
        }
    }

    @Test(groups = "1s")
    public void test11() {
        for (String op1 : new String[]{"+", "-"}) {
            for (String op2 : new String[]{"=", "!=", ">", "<", ">=", "<="}) {
                Solver solver = new Solver("Choco");
                IntVar v = VF.enumerated("v", 1, 4, solver);
                IntVar w = VF.enumerated("v", 1, 4, solver);
                solver.post(ICF.arithm(v, op2, w, op1, 1));

                Solver copy = solver.duplicateModel();

                solver.findAllSolutions();
                copy.findAllSolutions();
                Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
                Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
                Assert.assertEquals(copy.toString(), solver.toString());
                Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
            }
        }
    }

    @Test(groups = "1s")
    public void test12() {
        for (String op : new String[]{"=", "!=", ">", "<"}) {
            Solver solver = new Solver("Choco");
            IntVar v = VF.enumerated("v", 1, 4, solver);
            IntVar w = VF.enumerated("v", -6, 4, solver);
            solver.post(ICF.distance(v, w, op, 1));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test13() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 0, 3, solver);
        solver.post(ICF.element(v, new int[]{4, 3, 2, 1}, w));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test14() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 1, 16, solver);
        solver.post(ICF.square(w, v));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test15() {
        for (String op : new String[]{"AC3", "AC3rm", "AC3bit+rm", "AC2001", "FC"}) {
            Solver solver = new Solver("Choco");
            IntVar v = VF.enumerated("v", 1, 4, solver);
            IntVar w = VF.enumerated("v", -6, 4, solver);
            solver.post(ICF.table(v, w, TuplesFactory.allEquals(v, w), op));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test16() {
        for (String op : new String[]{"=", ">", "<"}) {
            Solver solver = new Solver("Choco");
            IntVar v = VF.enumerated("v", 1, 4, solver);
            IntVar w = VF.enumerated("v", -6, 4, solver);
            IntVar x = VF.enumerated("v", 2, 4, solver);
            solver.post(ICF.distance(v, w, op, x));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test17() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 1, 2, solver);
        IntVar x = VF.enumerated("v", 0, 3, solver);
        solver.post(ICF.eucl_div(v, w, x));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test18() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 1, 2, solver);
        IntVar x = VF.enumerated("v", 0, 3, solver);
        solver.post(ICF.maximum(v, w, x));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test19() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 1, 2, solver);
        IntVar x = VF.enumerated("v", 0, 3, solver);
        solver.post(ICF.minimum(v, w, x));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test20() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 1, 2, solver);
        IntVar x = VF.enumerated("v", 0, 3, solver);
        solver.post(ICF.mod(v, w, x));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test21() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 1, 2, solver);
        IntVar x = VF.enumerated("v", 0, 3, solver);
        solver.post(new Constraint("times", new PropTimesNaive(v, w, x)));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test22() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar x = VF.enumerated("v", 0, 3, solver);
        solver.post(new Constraint("times", new PropScale(v, 3, x)));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test23() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar x = VF.enumerated("v", 0, 3, solver);
        solver.post(new Constraint("times", new PropScale(v, 3, x)));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test24() {
        for (String CS : new String[]{"BC", "AC", "FC", "DEFAULT"}) {
            Solver solver = new Solver("Choco");
            IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
            solver.post(ICF.alldifferent(vs, CS));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test25() {
        Solver solver = new Solver("Choco");
        IntVar x = VF.enumerated("x", 1, 4, solver);
        IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
        solver.post(ICF.among(x, vs, new int[]{1, 2}));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test26() {
        for (boolean ac : new boolean[]{true, false}) {
            Solver solver = new Solver("Choco");
            IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
            IntVar x = VF.enumerated("x", 1, 4, solver);
            solver.post(ICF.atleast_nvalues(vs, x, ac));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test27() {
        for (boolean ac : new boolean[]{false, true}) {
            Solver solver = new Solver("Choco");
            IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
            IntVar x = VF.enumerated("x", 1, 4, solver);
            solver.post(ICF.atmost_nvalues(vs, x, ac));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test28() {
        Solver solver = new Solver("Choco");
        IntVar x = VF.enumerated("x", 1, 4, solver);
        BoolVar[] bs = VF.boolArray("bs", 4, solver);
        solver.post(ICF.boolean_channeling(bs, x, 1));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test29() {
        for (CircuitConf cf : CircuitConf.values()) {
            Solver solver = new Solver("Choco");
            IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
            solver.post(ICF.circuit(vs, 1, cf));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test30() {
        int n = 14;
        FiniteAutomaton auto = new FiniteAutomaton("(0|1|2)*(0|1)(0|1)(0|1)(0|1|2)*");
        int[][][] c2 = new int[n][3][auto.getNbStates()];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < auto.getNbStates(); k++) {
                c2[i][0][k] = 1;
                c2[i][1][k] = 2;
            }
        }
        Solver solver = new Solver("Choco");
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }
        IntVar cost = VariableFactory.bounded("z", n / 2, n / 2 + 1, solver);
        solver.post(IntConstraintFactory.cost_regular(vars, cost, CostAutomaton.makeSingleResource(auto, c2, cost.getLB(), cost.getUB())));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test31() {
        Solver solver = new Solver("Choco");
        IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
        IntVar x = VF.enumerated("x", 1, 4, solver);
        solver.post(ICF.count(2, vs, x));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test32() {
        Solver solver = new Solver("Choco");
        IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
        IntVar x = VF.bounded("x", 1, 4, solver);
        IntVar z = VF.enumerated("x", 2, 3, solver);
        solver.post(ICF.count(z, vs, x));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test33() {
        Solver solver = new Solver("Choco");
        IntVar capa = VF.fixed(6, solver);
        int n = 4;
        int max = 3;
        IntVar[] start = VF.boundedArray("start", n, 0, max, solver);
        IntVar[] end = new IntVar[n];
        IntVar[] duration = new IntVar[n];
        IntVar[] height = new IntVar[n];
        Task[] task = new Task[n];
        Random rd = new Random(0);
        for (int i = 0; i < n; i++) {
            duration[i] = VF.fixed(rd.nextInt(20) + 1, solver);
            height[i] = VF.fixed(rd.nextInt(5) + 1, solver);
            end[i] = VF.offset(start[i], duration[i].getValue());
            task[i] = new Task(start[i], duration[i], end[i]);
        }
        solver.post(ICF.cumulative(task, height, capa, true));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(sort(copy.toString()), sort(solver.toString()));
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test33bis() {
        Solver solver = new Solver("Choco");
        IntVar capa = VF.fixed(6, solver);
        int n = 4;
        int max = 3;
        IntVar[] start = VF.boundedArray("start", n, 0, max, solver);
        IntVar[] end = new IntVar[n];
        IntVar[] duration = new IntVar[n];
        IntVar[] height = new IntVar[n];
        Task[] task = new Task[n];
        Random rd = new Random(0);
        for (int i = 0; i < n; i++) {
            duration[i] = VF.fixed(rd.nextInt(20) + 1, solver);
            height[i] = VF.fixed(rd.nextInt(5) + 1, solver);
            end[i] = VF.offset(start[i], duration[i].getValue());
            task[i] = new Task(start[i], duration[i], end[i]);
        }
        solver.post(ICF.cumulative(task, height, capa, false));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(sort(copy.toString()), sort(solver.toString()));
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test34() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.enumeratedArray("vs", 2, 1, 4, solver);
        IntVar[] Y = VF.enumeratedArray("vs", 2, 2, 4, solver);

        IntVar[] dX = VF.enumeratedArray("vs", 2, 1, 3, solver);
        IntVar[] dY = VF.enumeratedArray("vs", 2, 3, 4, solver);

        solver.post(ICF.diffn(X, Y, dX, dY, false));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test35() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.enumeratedArray("vs", 3, 1, 4, solver);
        IntVar V = VF.enumerated("V", 2, 4, solver);
        IntVar I = VF.enumerated("I", 0, 1, solver);


        solver.post(ICF.element(V, X, I, 0));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test36() {
        for (boolean cl : new boolean[]{true, false}) {
            Solver solver = new Solver("Choco");
            IntVar[] X = VF.enumeratedArray("vs", 3, 1, 4, solver);
            IntVar[] Y = VF.enumeratedArray("vs", 2, 1, 2, solver);

            solver.post(ICF.global_cardinality(X, new int[]{2, 3}, Y, cl));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test37() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.enumeratedArray("vs", 3, 1, 4, solver);
        IntVar[] Y = VF.enumeratedArray("xs", 3, 1, 4, solver);

        solver.post(ICF.inverse_channeling(X, Y, 1, 1));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test38() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar[] Y = VF.boundedArray("xs", 3, 1, 4, solver);

        solver.post(ICF.inverse_channeling(X, Y, 1, 1));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }


    @Test(groups = "1s")
    public void test39() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar C = VF.enumerated("C", 0, 10, solver);
        IntVar E = VF.bounded("E", 0, 15, solver);
        solver.post(ICF.knapsack(X, C, E, new int[]{2, 3, 4, 1}, new int[]{5, 2, 3, 4}));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test40() {
        Solver solver = new Solver("Choco");
        IntVar[][] X = VF.boundedMatrix("vs", 3, 3, 1, 4, solver);
        solver.post(ICF.lex_chain_less(X));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test41() {
        Solver solver = new Solver("Choco");
        IntVar[][] X = VF.boundedMatrix("vs", 3, 3, 1, 4, solver);
        solver.post(ICF.lex_chain_less_eq(X));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test42() {
        Solver solver = new Solver("Choco");
        IntVar[][] X = VF.boundedMatrix("vs", 2, 3, 1, 4, solver);
        solver.post(ICF.lex_less(X[0], X[1]));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test43() {
        Solver solver = new Solver("Choco");
        IntVar[][] X = VF.boundedMatrix("vs", 2, 3, 1, 4, solver);
        solver.post(ICF.lex_less_eq(X[0], X[1]));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test44() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar M = VF.bounded("M", 0, 5, solver);
        solver.post(ICF.maximum(M, X));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test45() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar M = VF.bounded("M", 0, 5, solver);
        solver.post(ICF.minimum(M, X));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test46() {
        Solver solver = new Solver("Choco");
        BoolVar[] X = VF.boolArray("vs", 3, solver);
        BoolVar M = VF.bool("M", solver);
        solver.post(ICF.maximum(M, X));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test47() {
        Solver solver = new Solver("Choco");
        BoolVar[] X = VF.boolArray("vs", 3, solver);
        BoolVar M = VF.bool("M", solver);
        solver.post(ICF.minimum(M, X));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test48() {
        Solver solver = new Solver();
        int period = 5;
        IntVar[] sequence = VariableFactory.enumeratedArray("x", period, 0, 2, solver);
        IntVar[] bounds = new IntVar[4];
        bounds[0] = VariableFactory.bounded("z_0", 0, 80, solver);
        bounds[1] = VariableFactory.bounded("day", 0, 28, solver);
        bounds[2] = VariableFactory.bounded("night", 0, 28, solver);
        bounds[3] = VariableFactory.bounded("rest", 0, 28, solver);

        FiniteAutomaton auto = new FiniteAutomaton();
        int idx = auto.addState();
        auto.setInitialState(idx);
        auto.setFinal(idx);
        idx = auto.addState();
        int DAY = 0;
        auto.addTransition(auto.getInitialState(), idx, DAY);
        int next = auto.addState();
        int NIGHT = 1;
        auto.addTransition(idx, next, DAY, NIGHT);
        int REST = 2;
        auto.addTransition(next, auto.getInitialState(), REST);
        auto.addTransition(auto.getInitialState(), next, NIGHT);

        int[][][][] costMatrix = new int[period][3][4][auto.getNbStates()];
        for (int i = 0; i < costMatrix.length; i++) {
            for (int j = 0; j < costMatrix[i].length; j++) {
                for (int r = 0; r < costMatrix[i][j].length; r++) {
                    if (r == 0) {
                        if (j == DAY)
                            costMatrix[i][j][r] = new int[]{3, 5, 0};
                        else if (j == NIGHT)
                            costMatrix[i][j][r] = new int[]{8, 9, 0};
                        else if (j == REST)
                            costMatrix[i][j][r] = new int[]{0, 0, 2};
                    } else if (r == 1) {
                        if (j == DAY)
                            costMatrix[i][j][r] = new int[]{1, 1, 0};
                    } else if (r == 2) {
                        if (j == NIGHT)
                            costMatrix[i][j][r] = new int[]{1, 1, 0};
                    } else if (r == 3) {
                        if (j != REST)
                            costMatrix[i][j][r] = new int[]{1, 1, 0};
                    }
                }
            }
        }
        ICostAutomaton costAutomaton = CostAutomaton.makeMultiResources(auto, costMatrix, bounds);
        solver.post(IntConstraintFactory.multicost_regular(sequence, bounds, costAutomaton));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test49() {
        Solver solver = new Solver("Choco");
        BoolVar[] X = VF.boolArray("vs", 3, solver);
        BoolVar M = VF.bool("M", solver);
        solver.post(ICF.nvalues(X, M));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test50() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar S = VF.bounded("S", 0, 5, solver);
        IntVar E = VF.bounded("E", 0, 5, solver);
        solver.post(ICF.path(X, S, E, 0));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test51() {
        Solver solver = new Solver("Choco");
        IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
        FiniteAutomaton auto = new FiniteAutomaton("(0|1|2)*(0|1)(0|1)(0|1)(0|1|2)*");
        solver.post(ICF.regular(vs, auto));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test52() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar S = VF.bounded("S", 0, 5, solver);
        solver.post(ICF.scalar(X, new int[]{1, 2, 3}, S));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test53() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar[] Y = VF.boundedArray("ws", 3, 1, 4, solver);
        solver.post(ICF.sort(X, Y));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test54() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar S = VF.bounded("S", 0, 2, solver);
        solver.post(ICF.subcircuit(X, -1, S));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test55() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar S = VF.bounded("S", 0, 5, solver);
        solver.post(ICF.sum(X, S));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test56() {
        Solver solver = new Solver("Choco");
        BoolVar[] X = VF.boolArray("vs", 3, solver);
        IntVar S = VF.bounded("S", 1, 2, solver);
        solver.post(ICF.sum(X, S));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test57() {
        Solver solver = new Solver("Choco");
        BoolVar[] X = VF.boolArray("vs", 12, solver);
        IntVar S = VF.bounded("S", 1, 2, solver);
        solver.post(ICF.sum(X, S));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test58() {
        for (String op : new String[]{"GAC3rm", "GAC2001", "GACSTR", "GAC2001+", "GAC3rm+", "FC", "STR2+"}) {
            Solver solver = new Solver("Choco");
            IntVar[] v = VF.enumeratedArray("v", 3, 1, 4, solver);
            solver.post(ICF.table(v, TuplesFactory.allEquals(v), op));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test59() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar S = VF.bounded("S", 0, 3, solver);
        solver.post(ICF.tree(X, S, -1));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test60() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 5, 1, 5, solver);
        IntVar S = VF.bounded("S", 0, 3, solver);
        solver.post(ICF.tsp(X, S, new int[][]{{0, 1, 2, 3, 4}, {1, 0, 1, 2, 3}, {2, 1, 0, 1, 2}, {3, 2, 1, 0, 1}, {4, 3, 2, 1, 0}}));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test61() {
        Solver solver = new Solver("Choco");
        SetVar[] S = new SetVar[4];
        for (int i = 0; i < S.length; i++) {
            S[i] = VF.set("S_" + i, new int[]{1, 2, 3, 4}, solver);
        }
        SetVar U = VF.set("U", new int[]{2, 3}, solver);

        solver.post(SCF.union(S, U));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test62() {
        Solver solver = new Solver("Choco");
        SetVar[] S = new SetVar[4];
        for (int i = 0; i < S.length; i++) {
            S[i] = VF.set("S_" + i, new int[]{1, 2, 3, 4}, solver);
        }
        SetVar U = VF.set("U", new int[]{2, 3}, solver);

        solver.post(SCF.intersection(S, U));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test63() {
        Solver solver = new Solver("Choco");
        SetVar[] S = new SetVar[4];
        for (int i = 0; i < S.length; i++) {
            S[i] = VF.set("S_" + i, new int[]{1, 2, 3, 4}, solver);
        }

        solver.post(SCF.subsetEq(S));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test64() {
        Solver solver = new Solver("Choco");
        SetVar U = VF.set("U", new int[]{2, 3}, solver);
        IntVar I = VF.enumerated("I", 1, 4, solver);

        solver.post(SCF.cardinality(U, I));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test65() {
        Solver solver = new Solver("Choco");
        SetVar[] S = new SetVar[4];
        for (int i = 0; i < S.length; i++) {
            S[i] = VF.set("S_" + i, new int[]{1, 2, 3, 4}, solver);
        }
        IntVar I = VF.enumerated("I", 1, 4, solver);

        solver.post(SCF.nbEmpty(S, I));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test66() {
        Solver solver = new Solver("Choco");
        SetVar[] S = new SetVar[2];
        for (int i = 0; i < S.length; i++) {
            S[i] = VF.set("S_" + i, new int[]{1, 2, 3, 4}, solver);
        }

        solver.post(SCF.offSet(S[0], S[1], 1));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test67() {
        Solver solver = new Solver("Choco");
        SetVar U = VF.set("U", new int[]{2, 3}, solver);

        solver.post(SCF.notEmpty(U));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test68() {
        Solver solver = new Solver("Choco");
        SetVar U = VF.set("U", new int[]{2, 3}, solver);
        IntVar I = VF.enumerated("I", 1, 4, solver);

        solver.post(SCF.sum(U, I, false));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test69() {
        Solver solver = new Solver("Choco");
        SetVar U = VF.set("U", new int[]{2, 3}, solver);
        IntVar I = VF.enumerated("I", 1, 4, solver);

        solver.post(SCF.max(U, I, false));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test70() {
        Solver solver = new Solver("Choco");
        SetVar U = VF.set("U", new int[]{2, 3}, solver);
        IntVar I = VF.enumerated("I", 1, 4, solver);

        solver.post(SCF.min(U, I, false));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test71() {
        Solver solver = new Solver("Choco");
        BoolVar[] bvars = VF.boolArray("b", 4, solver);
        SetVar U = VF.set("U", new int[]{2, 3}, solver);

        solver.post(SCF.bool_channel(bvars, U, 1));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test72() {
        Solver solver = new Solver("Choco");
        SetVar[] S = new SetVar[2];
        for (int i = 0; i < S.length; i++) {
            S[i] = VF.set("S_" + i, new int[]{1, 2, 3, 4}, solver);
        }
        IntVar[] I = VF.enumeratedArray("I", 4, 1, 5, solver);

        solver.post(SCF.int_channel(S, I, 1, 1));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test73() {
        Solver solver = new Solver("Choco");
        SetVar U = VF.set("U", new int[]{2, 3}, solver);
        SetVar V = VF.set("V", new int[]{1, 2, 3}, solver);

        solver.post(SCF.disjoint(U, V));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }


    @Test(groups = "1s")
    public void test74() {
        Solver solver = new Solver("Choco");
        SetVar[] S = new SetVar[4];
        for (int i = 0; i < S.length; i++) {
            S[i] = VF.set("S_" + i, new int[]{1, 2, 3, 4}, solver);
        }
        solver.post(SCF.all_disjoint(S));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test75() {
        Solver solver = new Solver("Choco");
        SetVar[] S = new SetVar[4];
        for (int i = 0; i < S.length; i++) {
            S[i] = VF.set("S_" + i, new int[]{1, 2, 3, 4}, solver);
        }
        solver.post(SCF.all_different(S));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test76() {
        Solver solver = new Solver("Choco");
        SetVar[] S = new SetVar[4];
        for (int i = 0; i < S.length; i++) {
            S[i] = VF.set("S_" + i, new int[]{1, 2, 3, 4}, solver);
        }
        solver.post(SCF.all_equal(S));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test77() {
        Solver solver = new Solver("Choco");
        SetVar[] S = new SetVar[4];
        for (int i = 0; i < S.length; i++) {
            S[i] = VF.set("S_" + i, new int[]{1, 2, 3, 4}, solver);
        }
        SetVar U = VF.set("U", new int[]{2, 3}, solver);
        solver.post(SCF.partition(S, U));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test78() {
        Solver solver = new Solver("Choco");
        SetVar[] S = new SetVar[4];
        for (int i = 0; i < S.length; i++) {
            S[i] = VF.set("S_" + i, new int[]{1, 2, 3, 4}, solver);
        }

        SetVar[] T = new SetVar[4];
        for (int i = 0; i < T.length; i++) {
            T[i] = VF.set("T_" + i, new int[]{1, 2, 3, 4}, solver);
        }
        solver.post(SCF.inverse_set(S, T, 1, 1));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test79() {
        Solver solver = new Solver("Choco");
        SetVar[] S = new SetVar[4];
        for (int i = 0; i < S.length; i++) {
            S[i] = VF.set("S_" + i, new int[]{-5, 0, 1, 2, 4}, solver);
        }
        solver.post(SCF.symmetric(S, -1));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test80() {
        Solver solver = new Solver("Choco");
        SetVar[] S = new SetVar[2];
        for (int i = 0; i < S.length; i++) {
            S[i] = VF.set("S_" + i, new int[]{1, 2, 3, 4}, solver);
        }
        IntVar I = VF.enumerated("I", 1, 5, solver);
        SetVar T = VF.set("T", 1, 3, solver);

        solver.post(SCF.element(I, S, 1, T));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test81() {
        Solver solver = new Solver("Choco");
        SetVar[] S = new SetVar[2];
        for (int i = 0; i < S.length; i++) {
            S[i] = VF.set("S_" + i, new int[]{1, 2, 3, 4}, solver);
        }
        SetVar T = VF.set("T", 1, 3, solver);

        solver.post(SCF.member(S, T));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test82() {
        Solver solver = new Solver("Choco");
        IntVar I = VF.enumerated("I", 1, 5, solver);
        SetVar T = VF.set("T", 1, 3, solver);

        solver.post(SCF.member(I, T));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test
    public void test83() {
        Solver solver = new Solver();
        IntVar[] vars = VF.enumeratedArray("X", 2, -2, 2, solver);
        Tuples tuples = new Tuples();
        tuples.add(0, -1);
        tuples.add(1, -1);
        tuples.add(0, 1);
        solver.post(ICF.mddc(vars, new MultivaluedDecisionDiagram(vars, tuples)));

        Solver copy = solver.duplicateModel();
        solver.findAllSolutions();

        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test84() {
        Solver solver = new Solver("Choco");
        IntVar I = VF.enumerated("I", 1, 5, solver);
        SetVar T = VF.set("T", 1, 3, solver);

        solver.post(SCF.not_member(I, T));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }


    @Test(groups = "1s")
    public void test85() {
        Solver solver = new Solver("Choco");
        BoolVar a = VF.bool("a", solver);
        BoolVar b = VF.bool("b", solver);
        BoolVar c = VF.bool("c", solver);

        SatFactory.addBoolLe(a, b);
        SatFactory.addBoolOrArrayEqualTrue(new BoolVar[]{a, b, c});

        Solver copy = solver.duplicateModel();
        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test86() {
        Solver solver = new Solver("Choco");
        IntVar[][] X = VF.enumeratedMatrix("X", 2, 3, 1, 3, solver);
        IntVar[][] Y = VF.enumeratedMatrix("Y", 2, 3, 1, 3, solver);

        solver.post(ICF.keysorting(X, null, Y, 2));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }
}

