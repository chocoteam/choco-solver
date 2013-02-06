/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.propagation.PropagationStrategies;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * Created by IntelliJ IDEA.
 * User: xlorca
 */
public class TestSolveur {

    @Test(groups = "10s")
    public void testBinaryCliqueNeq() {
        int nbSol = 1;
        for (int kk = 2; kk <= 9; kk++) {
            int n = kk;
            int m = (n * (n - 1)) / 2;
            int min = 1;
            int max = n;
            nbSol *= kk;
            Solver s = new Solver();
            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VariableFactory.enumerated("v_" + i, min, max, s);
            }
            Constraint[] cstrs = new Constraint[m];
            int k = 0;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    //System.out.print("C"+k+" :: "+ vars[i]+ " != " + vars[j]);
                    cstrs[k] = IntConstraintFactory.arithm(vars[i], "!=", vars[j]);
                    //System.out.println(cstrs[k]+ " ");
                    k++;
                }
            }

            s.post(cstrs);
            s.set(IntStrategyFactory.presetI(vars));
            s.findAllSolutions();
            Assert.assertEquals(s.getMeasures().getSolutionCount(), nbSol, "nb sol");
        }
    }

    @Test(groups = "10s")
    public void testOneAllDiff() {
        int nbSol = 1;
        for (int k = 2; k <= 9; k++) {
            int n = k;
            int m = 1;
            int min = 1;
            int max = n;
            nbSol *= k;
            Solver s = new Solver();
            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VariableFactory.enumerated("v_" + i, min, max, s);
            }
            Constraint[] cstrs = new Constraint[m];
            for (int i = 0; i < cstrs.length; i++) {
                cstrs[i] = IntConstraintFactory.alldifferent(vars, "BC");
            }

            s.post(cstrs);
            s.set(IntStrategyFactory.presetI(vars));
            long t = System.currentTimeMillis();
            System.out.println("nb solutions : " + s.findAllSolutions());
            t = System.currentTimeMillis() - t;
            System.out.println("time : " + t);
            Assert.assertEquals(s.getMeasures().getSolutionCount(), nbSol, "nb sol");
        }
    }

    public static void testCycleNeq(int k, int nbSol, int nbNod) {

        int min = 1;
        int max = k - 1;
        Solver s = new Solver();
        IntVar[] vars = new IntVar[k];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("v_" + i, min, max, s);
        }
        Constraint[] cstrs = new Constraint[k];
        int i;
        for (i = 0; i < k - 1; i++) {
            //System.out.println("C("+vars[i]+","+vars[i+1]+")");
            cstrs[i] = IntConstraintFactory.arithm(vars[i], "!=", vars[i + 1]);
        }
        //System.out.println("C("+vars[n-1]+","+vars[0]+")");
        cstrs[i] = IntConstraintFactory.arithm(vars[k - 1], "!=", vars[0]);

        s.post(cstrs);
        s.set(IntStrategyFactory.presetI(vars));
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), nbSol, "nb sol");
        Assert.assertEquals(s.getMeasures().getNodeCount(), nbNod, "nb nod");
    }

    @Test(groups = {"10s"})
    public void testCN3to8() {
        int[] nbSol = {0, 0, 0, 0, 18, 240, 4100, 78120, 1679622};
        int[] nbNod = {0, 0, 0, 1, 35, 479, 8199, 156239, 3359243};
        for (int i = 3; i < 9; i++) {
            testCycleNeq(i, nbSol[i], nbNod[i]);
        }
    }

    @Test(groups = {"10m"})
    public void testCN9() {
        testCycleNeq(9, 40353600, 80707199);
    }

    @Test(groups = {"1s"})
    public void testCycleLt() {
        for (int k = 5; k <= 12; k++) {
            int n = k;
            int m = n - 1;
            int min = 1;
            int max = 2 * n;
            Solver s = new Solver();
            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VariableFactory.enumerated("v_" + i, min, max, s);
            }
            Constraint[] cstrs = new Constraint[m + 1];
            int i;
            for (i = 0; i < n - 1; i++) {
                //System.out.println("C("+vars[i]+","+vars[i+1]+")");
                cstrs[i] = IntConstraintFactory.arithm(vars[i], "<", vars[i + 1]);
            }
            //System.out.println("C("+vars[n-1]+","+vars[0]+")");
            cstrs[i] = IntConstraintFactory.arithm(vars[n - 1], "<", vars[0]);

            s.post(cstrs);
            s.set(IntStrategyFactory.presetI(vars));
            s.findAllSolutions();
            Assert.assertEquals(s.getMeasures().getSolutionCount(), 0, "nb sol");
            Assert.assertEquals(s.getMeasures().getNodeCount(), 0, "nb nod");
        }
    }


    public static void testDecomp(int k, int nbSol, int nbNod) {
        int n = (2 * k);
        int m = n - 1;
        int min = 1;
        int max = k - 2;
        Solver s = new Solver();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("v_" + i, min, max, s);
        }
        Constraint[] cstrs = new Constraint[m];
        int i;
        for (i = 0; i < (n / 2) - 1; i++) {
            //System.out.println("<("+vars[i]+","+vars[i+1]+")");
            cstrs[i] = IntConstraintFactory.arithm(vars[i], "!=", vars[i + 1]);
            //System.out.println(cstrs[i]);
            int j = (n / 2);
            //System.out.println("<("+vars[i+j]+","+vars[i+j+1]+")");
            cstrs[i + j] = IntConstraintFactory.arithm(vars[i + j], "!=", vars[i + j + 1]);
            //System.out.println(cstrs[i+j]);
        }
        cstrs[(n / 2) - 1] = IntConstraintFactory.arithm(vars[(n / 2) - 1], "<", vars[n / 2]);
        //System.out.println(cstrs[(n/2)-1]);

        s.post(cstrs);
        s.set(IntStrategyFactory.presetI(vars));
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), nbSol, "nb sol");
        Assert.assertEquals(s.getMeasures().getNodeCount(), nbNod, "nb nod");
    }

    @Test(groups = {"10s"})
    public void testD3to6() {
        int[] nbSol = {0, 0, 0, 0, 1, 768, 354294};
        int[] nbNod = {0, 0, 0, 0, 1, 1535, 708587};
        for (int i = 3; i < 7; i++) {
            testDecomp(i, nbSol[i], nbNod[i]);
        }
    }

    @Test(groups = {"30m"})
    public void testD7() {
        testDecomp(7, 167772160, 335544319);
    }


    private static void testDecompOpt(int k, int nbSol, int nbNod) {
        int n = (2 * k);
        int m = n - 1;
        int min = 1;
        int max = k - 2;
        Solver s = new Solver();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("v_" + i, min, max, s);
        }
        int i;
        for (i = 0; i < (n / 2) - 1; i++) {
            s.post(IntConstraintFactory.arithm(vars[i], "!=", vars[i + 1]));
            int j = (n / 2);
            s.post(IntConstraintFactory.arithm(vars[i + j], "!=", vars[i + j + 1]));
        }
        s.post(IntConstraintFactory.arithm(vars[(n / 2) - 1], "<", vars[n / 2]));

        s.set(IntStrategyFactory.presetI(vars));
        s.findAllSolutions();
        s.getMeasures().getSolutionCount();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), nbSol, "nb sol");
        Assert.assertEquals(s.getMeasures().getNodeCount(), nbNod, "nb nod");
    }


    @Test(groups = {"1s"})
    public void testDO3to6() {
        int[] nbSol = {0, 0, 0, 0, 1, 768, 354294};
        int[] nbNod = {0, 0, 0, 0, 1, 1535, 708587};
        for (int i = 3; i < 7; i++) {
            testDecompOpt(i, nbSol[i], nbNod[i]);
        }
    }

    @Test(groups = {"30m"})
    public void testDO7() {
        testDecompOpt(7, 167772160, 335544319);
    }

    @Test(groups = {"1s"})
    public void fakePigeonHolesTest() {
        int n = 5;
        Solver solver = new Solver();
        IntVar[] vars = VariableFactory.enumeratedArray("p", n, 0, n, solver);

        for (int i = 0; i < n - 1; i++) {
            solver.post(IntConstraintFactory.arithm(vars[i], "<", vars[i + 1]));
        }
        solver.post(IntConstraintFactory.arithm(vars[0], "=", vars[n - 1]));

        solver.set(IntStrategyFactory.inputOrder_InDomainMin(vars));
        PropagationStrategies.CONSTRAINT.make(solver);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 0, "nb sol");
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 0, "nb nod");
    }


}
