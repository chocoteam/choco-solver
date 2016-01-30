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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by cprudhom on 14/01/15.
 * Project: choco.
 */
public class ClauseChannelingTest {

    @Test(groups = "1m", timeOut=60000)
    public void test1E() {
        for (int i = 1; i < 200; i++) {
            for (int seed = 1; seed < 200; seed++) {
                Solver solver = new Solver();
                IntVar iv = VF.enumerated("iv", 1, i, solver);
                BoolVar[] eqs = VF.boolArray("eq", i, solver);
                BoolVar[] lqs = VF.boolArray("lq", i, solver);

                solver.post(ICF.clause_channeling(iv, eqs, lqs));

                solver.set(ISF.random_value(ArrayUtils.append(new IntVar[]{iv}, eqs, lqs), seed));
                solver.findAllSolutions();
                Assert.assertEquals(solver.getMeasures().getSolutionCount(), i);
            }
        }
    }

    @Test(groups = "1m", timeOut=60000)
    public void test1B() {
        for (int i = 1; i < 200; i++) {
            for (int seed = 1; seed < 200; seed++) {
                Solver solver = new Solver();
                IntVar iv = VF.bounded("iv", 1, i, solver);
                BoolVar[] eqs = VF.boolArray("eq", i, solver);
                BoolVar[] lqs = VF.boolArray("lq", i, solver);

                solver.post(ICF.clause_channeling(iv, eqs, lqs));

                solver.set(ISF.random_bound(ArrayUtils.append(new IntVar[]{iv}, eqs, lqs), seed));
                solver.findAllSolutions();
                Assert.assertEquals(solver.getMeasures().getSolutionCount(), i);
            }
        }
    }

    @Test(groups = "1m", timeOut=60000)
    public void test2E() {
        for (int i = 1; i < 200; i++) {
            for (int seed = 0; seed < 20; seed++) {
                Solver sr = new Solver();
                Solver sc = new Solver();
                {
                    IntVar iv = VF.enumerated("iv", 1, i, sr);
                    BoolVar[] eqs = VF.boolArray("eq", i, sr);
                    BoolVar[] lqs = VF.boolArray("lq", i, sr);

                    for (int j = 1; j <= i; j++) {
                        ICF.arithm(iv, "=", j).reifyWith(eqs[j - 1]);
                    }
                    for (int j = 1; j <= i; j++) {
                        ICF.arithm(iv, "<=", j).reifyWith(lqs[j - 1]);
                    }

                    sr.set(ISF.random_value(ArrayUtils.append(new IntVar[]{iv}, eqs, lqs), seed));
                    sr.findAllSolutions();
                }
                {
                    IntVar iv = VF.enumerated("iv", 1, i, sc);
                    BoolVar[] eqs = VF.boolArray("eq", i, sc);
                    BoolVar[] lqs = VF.boolArray("lq", i, sc);

                    sc.post(ICF.clause_channeling(iv, eqs, lqs));

                    sc.set(ISF.random_value(ArrayUtils.append(new IntVar[]{iv}, eqs, lqs), seed));
                    sc.findAllSolutions();
                }
                Assert.assertEquals(sr.getMeasures().getSolutionCount(), i);
                Assert.assertEquals(sc.getMeasures().getSolutionCount(), i);
                Assert.assertEquals(sc.getMeasures().getNodeCount(), sr.getMeasures().getNodeCount());

            }
        }
    }

    @Test(groups = "1m", timeOut=60000)
    public void test2B() {
        for (int i = 1; i < 200; i++) {
            for (int seed = 3; seed < 20; seed++) {
                Solver sr = new Solver();
                Solver sc = new Solver();
                {
                    IntVar iv = VF.bounded("iv", 1, i, sr);
                    BoolVar[] eqs = VF.boolArray("eq", i, sr);
                    BoolVar[] lqs = VF.boolArray("lq", i, sr);

                    for (int j = 1; j <= i; j++) {
                        ICF.arithm(iv, "=", j).reifyWith(eqs[j - 1]);
                    }
                    for (int j = 1; j <= i; j++) {
                        ICF.arithm(iv, "<=", j).reifyWith(lqs[j - 1]);
                    }

                    sr.set(ISF.random_bound(ArrayUtils.append(new IntVar[]{iv}, eqs, lqs), seed));
                    sr.findAllSolutions();
                }
                {
                    IntVar iv = VF.bounded("iv", 1, i, sc);
                    BoolVar[] eqs = VF.boolArray("eq", i, sc);
                    BoolVar[] lqs = VF.boolArray("lq", i, sc);

                    sc.post(ICF.clause_channeling(iv, eqs, lqs));

                    sc.set(ISF.random_bound(ArrayUtils.append(new IntVar[]{iv}, eqs, lqs), seed));
                    sc.findAllSolutions();
                }
                Assert.assertEquals(sr.getMeasures().getSolutionCount(), i);
                Assert.assertEquals(sc.getMeasures().getSolutionCount(), i);
                Assert.assertEquals(sc.getMeasures().getNodeCount(), sr.getMeasures().getNodeCount());

            }
        }
    }

}
