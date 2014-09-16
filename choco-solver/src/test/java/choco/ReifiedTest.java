/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco;

import choco.checker.DomainBuilder;
import gnu.trove.set.hash.TIntHashSet;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.LogicalConstraintFactory;
import solver.exception.ContradictionException;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static solver.constraints.IntConstraintFactory.member;
import static solver.constraints.IntConstraintFactory.not_member;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19 nov. 2010
 */
public class ReifiedTest {


    @Test(groups = "1s")
    public void testRandomEq() {
        for (int seed = 0; seed < 200; seed++) {
            Random r = new Random(seed);
            double d = r.nextDouble() / 2 + 0.5;
            Solver s = new Solver();

            BoolVar b = VariableFactory.bool("b", s);
            int[][] values = DomainBuilder.buildFullDomains(2, 0, 15, r, d, false);
            IntVar x = VariableFactory.enumerated("x", values[0], s);
            IntVar y = VariableFactory.enumerated("y", values[1], s);
            IntVar[] vars = new IntVar[]{b, x, y};

            Constraint cons = IntConstraintFactory.arithm(x, "=", y);
            Constraint oppCons = IntConstraintFactory.arithm(x, "!=", y);

            s.post(LogicalConstraintFactory.ifThenElse(b, cons, oppCons));
            s.set(IntStrategyFactory.lexico_LB(vars));
            s.findAllSolutions();
            long sol = s.getMeasures().getSolutionCount();
            Assert.assertEquals(sol, values[0].length * values[1].length, "nb sol incorrect");
        }
    }

