/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.tree;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.connectivity.StrongConnectivityFinder;
import org.chocosolver.util.graphOperations.dominance.AbstractLengauerTarjanDominatorsFinder;
import org.chocosolver.util.graphOperations.dominance.AlphaDominatorsFinder;
import org.chocosolver.util.graphOperations.dominance.SimpleDominatorsFinder;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.BitSet;

/**
 * Arborescences constraint (simplification from tree constraint) based on dominators
 * CONSIDERS THAT EACH NODE WITH NO PREDECESSOR IS A ROOT (needs at least one such node)
 *
 * @author Jean-Guillaume Fages
 */
public class PropArborescences extends Propagator<DirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // flow graph
    protected DirectedGraphVar g;
    protected DirectedGraph connectedGraph;
    // number of nodes
    protected int n;
    // dominators finder that contains the dominator tree
    protected AbstractLengauerTarjanDominatorsFinder domFinder;
    protected ISet[] successors;
    protected BitSet mandVert;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropArborescences(DirectedGraphVar graph) {
        this(graph, false);
    }

    public PropArborescences(DirectedGraphVar graph, boolean simple) {
        super(new DirectedGraphVar[]{graph}, PropagatorPriority.QUADRATIC, false);
        g = graph;
        n = g.getNbMaxNodes();
        successors = new ISet[n];
        connectedGraph = new DirectedGraph(n + 1, SetType.BITSET, true);
        mandVert = new BitSet(n);
        if (simple) {
            domFinder = new SimpleDominatorsFinder(n, connectedGraph);
        } else {
            domFinder = new AlphaDominatorsFinder(n, connectedGraph);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // reset data structures
        reset();
        // reach all nodes from root
        if (domFinder.findDominators()) {
            // remove backward arcs
            remBackArcs();
            // enforce dominators and arc-dominators
            enforceDominators();
        } else {
            fails();
        }
    }

    protected void reset() {
        // reset data structure
        for (int i = 0; i < n + 1; i++) {
            connectedGraph.getSuccessorsOf(i).clear();
            connectedGraph.getPredecessorsOf(i).clear();
        }
        ISet nei;
        for (int i = 0; i < n; i++) {
            for (int y : g.getPotentialPredecessorOf(i)) {
                connectedGraph.addEdge(y, i);
            }
            nei = g.getMandatoryPredecessorsOf(i);
            if (nei.isEmpty()) {
                connectedGraph.addEdge(n, i);
            }
        }
    }

    protected void remBackArcs() throws ContradictionException {
        // remove backward arcs
        for (int x : g.getPotentialNodes()) {
            g.removeEdge(x, x, this); // no loop
            for (int y : g.getPotentialSuccessorsOf(x)) {
                if (domFinder.isDomminatedBy(x, y)) {
                    g.removeEdge(x, y, this);
                }
            }
        }
    }

    protected void enforceDominators() throws ContradictionException {
        // enforce dominator nodes and arcs
        mandVert.clear();
        for (int x : g.getMandatoryNodes()) {
            mandVert.set(x);
        }
        while (mandVert.nextSetBit(0) >= 0) {
            enforceDominatorsFrom(mandVert.nextSetBit(0));
        }
    }

    protected void enforceDominatorsFrom(int j) throws ContradictionException {
        mandVert.clear(j);
        int i = domFinder.getImmediateDominatorsOf(j);
        if (i != n) {
            if (!domFinder.isDomminatedBy(j, i)) {
                throw new UnsupportedOperationException();
            }
            // DOMINATOR enforcing
            if (g.enforceNode(i, this)) {
                mandVert.set(i);
            }
            // ARC-DOMINATOR enforcing
            ISet pred = g.getPotentialPredecessorOf(j);
            if (pred.contains(i) && !g.getMandatoryPredecessorsOf(j).contains(i)) {
                boolean arcDom = true;
                for (int p : pred) {
                    if (p != i && !domFinder.isDomminatedBy(p, j)) {
                        arcDom = false;
                    }
                }
                if (arcDom) {
                    g.enforceEdge(i, j, this);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        // First assert that the graph LB is acyclic, which is equivalent to asserting that the number of strongly
        // connected components is equal to the number of mandatory nodes
        StrongConnectivityFinder scfinder = new StrongConnectivityFinder(g.getLB());
        scfinder.findAllSCC();
        if (g.getMandatoryNodes().size() - scfinder.getNbSCC() > 0) {
            return ESat.FALSE;
        }
        // Then assert that no mandatory node has more than one mandatory predecessor
        for (int i : g.getMandatoryNodes()) {
            if (g.getMandatoryPredecessorsOf(i).size() > 1) {
                return ESat.FALSE;
            }
        }
        if (g.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
