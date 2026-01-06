/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.nqueen;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public abstract class AbstractNQueen extends AbstractProblem {

    @Option(name = "-q", usage = "Number of queens.", required = false)
    int n = 1700;
    IntVar[] vars;

    @Override
    public void configureSearch() {
        model.getSolver().setSearch(Search.minDomLBSearch(vars));
//        model.getSolver().setSearch(Search.inputOrderLBSearch(vars));
    }

    @Override
    public void solve() {
        model.getSolver().showStatistics();
        model.getSolver().solve();
    }
}
