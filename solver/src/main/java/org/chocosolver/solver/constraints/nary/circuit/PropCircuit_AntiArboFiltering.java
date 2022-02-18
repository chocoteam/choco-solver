/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.circuit;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

/**
 * Propagator for circuit constraint based on dominators
 * Redundant propagator
 *
 * @author Jean-Guillaume Fages
 */
public class PropCircuit_AntiArboFiltering extends PropCircuit_ArboFiltering {

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropCircuit_AntiArboFiltering(IntVar[] succs, int offSet, CircuitConf conf) {
        super(succs, offSet, conf);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

	protected void filterFromDom(int duplicatedNode) throws ContradictionException {
		for (int i = 0; i < n + 1; i++) {
			connectedGraph.getSuccessorsOf(i).clear();
			connectedGraph.getPredecessorsOf(i).clear();
		}
		for (int i = 0; i < n; i++) {
			int ub = vars[i].getUB();
			for (int y = vars[i].getLB(); y <= ub; y = vars[i].nextValue(y)) {
				if (y - offSet == duplicatedNode) {
					connectedGraph.addEdge(n, i);
				}else {
					connectedGraph.addEdge(y - offSet, i);
				}
			}
		}
		if (domFinder.findDominators()) {
			for (int x = 0; x < n; x++) {
				int ub = vars[x].getUB();
				for (int y = vars[x].getLB(); y <= ub; y = vars[x].nextValue(y)) {
					if(y-offSet!=duplicatedNode) {
						if (domFinder.isDomminatedBy(y - offSet,x)) {
							if(x==duplicatedNode) {
								throw new UnsupportedOperationException();
							}
							vars[x].removeValue(y, this);
						}
					}
				}
			}
		} else {
			// "the source cannot reach all nodes"
			fails();
		}
	}
}
