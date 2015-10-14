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
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.loop.lns.LargeNeighborhoodSearch;
import org.chocosolver.solver.search.loop.lns.neighbors.*;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/06/13
 */
public class LNSTest {

    Solver solver;
    IntVar[] vars;

    private void knapsack20(final int lns) {
        int[] capacities = {99, 1101};
        int[] volumes = {54, 12, 47, 33, 30, 65, 56, 57, 91, 88, 77, 99, 29, 23, 39, 86, 12, 85, 22, 64};
        int[] energies = {38, 57, 69, 90, 79, 89, 28, 70, 38, 71, 46, 41, 49, 43, 36, 68, 92, 33, 84, 90};

        solver = new Solver();
        int nos = 20;
        // occurrence of each item
        IntVar[] objects = new IntVar[nos];
        for (int i = 0; i < nos; i++) {
            objects[i] = VariableFactory.bounded("o_" + (i + 1), 0, (int) Math.ceil(capacities[1] / volumes[i]), solver);
        }
        final IntVar power = VariableFactory.bounded("power", 0, 99999, solver);
        IntVar scalar = VariableFactory.bounded("weight", capacities[0], capacities[1], solver);
        solver.post(IntConstraintFactory.scalar(objects, volumes, scalar));
        solver.post(IntConstraintFactory.scalar(objects, energies, power));
        solver.post(IntConstraintFactory.knapsack(objects, scalar, power, volumes, energies));
        solver.set(IntStrategyFactory.lexico_LB(objects));
//        SearchMonitorFactory.log(solver, true, false);
        switch (lns) {
            case 0:
                break;
            case 1:
                solver.getSearchLoop().plugSearchMonitor(new LargeNeighborhoodSearch(solver,
                        new RandomNeighborhood(solver, objects, 200, 123456L), true));
                SearchMonitorFactory.limitThreadTime(solver, 10000);
                break;
            case 2:
                solver.getSearchLoop().plugSearchMonitor(new LargeNeighborhoodSearch(solver,
                        new PropagationGuidedNeighborhood(solver, objects, 123456L, 100, 10), true));
                SearchMonitorFactory.limitThreadTime(solver, 10000);
                break;
            case 3:
                solver.getSearchLoop().plugSearchMonitor(new LargeNeighborhoodSearch(solver,
                        new SequenceNeighborhood(
                                new PropagationGuidedNeighborhood(solver, objects, 123456L, 100, 10),
                                new ReversePropagationGuidedNeighborhood(solver, objects, 123456L, 100, 10)
                        ), true));
                SearchMonitorFactory.limitThreadTime(solver, 10000);
                break;
            case 4:
                solver.getSearchLoop().plugSearchMonitor(new LargeNeighborhoodSearch(solver,
                        new SequenceNeighborhood(
                                new PropagationGuidedNeighborhood(solver, objects, 123456L, 100, 10),
                                new ReversePropagationGuidedNeighborhood(solver, objects, 123456L, 100, 10),
                                new RandomNeighborhood(solver, objects, 200, 123456L)
                        ), true));
                SearchMonitorFactory.limitThreadTime(solver, 10000);
                break;
            case 5:
                solver.getSearchLoop().plugSearchMonitor(new LargeNeighborhoodSearch(solver,
                        new ExplainingCut(solver, 200, 123456L), true));
                SearchMonitorFactory.limitThreadTime(solver, 10000);
                break;
            case 6:
                solver.getSearchLoop().plugSearchMonitor(new LargeNeighborhoodSearch(solver,
                        new ExplainingObjective(solver, 200, 123456L), true));
                SearchMonitorFactory.limitThreadTime(solver, 10000);
                break;
            case 7:
                SequenceNeighborhood ngb = new SequenceNeighborhood(
                        new ExplainingObjective(solver, 200, 123456L),
                        new ExplainingCut(solver, 200, 123456L),
                        new RandomNeighborhood4Explanation(solver, objects, 200, 123456L));
                solver.getSearchLoop().plugSearchMonitor(new LargeNeighborhoodSearch(solver, ngb, true));
                SearchMonitorFactory.limitThreadTime(solver, 10000);
                break;
        }
//        Chatterbox.showDecisions(solver, ()->""+solver.getEnvironment().getWorldIndex());
        solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, power);
        Chatterbox.printSolutions(solver);
    }


    @Test(groups = "1m")
    public void
    test1() {
        // opt: 8372
        knapsack20(0);
        knapsack20(1);
        knapsack20(2);
        knapsack20(3);
        knapsack20(4);
        knapsack20(5);
        knapsack20(6);
        knapsack20(7);
    }


}
