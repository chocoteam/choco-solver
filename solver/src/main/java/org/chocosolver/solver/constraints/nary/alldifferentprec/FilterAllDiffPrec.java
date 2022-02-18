/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/*
@author Arthur Godet <arth.godet@gmail.com>
@since 05/02/2021
*/

package org.chocosolver.solver.constraints.nary.alldifferentprec;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.DirectedGraph;

public abstract class FilterAllDiffPrec {
    protected final IntVar[] variables;
    protected final boolean[][] precedence;

    public FilterAllDiffPrec(IntVar[] variables, boolean[][] precedence) {
        this.variables = variables;
        this.precedence = precedence;
    }

    public abstract PropagatorPriority getPriority();

    public abstract int getPropagationConditions(int vIdx);

    public abstract boolean propagate(DirectedGraph precedenceGraph, int[] topologicalTraversal, ICause aCause) throws ContradictionException;
}
