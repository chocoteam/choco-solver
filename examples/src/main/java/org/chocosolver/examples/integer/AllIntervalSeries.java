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
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;

/**
 * CSPLib prob007:<br/>
 * "Given n in N, find a vector s = (s_1, ..., s_n), such that
 * <ul>
 * <li>s is a permutation of Z_n = {0,1,...,n-1};</li>
 * <li>the interval vector v = (|s_2-s_1|, |s_3-s_2|, ... |s_n-s_{n-1}|) is a permutation of Z_n-{0} = {1,2,...,n-1}.</li>
 * </ul>
 * <br/>
 * A vector v satisfying these conditions is called an all-interval series of size n;
 * the problem of finding such a series is the all-interval series problem of size n."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/08/11
 */
public class AllIntervalSeries extends AbstractProblem {
    @SuppressWarnings("FieldMayBeFinal")
    @Option(name = "-o", usage = "All interval series size.", required = false)
    private int m = 1000;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(name = "-v", usage = " use views instead of constraints.", required = false)
    private boolean use_views = false;

    IntVar[] vars;
    IntVar[] dist;

    @Override
    public void buildModel() {
        model = new Model("AllIntervalSeries");
        vars = model.intVarArray("v", m, 0, m - 1, false);
        dist = new IntVar[m - 1];

        if (!use_views) {
            dist = model.intVarArray("dist", m - 1, 1, m - 1, false);
            for (int i = 0; i < m - 1; i++) {
                model.distance(vars[i + 1], vars[i], "=", dist[i]).post();
            }
        } else {
            for (int i = 0; i < m - 1; i++) {
                IntVar k = model.intVar(model.generateName(), -20000, 20000, true);
                model.sum(new IntVar[]{vars[i], k}, "=", vars[i + 1]).post();
                dist[i] = model.intAbsView(k);
                model.member(dist[i], 1, m - 1).post();
            }
        }

        model.allDifferent(vars, "BC").post();
        model.allDifferent(dist, "BC").post();

        // break symetries
        model.arithm(vars[1], ">", vars[0]).post();
        model.arithm(dist[0], ">", dist[m - 2]).post();
    }

    @Override
    public void configureSearch() {
        model.getSolver().setSearch(Search.minDomLBSearch(vars));
    }

    @Override
    public void solve() {
        model.getSolver().solve();
        System.out.println(String.format("All interval series(%s)", m));
        StringBuilder st = new StringBuilder();
        st.append("\t");
        for (int i = 0; i < m - 1; i++) {
            st.append(String.format("%d <%d> ", vars[i].getValue(), dist[i].getValue()));
            if (i % 10 == 9) {
                st.append("\n\t");
            }
        }
        st.append(String.format("%d", vars[m - 1].getValue()));
        System.out.println(st);
    }

    public static void main(String[] args) {
        new AllIntervalSeries().execute(args);
    }
}
