/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.channeling.edges;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * @author Jean-Guillaume Fages
 */
public class PropNeighIntsChannel1 extends Propagator<UndirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n;
    private IntVar[] succs;
    private UndirectedGraphVar g;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNeighIntsChannel1(final IntVar[] succs, UndirectedGraphVar gV) {
        super(new UndirectedGraphVar[]{gV}, PropagatorPriority.LINEAR, false);
        this.succs = succs;
        n = succs.length;
        this.g = gV;
        assert (n == g.getNbMaxNodes());
        for (int i = 0; i < n; i++) {
            assert succs[i].hasEnumeratedDomain() : "channeling variables should be enumerated";
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            ISet tmp = g.getMandatoryNeighborsOf(i);
            for (int j : tmp) {
                if (!succs[i].contains(j)) {
                    succs[j].instantiateTo(i, this);
                } else if (!succs[j].contains(i)) {
                    succs[i].instantiateTo(j, this);
                }
            }
            for (int j = succs[i].getLB(); j <= succs[i].getUB(); j = succs[i].nextValue(j)) {
                if (!g.getPotentialNeighborsOf(i).contains(j)) {
                    succs[i].removeValue(j, this);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            if (succs[i].isInstantiated() && !g.getPotentialNeighborsOf(i).contains(succs[i].getValue())) {
                return ESat.FALSE;
            }
            ISet tmp = g.getMandatoryNeighborsOf(i);
            for (int j : tmp) {
                if ((!succs[i].contains(j)) && (!succs[j].contains(i))) {
                    return ESat.FALSE;
                }
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
