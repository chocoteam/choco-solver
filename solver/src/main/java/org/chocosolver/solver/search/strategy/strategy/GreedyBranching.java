/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.Variable;

public class GreedyBranching extends AbstractStrategy {

    private AbstractStrategy mainSearch;

    public GreedyBranching(AbstractStrategy mainSearch){
        //noinspection unchecked
        super(mainSearch.getVariables());
        this.mainSearch = mainSearch;
    }

    @Override
    public boolean init() {
        return mainSearch.init();
    }

    @Override
    public void remove() {
        this.mainSearch.remove();
    }

    @Override
    public Decision getDecision() {
        Decision d = mainSearch.getDecision();
        if (d != null) {
            d.setRefutable(false);
        }
        return d;
    }

    @Override
    public Decision computeDecision(Variable variable) {
        //noinspection unchecked
        Decision d = mainSearch.computeDecision(variable);
        if (d != null) {
            d.setRefutable(false);
        }
        return d;
    }
}
