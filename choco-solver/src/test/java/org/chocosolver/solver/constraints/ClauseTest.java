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
package org.chocosolver.solver.constraints;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.constraints.nary.cnf.PropSat;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.constraints.SatFactory.addClauses;
import static org.chocosolver.solver.constraints.nary.cnf.LogOp.*;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.firstLBSearch;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.randomSearch;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22 nov. 2010
 */
public class ClauseTest {

    @Test(groups="5m", timeOut=300000)
    public void test1() {
        int nSol = 1;
        for (int n = 1; n < 16; n++) {
            for (int i = 0; i <= n; i++) {
                Model s = new Model();

                final BoolVar[] bsource = new BoolVar[n];
                final BoolVar[] bs = new BoolVar[n];

                for (int j = 0; j < n; j++) {
                    bsource[j] = s.boolVar("b" + j);
                }

                for (int j = 0; j < n; j++) {
                    if (j >= i) {
                        bs[j] = bsource[j].not();
                    } else {
                        bs[j] = bsource[j];
                    }
                }

                LogOp or = or(bs);
                addClauses(or, s);
                s.getResolver().set(firstLBSearch(bs));

                while (s.solve()) ;
                long sol = s.getResolver().getMeasures().getSolutionCount();
                assertEquals(sol, nSol);
            }
            nSol = nSol * 2 + 1;
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testBothAnd() {
        Model s = new Model();

        BoolVar[] bs = new BoolVar[1];
        bs[0] = s.boolVar("to be");

        LogOp and = and(bs[0], bs[0].not());

        addClauses(and, s);
        s.getResolver().set(firstLBSearch(bs));
        while (s.solve()) ;
        long sol = s.getResolver().getMeasures().getSolutionCount();
        assertEquals(sol, 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBothOr() {
        Model s = new Model();

        BoolVar b = s.boolVar("to be");

        LogOp or = or(b, b.not());

        addClauses(or, s);

        BoolVar[] bs = new BoolVar[]{b};
        s.getResolver().set(firstLBSearch(bs));
//        SMF.log(s, true, true);
        while (s.solve()) ;
        long sol = s.getResolver().getMeasures().getSolutionCount();
        assertEquals(sol, 2);
    }


    @Test(groups="1s", timeOut=60000)
    public void test2() {
        Model model = new Model();
        BoolVar[] bvars = model.boolVarArray("b", 2);
        LogOp tree = LogOp.or(bvars[0], bvars[1]);
        SatFactory.addClauses(tree, model);

        try {
            model.getResolver().propagate();
            bvars[1].instantiateTo(0, Cause.Null);
            bvars[0].instantiateTo(1, Cause.Null);
            model.getResolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test30() {
        Model model = new Model();
        BoolVar[] bvars = model.boolVarArray("b", 2);
        LogOp tree = LogOp.or(bvars[0], bvars[1]);
        SatFactory.addClauses(tree, model);

        try {
            model.getResolver().propagate();
            bvars[1].instantiateTo(1, Cause.Null);
            bvars[0].instantiateTo(0, Cause.Null);
            model.getResolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test31() {
        Model model = new Model();
        BoolVar[] bvars = model.boolVarArray("b", 2);
        LogOp tree = LogOp.or(bvars[0], bvars[1].not());
        SatFactory.addClauses(tree, model);

        try {
            model.getResolver().propagate();
            bvars[0].instantiateTo(0, Cause.Null);
            model.getResolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
        Assert.assertTrue(bvars[1].isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void test32() {
        Model model = new Model();
        BoolVar[] bvars = model.boolVarArray("b", 2);
        LogOp tree = LogOp.or(bvars[0], bvars[1].not());
        SatFactory.addClauses(tree, model);

        try {
            model.getResolver().propagate();
            bvars[1].instantiateTo(1, Cause.Null);
            model.getResolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
        Assert.assertTrue(bvars[0].isInstantiatedTo(1));
    }

    @Test(groups="1s", timeOut=60000)
    public void test33() {
        Model model = new Model();
        BoolVar[] bvars = model.boolVarArray("b", 3);
        LogOp tree = LogOp.or(bvars[0], bvars[1].not(), bvars[2].not());
        SatFactory.addClauses(tree, model);

        try {
            model.getResolver().propagate();
            bvars[0].instantiateTo(0, Cause.Null);
            bvars[2].instantiateTo(0, Cause.Null);
            bvars[1].instantiateTo(1, Cause.Null);
            model.getResolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        for (int seed = 0; seed < 2000; seed++) {
            long n1, n2;
            {
                Model model = new Model();
                BoolVar[] bvars = model.boolVarArray("b", 3);
                LogOp tree = ifOnlyIf(
                        and(bvars[1], bvars[2]),
                        bvars[0]);
                addClauses(tree, model);

                model.getResolver().set(randomSearch(bvars, seed));
                while (model.solve()) ;
                n1 = model.getResolver().getMeasures().getSolutionCount();
            }
            {
                Model model = new Model();
                BoolVar[] bvars = model.boolVarArray("b", 3);
                model.times(bvars[1], bvars[2], bvars[0]).post();

                model.getResolver().set(randomSearch(bvars, seed));
                while (model.solve()) ;
                n2 = model.getResolver().getMeasures().getSolutionCount();
            }
            Assert.assertEquals(n2, n1, String.format("seed: %d", seed));
        }

    }

    @Test(groups="10s", timeOut=60000)
    public void test5() {
        Random rand = new Random();
        for (int seed = 0; seed < 20000; seed++) {
            rand.setSeed(seed);
            int n1 = rand.nextInt(3);
            int n2 = rand.nextInt(3);
            while (n1 == n2) {
                n2 = rand.nextInt(3);
            }
            boolean b1 = rand.nextBoolean();
            boolean b2 = rand.nextBoolean();
            boolean s1, s2;
            {
                Model model = new Model();
                BoolVar[] bvars = model.boolVarArray("b", 3);
                LogOp tree = LogOp.ifOnlyIf(
                        LogOp.and(bvars[1], bvars[2]),
                        bvars[0]);
                SatFactory.addClauses(tree, model);
                try {
                    model.getResolver().propagate();
                    bvars[n1].instantiateTo(b1 ? 1 : 0, Cause.Null);
                    bvars[n2].instantiateTo(b2 ? 1 : 0, Cause.Null);
                    s1 = true;
                } catch (ContradictionException cex) {
                    s1 = false;
                }
            }
            {
                Model model = new Model();
                BoolVar[] bvars = model.boolVarArray("b", 3);
                model.times(bvars[1], bvars[2], bvars[0]).post();
                try {
                    model.getResolver().propagate();
                    bvars[n1].instantiateTo(b1 ? 1 : 0, Null);
                    bvars[n2].instantiateTo(b2 ? 1 : 0, Null);
                    s2 = true;
                } catch (ContradictionException cex) {
                    s2 = false;
                }
            }
            Assert.assertEquals(s2, s1, String.format("seed: %d", seed));
        }

    }

    @Test(groups="1s", timeOut=60000)
    public void test6() throws ContradictionException {
        int n = 10;
        Model s = new Model();
        IEnvironment e = s.getEnvironment();
        BoolVar[] bs = new BoolVar[n];
        bs[0] = s.boolVar("b0");
        SatFactory.addFalse(bs[0]);
        PropSat sat = s.getMinisat().getPropSat();

        e.worldPush();
        s.getResolver().propagate();
        for (int i = 1; i < n; i++) {
            e.worldPush();
            bs[i] = s.boolVar("b" + i);
            sat.addLearnt(sat.Literal(bs[i]));
            s.getResolver().propagate();
            Assert.assertTrue(bs[i].isInstantiatedTo(1));
        }
        for (int i = n - 1; i > 0; i--) {
            e.worldPop();
            Assert.assertFalse(bs[i].isInstantiated());
            s.getResolver().propagate();
            Assert.assertTrue(bs[i].isInstantiatedTo(1));
        }
    }

}
