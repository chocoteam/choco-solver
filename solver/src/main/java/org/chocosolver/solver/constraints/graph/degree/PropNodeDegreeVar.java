/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.degree;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.Orientation;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.BitSet;

/**
 * Propagator that ensures that a node has at most N successors/predecessors/neighbors
 * ENSURES EVERY VERTEX i FOR WHICH DEGREE[i]>0 IS MANDATORY
 *
 * @author Jean-Guillaume Fages
 */
public class PropNodeDegreeVar extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private final GraphVar g;
    private final IntVar[] degrees;
    private final IncidentSet target;
    private BitSet toDo;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNodeDegreeVar(DirectedGraphVar graph, Orientation setType, IntVar[] degrees) {
        super(ArrayUtils.append(degrees, new Variable[]{graph}), PropagatorPriority.BINARY, false);
        this.g = graph;
        this.n = g.getNbMaxNodes();
        this.degrees = degrees;
        if (setType == Orientation.PREDECESSORS) {
            this.target = new IncidentSet.PredecessorsSet();
        } else {
            this.target = new IncidentSet.SuccessorsSet();
        }
    }

    public PropNodeDegreeVar(UndirectedGraphVar graph, IntVar[] degrees) {
        super(ArrayUtils.append(degrees, new Variable[]{graph}), PropagatorPriority.BINARY, false);
        this.target = new IncidentSet.SuccessorsSet();
        this.g = graph;
        this.n = g.getNbMaxNodes();
        this.degrees = degrees;
        this.toDo = new BitSet(n);
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (g.isDirected()) {
            propagateDirected();
        } else {
            propagateUndirected();
        }
    }

    public void propagateDirected() throws ContradictionException {
        for (int i = 0; i < n; i++) {
            if (!g.getPotentialNodes().contains(i)) {
                degrees[i].instantiateTo(0, this);
            } else if (degrees[i].getLB() > 0) {
                g.enforceNode(i, this);
            }
            ISet env = target.getPotentialSet(g, i);
            ISet ker = target.getMandatorySet(g, i);
            degrees[i].updateLowerBound(ker.size(), this);
            degrees[i].updateUpperBound(env.size(), this);
            if (ker.size() < env.size() && degrees[i].isInstantiated()) {
                int d = degrees[i].getValue();
                if (env.size() == d) {
                    for (int s : env) {
                        target.enforce(g, i, s, this);
                    }
                } else if (ker.size() == d) {
                    for (int s : env) {
                        if (!ker.contains(s)) {
                            target.remove(g, i, s, this);
                        }
                    }
                }
            }
        }
    }

    public void propagateUndirected() throws ContradictionException {
        assert !g.isDirected();
        toDo.clear();
        for (int i = 0; i < n; i++) {
            toDo.set(i);
        }
        int i = toDo.nextSetBit(0);
        do {
            toDo.clear(i);
            if (!g.getPotentialNodes().contains(i)) {
                degrees[i].instantiateTo(0, this);
            } else if (degrees[i].getLB() > 0) {
                g.enforceNode(i, this);
            }
            ISet env = target.getPotentialSet(g, i);
            ISet ker = target.getMandatorySet(g, i);
            degrees[i].updateLowerBound(ker.size(), this);
            degrees[i].updateUpperBound(env.size(), this);
            if (ker.size() < env.size() && degrees[i].isInstantiated()) {
                int d = degrees[i].getValue();
                if (env.size() == d) {
                    for (int s : env) {
                        if (target.enforce(g, i, s, this)) {
                            toDo.set(s);
                        }
                    }
                } else if (ker.size() == d) {
                    for (int s : env) {
                        if (!ker.contains(s)) {
                            if (target.remove(g, i, s, this)) {
                                toDo.set(s);
                            }
                        }
                    }
                }
            }
            i = toDo.nextSetBit(0);
        } while (i >= 0);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public ESat isEntailed() {
        boolean done = true;
        for (int i = 0; i < n; i++) {
            if ((!degrees[i].contains(0)) && !g.getPotentialNodes().contains(i)) {
                return ESat.FALSE;
            }
            ISet env = target.getPotentialSet(g, i);
            ISet ker = target.getMandatorySet(g, i);
            if (degrees[i].getLB() > env.size()
                    || degrees[i].getUB() < ker.size()) {
                return ESat.FALSE;
            }
            if (env.size() != ker.size() || !degrees[i].isInstantiated()) {
                done = false;
            }
        }
        if (!done) {
            return ESat.UNDEFINED;
        }
        return ESat.TRUE;
    }
}
