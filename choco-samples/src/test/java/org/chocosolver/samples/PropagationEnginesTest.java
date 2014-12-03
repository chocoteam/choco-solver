/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package org.chocosolver.samples;

import org.chocosolver.samples.integer.*;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.testng.Assert;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/01/2014
 */
public class PropagationEnginesTest {


    private static PropagationEngineFactory[] engines = new PropagationEngineFactory[]{
            PropagationEngineFactory.PROPAGATORDRIVEN_7QD,
            PropagationEngineFactory.TWOBUCKETPROPAGATIONENGINE
    };


    private static AbstractProblem[] problems = new AbstractProblem[]{
            new Alpha(),
            new AllIntervalSeries(),
            //new BACP(),
            new BIBD(),
            //new BigSum(),
            new CarSequencing(),
            new CostasArrays(),
            //new Donald(),
            //new Eq20(),
            new GolombRuler(),
            //new Grocery(),
            //new Knapsack(),
            new Langford(),
            new LatinSquare(),
            new MagicSequence(),
            new MagicSeries(),
            //new MagicSquare(),
            //new MarioKart(),
            new Nonogram(),
            new OpenStacks(),
            new Ordering(),
            //new OrthoLatinSquare(),
            new Partition(),
            //new Photo(),
            //new RLFAP(),
            new SchurLemma(),
            //new SocialGolfer(),
            //new StableMarriage(),
            new WarehouseLocation()

    };

    public static void main(String[] args) {
        long[][] stats = new long[2][3];
        for (AbstractProblem problem : problems) {
            //System.out.printf("%s", problem.getClass().getName());
            for (int i = 0; i < 1; i++) {
                for (PropagationEngineFactory pe : engines) {
//                    System.out.printf(".");
                    problem.createSolver();
                    Solver solver = problem.getSolver();
                    problem.buildModel();
                    problem.configureSearch();
                    solver.set(pe.make(solver));
                    //SMF.toCSV(solver, problem.getClass().getCanonicalName() + ";" + pe.name(), "/Users/kyzrsoze/Sandbox/pren/pe.csv");
                    problem.solve();
                    switch (pe) {
                        case PROPAGATORDRIVEN_7QD:
                            stats[0][0] = problem.solver.getMeasures().getSolutionCount();
                            stats[0][1] = problem.solver.getMeasures().getNodeCount();
                            stats[0][2] = problem.solver.getMeasures().getFailCount();
                            break;
                        case TWOBUCKETPROPAGATIONENGINE:
                            stats[1][0] = problem.solver.getMeasures().getSolutionCount();
                            stats[1][1] = problem.solver.getMeasures().getNodeCount();
                            stats[1][2] = problem.solver.getMeasures().getFailCount();
                            break;
                    }
                }
                Assert.assertEquals(stats[0], stats[1], problem.getClass().getCanonicalName()+":"+Arrays.toString(stats[0])+"!="+Arrays.toString(stats[1]));
            }
            //System.out.printf("OK\n");
        }
    }
}
