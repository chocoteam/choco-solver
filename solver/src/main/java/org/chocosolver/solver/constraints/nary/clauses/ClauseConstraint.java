/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.clauses;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

/**
 * A constraint dedicated to store and manage signed-clauses.
 * @see ClauseStore
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 24/07/2018.
 */
public class ClauseConstraint extends Constraint {

    private final ClauseStore clauseStore;

    public ClauseConstraint(Model model) {
        super(ConstraintsName.CLAUSECONSTRAINT, new ClauseStore(model));
        clauseStore = (ClauseStore) propagators[0];
    }

    /**
     * Add a new clause to the clause store, like: (vars[0] ∈ ranges[0]) &or; (vars[1] ∈ ranges[1]) &or; ...
     * @param vars set of variables
     * @param ranges set of allowed ranges
     */
    public void addClause(IntVar[] vars, IntIterableRangeSet[] ranges){
        clauseStore.add(vars, ranges);
    }

    @Override
    public ESat isSatisfied() {
        return clauseStore.isEntailed();
    }

    public ClauseStore getClauseStore() {
        return clauseStore;
    }
}
