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
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.constraints.checker.DomainBuilder.buildFullDomains;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.firstLBSearch;
import static org.testng.Assert.assertEquals;


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
            Model s = new Model();

            BoolVar b = s.boolVar("b");
            int[][] values = buildFullDomains(2, 0, 15, r, d, false);
            IntVar x = s.intVar("x", values[0]);
            IntVar y = s.intVar("y", values[1]);
            IntVar[] vars = new IntVar[]{b, x, y};

            Constraint cons = s.arithm(x, "=", y);
            Constraint oppCons = s.arithm(x, "!=", y);

            s.ifThenElse(b, cons, oppCons);
            s.getResolver().set(firstLBSearch(vars));
            while (s.solve()) ;
            long sol = s.getResolver().getMeasures().getSolutionCount();
            assertEquals(sol, values[0].length * values[1].length, "nb sol incorrect");
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testRandomMember() {
        Model s = new Model();

        BoolVar a = s.boolVar("a");
        BoolVar b = s.boolVar("b");
        BoolVar c = s.boolVar("c");
        IntVar x = s.intVar("x", 1, 3, false);
        IntVar y = s.intVar("y", 1, 1, false);
        IntVar z = s.intVar("z", 1, 2, false);

        s.ifThenElse(a, s.member(x, new int[]{1, 1}), s.notMember(x, new int[]{1, 1}));
        s.ifThenElse(b, s.member(y, new int[]{1, 1}), s.notMember(y, new int[]{1, 1}));
        s.ifThenElse(c, s.member(z, new int[]{1, 1}), s.notMember(z, new int[]{1, 1}));

        s.sum(new IntVar[]{a, b, c}, "=", s.boolVar("sum")).post();

        s.getResolver().set(firstLBSearch(new IntVar[]{x, y, z}));
        while (s.solve()) ;
        long sol = s.getResolver().getMeasures().getSolutionCount();
        assertEquals(sol, 2, "nb sol incorrect");
    }

    @Test(groups="1s", timeOut=60000)
    public void testRandomNeq() {
        for (int seed = 0; seed < 200; seed++) {
            Random r = new Random(seed);
            double d = r.nextDouble() / 2 + 0.5;
            Model s = new Model();

            BoolVar b = s.boolVar("b");
            int[][] values = buildFullDomains(2, 0, 15, r, d, false);
            IntVar x = s.intVar("x", values[0]);
            IntVar y = s.intVar("y", values[1]);
            IntVar[] vars = new IntVar[]{b, x, y};

            Constraint cons = s.arithm(x, "!=", y);
            Constraint oppCons = s.arithm(x, "=", y);

            s.ifThenElse(b, cons, oppCons);

            s.getResolver().set(firstLBSearch(vars));
            while (s.solve()) ;
            long sol = s.getResolver().getMeasures().getSolutionCount();
            assertEquals(sol, values[0].length * values[1].length, "nb sol incorrect");
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


    private Model model1(int i, int[][] values) {
        Model s1 = new Model();

        IntVar[] vars1 = new IntVar[i];
        for (int j = 0; j < i; j++) {
            vars1[j] = s1.intVar("v_" + j, values[j]);
        }

        s1.allDifferent(vars1, "AC").post();

        s1.getResolver().set(firstLBSearch(vars1));
        return s1;
    }

    private Model model2(int i, int[][] values) {
        Model s2 = new Model();


        IntVar[] X = new IntVar[i];
        for (int j = 0; j < i; j++) {
            X[j] = s2.intVar("v_" + j, values[j]);
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
                    BoolVar a = s2.boolVar("A" + j + "_" + p + "_" + q);
                    mA[j][p - l][q - p] = a;
                    listA.add(a);

                    Constraint cA = s2.member(X[j], p, q);
                    Constraint ocA = s2.notMember(X[j], p, q);

                    s2.ifThenElse(a, cA, ocA);
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
                s2.sum(ai, "=", s2.intVar("sum", 0, q - p + 1, true)).post();
            }
        }

        s2.getResolver().set(firstLBSearch(X));
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

                int[][] values = buildFullDomains(i, 1, i, r, d, false);
                Model s1 = model1(i, values);
                while (s1.solve()) ;

                ////////////////////////

                Model s2 = model2(i, values);
                while (s2.solve()) ;


                ////////////////////////
                long sol1 = s1.getResolver().getMeasures().getSolutionCount();
                long sol2 = s2.getResolver().getMeasures().getSolutionCount();
                assertEquals(sol2, sol1, "nb sol incorrect");
            }
        }

    }

    @Test(groups="1s", timeOut=60000)
    public void testAllDifferentDecompSpe1() {

        int[][] values; //= DomainBuilder.buildFullDomains(i, 1, i, r, d, false);
        values = new int[][]{{1, 2}, {1}};
        Model s1 = model1(2, values);
        while (s1.solve()) ;

        ////////////////////////

        Model s2 = model2(2, values);
        while (s2.solve()) ;


        ////////////////////////
        long sol1 = s1.getResolver().getMeasures().getSolutionCount();
        long sol2 = s2.getResolver().getMeasures().getSolutionCount();
        assertEquals(sol2, sol1, "nb sol incorrect");

    }

    @Test(groups="1s", timeOut=60000)
    public void testBACP() {
        Model model = new Model();
        IntVar cp = model.intVar("cp", 1, 10, false);
        BoolVar[] bv = model.boolVarArray("b1", 10);
        for (int i = 1; i <= 10; i++) {
            model.ifThenElse(bv[i - 1],
                    model.arithm(cp, "=", i),
                    model.arithm(cp, "!=", i));
        }

        IntVar cp2 = model.intVar("cp27", 1, 10, false);
        model.arithm(cp2, ">=", cp).post();

        BoolVar[] bv2 = model.boolVarArray("b2", 10);
        for (int i = 1; i <= 10; i++) {
            model.ifThenElse(bv2[i - 1],
                    model.arithm(model.intVar(i), "<", cp),
                    model.arithm(model.intVar(i), ">=", cp));
        }

        try {
            model.getResolver().propagate();
            cp.updateUpperBound(5, Null);
            model.getResolver().propagate();
            bv[0].instantiateTo(1, Null);
            model.getResolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }

    }

    @Test(groups="1s", timeOut=60000)
    public void test_wellaweg1() {
        Model s = new Model();

        IntVar row[] = new IntVar[3];
        row[0] = s.intVar(2);
        row[1] = s.intVar("R", 0, 100, true);
        row[2] = s.intVar(16);

        IntVar calc[] = new IntVar[2];
        calc[0] = s.intOffsetView(row[0], 2);
        calc[1] = s.intVar("C", 0, 80, true);
        s.sum(new IntVar[]{row[0], row[1]}, "=", calc[1]).post();

        Constraint[] constraints = new Constraint[4];
        constraints[0] = s.arithm(row[1], "=", calc[0]);
        constraints[1] = s.arithm(row[1], "!=", calc[0]);
        constraints[2] = s.arithm(row[2], "=", calc[1]);
        constraints[3] = s.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = s.boolVarArray("A", 2);

        s.ifThenElse(ab[0], constraints[0], constraints[1]);
        s.ifThenElse(ab[1], constraints[2], constraints[3]);


        //one row must be wrong
        int max_abs = 1;
        s.sum(ab, "=", ab.length - max_abs).post();

        while (s.solve()) ;

        assertEquals(s.getResolver().getMeasures().getSolutionCount(), 2);

    }

    @Test(groups="1s", timeOut=60000)
    public void test_wellaweg3() {
        Model s = new Model();

        IntVar row[] = new IntVar[3];
        row[0] = s.intVar(2);
        row[1] = s.intVar("R", 0, 100, true);
        row[2] = s.intVar(16);

        IntVar calc[] = new IntVar[2];
        calc[0] = s.intScaleView(row[0], 2);
        calc[1] = s.intVar("C", 0, 1600, true);
        s.times(row[0], row[1], calc[1]).post();

        Constraint[] constraints = new Constraint[4];
        constraints[0] = s.arithm(row[1], "=", calc[0]);
        constraints[1] = s.arithm(row[1], "!=", calc[0]);
        constraints[2] = s.arithm(row[2], "=", calc[1]);
        constraints[3] = s.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = s.boolVarArray("A", 2);

        s.ifThenElse(ab[0], constraints[0], constraints[1]);
        s.ifThenElse(ab[1], constraints[2], constraints[3]);


        //one row must be wrong
        int max_abs = 1;
        s.sum(ab, "=", ab.length - max_abs).post();

        while (s.solve()) ;

        assertEquals(s.getResolver().getMeasures().getSolutionCount(), 2);

    }

    @Test(groups="1s", timeOut=60000)
    public void test_wellaweg4() {
        Model s = new Model();

        IntVar row[] = new IntVar[3];
        row[0] = s.intVar(20);
        row[1] = s.intVar("R", 0, 100, true);
        row[2] = s.intVar(5);

        IntVar calc[] = s.intVarArray("C", 2, 0, 100, true);

        s.div(row[0], s.intVar(2), calc[0]).post();
        s.div(row[0], row[1], calc[1]).post();

        Constraint[] constraints = new Constraint[4];
        constraints[0] = s.arithm(row[1], "=", calc[0]);
        constraints[1] = s.arithm(row[1], "!=", calc[0]);
        constraints[2] = s.arithm(row[2], "=", calc[1]);
        constraints[3] = s.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = s.boolVarArray("A", 2);

        s.ifThenElse(ab[0], constraints[0], constraints[1]);
        s.ifThenElse(ab[1], constraints[2], constraints[3]);


        //one row must be wrong
        int max_abs = 1;
        s.sum(ab, "=", ab.length - max_abs).post();

//        SearchMonitorFactory.log(s, true, false);
        while (s.solve()) ;

        assertEquals(s.getResolver().getMeasures().getSolutionCount(), 2);

    }

    @Test(groups="1s", timeOut=60000)
    public void test_wellaweg5() {
        Model s = new Model();

        IntVar row[] = new IntVar[3];
        row[0] = s.intVar(100);
        row[1] = s.intVar("R1", 0, 100, true);
        row[2] = s.intVar(5);

        IntVar calc[] = s.intVarArray("C", 2, 0, 100, true);

        s.div(row[0], s.intVar(25), calc[0]).post();
        s.div(row[0], row[1], calc[1]).post();

        Constraint[] constraints = new Constraint[4];
        constraints[0] = s.arithm(row[1], "=", calc[0]);
        constraints[1] = s.arithm(row[1], "!=", calc[0]);
        constraints[2] = s.arithm(row[2], "=", calc[1]);
        constraints[3] = s.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = s.boolVarArray("A", 2);

        s.ifThenElse(ab[0], constraints[0], constraints[1]);
        s.ifThenElse(ab[1], constraints[2], constraints[3]);


        //one row must be wrong
        int max_abs = 1;
        s.sum(ab, "=", ab.length - max_abs).post();

//        SearchMonitorFactory.log(s, true, false);
        while (s.solve()) ;

        assertEquals(s.getResolver().getMeasures().getSolutionCount(), 5);

    }
}