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

package choco;

import choco.checker.DomainBuilder;
import gnu.trove.set.hash.TIntHashSet;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.IntLinComb;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.reified.ReifiedConstraint;
import solver.constraints.unary.Member;
import solver.constraints.unary.NotMember;
import solver.search.strategy.StrategyFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

            Constraint cons = ConstraintFactory.eq(x, y, s);
            Constraint oppCons = ConstraintFactory.neq(x, y, s);

            Constraint[] cstrs = new Constraint[]{new ReifiedConstraint(b, cons, oppCons, s)};

            s.post(cstrs);
            s.set(StrategyFactory.presetI(vars, s.getEnvironment()));
            s.findAllSolutions();
            long sol = s.getMeasures().getSolutionCount();
            Assert.assertEquals(sol, x.getDomainSize() * y.getDomainSize(), "nb sol incorrect");
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

        List<Constraint> lcstrs = new ArrayList<Constraint>();
        lcstrs.add(new ReifiedConstraint(a, new Member(x, new int[]{1, 1}, s), new NotMember(x, new int[]{1, 1}, s), s));
        lcstrs.add(new ReifiedConstraint(b, new Member(y, new int[]{1, 1}, s), new NotMember(y, new int[]{1, 1}, s), s));
        lcstrs.add(new ReifiedConstraint(c, new Member(z, new int[]{1, 1}, s), new NotMember(z, new int[]{1, 1}, s), s));

        lcstrs.add(ConstraintFactory.sum(new IntVar[]{a, b, c}, IntLinComb.Operator.LEQ, 1, s));

        Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

        s.post(cstrs);
        s.set(StrategyFactory.presetI(new IntVar[]{x, y, z}, s.getEnvironment()));
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

            Constraint cons = ConstraintFactory.neq(x, y, s);
            Constraint oppCons = ConstraintFactory.eq(x, y, s);

            Constraint[] cstrs = new Constraint[]{new ReifiedConstraint(b, cons, oppCons, s)};

            s.post(cstrs);
            s.set(StrategyFactory.presetI(vars, s.getEnvironment()));
            s.findAllSolutions();
            long sol = s.getMeasures().getSolutionCount();
            Assert.assertEquals(sol, x.getDomainSize() * y.getDomainSize(), "nb sol incorrect");
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

        s1.post(new AllDifferent(vars1, s1, AllDifferent.Type.AC));

        s1.set(StrategyFactory.presetI(vars1, s1.getEnvironment()));
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

                    Constraint cA = new Member(X[j], p, q, s2);
                    Constraint ocA = new NotMember(X[j], p, q, s2);

                    s2.post(new ReifiedConstraint(a, cA, ocA, s2));
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
                s2.post(ConstraintFactory.sum(ai, IntLinComb.Operator.LEQ, q - p + 1, s2));
            }
        }

        s2.set(StrategyFactory.presetI(X, s2.getEnvironment()));
        return s2;
    }

    /**
     * "Decompositions of All Different, Global Cardinality and Related Constraints"
     * C. Bessiere, G. Katsirelos, N. Narodytska, C.G. Quimper, T. Walsh.
     * Proceedings IJCAI'09, Pasadena CA, pages 419-424.
     */
    @Test(groups = {"10s"})
    public void testAllDifferentDecomp() {

        for (int i = 1; i < 12; i++) {
            System.out.printf("i : %d\n", i);
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

}
