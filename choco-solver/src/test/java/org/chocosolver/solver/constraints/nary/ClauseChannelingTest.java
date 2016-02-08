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
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Resolver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.randomSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;

/**
 * Created by cprudhom on 14/01/15.
 * Project: choco.
 */
public class ClauseChannelingTest {

    @Test(groups="5m", timeOut=300000)
    public void test1E() {
        for (int i = 1; i < 200; i++) {
            for (int seed = 1; seed < 100; seed++) {
                Model model = new Model();
                IntVar iv = model.intVar("iv", 1, i, false);
                BoolVar[] eqs = model.boolVarArray("eq", i);
                BoolVar[] lqs = model.boolVarArray("lq", i);

                model.clausesIntChanneling(iv, eqs, lqs).post();

                Resolver r = model.getResolver();
                r.set(randomSearch(append(new IntVar[]{iv}, eqs, lqs), seed));
                while (model.solve()) ;
                assertEquals(r.getMeasures().getSolutionCount(), i);
            }
        }
    }

    @Test(groups="5m", timeOut=300000)
    public void test1B() {
        for (int i = 1; i < 100; i++) {
            for (int seed = 1; seed < 200; seed++) {
                Model model = new Model();
                IntVar iv = model.intVar("iv", 1, i, true);
                BoolVar[] eqs = model.boolVarArray("eq", i);
                BoolVar[] lqs = model.boolVarArray("lq", i);

                model.clausesIntChanneling(iv, eqs, lqs).post();

                Resolver r = model.getResolver();
                r.set(randomSearch(append(new IntVar[]{iv}, eqs, lqs), seed));
                while (model.solve()) ;
                assertEquals(r.getMeasures().getSolutionCount(), i);
            }
        }
    }

    @Test(groups="5m", timeOut=300000)
    public void test2E() {
        for (int i = 1; i < 100; i++) {
            for (int seed = 0; seed < 20; seed++) {
                Model sr = new Model();
                Model sc = new Model();
                {
                    IntVar iv = sr.intVar("iv", 1, i, false);
                    BoolVar[] eqs = sr.boolVarArray("eq", i);
                    BoolVar[] lqs = sr.boolVarArray("lq", i);

                    for (int j = 1; j <= i; j++) {
                        sr.arithm(iv, "=", j).reifyWith(eqs[j - 1]);
                    }
                    for (int j = 1; j <= i; j++) {
                        sr.arithm(iv, "<=", j).reifyWith(lqs[j - 1]);
                    }

                    Resolver r = sr.getResolver();
                    r.set(randomSearch(append(new IntVar[]{iv}, eqs, lqs), seed));
                    while (sr.solve()) ;
                }
                {
                    IntVar iv = sc.intVar("iv", 1, i, false);
                    BoolVar[] eqs = sc.boolVarArray("eq", i);
                    BoolVar[] lqs = sc.boolVarArray("lq", i);

                    sc.clausesIntChanneling(iv, eqs, lqs).post();

                    Resolver r = sc.getResolver();
                    r.set(randomSearch(append(new IntVar[]{iv}, eqs, lqs), seed));
                    while (sc.solve()) ;
                }
                Assert.assertEquals(sr.getResolver().getMeasures().getSolutionCount(), i);
                Assert.assertEquals(sc.getResolver().getMeasures().getSolutionCount(), i);
                Assert.assertEquals(sc.getResolver().getMeasures().getNodeCount(), sr.getResolver().getMeasures().getNodeCount());

            }
        }
    }

    @Test(groups="5m", timeOut=300000)
    public void test2B() {
        for (int i = 1; i < 100; i++) {
            for (int seed = 3; seed < 20; seed++) {
                Model sr = new Model();
                Model sc = new Model();
                {
                    IntVar iv = sr.intVar("iv", 1, i, true);
                    BoolVar[] eqs = sr.boolVarArray("eq", i);
                    BoolVar[] lqs = sr.boolVarArray("lq", i);

                    for (int j = 1; j <= i; j++) {
                        sr.arithm(iv, "=", j).reifyWith(eqs[j - 1]);
                    }
                    for (int j = 1; j <= i; j++) {
                        sr.arithm(iv, "<=", j).reifyWith(lqs[j - 1]);
                    }

                    Resolver r = sr.getResolver();
                    r.set(randomSearch(append(new IntVar[]{iv}, eqs, lqs), seed));
                    while (sr.solve()) ;
                }
                {
                    IntVar iv = sc.intVar("iv", 1, i, true);
                    BoolVar[] eqs = sc.boolVarArray("eq", i);
                    BoolVar[] lqs = sc.boolVarArray("lq", i);

                    sc.clausesIntChanneling(iv, eqs, lqs).post();

                    Resolver r = sc.getResolver();
                    r.set(randomSearch(append(new IntVar[]{iv}, eqs, lqs), seed));
                    while (sc.solve()) ;
                }
                Assert.assertEquals(sr.getResolver().getMeasures().getSolutionCount(), i);
                Assert.assertEquals(sc.getResolver().getMeasures().getSolutionCount(), i);
                Assert.assertEquals(sc.getResolver().getMeasures().getNodeCount(), sr.getResolver().getMeasures().getNodeCount());

            }
        }
    }

}
