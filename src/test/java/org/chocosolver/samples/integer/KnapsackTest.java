/**
 * Copyright (c) 2016, chocoteam
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of samples nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples.integer;

import gnu.trove.list.array.TFloatArrayList;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.chocosolver.solver.ResolutionPolicy.MAXIMIZE;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19 juil. 2010
 */
public class KnapsackTest {
    private final static TFloatArrayList times = new TFloatArrayList();

    public Model modelIt(String data, int n) throws IOException {
        Knapsack pb = new Knapsack();
        pb.readArgs("-d", data, "-n", "" + n);
        pb.buildModel();
        for (IntVar v : pb.objects) {
            if (v == null) {
                throw new UnsupportedOperationException();
            }
        }
        return pb.getModel();
    }

    public void solveIt(Model s, boolean optimize) {
        if (optimize) {
            // BEWARE trick to find power variable
            IntVar power = null;
            for (int i = s.getNbVars() - 1; i >= 0; i--) {
                if (s.getVar(i).getName().equals("power")) {
                    if (power != null) {
                        throw new UnsupportedOperationException("The solver has more than one power variable");
                    }
                    power = (IntVar) s.getVar(i);
                }
            }
            if (power == null) {
                throw new UnsupportedOperationException("The solver has no power variable");
            }
            // end of trick
            s.setObjective(MAXIMIZE, power);
        }
        while(s.getSolver().solve());
        times.add(s.getSolver().getTimeCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testMain() throws IOException {
        KnapsackTest ks = new KnapsackTest();
        ks.solveIt(ks.modelIt("k10", 10), true);
        ks.solveIt(ks.modelIt("k20", 13), true);
    }

    @Test(groups="1s", timeOut=60000)
    public void testALL5() throws IOException {
        times.clear();
        KnapsackTest ks = new KnapsackTest();
        for (int i = 0; i < 1; i++) {
            Model s = ks.modelIt("k10", 3);
            ks.solveIt(s, true);
            Assert.assertEquals(s.getSolver().getBestSolutionValue().intValue(), 1078, "obj val");
        }
    }

	@Test(groups="1s", timeOut=60000)
    public void testALL10() throws IOException {
        times.clear();
        KnapsackTest ks = new KnapsackTest();
        for (int i = 0; i < 1; i++) {
            Model s = ks.modelIt("k10", 10);
            ks.solveIt(s, true);
            Assert.assertEquals(s.getSolver().getBestSolutionValue().intValue(), 1078, "obj val");
        }
    }

	@Test(groups="1s", timeOut=60000)
    public void testALL0() throws IOException {
        times.clear();
        KnapsackTest ks = new KnapsackTest();
        for (int i = 0; i < 1; i++) {
            Model s = ks.modelIt("k0", 10);
            ks.solveIt(s, true);
            Assert.assertEquals(s.getSolver().getBestSolutionValue().intValue(), 7546, "obj val");
        }
    }

	@Test(groups="1s", timeOut=60000)
    public void testOPT13() throws IOException {
        KnapsackTest ks = new KnapsackTest();
        Model s = ks.modelIt("k20", 13);
        ks.solveIt(s, true);
        Assert.assertEquals(s.getSolver().getBestSolutionValue().intValue(), 2657, "obj val");
    }

	@Test(groups="1s", timeOut=60000)
    public void testOPT14() throws IOException {
        KnapsackTest ks = new KnapsackTest();
        Model s = ks.modelIt("k20", 14);
        ks.solveIt(s, true);
        Assert.assertEquals(s.getSolver().getBestSolutionValue().intValue(), 2657, "obj val");
    }

	@Test(groups="1s", timeOut=60000)
    public void testOPT15() throws IOException {
        KnapsackTest ks = new KnapsackTest();
        Model s = ks.modelIt("k20", 15);
        ks.solveIt(s, true);
        Assert.assertEquals(s.getSolver().getBestSolutionValue().intValue(), 2657, "obj val");
    }

}
