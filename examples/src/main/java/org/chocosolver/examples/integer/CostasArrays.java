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

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;


/**
 * Costas Arrays
 * "Given n in N, find an array s = [s_1, ..., s_n], such that
 * <ul>
 * <li>s is a permutation of Z_n = {0,1,...,n-1};</li>
 * <li>the vectors v(i,j) = (j-i)x + (s_j-s_i)y are all different </li>
 * </ul>
 * <br/>
 * An array v satisfying these conditions is called a Costas array of size n;
 * the problem of finding such an array is the Costas Array problem of size n."
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 25/01/11
 */
public class CostasArrays extends AbstractProblem {

    @Option(name = "-o", usage = "Costas array size.", required = false)
    private static int n = 14;  // should be <15 to be solved quickly

    IntVar[] vars, vectors;

    @Override
	public void buildModel() {
		model = new Model("CostasArrays");
		vars = model.intVarArray("v", n, 0, n - 1, false);
		vectors = new IntVar[(n * (n - 1)) / 2];
		for (int i = 0, k = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++, k++) {
				IntVar d = model.intVar(model.generateName(), -n, n, false);
				model.arithm(d, "!=", 0).post();
				model.sum(new IntVar[]{vars[i], d}, "=", vars[j]).post();
				vectors[k] = model.intOffsetView(d, 2 * n * (j - i));
			}
		}
		model.allDifferent(vars, "AC").post();
		model.allDifferent(vectors, "BC").post();

		// symmetry-breaking
		model.arithm(vars[0], "<", vars[n - 1]).post();
	}

    @Override
    public void solve() {
        model.getSolver().solve();
		model.getSolver().printStatistics();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            s.append("|");
            for (int j = 0; j < n; j++) {
                if (j == vars[i].getValue()) {
                    s.append("x|");
                } else {
                    s.append("-|");
                }
            }
            s.append("\n");
        }
        System.out.println(s);
    }
}
