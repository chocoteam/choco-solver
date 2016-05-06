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
package org.chocosolver.solver;

import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.propagation.PropagationEngineFactory.TWOBUCKETPROPAGATIONENGINE;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;
import static org.testng.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: xlorca
 */
public class TestSolveur {

    @Test(groups="10s", timeOut=60000)
    public void testBinaryCliqueNeq() {
        int nbSol = 1;
        for (int kk = 2; kk <= 9; kk++) {
            int m = (kk * (kk - 1)) / 2;
            int min = 1;
            nbSol *= kk;
            Model s = new Model();
            IntVar[] vars = new IntVar[kk];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = s.intVar("v_" + i, min, kk, false);
            }
            for (int i = 0; i < kk - 1; i++) {
                for (int j = i + 1; j < kk; j++) {
                    //System.out.print("C"+k+" :: "+ vars[i]+ " != " + vars[j]);
                    s.arithm(vars[i], "!=", vars[j]).post();
                    //System.out.println(cstrs[k]+ " ");
                }
            }

            s.getSolver().set(inputOrderLBSearch(vars));
            while (s.getSolver().solve()) ;
            assertEquals(s.getSolver().getSolutionCount(), nbSol, "nb sol");
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void testOneAllDiff() {
        int nbSol = 1;
        for (int k = 2; k <= 9; k++) {
            int m = 1;
            int min = 1;
            nbSol *= k;
            Model s = new Model();
            IntVar[] vars = new IntVar[k];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = s.intVar("v_" + i, min, k, false);
            }
            for (int i = 0; i < m; i++) {
                s.allDifferent(vars, "BC").post();
            }
            s.getSolver().set(inputOrderLBSearch(vars));
            while (s.getSolver().solve()) ;
            assertEquals(s.getSolver().getSolutionCount(), nbSol, "nb sol");
        }
    }

    public static void testCycleNeq(int k, int nbSol, int nbNod) {

        int min = 1;
        int max = k - 1;
        Model s = new Model();
        IntVar[] vars = new IntVar[k];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = s.intVar("v_" + i, min, max, false);
        }
        for (int i = 0; i < k - 1; i++) {
            //System.out.println("C("+vars[i]+","+vars[i+1]+")");
            s.arithm(vars[i], "!=", vars[i + 1]).post();
        }
        //System.out.println("C("+vars[n-1]+","+vars[0]+")");
        s.arithm(vars[k - 1], "!=", vars[0]).post();
        s.getSolver().set(inputOrderLBSearch(vars));
        while (s.getSolver().solve()) ;
        assertEquals(s.getSolver().getSolutionCount(), nbSol, "nb sol");
        assertEquals(s.getSolver().getNodeCount(), nbNod, "nb nod");
    }

    @Test(groups="10s", timeOut=60000)
    public void testCN3to8() {
        int[] nbSol = {0, 0, 0, 0, 18, 240, 4100, 78120, 1679622};
        int[] nbNod = {0, 0, 0, 1, 35, 479, 8199, 156239, 3359243};
        for (int i = 3; i < 9; i++) {
            testCycleNeq(i, nbSol[i], nbNod[i]);
        }
    }

    @Test(groups="5m", timeOut=300000)
    public void testCN9() {
        testCycleNeq(9, 40353600, 80707199);
    }

    @Test(groups="1s", timeOut=60000)
    public void testCycleLt() {
        for (int k = 5; k <= 12; k++) {
            int m = k - 1;
            int min = 1;
            int max = 2 * k;
            Model s = new Model();
            IntVar[] vars = new IntVar[k];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = s.intVar("v_" + i, min, max, false);
            }
            for (int i = 0; i < k - 1; i++) {
                //System.out.println("C("+vars[i]+","+vars[i+1]+")");
                s.arithm(vars[i], "<", vars[i + 1]).post();
            }
            //System.out.println("C("+vars[n-1]+","+vars[0]+")");
            s.arithm(vars[k - 1], "<", vars[0]).post();

            s.getSolver().set(inputOrderLBSearch(vars));
            while (s.getSolver().solve()) ;
            assertEquals(s.getSolver().getSolutionCount(), 0, "nb sol");
            assertEquals(s.getSolver().getNodeCount(), 0, "nb nod");
        }
    }


    public static void testDecomp(int k, int nbSol, int nbNod) {
        int n = (2 * k);
        int m = n - 1;
        int min = 1;
        int max = k - 2;
        Model s = new Model();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = s.intVar("v_" + i, min, max, false);
        }
        for (int i = 0; i < (n / 2) - 1; i++) {
            //System.out.println("<("+vars[i]+","+vars[i+1]+")");
            s.arithm(vars[i], "!=", vars[i + 1]).post();
            //System.out.println(cstrs[i]);
            int j = (n / 2);
            //System.out.println("<("+vars[i+j]+","+vars[i+j+1]+")");
            s.arithm(vars[i + j], "!=", vars[i + j + 1]).post();
            //System.out.println(cstrs[i+j]);
        }
        s.arithm(vars[(n / 2) - 1], "<", vars[n / 2]).post();
        //System.out.println(cstrs[(n/2)-1]);

        s.getSolver().set(inputOrderLBSearch(vars));
        while (s.getSolver().solve()) ;
        assertEquals(s.getSolver().getSolutionCount(), nbSol, "nb sol");
        assertEquals(s.getSolver().getNodeCount(), nbNod, "nb nod");
    }

    @Test(groups="10s", timeOut=60000)
    public void testD3to6() {
        int[] nbSol = {0, 0, 0, 0, 1, 768, 354294};
        int[] nbNod = {0, 0, 0, 0, 1, 1535, 708587};
        for (int i = 3; i < 7; i++) {
            testDecomp(i, nbSol[i], nbNod[i]);
        }
    }

    private static void testDecompOpt(int k, int nbSol, int nbNod) {
        int n = (2 * k);
        int min = 1;
        int max = k - 2;
        Model s = new Model();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = s.intVar("v_" + i, min, max, false);
        }
        int i;
        for (i = 0; i < (n / 2) - 1; i++) {
            s.arithm(vars[i], "!=", vars[i + 1]).post();
            int j = (n / 2);
            s.arithm(vars[i + j], "!=", vars[i + j + 1]).post();
        }
        s.arithm(vars[(n / 2) - 1], "<", vars[n / 2]).post();

        s.getSolver().set(inputOrderLBSearch(vars));
        while (s.getSolver().solve()) ;
        s.getSolver().getSolutionCount();
        assertEquals(s.getSolver().getSolutionCount(), nbSol, "nb sol");
        assertEquals(s.getSolver().getNodeCount(), nbNod, "nb nod");
    }


    @Test(groups="10s", timeOut=60000)
    public void testDO3to6() {
        int[] nbSol = {0, 0, 0, 0, 1, 768, 354294};
        int[] nbNod = {0, 0, 0, 0, 1, 1535, 708587};
        for (int i = 3; i < 7; i++) {
            testDecompOpt(i, nbSol[i], nbNod[i]);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void fakePigeonHolesTest() {
        int n = 5;
        Model model = new Model();
        IntVar[] vars = model.intVarArray("p", n, 0, n, false);

        for (int i = 0; i < n - 1; i++) {
            model.arithm(vars[i], "<", vars[i + 1]).post();
        }
        model.arithm(vars[0], "=", vars[n - 1]).post();

        model.getSolver().set(inputOrderLBSearch(vars));
        TWOBUCKETPROPAGATIONENGINE.make(model);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 0, "nb sol");
        assertEquals(model.getSolver().getNodeCount(), 0, "nb nod");
    }


}
