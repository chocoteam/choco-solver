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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.graph.UndirectedGraphView;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;

public class BinaryMatrixUndirectedGraphView extends UndirectedGraphView<BoolVar> {

    int nbRows;
    int nbCols;

    int boolTrue;
    int boolFalse;

    UndirectedGraph lb;
    UndirectedGraph ub;

    /**
     * Creates a graph view.
     *
     * @param name      name of the view
     * @param variables observed variables
     */
    public BinaryMatrixUndirectedGraphView(String name, BoolVar[][] variables, boolean boolValue) {
        super(name, ArrayUtils.flatten(variables));
        this.boolTrue = boolValue ? BoolVar.kTRUE : BoolVar.kFALSE;
        this.boolFalse = boolValue ? BoolVar.kFALSE : BoolVar.kTRUE;
        this.nbRows = variables.length;
        this.nbCols = variables[0].length;
        this.lb = GraphFactory.makeStoredUndirectedGraph(getModel(), nbRows * nbCols, SetType.BITSET, SetType.BITSET);
        this.ub = GraphFactory.makeStoredUndirectedGraph(getModel(), nbRows * nbCols, SetType.BITSET, SetType.BITSET);
        for (int i = 0; i < nbRows; i++) {
            for (int j = 0; j < nbCols; j++) {
                if (variables[i][j].isInstantiatedTo(boolTrue)) {
                    lb.addNode(coordsToIndex(i, j));
                }
                if (variables[i][j].contains(boolTrue)) {
                    ub.addNode(coordsToIndex(i, j));
                }
            }
        }
        for (int i : ub.getNodes()) {
            for (int j : getNeighbors(i)) {
                if (lb.containsNode(i) && lb.containsNode(j)) {
                    lb.addEdge(i, j);
                }
                if (ub.containsNode(j)) {
                    ub.addEdge(i, j);
                }
            }
        }
    }

    private int coordsToIndex(int row, int col) {
        return nbCols * row + col;
    }

    private ISet getNeighbors(int index) {
        ISet neighbors = SetFactory.makeBitSet(0);
        if (index % nbCols != 0) {
            neighbors.add(index - 1);
        }
        if ((index + 1) % nbCols != 0) {
            neighbors.add(index + 1);
        }
        if (index >= nbCols) {
            neighbors.add(index - nbCols);
        }
        if (index < nbCols * (nbRows - 1)) {
            neighbors.add(index + nbCols);
        }
        return neighbors;
    }

    @Override
    public UndirectedGraph getLB() {
        return lb;
    }

    @Override
    public UndirectedGraph getUB() {
        return ub;
    }

    @Override
    public int getNbMaxNodes() {
        return nbCols * nbRows;
    }

    @Override
    protected boolean doRemoveNode(int node) throws ContradictionException {
        if (getVariables()[node].instantiateTo(boolFalse, this)) {
            ub.removeNode(node);
            return true;
        }
        return false;
    }

    @Override
    protected boolean doEnforceNode(int node) throws ContradictionException {
        if (getVariables()[node].instantiateTo(boolTrue, this)) {
            lb.addNode(node);
            for (int i : getNeighbors(node)) {
                if (lb.containsNode(i)) {
                    lb.addEdge(node, i);
                } else if (ub.containsNode(i) && !ub.containsEdge(node, i)) {
                    removeNode(i, this);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean doRemoveEdge(int from, int to) throws ContradictionException {
        boolean fromInKer = getMandatoryNodes().contains(from);
        boolean toInKer = getMandatoryNodes().contains(to);
        if (fromInKer && toInKer) {
            this.contradiction(this, "Remove mandatory edge");
        }
        if (ub.removeEdge(from, to)) {
            if (fromInKer && !toInKer) {
                doRemoveNode(to);
            }
            if (!fromInKer && toInKer) {
                doRemoveNode(from);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean doEnforceEdge(int from, int to) throws ContradictionException {
        return lb.addEdge(from, to);
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        if (this.getVariables()[variableIdx].isInstantiatedTo(boolTrue)) {
            lb.addNode(variableIdx);
            notifyPropagators(GraphEventType.ADD_NODE, this);
            for (int i : getNeighbors(variableIdx)) {
                if (lb.containsNode(i)) {
                    lb.addEdge(variableIdx, i);
                    notifyPropagators(GraphEventType.ADD_EDGE, this);
                } else if (ub.containsNode(i) && !ub.containsEdge(variableIdx, i)) {
                    ub.removeNode(i);
                    notifyPropagators(GraphEventType.REMOVE_NODE, this);
                }
            }
        } else if (this.getVariables()[variableIdx].isInstantiatedTo(boolFalse)) {
            ub.removeNode(variableIdx);
            notifyPropagators(GraphEventType.REMOVE_NODE, this);
        }
    }

}
