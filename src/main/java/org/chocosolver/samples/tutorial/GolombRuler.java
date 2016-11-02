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
package org.chocosolver.samples.tutorial;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;

/**
 * Golomb ruler problem
 * <p>
 *
 * @author Charles Prud'homme
 * @since 22/09/2016.
 */
public class GolombRuler {

    public void modelAndSolve() {
        int m = 10;
        // A new model instance
        Model model = new Model("Golomb ruler");

        // VARIABLES
        // set of marks that should be put on the ruler
        IntVar[] ticks = ticks = model.intVarArray("a", m, 0, 9999, true);
        // set of distances between two distinct marks
        IntVar[] diffs = model.intVarArray("d", (m * (m - 1)) / 2, 0, 9999, true);

        // CONSTRAINTS
        // the first mark is set to 0
        model.arithm(ticks[0], "=", 0).post();

        for (int i = 0, k = 0; i < m - 1; i++) {
            // // the mark variables are ordered
            model.arithm(ticks[i + 1], ">", ticks[i]).post();
            for (int j = i + 1; j < m; j++, k++) {
                // declare the distance constraint between two distinct marks
                model.scalar(new IntVar[]{ticks[j], ticks[i]}, new int[]{1, -1}, "=", diffs[k]).post();
                // redundant constraints on bounds of diffs[k]
                model.arithm(diffs[k], ">=", (j - i) * (j - i + 1) / 2).post();
                model.arithm(diffs[k], "<=", ticks[m - 1], "-", ((m - 1 - j + i) * (m - j + i)) / 2).post();
            }
        }
        // all distances must be distinct
        model.allDifferent(diffs, "BC").post();
        //symmetry-breaking constraints
        model.arithm(diffs[0], "<", diffs[diffs.length - 1]).post();

        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(ticks));
        // show resolution statistics
        solver.showShortStatistics();
        // Find a solution that minimizes the last mark
        solver.findOptimalSolution(ticks[m - 1], false);
    }


    public static void main(String[] args) {
        new GolombRuler().modelAndSolve();
    }
}
