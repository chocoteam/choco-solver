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

/**
 *
 * Magic sequence in Choco3.
 *
 * http://www.dcs.st-and.ac.uk/~ianm/CSPLib/prob/prob019/spec.html
 * """
 * A magic sequence of length n is a sequence of integers x0 . . xn-1 between
 * 0 and n-1, such that for all i in 0 to n-1, the number i occurs exactly xi
 * times in the sequence. For instance, 6,2,1,0,0,0,1,0,0,0 is a magic sequence
 * since 0 occurs 6 times in it, 1 occurs twice, ...
 * """
 *
 * Choco3 model by Hakan Kjellerstrand (hakank@gmail.com)
 * http://www.hakank.org/choco3/
 *
 */

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import org.kohsuke.args4j.Option;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;

public class MagicSequence extends AbstractProblem {

    @Option(name = "-n", usage = "Size of problem (default 10).", required = false)
    int n = 10;

    IntVar[] x;

    @Override
    public void buildModel() {
        model = new Model();

        int[] values = ArrayUtils.oneToN(n-1);

        x = model.intVarArray("x", n, 0, n - 1, false);

        boolean closed = true; // restricts domains of VARS to VALUES if set to true
        model.globalCardinality(x, values, x, closed).post();

        // Redundant constraint
        model.sum(x, "=", n).post();

    }

    @Override
    public void configureSearch() {
        model.getSolver().set(inputOrderLBSearch(x));
    }

    @Override
    public void solve() {
        model.getSolver().solve();

        if (model.getSolver().isFeasible() == ESat.TRUE) {
            int num_solutions = 0;
            do {
                for (int i = 0; i < n; i++) {
                    System.out.print(x[i].getValue() + " ");
                }
                System.out.println();
                num_solutions++;
            } while (model.getSolver().solve() == Boolean.TRUE);
            System.out.println("It was " + num_solutions + " solutions.");
        } else {
            System.out.println("No solution.");
        }
    }

    public static void main(String args[]) {
        new MagicSequence().execute(args);
    }
}
