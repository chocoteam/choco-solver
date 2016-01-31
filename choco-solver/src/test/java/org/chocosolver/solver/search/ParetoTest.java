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
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by cprudhom on 18/02/15.
 * Project: choco.
 */
public class ParetoTest {

    //******************************************************************
    // CP VARIABLES
    //******************************************************************

    // --- CP Solver
    private Solver s;

    // --- Decision variables
    private IntVar[] occurrences;

    // --- Cumulated profit_1 of selected items
    private IntVar totalProfit_1;

    // --- Cumulated profit_2 of selected items
    private IntVar totalProfit_2;

    // --- Cumulated weight of selected items
    private IntVar totalWeight;

    //******************************************************************
    // DATA
    //******************************************************************

    // --- Capacity of the knapsack
    private final int capacity;

    // --- Maximal profit_1 of the knapsack
    private int maxProfit_1;

    // --- Maximal profit_2 of the knapsack
    private int maxProfit_2;

    // --- Number of items in each category
    private final int[] nbItems;

    // --- Weight of items in each category
    private final int[] weights;

    // --- Profit_1 of items in each category
    private final int[] profits_1;

    // --- Profit_2 of items in each category
    private final int[] profits_2;

    //******************************************************************
    // CONSTRUCTOR
    //******************************************************************

    public ParetoTest(final int capacity, final String... items) {
        this.capacity = capacity;
        this.nbItems = new int[items.length];
        this.weights = new int[items.length];
        this.profits_1 = new int[items.length];
        this.profits_2 = new int[items.length];

        this.maxProfit_1 = 0;
        this.maxProfit_2 = 0;
        for (int it = 0; it < items.length; it++) {
            String item = items[it];
            item = item.trim();
            final String[] itemData = item.split(";");
            this.nbItems[it] = Integer.parseInt(itemData[0]);
            this.weights[it] = Integer.parseInt(itemData[1]);
            this.profits_1[it] = Integer.parseInt(itemData[2]);
            this.profits_2[it] = Integer.parseInt(itemData[3]);
            this.maxProfit_1 += this.nbItems[it] * this.profits_1[it];
            this.maxProfit_2 += this.nbItems[it] * this.profits_2[it];
        }
    }

    //******************************************************************
    // METHODS
    //******************************************************************

    private void createSolver() {
        // --- Creates a solver
        s = new Solver(new EnvironmentCopying(), "Knapsack");
    }

    private void buildModel() {
        createVariables();
        postConstraints();
    }

    private void createVariables() {

        // --- Creates decision variables
        occurrences = new IntVar[nbItems.length];
        for (int i = 0; i < nbItems.length; i++) {
            occurrences[i] = VariableFactory.bounded("occurrences_" + i, 0, nbItems[i], s);
        }
        totalWeight = VariableFactory.bounded("totalWeight", 0, capacity, s);
        totalProfit_1 = VariableFactory.bounded("totalProfit_1", 0, maxProfit_1, s);
        totalProfit_2 = VariableFactory.bounded("totalProfit_2", 0, maxProfit_2, s);
    }

    private void postConstraints() {
        // --- Posts a knapsack constraint on profit_1
        s.post(IntConstraintFactory.knapsack(occurrences, totalWeight, totalProfit_1, weights, profits_1));

        // --- Posts a knapsack constraint on profit_2
        s.post(IntConstraintFactory.knapsack(occurrences, totalWeight, totalProfit_2, weights, profits_2));
    }

    static int bestProfit1 = 0;

    private void solve() {
        // --- Solves the problem

        s.plugMonitor((IMonitorSolution) () -> bestProfit1 = Math.max(bestProfit1, totalProfit_1.getValue()));
//        Chatterbox.showSolutions(s);
        s.findParetoFront(ResolutionPolicy.MAXIMIZE, totalProfit_1, totalProfit_2);
    }


    //******************************************************************
    // MAIN
    //******************************************************************

    @Test(groups = "1s", timeOut=1000)
    public static void main() {
        ParetoTest instance = new ParetoTest(30, "10;1;2;5", "5;3;7;4", "2;5;11;3");
        instance.createSolver();
        instance.buildModel();
        instance.solve();
        Assert.assertTrue(bestProfit1 > 60);

    }
}
