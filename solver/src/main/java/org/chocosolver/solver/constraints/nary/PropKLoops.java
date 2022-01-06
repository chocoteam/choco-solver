/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

import static org.chocosolver.solver.constraints.PropagatorPriority.UNARY;
import static org.chocosolver.util.tools.ArrayUtils.concat;

/**
 * Incremental propagator which restricts the number of loops:
 * |{succs[i]=i+offSet}| = nbLoops
 *
 * @author Jean-Guillaume Fages
 */
public class PropKLoops extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // number of nodes
    private final int n;
    // offset (usually 0 but 1 with MiniZinc)
    private final int offSet;
    // uninstantiated variables that can be loops
    private final ISet possibleLoops;
    private final IStateInt nbMinLoops;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

	/**
	 * Incremental propagator which restricts the number of loops:
	 * |{succs[i]=i+offSet}| = nbLoops
	 *
	 * @param succs array of integer variables
	 * @param offSet offset
	 * @param nbLoops integer variable
	 */
	public PropKLoops(IntVar[] succs, int offSet, IntVar nbLoops) {
		super(concat(succs, nbLoops), UNARY, true);
		this.n = succs.length;
		this.offSet = offSet;
		IEnvironment environment = model.getEnvironment();
		this.possibleLoops = SetFactory.makeStoredSet(SetType.BIPARTITESET, 0, model);
		this.nbMinLoops = environment.makeInt();
	}

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        possibleLoops.clear();
        nbMinLoops.set(0);
        for (int i = 0; i < n; i++) {
            if (vars[i].contains(i + offSet)) {
                if (vars[i].isInstantiated()) {
                    nbMinLoops.add(1);
                } else {
                    possibleLoops.add(i);
                }
            }
        }
        filter();
    }

    private void filter() throws ContradictionException {
        int nbMin = nbMinLoops.get();
        int nbMax = nbMin + possibleLoops.size();
        vars[n].updateBounds(nbMin, nbMax, this);
        if (vars[n].isInstantiated() && nbMin != nbMax) {
            if (vars[n].getValue() == nbMax) {
                ISetIterator iter = possibleLoops.iterator();
                while (iter.hasNext()) {
                    int i = iter.nextInt();
                    vars[i].instantiateTo(i + offSet, this);
                    assert vars[i].isInstantiatedTo(i + offSet);
                    nbMinLoops.add(1);
                }
                possibleLoops.clear();
                setPassive();
            } else if (vars[n].getValue() == nbMin) {
                ISetIterator iter = possibleLoops.iterator();
                while (iter.hasNext()) {
                    int i = iter.nextInt();
                    if (vars[i].removeValue(i + offSet, this)) {
                        possibleLoops.remove(i);
                    }
                }
                if (possibleLoops.isEmpty()) {
                    setPassive();
                }
            }
        }
    }

    @Override
    public void propagate(int idV, int mask) throws ContradictionException {
        if (idV < n) {
            if (possibleLoops.contains(idV)) {
                if (vars[idV].contains(idV + offSet)) {
                    if (vars[idV].isInstantiated()) {
                        nbMinLoops.add(1);
                        possibleLoops.remove(idV);
                    }
                } else {
                    possibleLoops.remove(idV);
                }
            }
        }
        filter();
    }

    @Override
    public ESat isEntailed() {
        int nbMax = 0;
        int nbMin = 0;
        for (int i = 0; i < n; i++) {
            if (vars[i].contains(i + offSet)) {
                nbMax++;
                if (vars[i].isInstantiated()) {
                    nbMin++;
                }
            }
        }
        if (vars[n].getLB() > nbMax || vars[n].getUB() < nbMin) {
            return ESat.FALSE;
        }
        if (nbMin == nbMax && vars[n].isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
