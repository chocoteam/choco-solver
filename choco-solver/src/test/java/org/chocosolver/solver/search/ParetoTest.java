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
package org.chocosolver.solver.search;

import org.chocosolver.memory.copy.EnvironmentCopying;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static org.chocosolver.solver.ResolutionPolicy.MAXIMIZE;

/**
 * Created by cprudhom on 18/02/15.
 * Project: choco.
 */
public class ParetoTest {

    int bestProfit1 = 0;

    //******************************************************************
    // MAIN
    //******************************************************************

    @Test(groups="1s", timeOut=60000)
    public void testPareto() {
        runKnapsackPareto(30, "10;1;2;5", "5;3;7;4", "2;5;11;3");
        Assert.assertTrue(bestProfit1 > 60);
    }

    private void runKnapsackPareto(final int capacity, final String... items) {
        int[] nbItems = new int[items.length];
        int[] weights = new int[items.length];
        int[] profits_1 = new int[items.length];
        int[] profits_2 = new int[items.length];

        int maxProfit_1 = 0;
        int maxProfit_2 = 0;
        for (int it = 0; it < items.length; it++) {
            String item = items[it];
            item = item.trim();
            final String[] itemData = item.split(";");
            nbItems[it] = parseInt(itemData[0]);
            weights[it] = parseInt(itemData[1]);
            profits_1[it] = parseInt(itemData[2]);
            profits_2[it] = parseInt(itemData[3]);
            maxProfit_1 += nbItems[it] * profits_1[it];
            maxProfit_2 += nbItems[it] * profits_2[it];
        }

        Model s = new Model(new EnvironmentCopying(), "Knapsack");
        // --- Creates decision variables
        IntVar[] occurrences = new IntVar[nbItems.length];
        for (int i = 0; i < nbItems.length; i++) {
            occurrences[i] = s.intVar("occurrences_" + i, 0, nbItems[i], true);
        }
        IntVar totalWeight = s.intVar("totalWeight", 0, capacity, true);
        IntVar totalProfit_1 = s.intVar("totalProfit_1", 0, maxProfit_1, true);
        IntVar totalProfit_2 = s.intVar("totalProfit_2", 0, maxProfit_2, true);

        // --- Posts constraints
        s.knapsack(occurrences, totalWeight, totalProfit_1, weights, profits_1).post();
        s.knapsack(occurrences, totalWeight, totalProfit_2, weights, profits_2).post();
        // --- Monitor
        s.plugMonitor((IMonitorSolution) () -> bestProfit1 = max(bestProfit1, totalProfit_1.getValue()));
        // --- solve
        s.findParetoFront(MAXIMIZE, totalProfit_1, totalProfit_2);
    }
}
