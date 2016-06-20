/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/2016
 */
public class ConflictOrderingSearchTest {

    Model model;
    IntVar[] mvars;
    ConflictOrderingSearch<IntVar> cos;

    @BeforeMethod(alwaysRun = true)
    public void before() {
        model = new Model();
        mvars = model.intVarArray(10, 0, 5);
        cos = new ConflictOrderingSearch(model, Search.inputOrderLBSearch(mvars));
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testStampIt1() throws Exception {
        cos.stampIt(mvars[0]);
        Assert.assertEquals(cos.vars.size(), 1);
        Assert.assertEquals(cos.vars.get(0), mvars[0]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), -1);
        Assert.assertEquals(cos.pcft, 0);
        cos.stampIt(mvars[1]);
        Assert.assertEquals(cos.vars.size(), 2);
        Assert.assertEquals(cos.vars.get(1), mvars[1]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), 1);
        Assert.assertEquals(cos.prev.get(1), 0);
        Assert.assertEquals(cos.next.get(1), -1);
        Assert.assertEquals(cos.pcft, 1);
        cos.stampIt(mvars[2]);
        Assert.assertEquals(cos.vars.size(), 3);
        Assert.assertEquals(cos.vars.get(2), mvars[2]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), 1);
        Assert.assertEquals(cos.prev.get(1), 0);
        Assert.assertEquals(cos.next.get(1), 2);
        Assert.assertEquals(cos.prev.get(2), 1);
        Assert.assertEquals(cos.next.get(2), -1);
        Assert.assertEquals(cos.pcft, 2);
        cos.stampIt(mvars[3]);
        Assert.assertEquals(cos.vars.size(), 4);
        Assert.assertEquals(cos.vars.get(3), mvars[3]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), 1);
        Assert.assertEquals(cos.prev.get(1), 0);
        Assert.assertEquals(cos.next.get(1), 2);
        Assert.assertEquals(cos.prev.get(2), 1);
        Assert.assertEquals(cos.next.get(2), 3);
        Assert.assertEquals(cos.prev.get(3), 2);
        Assert.assertEquals(cos.next.get(3), -1);
        Assert.assertEquals(cos.pcft, 3);
        cos.stampIt(mvars[0]);
        Assert.assertEquals(cos.vars.size(), 4);
        Assert.assertEquals(cos.prev.get(1), -1);
        Assert.assertEquals(cos.next.get(1), 2);
        Assert.assertEquals(cos.prev.get(2), 1);
        Assert.assertEquals(cos.next.get(2), 3);
        Assert.assertEquals(cos.prev.get(3), 2);
        Assert.assertEquals(cos.next.get(3), 0);
        Assert.assertEquals(cos.prev.get(0), 3);
        Assert.assertEquals(cos.next.get(0), -1);
        Assert.assertEquals(cos.pcft, 0);
        cos.stampIt(mvars[3]);
        Assert.assertEquals(cos.vars.size(), 4);
        Assert.assertEquals(cos.prev.get(1), -1);
        Assert.assertEquals(cos.next.get(1), 2);
        Assert.assertEquals(cos.prev.get(2), 1);
        Assert.assertEquals(cos.next.get(2), 0);
        Assert.assertEquals(cos.prev.get(0), 2);
        Assert.assertEquals(cos.next.get(0), 3);
        Assert.assertEquals(cos.prev.get(3), 0);
        Assert.assertEquals(cos.next.get(3), -1);
        Assert.assertEquals(cos.pcft, 3);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testStampIt2() throws Exception {
        cos.stampIt(mvars[0]);
        Assert.assertEquals(cos.vars.size(), 1);
        Assert.assertEquals(cos.vars.get(0), mvars[0]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), -1);
        Assert.assertEquals(cos.pcft, 0);
        cos.stampIt(mvars[0]);
        Assert.assertEquals(cos.vars.size(), 1);
        Assert.assertEquals(cos.vars.get(0), mvars[0]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), -1);
        Assert.assertEquals(cos.pcft, 0);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testStampIt3() throws Exception {
        for(int n = 3; n < 20; n++) {
            IntVar[] cvars = model.intVarArray(n, 1, 1);
            Random rnd = new Random(0);
            for (int i = 0; i < 200; i++) {
                for (int j = 0; j < (n * 3 / 2) + 1; j++) {
                    int x = rnd.nextInt(n);
                    cos.stampIt(cvars[x]);
                    Assert.assertTrue(cos.check());
                    Assert.assertNull(cos.firstNotInst());
                }
            }
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testfirstNotInst() throws ContradictionException {
        cos.stampIt(mvars[0]);
        cos.stampIt(mvars[1]);
        cos.stampIt(mvars[2]);
        cos.stampIt(mvars[3]);
        cos.stampIt(mvars[4]);
        cos.stampIt(mvars[5]);
        Assert.assertEquals(cos.firstNotInst(), mvars[5]);
        mvars[5].instantiateTo(0, Cause.Null);
        Assert.assertEquals(cos.firstNotInst(), mvars[4]);
        mvars[4].instantiateTo(0, Cause.Null);
        mvars[3].instantiateTo(0, Cause.Null);
        Assert.assertEquals(cos.firstNotInst(), mvars[2]);
        mvars[2].instantiateTo(0, Cause.Null);
        Assert.assertEquals(cos.firstNotInst(), mvars[1]);
        mvars[1].instantiateTo(0, Cause.Null);
        mvars[0].instantiateTo(0, Cause.Null);
        Assert.assertNull(cos.firstNotInst());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testM1(){
        Model model = ProblemMaker.makeGolombRuler(6);
        model.getSolver().setSearch(Search.conflictOrderingSearch(
                Search.domOverWDegSearch(model.retrieveIntVars(
                        true
                ))
        ));
        model.getSolver().findAllSolutions();
    }

}