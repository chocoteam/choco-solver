/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.graph;

import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.view.GraphView;
import org.chocosolver.util.objects.graphs.DirectedGraph;

/**
 * An abstract class for directed graph views over other variables
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public abstract class DirectedGraphView<V extends Variable> extends GraphView<V, DirectedGraph> implements DirectedGraphVar {

    /**
     * Creates a graph view.
     *
     * @param name      name of the view
     * @param variables observed variables
     */
    protected DirectedGraphView(String name, V[] variables) {
        super(name, variables);
    }

    @Override
    public boolean isDirected() {
        return true;
    }
}
