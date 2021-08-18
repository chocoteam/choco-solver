/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.graph.undirected;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.view.graph.UndirectedGraphView;
import org.chocosolver.util.objects.graphs.UndirectedGraph;

public class UndirectedGraphSetInducedView extends UndirectedGraphView<SetVar> {

    /**
     * Creates a graph view.
     *
     * @param name      name of the view
     * @param setVar observed variable
     */
    protected UndirectedGraphSetInducedView(String name, SetVar setVar, UndirectedGraph g) {
        super(name, new SetVar[]{setVar});
    }

    @Override
    public UndirectedGraph getLB() {
        return null;
    }

    @Override
    public UndirectedGraph getUB() {
        return null;
    }

    @Override
    public int getNbMaxNodes() {
        return 0;
    }

    @Override
    protected boolean doRemoveNode(int node) throws ContradictionException {
        return false;
    }

    @Override
    protected boolean doEnforceNode(int node) throws ContradictionException {
        return false;
    }

    @Override
    protected boolean doRemoveEdge(int from, int to) throws ContradictionException {
        return false;
    }

    @Override
    protected boolean doEnforceEdge(int from, int to) throws ContradictionException {
        return false;
    }
}
