/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 18:25
 */

package org.chocosolver.examples.set;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.SetVar;

import static org.chocosolver.solver.search.strategy.Search.setVarSearch;

/**
 * Small problem to illustrate how to use set variables
 * enumerates sets such that z = union(x,y)
 *
 * @author Jean-Guillaume Fages
 */
public class SetUnion extends AbstractProblem {

    private SetVar x, y, z;
    private final boolean noEmptySet = true;

    @Override
    public void buildModel() {
        model = new Model();
        // x initial domain
        x = model.setVar("x", new int[]{1}, new int[]{1, -2, 3});
        // y initial domain
        y = model.setVar("y", new int[]{}, new int[]{-6, -2, 7});
        // z initial domain
        z = model.setVar("z", new int[]{}, new int[]{-2, -1, 0, 1, 2, 3, 4, 5, 6, 7});
        // set-union constraint
        model.union(new SetVar[]{x, y}, z).post();
        if (noEmptySet) {
            model.nbEmpty(new SetVar[]{x, y, z}, model.intVar(0)).post();
        }
    }

    @Override
    public void configureSearch() {
        Solver r = model.getSolver();
        r.setSearch(setVarSearch(x, y, z));
    }

    @Override
    public void solve() {
        while (model.getSolver().solve()) ;
    }
}
