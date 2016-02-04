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
package org.chocosolver.solver.constraints.reification;

import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.constraints.checker.DomainBuilder;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.chocosolver.solver.constraints.IntConstraintFactory.member;
import static org.chocosolver.solver.constraints.IntConstraintFactory.not_member;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19 nov. 2010
 */
public class ReifiedTest {


    @Test(groups="1s", timeOut=60000)
    public void testRandomEq() {
        for (int seed = 0; seed < 200; seed++) {
            Random r = new Random(seed);
            double d = r.nextDouble() / 2 + 0.5;
            Solver s = new Solver();

            BoolVar b = s.makeBoolVar("b");
            int[][] values = DomainBuilder.buildFullDomains(2, 0, 15, r, d, false);
            IntVar x = s.makeIntVar("x", values[0]);
            IntVar y = s.makeIntVar("y", values[1]);
            IntVar[] vars = new IntVar[]{b, x, y};

            Constraint cons = IntConstraintFactory.arithm(x, "=", y);
            Constraint oppCons = IntConstraintFactory.arithm(x, "!=", y);

            LogicalConstraintFactory.ifThenElse(b, cons, oppCons);
            s.set(IntStrategyFactory.lexico_LB(vars));
            s.findAllSolutions();
            long sol = s.getMeasures().getSolutionCount();
            Assert.assertEquals(sol, values[0].length * values[1].length, "nb sol incorrect");
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testRandomMember() {
        Solver s = new Solver();

        BoolVar a = s.makeBoolVar("a");
        BoolVar b = s.makeBoolVar("b");
        BoolVar c = s.makeBoolVar("c");
        IntVar x = s.makeIntVar("x", 1, 3, false);
        IntVar y = s.makeIntVar("y", 1, 1, false);
        IntVar z = s.makeIntVar("z", 1, 2, false);

        LogicalConstraintFactory.ifThenElse(a, member(x, new int[]{1, 1}), not_member(x, new int[]{1, 1}));
        LogicalConstraintFactory.ifThenElse(b, member(y, new int[]{1, 1}), not_member(y, new int[]{1, 1}));
        LogicalConstraintFactory.ifThenElse(c, member(z, new int[]{1, 1}), not_member(z, new int[]{1, 1}));

        s.post(IntConstraintFactory.sum(new IntVar[]{a, b, c}, "=", s.makeBoolVar("sum")));

        s.set(IntStrategyFactory.lexico_LB(new IntVar[]{x, y, z}));
        s.findAllSolutions();
        long sol = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sol, 2, "nb sol incorrect");
    }

    @Test(groups="1s", timeOut=60000)
    public void testRandomNeq() {
        for (int seed = 0; seed < 200; seed++) {
            Random r = new Random(seed);
            double d = r.nextDouble() / 2 + 0.5;
            Solver s = new Solver();

            BoolVar b = s.makeBoolVar("b");
            int[][] values = DomainBuilder.buildFullDomains(2, 0, 15, r, d, false);
            IntVar x = s.makeIntVar("x", values[0]);
            IntVar y = s.makeIntVar("y", values[1]);
            IntVar[] vars = new IntVar[]{b, x, y};

            Constraint cons = IntConstraintFactory.arithm(x, "!=", y);
            Constraint oppCons = IntConstraintFactory.arithm(x, "=", y);

            LogicalConstraintFactory.ifThenElse(b, cons, oppCons);

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
            vars1[j] = s1.makeIntVar("v_" + j, values[j]);
        }

        s1.post(IntConstraintFactory.alldifferent(vars1, "AC"));

        s1.set(IntStrategyFactory.lexico_LB(vars1));
        return s1;
    }

    private Solver model2(int i, int[][] values) {
        Solver s2 = new Solver();


        IntVar[] X = new IntVar[i];
        for (int j = 0; j < i; j++) {
            X[j] = s2.makeIntVar("v_" + j, values[j]);
        }

        int[] union = union(values);
        int l = union[0];
        int u = union[union.length - 1];

        BoolVar[][][] mA = new BoolVar[i][][];
        List<BoolVar> listA = new ArrayList<>();
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
                    BoolVar a = s2.makeBoolVar("A" + j + "_" + p + "_" + q);
                    mA[j][p - l][q - p] = a;
                    listA.add(a);

                    Constraint cA = member(X[j], p, q);
                    Constraint ocA = not_member(X[j], p, q);

                    LogicalConstraintFactory.ifThenElse(a, cA, ocA);
                }
            }
        }
