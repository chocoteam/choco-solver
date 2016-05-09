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
package org.chocosolver.solver.constraints.nary.allen;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.checker.DomainBuilder;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.SearchStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.constraints.nary.allen.AllenRelationMats.e;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 19/01/2016.
 */
public class AllenRelationMeTest {

    @Test(groups = "1s", timeOut=60000)
    public void testforbidden_region1() {
        Model model = new Model("AllenRelationMats");
        IntVar re = model.intVar("Rel", new int[]{e});
        IntVar oi = model.intVar("Oi", 0, 1, false);
        IntVar li = model.intVar("Li", 8, 10, false);
        IntVar oj = model.intVar("Oj", 0, 0, false);
        IntVar lj = model.intVar("Lj", 10, 10, false);
        AllenRelationMe ar = new AllenRelationMe(re, oi, li, oj, lj, Cause.Null);
        try {
            Assert.assertTrue(ar.filter());
        } catch (ContradictionException e) {
            Assert.fail();
        }
        Assert.assertTrue(oi.isInstantiatedTo(0));
        Assert.assertTrue(li.isInstantiatedTo(10));
        ar.check();
    }

    @Test(groups = "5m", timeOut=900000)
    public void testAll() {
        Random rnd = new Random();
        int[][] domains;
        for (int k = 0; k < 2_000_000; k++) {
            System.out.printf("Seed (%d)\n", k);
            rnd.setSeed(k);
            domains = DomainBuilder.buildFullDomains(4, 0, 15, rnd, rnd.nextDouble(), rnd.nextBoolean());
            Model m = new Model();
            IntVar[] vars = new IntVar[15];
            for(int i = 0; i < 3; i++){
                vars[(i * 5)] = m.intVar("o_i_" + i, domains[0]);
                vars[(i * 5) + 1] = m.intVar("l_i_" + i, domains[1]);
                vars[(i * 5) + 2] = m.intVar("o_j_" + i, domains[2]);
                vars[(i * 5) + 3] = m.intVar("l_j_" + i, domains[3]);
                vars[(i * 5) + 4] = m.intVar("r_" + i, 1, 13, false);
            }
            for (int i = 1; i < 14; i++) {
                System.out.printf("%d\n", i);
                m.getEnvironment().worldPush();
                try {
                    vars[4].instantiateTo(i, Cause.Null);
                    vars[9].instantiateTo(i, Cause.Null);
                    vars[14].instantiateTo(i, Cause.Null);
                } catch (ContradictionException e1) {
                    e1.printStackTrace();
                }
                AllenRelationMe me = new AllenRelationMe(vars[4], vars[0], vars[1], vars[2], vars[3], Cause.Null);
                AllenRelationMats mats = new AllenRelationMats(vars[9], vars[5], vars[6], vars[7], vars[8], Cause.Null);
                AllenRelationMe check = new AllenRelationMe(vars[14], vars[10], vars[11], vars[12], vars[13], Cause.Null);
                try {
                    me.filter();
                    Assert.assertTrue(me.check(), "wrong filtering on me"); // check all remaining values
                    {
                        // check all removed values
                        for (int j = 0; j < 4; j++) {
                            for (int k1 = 0; k1 < domains[j].length; k1++) {
                                if (!vars[j].contains(domains[j][k1])) {
                                    m.getEnvironment().worldPush();
                                    vars[j + 10].instantiateTo(domains[j][k1], Cause.Null);
                                    try {
                                        check.filter();
                                        Assert.fail("unexpected removed value :" + domains[j][k1]+" from "+vars[j + 10].getName());
                                    } catch (ContradictionException ignored) {
                                        // expected behavior
                                    }
                                    m.getEnvironment().worldPop();
                                }
                            }
                        }
                    }
                    // now check with mats
                    try {
                        mats.filter();
                        Assert.assertTrue(mats.check(), "wrong filtering on Mats");
                        for (int j = 0; j < 5; j++) {
                            Assert.assertEquals(vars[j].getDomainSize(), vars[j+5].getDomainSize(), vars[j]+" incorrect domain size (expected : "+vars[j+5]+")");
                            for(int v = vars[j].getLB(); v <= vars[j].getLB(); v = vars[j].nextValue(v)){
                                Assert.assertTrue(vars[j+5].contains(v), vars[j]+" differs from Mats");
                            }
                        }
                    }catch (ContradictionException cex){
                        Assert.fail("works with me not with Mats");
                    }
                } catch (ContradictionException cex) {
                    Assert.assertFalse(check.check());
                    try{
                        mats.filter();
                        Assert.fail("Failure expected");
                    }catch (ContradictionException ignored){}
                }
                m.getEnvironment().worldPop();
            }
        }
    }

    @DataProvider(name="localornot")
    public Object[][] provider(){
        return new Object[][]{{true}, {false}};
    }

    @Test(groups="1s", timeOut = 6000000, dataProvider = "localornot")
    public void testB1(boolean local){
        Model model = new Model();
        IntVar bs = model.intVar("bs", new int[]{0, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
                49, 50, 51, 52, 53, 54, 55, 56, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77,
                78, 79, 80, 81, 82, 83, 84, 85, 101, 102, 103, 104, 111, 112, 113, 114, 115, 116, 119, 122, 123, 127});
        IntVar ds = model.intVar("ds", 32,120, false);
        Constraint[] disjunctions = new Constraint[3];

        IntVar r1 = model.intVar("r1", new int[]{4,5,7,13});
        disjunctions[0] = new Constraint("Allen 1",
                new PropAllenGAC(r1, bs, ds,
                        model.intVar(27), model.intVar(19), false));

        IntVar r2 = model.intVar("r2", new int[]{4,5,7,13});
        disjunctions[1] = new Constraint("Allen 1",
                new PropAllenGAC(r2, bs, ds,
                        model.intVar(55), model.intVar(16), false));

        IntVar r3 = model.intVar("r3", new int[]{4,5,7,13});
        disjunctions[2] = new Constraint("Allen 1",
                new PropAllenGAC(r3, bs, ds,
                        model.intVar(114), model.intVar(2), false));

        model.addConstructiveDisjunction(local, disjunctions);
        model.getSolver().set(SearchStrategyFactory.inputOrderLBSearch(new IntVar[]{bs, ds}));
        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 4970);
    }
}