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
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;

/**
 * Simple example which orders a set of integer variables
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28/03/12
 */
public class Ordering extends AbstractProblem {

    @Option(name = "-n", aliases = "--number", usage = "number of variables.", required = false)
    int n = 1000;

    IntVar[] vars;


    @Override
    public void buildModel() {
        model = new Model();
        vars = model.intVarArray("v", n, 1, n, true);
        for (int i = 0; i < n - 1; i++) {
            model.arithm(vars[i], "<", vars[i + 1]).post();
        }
    }

    @Override
    public void configureSearch() {
    }

    @Override
    public void solve() {
        model.getSolver().solve();
    }

    public static void main(String[] args) {
        new Ordering().execute(args);
    }
}
