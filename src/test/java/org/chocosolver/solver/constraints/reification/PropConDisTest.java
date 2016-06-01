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
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Random;

import static java.lang.System.out;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;
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

    @DataProvider(name="localornot")
    public Object[][] provider(){
        return new Object[][]{{true}, {false}};
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "localornot")
    public void testCD1(boolean local) throws ContradictionException {
        Model m = new Model();
        IntVar a = m.intVar("A", 0, 10, false);
        Constraint c1 = m.arithm(a, "=", 9);
        Constraint c2 = m.arithm(a, "=", 10);
        m.addConstructiveDisjunction(local, c1, c2);
        m.getConDisStore().getPropCondis().initialize();
        m.getSolver().propagate();
        Assert.assertEquals(a.getDomainSize(), 2);
        Assert.assertEquals(a.getLB(), 9);
        Assert.assertEquals(a.getUB(), 10);
        while(m.getSolver().solve());
        Assert.assertEquals(m.getSolver().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "localornot")
    public void testCD2(boolean local) throws ContradictionException {
        Model m = new Model();
        IntVar X = m.intVar("X", 0, 10, false);
        IntVar Y = m.intVar("Y", 0, 10, false);
        Constraint c1 = m.arithm(X, "-", Y, "<=", -9);
        Constraint c2 = m.arithm(Y, "-", X, "<=", -9);

        m.addConstructiveDisjunction(local, c1,c2);
        m.getConDisStore().getPropCondis().initialize();
        m.getSolver().propagate();
        Assert.assertEquals(X.getDomainSize(), 4);
        Assert.assertEquals(Y.getDomainSize(), 4);
        Assert.assertTrue(X.contains(0));
        Assert.assertTrue(X.contains(1));
        Assert.assertTrue(X.contains(9));
        Assert.assertTrue(X.contains(10));
        Assert.assertTrue(Y.contains(0));
        Assert.assertTrue(Y.contains(1));
        Assert.assertTrue(Y.contains(9));
        Assert.assertTrue(Y.contains(10));
        while(m.getSolver().solve());
        Assert.assertEquals(m.getSolver().getSolutionCount(), 6);
    }


    @Test(groups="5m", timeOut=300000, dataProvider = "localornot")
    public void test3(boolean local) {
        Random rnd = new Random();
        for (int n = 1; n < 20; n += 1) {
            out.printf("Size: %d\n", n);
            Model or = modelPb(n, n, rnd, false, true, local);
            Model cd = modelPb(n, n, rnd, true, true, local);
            or.getSolver().setSearch(inputOrderLBSearch(ArrayUtils.append((IntVar[]) or.getHook("decvars"),new IntVar[]{(IntVar) or.getObjective()})));
            cd.getSolver().setSearch(inputOrderLBSearch(ArrayUtils.append((IntVar[]) cd.getHook("decvars"),new IntVar[]{(IntVar) cd.getObjective()})));
            Solution sor = new Solution(or);
            Solution scd = new Solution(cd);
            while(or.getSolver().solve()){
                sor.record();
            }
            while(cd.getSolver().solve()){
                scd.record();
            }
            assertEquals(scd.getIntVal((IntVar) cd.getObjective()),
                    sor.getIntVal((IntVar) or.getObjective()));
            assertEquals(cd.getSolver().getSolutionCount(), or.getSolver().getSolutionCount(), "wrong nb of solutions");
            assertTrue(or.getSolver().getNodeCount() >= cd.getSolver().getNodeCount(), "wrong nb of nodes");
        }
    }

    @Test(groups="5m", timeOut=3000000, dataProvider = "localornot")
    public void test4(boolean local) {
        Random rnd = new Random();
        for (int n = 1; n < 4; n += 1) {
            System.out.printf("Size: %d\n", n);
            for (int seed = 0; seed < 5; seed += 1) {
                out.printf("Size: %d (%d)\n", n, seed);
                Model or = modelPb(n, seed, rnd, false, false, local);
                or.getSolver().setSearch(randomSearch((IntVar[]) or.getHook("decvars"), 0));
                while (or.getSolver().solve()) ;
                Model cd = modelPb(n, seed, rnd, true, false, local);
                cd.getSolver().setSearch(randomSearch((IntVar[]) cd.getHook("decvars"), 0));
                while (cd.getSolver().solve()) ;
                assertEquals(cd.getSolver().getSolutionCount(), or.getSolver().getSolutionCount(), "wrong nb of solutions");
                assertTrue(or.getSolver().getNodeCount() >= cd.getSolver().getNodeCount(), "wrong nb of nodes");
            }
        }
    }

    private Model modelPb(int size, long seed, Random rnd, boolean cd, boolean optimize, boolean local) {
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
            Constraint[] disjunction = new Constraint[os.length];
            for (int j = 0; j < os.length; j++) {
                disjunction[j] = model.and(
                        model.arithm(OS[i], ">", os[j]),
                        model.arithm(OS[i], "+", LS[i], "<", os[j] + ls[j])
                );
            }
            if (cd) {
                model.addConstructiveDisjunction(local, disjunction);
            } else {
                BoolVar[] bvars = new BoolVar[os.length];
                for(int j = 0 ; j < os.length; j++){
                    bvars[j] = disjunction[j].reify();
                }
                model.addClausesBoolOrArrayEqualTrue(bvars);
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