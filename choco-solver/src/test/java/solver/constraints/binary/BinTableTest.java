/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package solver.constraints.binary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.constraints.extension.Tuples;
import solver.constraints.extension.TuplesFactory;
import solver.search.strategy.ISF;
import solver.variables.IntVar;
import solver.variables.VF;

import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/04/2014
 */
public class BinTableTest {


    private static String[] ALGOS = {"FC", "AC2001", "AC3", "AC3rm", "AC3bit+rm"};

    protected static final Logger LOGGER = LoggerFactory.getLogger("test");
    private Solver s;
    private IntVar v1, v2;
    Tuples feasible, infeasible;

    @BeforeMethod
    public void setUp() {
        s = new Solver();

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

    @AfterMethod
    public void tearDown() {
        v1 = null;
        v2 = null;
        feasible = null;
        infeasible = null;
    }


    @Test(groups = "1s")
    public void testFeas1() {
        for (String a : ALGOS) {
            s = new Solver();
            v1 = VF.enumerated("v1", 1, 4, s);
            v2 = VF.enumerated("v2", 1, 4, s);
            s.post(ICF.table(v1, v2, feasible, a));

            s.findAllSolutions();
            assertEquals(5, s.getMeasures().getSolutionCount());
        }
    }


    @Test(groups = "1s")
    public void testInfeas1() {
        for (String a : ALGOS) {
            s = new Solver();
            v1 = VF.enumerated("v1", 1, 4, s);
            v2 = VF.enumerated("v2", 1, 4, s);
            s.post(ICF.table(v1, v2, infeasible, a));

            s.findAllSolutions();
            assertEquals((16 - 5), s.getMeasures().getSolutionCount());
        }
    }


    private Constraint absolute(IntVar v1, IntVar v2, int algo) {
        if (algo > -1) {
            return ICF.table(v1, v2, TuplesFactory.absolute(v1, v2), ALGOS[algo]);
        } else {
            return ICF.absolute(v1, v2);
        }
    }

    @Test(groups = "1s")
    public void testAbsolute() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", -10, 10, solver);
        IntVar v2 = VF.enumerated("v2", -10, 10, solver);
        solver.post(absolute(v1, v2, -1));
        long nbs = solver.findAllSolutions();
        long nbn = solver.getMeasures().getNodeCount();
        for (int a = 0; a < ALGOS.length; a++) {
            for (int s = 0; s < 20; s++) {
                Solver tsolver = new Solver();
                IntVar tv1 = VF.enumerated("tv1", -10, 10, tsolver);
                IntVar tv2 = VF.enumerated("tv2", -10, 10, tsolver);
                tsolver.post(absolute(tv1, tv2, a));
                tsolver.set(ISF.random_value(new IntVar[]{tv1, tv2}));
                Assert.assertEquals(tsolver.findAllSolutions(), nbs);
                if (a > 1) Assert.assertEquals(tsolver.getMeasures().getNodeCount(), nbn);
            }
        }
    }

    private Constraint arithmLT(IntVar v1, IntVar v2, int algo) {
        if (algo > -1) {
            return ICF.table(v1, v2, TuplesFactory.arithm(v1, "<", v2), ALGOS[algo]);
        } else {
            return ICF.arithm(v1, "<", v2);
        }
    }

    @Test(groups = "1s")
    public void testArithmLT() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", -10, 10, solver);
        IntVar v2 = VF.enumerated("v2", -10, 10, solver);
        solver.post(arithmLT(v1, v2, -1));
        long nbs = solver.findAllSolutions();
        long nbn = solver.getMeasures().getNodeCount();
        for (int s = 0; s < 20; s++) {
            for (int a = 0; a < ALGOS.length; a++) {
                Solver tsolver = new Solver();
                IntVar tv1 = VF.enumerated("tv1", -10, 10, tsolver);
                IntVar tv2 = VF.enumerated("tv2", -10, 10, tsolver);
                tsolver.post(arithmLT(tv1, tv2, a));
                tsolver.set(ISF.random_value(new IntVar[]{tv1, tv2}));
                Assert.assertEquals(tsolver.findAllSolutions(), nbs);
                if (a > 1) Assert.assertEquals(tsolver.getMeasures().getNodeCount(), nbn);
            }
        }
    }

    private Constraint arithmNQ(IntVar v1, IntVar v2, int algo) {
        if (algo > -1) {
            return ICF.table(v1, v2, TuplesFactory.arithm(v1, "!=", v2), ALGOS[algo]);
        } else {
            return ICF.arithm(v1, "!=", v2);
        }
    }

    @Test(groups = "1s")
    public void testArithmNQ() {
        Solver solver = new Solver();
        IntVar v1 = VF.enumerated("v1", -10, 10, solver);
        IntVar v2 = VF.enumerated("v2", -10, 10, solver);
        solver.post(arithmNQ(v1, v2, -1));
        long nbs = solver.findAllSolutions();
        long nbn = solver.getMeasures().getNodeCount();
        for (int a = 0; a < ALGOS.length; a++) {
            for (int s = 0; s < 20; s++) {
                Solver tsolver = new Solver();
                IntVar tv1 = VF.enumerated("tv1", -10, 10, tsolver);
                IntVar tv2 = VF.enumerated("tv2", -10, 10, tsolver);
                tsolver.post(arithmNQ(tv1, tv2, a));
                tsolver.set(ISF.random_value(new IntVar[]{tv1, tv2}));
                Assert.assertEquals(tsolver.findAllSolutions(), nbs);
                if (a > 1) Assert.assertEquals(tsolver.getMeasures().getNodeCount(), nbn);
            }
        }
    }

    @Test(groups = "1s")
    public void test2() {
        for (String a : ALGOS) {
            for (int i = 0; i < 10; i++) {
                Tuples tuples = new Tuples(true);
                tuples.add(-2, -2);
                tuples.add(-1, -1);
                tuples.add(0, 0);
                tuples.add(1, 1);

                Solver solver = new Solver();
                IntVar[] vars = VF.enumeratedArray("X", 2, -1, 1, solver);
                solver.post(ICF.table(vars[0], vars[1], tuples, a));

                solver.set(ISF.random_value(vars));
                Assert.assertEquals(solver.findAllSolutions(), 3);
            }
        }
    }

    @Test(groups = "1s")
    public void test3() {
        for (String a : ALGOS) {
            for (int i = 0; i < 10; i++) {
                Tuples tuples = new Tuples(true);
                tuples.add(-2, -2);
                tuples.add(-1, -1);
                tuples.add(0, 0);
                tuples.add(1, 1);

                Solver solver = new Solver();
                IntVar[] vars = VF.enumeratedArray("X", 2, -1, 1, solver);
                solver.post(ICF.table(vars[0], vars[1], tuples, a));

                solver.set(ISF.random_value(vars));
                Assert.assertEquals(solver.findAllSolutions(), 3);
            }
        }
    }


}
