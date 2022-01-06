/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.set;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Restricts the number of empty sets
 * |{s in sets such that |s|=0}| = nbEmpty
 *
 * @author Jean-Guillaume Fages
 */
public class PropNbEmpty extends Propagator<Variable> {

    private final SetVar[] sets;
    private final IntVar nbEmpty;
    private final int n;
    private final ISet canBeEmpty;
    private final ISet isEmpty;
    private final IStateInt nbAlreadyEmpty;
    private final IStateInt nbMaybeEmpty;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Restricts the number of empty sets
     * |{s in sets such that |s|=0}| = nbEmpty
     *
     * @param sets    array of set variables
     * @param nbEmpty integer variable
     */
    public PropNbEmpty(SetVar[] sets, IntVar nbEmpty) {
        super(ArrayUtils.append(sets, new Variable[]{nbEmpty}), PropagatorPriority.UNARY, true);
        this.n = sets.length;
        this.sets = new SetVar[sets.length];
        for (int i = 0; i < sets.length; i++) {
            this.sets[i] = (SetVar) vars[i];
        }
        this.nbEmpty = (IntVar) vars[n];
        IEnvironment environment = model.getEnvironment();
        this.canBeEmpty = SetFactory.makeStoredSet(SetType.BIPARTITESET, 0, model);
        this.isEmpty = SetFactory.makeStoredSet(SetType.BIPARTITESET, 0, model);
        this.nbAlreadyEmpty = environment.makeInt();
        this.nbMaybeEmpty = environment.makeInt();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************


    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx < vars.length - 1) {
            return SetEventType.all();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            int nbMin = 0;
            int nbMax = 0;
            canBeEmpty.clear();
            isEmpty.clear();
            for (int i = 0; i < n; i++) {
                if (sets[i].getLB().size() == 0) {
                    nbMax++;
                    if (sets[i].getUB().size() == 0) {
                        nbMin++;
                        isEmpty.add(i);
                    } else {
                        canBeEmpty.add(i);
                    }
                }
            }
            nbAlreadyEmpty.set(nbMin);
            nbMaybeEmpty.set(nbMax - nbMin);
        }
        filter();
    }

    @Override
    public void propagate(int v, int mask) throws ContradictionException {
        if (v < n) {
            if (canBeEmpty.contains(v)) {
                if (sets[v].getLB().size() > 0) {
                    canBeEmpty.remove(v);
                    nbMaybeEmpty.add(-1);
                } else {
                    if (sets[v].getUB().size() == 0) {
                        isEmpty.add(v);
                        canBeEmpty.remove(v);
                        nbMaybeEmpty.add(-1);
                        nbAlreadyEmpty.add(1);
                    }
                }
            }
        }
        filter();
    }

    public void filter() throws ContradictionException {
        int nbMin = nbAlreadyEmpty.get();
        int nbMax = nbMin + nbMaybeEmpty.get();
        nbEmpty.updateBounds(nbMin, nbMax, this);
        ///////////////////////////////////////
        if (nbEmpty.isInstantiated() && nbMin < nbMax) {
            if (nbEmpty.getValue() == nbMax) {
                for (int i : canBeEmpty) {
                    for (int j : sets[i].getUB()) {
                        sets[i].remove(j, this);
                    }
                    canBeEmpty.remove(i);
                    isEmpty.add(i);
                }
                setPassive();
            }
            if (nbEmpty.getValue() == nbMin) {
                boolean allFixed = true;
                for (int i : canBeEmpty) {
                    if (sets[i].getUB().size() == 1) {
                        int val = sets[i].getUB().iterator().next();
                        sets[i].force(val, this);
                        canBeEmpty.remove(i);
                    } else {
                        allFixed = false;
                    }
                }
                if (allFixed) {
                    setPassive();
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int nbMin = 0;
        int nbMax = 0;
        for (int i = 0; i < n; i++) {
            if (sets[i].getLB().size() == 0) {
                nbMax++;
                if (sets[i].getUB().size() == 0) {
                    nbMin++;
                }
            }
        }
        if (nbEmpty.getLB() > nbMax || nbEmpty.getUB() < nbMin) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