//                BoolVar[] B =  Blist.toArray(new BoolVar[Blist.size()]);

        ArrayList<ArrayList<ArrayList<BoolVar>>> apmA = new ArrayList<>();

        for (int p = l; p <= u; p++) {
            apmA.add(p - l, new ArrayList<>());
            for (int q = p; q <= u; q++) {
                apmA.get(p - l).add(q - p, new ArrayList<>());
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
                s2.post(IntConstraintFactory.sum(ai, "=", s2.makeIntVar("sum", 0, q - p + 1, true)));
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
    @Test(groups="5m", timeOut=300000)
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

    @Test(groups="1s", timeOut=60000)
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

    @Test(groups="1s", timeOut=60000)
    public void testBACP() {
        Solver solver = new Solver();
        IntVar cp = solver.makeIntVar("cp", 1, 10, false);
        BoolVar[] bv = solver.makeBoolVarArray("b1", 10);
        for (int i = 1; i <= 10; i++) {
            LogicalConstraintFactory.ifThenElse(bv[i - 1],
                    IntConstraintFactory.arithm(cp, "=", i),
                    IntConstraintFactory.arithm(cp, "!=", i));
        }

        IntVar cp2 = solver.makeIntVar("cp27", 1, 10, false);
        solver.post(IntConstraintFactory.arithm(cp2, ">=", cp));

        BoolVar[] bv2 = solver.makeBoolVarArray("b2", 10);
        for (int i = 1; i <= 10; i++) {
            LogicalConstraintFactory.ifThenElse(bv2[i - 1],
                    IntConstraintFactory.arithm(solver.makeIntVar(i), "<", cp),
                    IntConstraintFactory.arithm(solver.makeIntVar(i), ">=", cp));
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

    @Test(groups="1s", timeOut=60000)
    public void test_wellaweg1() {
        Solver s = new Solver();

        IntVar row[] = new IntVar[3];
        row[0] = s.makeIntVar(2);
        row[1] = s.makeIntVar("R", 0, 100, true);
        row[2] = s.makeIntVar(16);

        IntVar calc[] = new IntVar[2];
        calc[0] = s.makeIntOffsetView(row[0], 2);
        calc[1] = s.makeIntVar("C", 0, 80, true);
        s.post(IntConstraintFactory.sum(new IntVar[]{row[0], row[1]}, "=", calc[1]));

        Constraint[] constraints = new Constraint[4];
        constraints[0] = IntConstraintFactory.arithm(row[1], "=", calc[0]);
        constraints[1] = IntConstraintFactory.arithm(row[1], "!=", calc[0]);
        constraints[2] = IntConstraintFactory.arithm(row[2], "=", calc[1]);
        constraints[3] = IntConstraintFactory.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = s.makeBoolVarArray("A", 2);

        LogicalConstraintFactory.ifThenElse(ab[0], constraints[0], constraints[1]);
        LogicalConstraintFactory.ifThenElse(ab[1], constraints[2], constraints[3]);


        //one row must be wrong
        int max_abs = 1;
        s.post(IntConstraintFactory.sum(ab, "=", ab.length - max_abs));

        s.findAllSolutions();

        Assert.assertEquals(s.getMeasures().getSolutionCount(), 2);

    }

    @Test(groups="1s", timeOut=60000)
    public void test_wellaweg3() {
        Solver s = new Solver();

        IntVar row[] = new IntVar[3];
        row[0] = s.makeIntVar(2);
        row[1] = s.makeIntVar("R", 0, 100, true);
        row[2] = s.makeIntVar(16);

        IntVar calc[] = new IntVar[2];
        calc[0] = s.makeIntScaleView(row[0], 2);
        calc[1] = s.makeIntVar("C", 0, 1600, true);
        s.post(IntConstraintFactory.times(row[0], row[1], calc[1]));

        Constraint[] constraints = new Constraint[4];
        constraints[0] = IntConstraintFactory.arithm(row[1], "=", calc[0]);
        constraints[1] = IntConstraintFactory.arithm(row[1], "!=", calc[0]);
        constraints[2] = IntConstraintFactory.arithm(row[2], "=", calc[1]);
        constraints[3] = IntConstraintFactory.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = s.makeBoolVarArray("A", 2);

        LogicalConstraintFactory.ifThenElse(ab[0], constraints[0], constraints[1]);
        LogicalConstraintFactory.ifThenElse(ab[1], constraints[2], constraints[3]);


        //one row must be wrong
        int max_abs = 1;
        s.post(IntConstraintFactory.sum(ab, "=", ab.length - max_abs));

        s.findAllSolutions();

        Assert.assertEquals(s.getMeasures().getSolutionCount(), 2);

    }

    @Test(groups="1s", timeOut=60000)
    public void test_wellaweg4() {
        Solver s = new Solver();

        IntVar row[] = new IntVar[3];
        row[0] = s.makeIntVar(20);
        row[1] = s.makeIntVar("R", 0, 100, true);
        row[2] = s.makeIntVar(5);

        IntVar calc[] = s.makeIntVarArray("C", 2, 0, 100, true);

        s.post(IntConstraintFactory.eucl_div(row[0], s.makeIntVar(2), calc[0]));
        s.post(IntConstraintFactory.eucl_div(row[0], row[1], calc[1]));

        Constraint[] constraints = new Constraint[4];
        constraints[0] = IntConstraintFactory.arithm(row[1], "=", calc[0]);
        constraints[1] = IntConstraintFactory.arithm(row[1], "!=", calc[0]);
        constraints[2] = IntConstraintFactory.arithm(row[2], "=", calc[1]);
        constraints[3] = IntConstraintFactory.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = s.makeBoolVarArray("A", 2);

        LogicalConstraintFactory.ifThenElse(ab[0], constraints[0], constraints[1]);
        LogicalConstraintFactory.ifThenElse(ab[1], constraints[2], constraints[3]);


        //one row must be wrong
        int max_abs = 1;
        s.post(IntConstraintFactory.sum(ab,"=",ab.length - max_abs));

//        SearchMonitorFactory.log(s, true, false);
        s.findAllSolutions();

        Assert.assertEquals(s.getMeasures().getSolutionCount(), 2);

    }

    @Test(groups="1s", timeOut=60000)
    public void test_wellaweg5() {
        Solver s = new Solver();

        IntVar row[] = new IntVar[3];
        row[0] = s.makeIntVar(100);
        row[1] = s.makeIntVar("R1", 0, 100, true);
        row[2] = s.makeIntVar(5);

        IntVar calc[] = s.makeIntVarArray("C", 2, 0, 100, true);

        s.post(IntConstraintFactory.eucl_div(row[0], s.makeIntVar(25), calc[0]));
        s.post(IntConstraintFactory.eucl_div(row[0], row[1], calc[1]));

        Constraint[] constraints = new Constraint[4];
        constraints[0] = IntConstraintFactory.arithm(row[1], "=", calc[0]);
        constraints[1] = IntConstraintFactory.arithm(row[1], "!=", calc[0]);
        constraints[2] = IntConstraintFactory.arithm(row[2], "=", calc[1]);
        constraints[3] = IntConstraintFactory.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = s.makeBoolVarArray("A", 2);

        LogicalConstraintFactory.ifThenElse(ab[0], constraints[0], constraints[1]);
        LogicalConstraintFactory.ifThenElse(ab[1], constraints[2], constraints[3]);


        //one row must be wrong
        int max_abs = 1;
        s.post(IntConstraintFactory.sum(ab, "=", ab.length - max_abs));

//        SearchMonitorFactory.log(s, true, false);
        s.findAllSolutions();

        Assert.assertEquals(s.getMeasures().getSolutionCount(), 5);

    }

    @Test(groups="1s", timeOut=60000)
    public void test_boussard1() {
        Solver solver = new Solver();
        BoolVar a = solver.makeBoolVar("a");
        BoolVar b = solver.makeBoolVar("b");
        BoolVar c = solver.makeBoolVar("c");

        LogicalConstraintFactory.ifThen(
                a,
                LogicalConstraintFactory.ifThen_reifiable(b, IntConstraintFactory.arithm(c, "=", 1)));
        solver.set(IntStrategyFactory.minDom_LB(new BoolVar[]{a, b, c}));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 7);
    }

}
