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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import java.util.Random;

import static java.lang.System.out;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.randomSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 26/01/2016.
 */
public class PropConDisTest {

    @Test(groups="1s", timeOut=60000)
    public void testCD1() throws ContradictionException {
        Model s = new Model();
        IntVar a = s.intVar("A", 0, 10, false);
        BoolVar b1 = s.arithm(a, "=", 9).reify();
        BoolVar b2 = s.arithm(a, "=", 10).reify();
        s.addConstructiveDisjunction(b1, b2);
        s.getSolver().propagate();
        assertEquals(a.getDomainSize(), 2);
        assertEquals(a.getLB(), 9);
        assertEquals(a.getUB(), 10);
        while (s.solve()) ;
        assertEquals(s.getSolver().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testCD2() throws ContradictionException {
        Model s = new Model();
        IntVar X = s.intVar("X", 0, 10, false);
        IntVar Y = s.intVar("Y", 0, 10, false);
        Constraint c1 = s.arithm(X, "-", Y, "<=", -9);
        Constraint c2 = s.arithm(Y, "-", X, "<=", -9);

        s.addConstructiveDisjunction(c1.reify(), c2.reify());
        s.getSolver().propagate();
        assertEquals(X.getDomainSize(), 4);
        assertEquals(Y.getDomainSize(), 4);
        assertTrue(X.contains(0));
        assertTrue(X.contains(1));
        assertTrue(X.contains(9));
        assertTrue(X.contains(10));
        assertTrue(Y.contains(0));
        assertTrue(Y.contains(1));
        assertTrue(Y.contains(9));
        assertTrue(Y.contains(10));
        while (s.solve()) ;
        assertEquals(s.getSolver().getSolutionCount(), 6);
    }


    @Test(groups="5m", timeOut=300000)
    public void test3() {
        Random rnd = new Random();
        for (int n = 1; n < 20; n += 1) {
            out.printf("Size: %d\n", n);
            Model or = modelPb(n, n, rnd, false, true);
            Model cd = modelPb(n, n, rnd, true, true);
            or.getSolver().set(inputOrderLBSearch((IntVar[]) or.getHook("decvars")));
            cd.getSolver().set(inputOrderLBSearch((IntVar[]) cd.getHook("decvars")));
            Solution sor = new Solution(or);
            Solution scd = new Solution(cd);
            while(or.solve()){
                sor.record();
            }
            while(cd.solve()){
                scd.record();
            }
            assertEquals(scd.getIntVal((IntVar) cd.getObjective()),
                    sor.getIntVal((IntVar) or.getObjective()));
            assertEquals(cd.getSolver().getSolutionCount(), or.getSolver().getSolutionCount(), "wrong nb of solutions");
            assertTrue(or.getSolver().getNodeCount() >= cd.getSolver().getNodeCount(), "wrong nb of nodes");
        }
    }

    @Test(groups="5m", timeOut=300000)
    public void test4() {
        Random rnd = new Random();
        for (int n = 1; n < 4; n += 1) {
            System.out.printf("Size: %d\n", n);
            for (int seed = 0; seed < 5; seed += 1) {
                out.printf("Size: %d (%d)\n", n, seed);
                Model or = modelPb(n, seed, rnd, false, false);
                or.getSolver().set(randomSearch((IntVar[]) or.getHook("decvars"), 0));
                while (or.solve()) ;
                Model cd = modelPb(n, seed, rnd, true, false);
                cd.getSolver().set(randomSearch((IntVar[]) cd.getHook("decvars"), 0));
                while (cd.solve()) ;
                assertEquals(cd.getSolver().getSolutionCount(), or.getSolver().getSolutionCount(), "wrong nb of solutions");
                assertTrue(or.getSolver().getNodeCount() >= cd.getSolver().getNodeCount(), "wrong nb of nodes");
            }
        }
    }

    private Model modelPb(int size, long seed, Random rnd, boolean cd, boolean optimize) {
        rnd.setSeed(seed);
        int[] os = new int[size * 2];
        int[] ls = new int[size * 2];
        os[0] = rnd.nextInt(5);
        ls[0] = 3 + rnd.nextInt(7);
        for (int j = 1; j < os.length; j++) {
            os[j] = 1 + os[j - 1] + ls[j - 1] + rnd.nextInt(5);
            ls[j] = 3 + rnd.nextInt(4);
        }
        Model model = new Model();
        IntVar[] OS = model.intVarArray("O", size, 0, os[2 * size - 1] + ls[2 * size - 1], false);
        IntVar[] LS = model.intVarArray("L", size, 1, 10, false);
        for (int i = 0; i < size - 1; i++) {
            model.sum(new IntVar[]{OS[i], LS[i]}, "<", OS[i + 1]).post();
        }
        for (int i = 0; i < size; i++) {
            BoolVar[] disjunction = new BoolVar[os.length];
            for (int j = 0; j < os.length; j++) {
                disjunction[j] = model.and(
                        model.arithm(OS[i], ">", os[j]),
                        model.arithm(OS[i], "+", LS[i], "<", os[j] + ls[j])
                ).reify();
            }
            if (cd) {
                model.addConstructiveDisjunction(disjunction);
            } else {
                model.addClausesBoolOrArrayEqualTrue(disjunction);
            }
        }
        IntVar horizon = model.intVar("H", 0, os[2 * size - 1] + ls[2 * size - 1], true);
        model.sum(new IntVar[]{OS[size - 1], LS[size - 1]}, "=", horizon).post();
        if (optimize) {
            model.setObjective(ResolutionPolicy.MINIMIZE, horizon);
        }
        model.addHook("decvars", append(OS, LS));
//        showShortStatistics(model);
        return model;
    }
}