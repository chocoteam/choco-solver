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
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.randomSearch;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/04/2014
 */
public class BinTableTest {


    private static String[] ALGOS = {"FC", "AC2001", "AC3", "AC3rm", "AC3bit+rm"};

    private Model s;
    private IntVar v1, v2;
    Tuples feasible, infeasible;

    public void setUp() {
        feasible = new Tuples(true);
        feasible.add(1, 2);
        feasible.add(1, 3);
        feasible.add(2, 1);
        feasible.add(3, 1);
        feasible.add(4, 1);

        infeasible = new Tuples(false);
        infeasible.add(1, 2);
        infeasible.add(1, 3);
        infeasible.add(2, 1);
        infeasible.add(3, 1);
        infeasible.add(4, 1);

    }

    public void tearDown() {
        v1 = null;
        v2 = null;
        feasible = null;
        infeasible = null;
    }


    @Test(groups="1s", timeOut=60000)
    public void testFeas1() {
        setUp();
        for (String a : ALGOS) {
            s = new Model();
            v1 = s.intVar("v1", 1, 4, false);
            v2 = s.intVar("v2", 1, 4, false);
            s.table(v1, v2, feasible, a).post();

            while (s.solve()) ;
            assertEquals(5, s.getSolver().getMeasures().getSolutionCount());
        }
        tearDown();
    }


    @Test(groups="1s", timeOut=60000)
    public void testInfeas1() {
        setUp();
        for (String a : ALGOS) {
            s = new Model();
            v1 = s.intVar("v1", 1, 4, false);
            v2 = s.intVar("v2", 1, 4, false);
            s.table(v1, v2, infeasible, a).post();

            while (s.solve()) ;
            assertEquals((16 - 5), s.getSolver().getMeasures().getSolutionCount());
        }
        tearDown();
    }


    private Constraint absolute(IntVar v1, IntVar v2, int algo) {
        if (algo > -1) {
            return v1.getModel().table(v1, v2, TuplesFactory.absolute(v1, v2), ALGOS[algo]);
        } else {
            return v1.getModel().absolute(v1, v2);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testAbsolute() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", -10, 10, false);
        IntVar v2 = model.intVar("v2", -10, 10, false);
        absolute(v1, v2, -1).post();
        long nbs = 0;
        while (model.solve()) {
            nbs++;
        }
        long nbn = model.getSolver().getMeasures().getNodeCount();
        for (int a = 0; a < ALGOS.length; a++) {
            for (int s = 0; s < 20; s++) {
                Model tsolver = new Model();
                IntVar tv1 = tsolver.intVar("tv1", -10, 10, false);
                IntVar tv2 = tsolver.intVar("tv2", -10, 10, false);
                absolute(tv1, tv2, a).post();
                tsolver.getSolver().set(randomSearch(new IntVar[]{tv1, tv2}, s));
                long nbSolutions = 0;
                while (tsolver.solve()) {
                    nbSolutions++;
                }
                assertEquals(nbSolutions, nbs);
                if (a > 1) assertEquals(tsolver.getSolver().getMeasures().getNodeCount(), nbn);
            }
        }
    }

    private static Constraint arithmLT(IntVar v1, IntVar v2, int algo) {
        if (algo > -1) {
            return v1.getModel().table(v1, v2, TuplesFactory.arithm(v1, "<", v2), ALGOS[algo]);
        } else {
            return v1.getModel().arithm(v1, "<", v2);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testArithmLT() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", -10, 10, false);
        IntVar v2 = model.intVar("v2", -10, 10, false);
        arithmLT(v1, v2, -1).post();
        long nbs = 0;
        while (model.solve()) {
            nbs++;
        }
        long nbn = model.getSolver().getMeasures().getNodeCount();
        for (int s = 0; s < 20; s++) {
            for (int a = 0; a < ALGOS.length; a++) {
                Model tsolver = new Model();
                IntVar tv1 = tsolver.intVar("tv1", -10, 10, false);
                IntVar tv2 = tsolver.intVar("tv2", -10, 10, false);
                arithmLT(tv1, tv2, a).post();
                tsolver.getSolver().set(randomSearch(new IntVar[]{tv1, tv2}, s));
                long nbSolutions = 0;
                while (tsolver.solve()) {
                    nbSolutions++;
                }
                assertEquals(nbSolutions, nbs);
                if (a > 1) assertEquals(tsolver.getSolver().getMeasures().getNodeCount(), nbn);
            }
        }
    }

    private static Constraint arithmNQ(IntVar v1, IntVar v2, int algo) {
        if (algo > -1) {
            return v1.getModel().table(v1, v2, TuplesFactory.arithm(v1, "!=", v2), ALGOS[algo]);
        } else {
            return v1.getModel().arithm(v1, "!=", v2);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testArithmNQ() {
        Model model = new Model();
        IntVar v1 = model.intVar("v1", -10, 10, false);
        IntVar v2 = model.intVar("v2", -10, 10, false);
        arithmNQ(v1, v2, -1).post();
        long nbs = 0;
        while (model.solve()) {
            nbs++;
        }
        long nbn = model.getSolver().getMeasures().getNodeCount();
        for (int a = 0; a < ALGOS.length; a++) {
            for (int s = 0; s < 20; s++) {
                Model tsolver = new Model();
                IntVar tv1 = tsolver.intVar("tv1", -10, 10, false);
                IntVar tv2 = tsolver.intVar("tv2", -10, 10, false);
                arithmNQ(tv1, tv2, a).post();
                tsolver.getSolver().set(randomSearch(new IntVar[]{tv1, tv2}, s));
                long nbSolutions = 0;
                while (tsolver.solve()) {
                    nbSolutions++;
                }
                assertEquals(nbSolutions, nbs);
                if (a > 1) assertEquals(tsolver.getSolver().getMeasures().getNodeCount(), nbn);
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        for (String a : ALGOS) {
            for (int i = 0; i < 10; i++) {
                Tuples tuples = new Tuples(true);
                tuples.add(-2, -2);
                tuples.add(-1, -1);
                tuples.add(0, 0);
                tuples.add(1, 1);

                Model model = new Model();
                IntVar[] vars = model.intVarArray("X", 2, -1, 1, false);
                model.table(vars[0], vars[1], tuples, a).post();

                model.getSolver().set(randomSearch(vars, i));
                long nbSolutions = 0;
                while (model.solve()) {
                    nbSolutions++;
                }
                assertEquals(nbSolutions, 3);
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        for (String a : ALGOS) {
            for (int i = 0; i < 10; i++) {
                Tuples tuples = new Tuples(true);
                tuples.add(-2, -2);
                tuples.add(-1, -1);
                tuples.add(0, 0);
                tuples.add(1, 1);

                Model model = new Model();
                IntVar[] vars = model.intVarArray("X", 2, -1, 1, false);
                model.table(vars[0], vars[1], tuples, a).post();

                model.getSolver().set(randomSearch(vars, i));
                long nbSolutions = 0;
                while (model.solve()) {
                    nbSolutions++;
                }
                assertEquals(nbSolutions, 3);
            }
        }
    }


}