    @Test(groups = "1s")
    public void testRandomMember() {
        Solver s = new Solver();

        BoolVar a = VariableFactory.bool("a", s);
        BoolVar b = VariableFactory.bool("b", s);
        BoolVar c = VariableFactory.bool("c", s);
        IntVar x = VariableFactory.enumerated("x", 1, 3, s);
        IntVar y = VariableFactory.enumerated("y", 1, 1, s);
        IntVar z = VariableFactory.enumerated("z", 1, 2, s);
        z.toString();

        s.post(LogicalConstraintFactory.ifThenElse(a, member(x, new int[]{1, 1}), not_member(x, new int[]{1, 1})));
        s.post(LogicalConstraintFactory.ifThenElse(b, member(y, new int[]{1, 1}), not_member(y, new int[]{1, 1})));
        s.post(LogicalConstraintFactory.ifThenElse(c, member(z, new int[]{1, 1}), not_member(z, new int[]{1, 1})));

        s.post(IntConstraintFactory.sum(new IntVar[]{a, b, c}, VariableFactory.bool("sum", s)));

        s.set(IntStrategyFactory.lexico_LB(new IntVar[]{x, y, z}));
        s.findAllSolutions();
        long sol = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sol, 2, "nb sol incorrect");
    }

    @Test(groups = "1s")
    public void testRandomNeq() {
        for (int seed = 0; seed < 200; seed++) {
            Random r = new Random(seed);
            double d = r.nextDouble() / 2 + 0.5;
            Solver s = new Solver();

            BoolVar b = VariableFactory.bool("b", s);
            int[][] values = DomainBuilder.buildFullDomains(2, 0, 15, r, d, false);
            IntVar x = VariableFactory.enumerated("x", values[0], s);
            IntVar y = VariableFactory.enumerated("y", values[1], s);
            IntVar[] vars = new IntVar[]{b, x, y};

            Constraint cons = IntConstraintFactory.arithm(x, "!=", y);
            Constraint oppCons = IntConstraintFactory.arithm(x, "=", y);

            Constraint cstr = LogicalConstraintFactory.ifThenElse(b, cons, oppCons);

            s.post(cstr);
            s.set(IntStrategyFactory.lexico_LB(vars));
            s.findAllSolutions();
            long sol = s.getMeasures().getSolutionCount();
            Assert.assertEquals(sol, values[0].length * values[1].length, "nb sol incorrect");
        }
    }

    private int[] union(int[][] domains) {
        TIntHashSet union = new TIntHashSet();
        for (int i = 0; i < domains.length; i++) {
            union.addAll(domains[i]);
        }
        int[] values = union.toArray();
        Arrays.sort(values);
        return values;
    }


    private Solver model1(int i, int[][] values) {
        Solver s1 = new Solver();

        IntVar[] vars1 = new IntVar[i];
        for (int j = 0; j < i; j++) {
            vars1[j] = VariableFactory.enumerated("v_" + j, values[j], s1);
        }

        s1.post(IntConstraintFactory.alldifferent(vars1, "AC"));

        s1.set(IntStrategyFactory.lexico_LB(vars1));
        return s1;
    }

    private Solver model2(int i, int[][] values) {
        Solver s2 = new Solver();


        IntVar[] X = new IntVar[i];
        for (int j = 0; j < i; j++) {
            X[j] = VariableFactory.enumerated("v_" + j, values[j], s2);
        }

        int[] union = union(values);
        int l = union[0];
        int u = union[union.length - 1];

        BoolVar[][][] mA = new BoolVar[i][][];
        List<BoolVar> listA = new ArrayList<BoolVar>();
//                List<BoolVar> Blist = new ArrayList<BoolVar>();
        for (int j = 0; j < i; j++) {
            mA[j] = new BoolVar[u - l + 1][];
            for (int p = l; p <= u; p++) {
                mA[j][p - l] = new BoolVar[u - p + 1];
//                        BoolVar b = VariableFactory.bool("B" + j + "_" + p, s2);
//                        Blist.add(b);
//                        Constraint cB = ConstraintFactory.leq(X[j], l, s2, eng2);
//                        Constraint ocB = ConstraintFactory.geq(X[j], l + 1, s2, eng2);
//                        lcstrs.add(new ReifiedConstraint(b, cB, ocB, s2, eng2));
                for (int q = p; q <= u; q++) {
                    BoolVar a = VariableFactory.bool("A" + j + "_" + p + "_" + q, s2);
                    mA[j][p - l][q - p] = a;
                    listA.add(a);

                    Constraint cA = member(X[j], p, q);
                    Constraint ocA = not_member(X[j], p, q);

                    s2.post(LogicalConstraintFactory.ifThenElse(a, cA, ocA));
                }
            }
        }
//                BoolVar[] B =  Blist.toArray(new BoolVar[Blist.size()]);

        ArrayList<ArrayList<ArrayList<BoolVar>>> apmA = new ArrayList<ArrayList<ArrayList<BoolVar>>>();

        for (int p = l; p <= u; p++) {
            apmA.add(p - l, new ArrayList<ArrayList<BoolVar>>());
            for (int q = p; q <= u; q++) {
                apmA.get(p - l).add(q - p, new ArrayList<BoolVar>());
                for (int j = 0; j < i; j++) {
                    apmA.get(p - l).get(q - p).add(mA[j][p - l][q - p]);
                }
            }
        }


        for (int p = l; p <= u; p++) {
            for (int q = p; q <= u; q++) {
                BoolVar[] ai = null;
                for (int j = 0; j < i; j++) {
                    ai = apmA.get(p - l).get(q - p).toArray(new BoolVar[apmA.get(p - l).get(q - p).size()]);
                }
                s2.post(IntConstraintFactory.sum(ai, VariableFactory.bounded("sum", 0, q - p + 1, s2)));
            }
        }

        s2.set(IntStrategyFactory.lexico_LB(X));
        return s2;
    }

    /**
     * "Decompositions of All Different, Global Cardinality and Related Constraints"
     * C. Bessiere, G. Katsirelos, N. Narodytska, C.G. Quimper, T. Walsh.
     * Proceedings IJCAI'09, Pasadena CA, pages 419-424.
     */
    @Test(groups = {"1m"})
    public void testAllDifferentDecomp() {

        for (int i = 1; i < 12; i++) {
//            System.out.printf("i : %d\n", i);
            Random r = new Random(i);
            for (double d = 1.0; d <= 1.0; d += 0.125) {

                int[][] values = DomainBuilder.buildFullDomains(i, 1, i, r, d, false);
                Solver s1 = model1(i, values);
                s1.findAllSolutions();

                ////////////////////////

                Solver s2 = model2(i, values);
                s2.findAllSolutions();


                ////////////////////////
                long sol1 = s1.getMeasures().getSolutionCount();
                long sol2 = s2.getMeasures().getSolutionCount();
                Assert.assertEquals(sol2, sol1, "nb sol incorrect");
            }
        }

    }

    @Test(groups = {"1s"})
    public void testAllDifferentDecompSpe1() {

        int[][] values; //= DomainBuilder.buildFullDomains(i, 1, i, r, d, false);
        values = new int[][]{{1, 2}, {1}};
        Solver s1 = model1(2, values);
        s1.findAllSolutions();

        ////////////////////////

        Solver s2 = model2(2, values);
        s2.findAllSolutions();


        ////////////////////////
        long sol1 = s1.getMeasures().getSolutionCount();
        long sol2 = s2.getMeasures().getSolutionCount();
        Assert.assertEquals(sol2, sol1, "nb sol incorrect");

    }

    @Test(groups = {"1s"})
    public void testBACP() {
        Solver solver = new Solver();
        IntVar cp = VariableFactory.enumerated("cp", 1, 10, solver);
        BoolVar[] bv = VariableFactory.boolArray("b1", 10, solver);
        for (int i = 1; i <= 10; i++) {
            solver.post(LogicalConstraintFactory.ifThenElse(bv[i - 1],
                    IntConstraintFactory.arithm(cp, "=", i),
                    IntConstraintFactory.arithm(cp, "!=", i)));
        }

        IntVar cp2 = VariableFactory.enumerated("cp27", 1, 10, solver);
        solver.post(IntConstraintFactory.arithm(cp2, ">=", cp));

        BoolVar[] bv2 = VariableFactory.boolArray("b2", 10, solver);
        for (int i = 1; i <= 10; i++) {
            solver.post(LogicalConstraintFactory.ifThenElse(bv2[i - 1],
                    IntConstraintFactory.arithm(VariableFactory.fixed(i, solver), "<", cp),
                    IntConstraintFactory.arithm(VariableFactory.fixed(i, solver), ">=", cp)));
        }

        try {
            solver.propagate();
            cp.updateUpperBound(5, Cause.Null);
            solver.propagate();
            bv[0].instantiateTo(1, Cause.Null);
            solver.propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }

    }

    @Test(groups = "1s")
    public void test_wellaweg1() {
        Solver s = new Solver();

        IntVar row[] = new IntVar[3];
        row[0] = VariableFactory.fixed(2, s);
        row[1] = VariableFactory.bounded("R", 0, 100, s);
        row[2] = VariableFactory.fixed(16, s);

        IntVar calc[] = new IntVar[2];
        calc[0] = VariableFactory.offset(row[0], 2);
        calc[1] = VariableFactory.bounded("C", 0, 80, s);
        s.post(IntConstraintFactory.sum(new IntVar[]{row[0], row[1]}, calc[1]));

        Constraint[] constraints = new Constraint[4];
        constraints[0] = IntConstraintFactory.arithm(row[1], "=", calc[0]);
        constraints[1] = IntConstraintFactory.arithm(row[1], "!=", calc[0]);
        constraints[2] = IntConstraintFactory.arithm(row[2], "=", calc[1]);
        constraints[3] = IntConstraintFactory.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = VariableFactory.boolArray("A", 2, s);

        s.post(LogicalConstraintFactory.ifThenElse(ab[0], constraints[0], constraints[1]));
        s.post(LogicalConstraintFactory.ifThenElse(ab[1], constraints[2], constraints[3]));


        //one row must be wrong
        int max_abs = 1;
        s.post(IntConstraintFactory.sum(ab, VariableFactory.fixed(ab.length - max_abs, s)));

        s.findAllSolutions();

        Assert.assertEquals(s.getMeasures().getSolutionCount(), 2);

    }

    @Test(groups = "1s")
    public void test_wellaweg3() {
        Solver s = new Solver();

        IntVar row[] = new IntVar[3];
        row[0] = VariableFactory.fixed(2, s);
        row[1] = VariableFactory.bounded("R", 0, 100, s);
        row[2] = VariableFactory.fixed(16, s);

        IntVar calc[] = new IntVar[2];
        calc[0] = VariableFactory.scale(row[0], 2);
        calc[1] = VariableFactory.bounded("C", 0, 1600, s);
        s.post(IntConstraintFactory.times(row[0], row[1], calc[1]));

        Constraint[] constraints = new Constraint[4];
        constraints[0] = IntConstraintFactory.arithm(row[1], "=", calc[0]);
        constraints[1] = IntConstraintFactory.arithm(row[1], "!=", calc[0]);
        constraints[2] = IntConstraintFactory.arithm(row[2], "=", calc[1]);
        constraints[3] = IntConstraintFactory.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = VariableFactory.boolArray("A", 2, s);

        s.post(LogicalConstraintFactory.ifThenElse(ab[0], constraints[0], constraints[1]));
        s.post(LogicalConstraintFactory.ifThenElse(ab[1], constraints[2], constraints[3]));


        //one row must be wrong
        int max_abs = 1;
        s.post(IntConstraintFactory.sum(ab, VariableFactory.fixed(ab.length - max_abs, s)));

        s.findAllSolutions();

        Assert.assertEquals(s.getMeasures().getSolutionCount(), 2);

    }

    @Test(groups = "1s")
    public void test_wellaweg4() {
        Solver s = new Solver();

        IntVar row[] = new IntVar[3];
        row[0] = VariableFactory.fixed(20, s);
        row[1] = VariableFactory.bounded("R", 0, 100, s);
        row[2] = VariableFactory.fixed(5, s);

        IntVar calc[] = VariableFactory.boundedArray("C", 2, 0, 100, s);

        s.post(IntConstraintFactory.eucl_div(row[0], VariableFactory.fixed(2, s), calc[0]));
        s.post(IntConstraintFactory.eucl_div(row[0], row[1], calc[1]));

        Constraint[] constraints = new Constraint[4];
        constraints[0] = IntConstraintFactory.arithm(row[1], "=", calc[0]);
        constraints[1] = IntConstraintFactory.arithm(row[1], "!=", calc[0]);
        constraints[2] = IntConstraintFactory.arithm(row[2], "=", calc[1]);
        constraints[3] = IntConstraintFactory.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = VariableFactory.boolArray("A", 2, s);

        s.post(LogicalConstraintFactory.ifThenElse(ab[0], constraints[0], constraints[1]));
        s.post(LogicalConstraintFactory.ifThenElse(ab[1], constraints[2], constraints[3]));


        //one row must be wrong
        int max_abs = 1;
        s.post(IntConstraintFactory.sum(ab, VariableFactory.fixed(ab.length - max_abs, s)));

//        SearchMonitorFactory.log(s, true, false);
        s.findAllSolutions();

        Assert.assertEquals(s.getMeasures().getSolutionCount(), 2);

    }

    @Test(groups = "1s")
    public void test_wellaweg5() {
        Solver s = new Solver();

        IntVar row[] = new IntVar[3];
        row[0] = VariableFactory.fixed(100, s);
        row[1] = VariableFactory.bounded("R1", 0, 100, s);
        row[2] = VariableFactory.fixed(5, s);

        IntVar calc[] = VariableFactory.boundedArray("C", 2, 0, 100, s);

        s.post(IntConstraintFactory.eucl_div(row[0], VariableFactory.fixed(25, s), calc[0]));
        s.post(IntConstraintFactory.eucl_div(row[0], row[1], calc[1]));

        Constraint[] constraints = new Constraint[4];
        constraints[0] = IntConstraintFactory.arithm(row[1], "=", calc[0]);
        constraints[1] = IntConstraintFactory.arithm(row[1], "!=", calc[0]);
        constraints[2] = IntConstraintFactory.arithm(row[2], "=", calc[1]);
        constraints[3] = IntConstraintFactory.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = VariableFactory.boolArray("A", 2, s);

        s.post(LogicalConstraintFactory.ifThenElse(ab[0], constraints[0], constraints[1]));
        s.post(LogicalConstraintFactory.ifThenElse(ab[1], constraints[2], constraints[3]));


        //one row must be wrong
        int max_abs = 1;
        s.post(IntConstraintFactory.sum(ab, VariableFactory.fixed(ab.length - max_abs, s)));

//        SearchMonitorFactory.log(s, true, false);
        s.findAllSolutions();

        Assert.assertEquals(s.getMeasures().getSolutionCount(), 5);

    }

    @Test(groups = "1s")
    public void test_boussard1() {
        Solver solver = new Solver();
        BoolVar a = VariableFactory.bool("a", solver);
        BoolVar b = VariableFactory.bool("b", solver);
        BoolVar c = VariableFactory.bool("c", solver);

        solver.post(LogicalConstraintFactory.ifThen(
                a,
                LogicalConstraintFactory.ifThen(
                        b,
                        IntConstraintFactory.arithm(c, "=", 1))));
        solver.set(IntStrategyFactory.minDom_LB(new BoolVar[]{a, b, c}));
        if (solver.findSolution()) {
            int index = 0;
            do {
                index++;
//                System.out.println(index + " : a=" + a.getValue() + ", b=" + b.getValue() + ",c= " + c.getValue());
            }
            while (solver.nextSolution());
//            System.out.println("nombre total de solutions = " + index);
        }
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 7);
    }

}
