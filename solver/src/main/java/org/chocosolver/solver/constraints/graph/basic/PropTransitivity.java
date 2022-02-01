/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.basic;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * Propagator that ensures that the relation of the graph is transitive : (a,b) + (b,c) implies (a,c)
 *
 * @author Jean-Guillaume Fages
 */
public class PropTransitivity<V extends GraphVar<?>> extends Propagator<V> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final V g;
    private final IGraphDeltaMonitor gdm;
    private final PairProcedure arcEnforced;
    private final PairProcedure arcRemoved;
    private final TIntArrayList eF;
    private final TIntArrayList eT;
    private final TIntArrayList rF;
    private final TIntArrayList rT;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropTransitivity(V graph) {
        super((V[]) new GraphVar[]{graph}, PropagatorPriority.LINEAR, true);
        g = graph;
        gdm = g.monitorDelta(this);
        int n = g.getNbMaxNodes();
        eF = new TIntArrayList(n);
        eT = new TIntArrayList(n);
        rF = new TIntArrayList(n);
        rT = new TIntArrayList(n);
        arcEnforced = this::arcEnforced;
        arcRemoved = this::arcRemoved;
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int n = g.getNbMaxNodes();
        for (int i : g.getPotentialNodes()) {
            for (int j = 0; j < n; j++) {
                if (g.getMandatorySuccessorsOf(i).contains(j)) {
                    arcEnforced(i, j);
                } else if (!g.getPotentialSuccessorsOf(i).contains(j)) {
                    arcRemoved(i, j);
                }
            }
        }
        filter();
        gdm.startMonitoring();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        rT.clear();
        rF.clear();
        eT.clear();
        eF.clear();
        gdm.forEachEdge(arcEnforced, GraphEventType.ADD_EDGE);
        gdm.forEachEdge(arcRemoved, GraphEventType.REMOVE_EDGE);
        filter();
    }

    private void filter() throws ContradictionException {
        // Fix point
        assert eF.size() == eT.size();
        while (!eF.isEmpty()) {
            assert eF.size() == eT.size();
            enfArc(eF.removeAt(eF.size() - 1), eT.removeAt(eT.size() - 1));
        }
        assert rF.size() == rT.size();
        while (!rF.isEmpty()) {
            assert rF.size() == rT.size();
            remArc(rF.removeAt(rF.size() - 1), rT.removeAt(rT.size() - 1));
        }
        assert eF.size() == eT.size();
        if (!eF.isEmpty()) {
            filter();
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return GraphEventType.REMOVE_EDGE.getMask() + GraphEventType.ADD_EDGE.getMask();
    }

    @Override
    public ESat isEntailed() {
        int n = g.getNbMaxNodes();
        for (int i = 0; i < n; i++) {
            for (int j : g.getMandatorySuccessorsOf(i)) {
                if (i != j) {
                    for (int j2 : g.getMandatorySuccessorsOf(j)) {
                        if (j2 != i && !g.getPotentialSuccessorsOf(i).contains(j2)) {
                            return ESat.FALSE;
                        }
                    }
                }
            }
        }
        if (g.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    //***********************************************************************************
    // PROCEDURE
    //***********************************************************************************

    private void arcEnforced(int x, int y) {
        eF.add(x);
        eT.add(y);
    }

    private void arcRemoved(int x, int y) {
        rF.add(x);
        rT.add(y);
    }

    // --- Arc enforcings
    private void enfArc(int from, int to) throws ContradictionException {
        if (from != to) {
            ISet ker = g.getMandatorySuccessorsOf(to);
            ISet env = g.getPotentialSuccessorsOf(to);
            for (int i : env) {
                if (i != to && i != from) {
                    if (ker.contains(i)) {
                        if (g.enforceEdge(from, i, this)) {
                            arcEnforced(from, i);
                        }
                    } else if (!g.getPotentialSuccessorsOf(from).contains(i)) {
                        if (g.removeEdge(to, i, this)) {
                            arcRemoved(to, i);
                        }
                    }
                }
            }
            ker = g.getMandatoryPredecessorsOf(from);
            env = g.getPotentialPredecessorOf(from);
            for (int i : env) {
                if (i != to && i != from) {
                    if (ker.contains(i)) {
                        if (g.enforceEdge(i, to, this)) {
                            arcEnforced(i, to);
                        }
                    } else if (!g.getPotentialSuccessorsOf(i).contains(to)) {
                        if (g.removeEdge(i, from, this)) {
                            arcRemoved(i, from);
                        }
                    }
                }
            }
        }
    }

    // --- Arc removals
    private void remArc(int from, int to) throws ContradictionException {
        if (from != to) {
            for (int i : g.getMandatorySuccessorsOf(from)) {
                if (g.removeEdge(i, to, this)) {
                    arcRemoved(i, to);
                }
            }
            for (int i : g.getMandatoryPredecessorsOf(to)) {
                if (g.removeEdge(from, i, this)) {
                    arcRemoved(from, i);
                }
            }
        }
    }
}
