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
import gnu.trove.list.array.TFloatArrayList;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.nary.Sum;
import solver.search.strategy.enumerations.sorters.AbstractSorter;
import solver.search.strategy.enumerations.sorters.Seq;
import solver.search.strategy.enumerations.validators.ValidatorFactory;
import solver.search.strategy.strategy.StrategyVarValAssign;
import solver.variables.IntVar;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19 juil. 2010
 */
public class KnapsackTest {
    private final static TFloatArrayList times = new TFloatArrayList();

    //public IntVar power; BEWARE removed static field for parallel solving


    public Solver modelIt(String data, int n) throws IOException {
        Knapsack pb = new Knapsack();
        pb.readArgs("-d", data, "-n", "" + n);
        pb.createSolver();
        pb.buildModel();
//		pb.configureSearch();
        AbstractSorter<IntVar> s1 = pb.c_energy.getComparator(Sum.VAR_DECRCOEFFS);
        AbstractSorter<IntVar> s2 = pb.c_size.getComparator(Sum.VAR_DOMOVERCOEFFS);

        AbstractSorter<IntVar> seq = new Seq<IntVar>(s1, s2);

        pb.solver.set(StrategyVarValAssign.dyn(pb.objects,
                seq,
                ValidatorFactory.instanciated,
                pb.solver.getEnvironment()));
//        power = pb.power;
        return pb.getSolver();
    }

    public void solveIt(Solver s, boolean optimize) {
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
            s.findOptimalSolution(ResolutionPolicy.MAXIMIZE, power);
        } else {
            s.findAllSolutions();
        }
        times.add(s.getMeasures().getTimeCount());
    }

    @Test(groups = "10m")
    public void testMain() throws IOException {
        KnapsackTest ks = new KnapsackTest();
        ks.solveIt(ks.modelIt("k10", 10), true);
        ks.solveIt(ks.modelIt("k20", 13), true);
    }

    @Test(groups = {"1m"})
    public void testALL5() throws IOException {
        times.clear();
        KnapsackTest ks = new KnapsackTest();
        for (int i = 0; i < 1; i++) {
            Solver s = ks.modelIt("k10", 3);
            ks.solveIt(s, true);
            Assert.assertEquals(s.getMeasures().getObjectiveValue(), 1078, "obj val");
            Assert.assertEquals(s.getMeasures().getSolutionCount(), 3, "nb sol");
            Assert.assertEquals(s.getMeasures().getNodeCount(), 7, "nb nod");
        }
    }

    @Test(groups = {"1m"})
    public void testALL10() throws IOException {
        times.clear();
        KnapsackTest ks = new KnapsackTest();
        for (int i = 0; i < 1; i++) {
            Solver s = ks.modelIt("k10", 10);
            ks.solveIt(s, true);
            Assert.assertEquals(s.getMeasures().getObjectiveValue(), 1078, "obj val");
            Assert.assertEquals(s.getMeasures().getSolutionCount(), 144, "nb sol");
            Assert.assertEquals(s.getMeasures().getNodeCount(), 470, "nb nod");
        }
    }

    @Test(groups = {"10m"})
    public void testOPT13() throws IOException {
        KnapsackTest ks = new KnapsackTest();
        Solver s = ks.modelIt("k20", 13);
        ks.solveIt(s, true);
        Assert.assertEquals(s.getMeasures().getObjectiveValue(), 2657, "obj val");
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 214, "nb sol");
        Assert.assertEquals(s.getMeasures().getNodeCount(), 210236, "nb nod");
    }

    @Test(groups = {"10m"})
    public void testOPT14() throws IOException {
        KnapsackTest ks = new KnapsackTest();
        Solver s = ks.modelIt("k20", 14);
        ks.solveIt(s, true);
        Assert.assertEquals(s.getMeasures().getObjectiveValue(), 2657, "obj val");
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 305, "nb sol");
        Assert.assertEquals(s.getMeasures().getNodeCount(), 379396, "nb nod");
    }

    @Test(groups = {"10m"})
    public void testOPT15() throws IOException {
        KnapsackTest ks = new KnapsackTest();
        Solver s = ks.modelIt("k20", 15);
//        SearchMonitorFactory.log(s, false, false);
        ks.solveIt(s, true);
        Assert.assertEquals(s.getMeasures().getObjectiveValue(), 2657, "obj val");
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 297, "nb sol");
        Assert.assertEquals(s.getMeasures().getNodeCount(), 1153919, "nb nod");
    }

}
