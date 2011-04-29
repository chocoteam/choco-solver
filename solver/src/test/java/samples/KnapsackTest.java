/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

package samples;

import choco.kernel.ResolutionPolicy;
import gnu.trove.TLongArrayList;
import org.testng.Assert;
import org.testng.annotations.Test;
import samples.knapsack.Knapsack;
import solver.Solver;
import solver.variables.IntVar;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19 juil. 2010
 */
public class KnapsackTest {
    static TLongArrayList times = new TLongArrayList();

    public IntVar power;


    public Solver modelIt(String file, int n) throws IOException {
        Knapsack pb = new Knapsack();
        pb.readArgs("-f", this.getClass().getResource(file).getFile(), "-n", Integer.toString(n));
        pb.buildModel();
        pb.configureSolver();
        power = pb.power;
        return pb.getSolver();
    }

    public void solveIt(Solver s, boolean optimize) {
        if (optimize) {
            s.findOptimalSolution(ResolutionPolicy.MAXIMIZE, power);
        } else {
            s.findAllSolutions();
        }
        times.add(s.getMeasures().getTimeCount());
    }

    @Test(groups = "10m")
    public void testMain() throws IOException {
        KnapsackTest ks = new KnapsackTest();
        ks.solveIt(ks.modelIt("../files/knapsack/knapsack.10-1.txt", 10), true);
        ks.solveIt(ks.modelIt("../files/knapsack/knapsack.20-1.txt", 13), true);
    }

    @Test(groups = {"1m"})
    public void testALL5() throws IOException {
        times.clear();
        KnapsackTest ks = new KnapsackTest();
        for (int i = 0; i < 1; i++) {
            Solver s = ks.modelIt("../files/knapsack/knapsack.10-1.txt", 3);
            ks.solveIt(s, true);
            Assert.assertEquals(s.getMeasures().getObjectiveValue(), 990, "obj val");
            Assert.assertEquals(s.getMeasures().getSolutionCount(), 1, "nb sol");
            Assert.assertEquals(s.getMeasures().getNodeCount(), 3, "nb nod");
        }
    }

    @Test(groups = {"1m"})
    public void testALL10() throws IOException {
        times.clear();
        KnapsackTest ks = new KnapsackTest();
        for (int i = 0; i < 1; i++) {
            Solver s = ks.modelIt("../files/knapsack/knapsack.10-1.txt", 10);
            ks.solveIt(s, true);
            Assert.assertEquals(s.getMeasures().getObjectiveValue(), 1845, "obj val");
            Assert.assertEquals(s.getMeasures().getSolutionCount(), 60, "nb sol");
            Assert.assertEquals(s.getMeasures().getNodeCount(), 6007, "nb nod");
        }
    }

    @Test(groups = {"10m"})
    public void testOPT13() throws IOException {
        KnapsackTest ks = new KnapsackTest();
        Solver s = ks.modelIt("../files/knapsack/knapsack.13-1.txt", 13);
        ks.solveIt(s, true);
        Assert.assertEquals(s.getMeasures().getObjectiveValue(), 5187, "obj val");
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 313, "nb sol");
        Assert.assertEquals(s.getMeasures().getNodeCount(), 648770, "nb nod");
    }

    @Test(groups = {"10m"})
    public void testOPT20() throws IOException {
        KnapsackTest ks = new KnapsackTest();
        Solver s = ks.modelIt("../files/knapsack/knapsack.20-1.txt", 14);
        ks.solveIt(s, true);
        Assert.assertEquals(s.getMeasures().getObjectiveValue(), 5187, "obj val");
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 345, "nb sol");
        Assert.assertEquals(s.getMeasures().getNodeCount(), 2943186, "nb nod");
    }

}
