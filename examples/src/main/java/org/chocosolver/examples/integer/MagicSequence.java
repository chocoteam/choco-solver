/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

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

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import org.kohsuke.args4j.Option;
import org.chocosolver.solver.search.strategy.Search;

public class MagicSequence extends AbstractProblem {

    @Option(name = "-n", usage = "Size of problem (default 10).", required = false)
    int n = 10;

    IntVar[] x;

    @Override
    public void buildModel() {
        model = new Model();

        int[] values = ArrayUtils.array(0, n-1);

        x = model.intVarArray("x", n, 0, n - 1, false);

        boolean closed = true; // restricts domains of VARS to VALUES if set to true
        model.globalCardinality(x, values, x, closed).post();

        // Redundant constraint
        model.sum(x, "=", n).post();

    }

    @Override
    public void configureSearch() {
        model.getSolver().setSearch(Search.minDomLBSearch(x));
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
