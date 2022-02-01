/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.ESat;
import org.kohsuke.args4j.Option;

import static org.chocosolver.util.tools.ArrayUtils.flatten;

/**
 * CSPLib: prob015:<br/>
 * "The problem is to put n balls labelled {1,...n} into 3 boxes so that
 * for any triple of balls (x,y,z) with x+y=z, not all are in the same box.
 * This has a solution iff n < 14.
 * <br/>
 * One natural generalization is to consider partitioning into k boxes (for k>3)."
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/08/11
 */
public class SchurLemma extends AbstractProblem {

    @Option(name = "-n", usage = "Number of balls.", required = false)
    int n = 43;

    @Option(name = "-k", usage = "Number of boxes.", required = false)
    int k = 4;


    BoolVar[][] M;


    @Override
    public void buildModel() {
        model = new Model();

        M = model.boolVarMatrix("b", n, k); // M_ij is true iff ball i is in box j

        for (int i = 0; i < n; i++) {
            model.sum(M[i], "=", 1).post();
        }

        for (int i = 0; i < k; i++) {
            for (int x = 1; x <= n; x++) {
                for (int y = 1; y <= n; y++) {
                    for (int z = 1; z <= n; z++) {
                        if (x + y == z)
                            model.sum(new BoolVar[]{M[x - 1][i], M[y - 1][i], M[z - 1][i]}, "=", model.intVar("sum", 0, 2, true)).post();
                    }
                }
            }
        }
    }

    @Override
    public void configureSearch() {
        model.getSolver().setSearch(Search.inputOrderLBSearch(flatten(M)));
    }

    @Override
    public void solve() {
        model.getSolver().solve();

        System.out.println(String.format("Schur's lemma (%d,%d)", n, k));
        StringBuilder st = new StringBuilder();
        if (model.getSolver().isFeasible() == ESat.TRUE) {
            for (int i = 0; i < k; i++) {
                st.append("\tBox #").append(i + 1).append(": ");
                for (int j = 0; j < n; j++) {
                    if (M[j][i].getValue() > 0) {
                        st.append(j + 1).append(" ");
                    }
                }
                st.append("\n");
            }
        } else {
            st.append("\tINFEASIBLE");
        }
        System.out.println(st);
    }

    public static void main(String[] args) {
        new SchurLemma().execute(args);
    }
}
