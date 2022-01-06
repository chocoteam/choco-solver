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

import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Propagator that ensures that Nb arcs/edges belong to the final graph
 *
 * @author Jean-Guillaume Fages
 */
public class PropNbEdges extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected GraphVar g;
    protected IntVar k;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNbEdges(GraphVar graph, IntVar k) {
        super(new Variable[]{graph, k}, PropagatorPriority.LINEAR, false);
        this.g = graph;
        this.k = k;
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int nbK = 0;
        int nbE = 0;
        ISet env = g.getPotentialNodes();
        for (int i : env) {
            nbE += g.getPotentialSuccessorsOf(i).size();
            nbK += g.getMandatorySuccessorsOf(i).size();
        }
        if (!g.isDirected()) {
            nbK /= 2;
            nbE /= 2;
        }
        filter(nbK, nbE);
    }

    private void filter(int nbK, int nbE) throws ContradictionException {
        k.updateLowerBound(nbK, this);
        k.updateUpperBound(nbE, this);
        if (nbK != nbE && k.isInstantiated()) {
            ISet nei;
            ISet env = g.getPotentialNodes();
            if (k.getValue() == nbE) {
                for (int i : env) {
                    nei = g.getUB().getSuccessorsOf(i);
                    for (int j : nei) {
                        g.enforceEdge(i, j, this);
                    }
                }
            }
            if (k.getValue() == nbK) {
                ISet neiKer;
                for (int i : env) {
                    nei = g.getUB().getSuccessorsOf(i);
                    neiKer = g.getLB().getSuccessorsOf(i);
                    for (int j : nei) {
                        if (!neiKer.contains(j)) {
                            g.removeEdge(i, j, this);
                        }
                    }
                }
            }
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return GraphEventType.REMOVE_EDGE.getMask() + GraphEventType.ADD_EDGE.getMask();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public ESat isEntailed() {
        int nbK = 0;
        int nbE = 0;
        ISet env = g.getPotentialNodes();
        for (int i : env) {
            nbE += g.getUB().getSuccessorsOf(i).size();
            nbK += g.getLB().getSuccessorsOf(i).size();
        }
        if (!g.isDirected()) {
            nbK /= 2;
            nbE /= 2;
        }
        if (nbK > k.getUB() || nbE < k.getLB()) {
            return ESat.FALSE;
        }
        if (k.isInstantiated() && g.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
