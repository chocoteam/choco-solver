/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.among;

import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Arrays;

/**
 * Incremental propagator for Among Constraint:
 * Counts the number of decision variables which take a value in the input value set
 * GCCAT:
 * NVAR is the number of variables of the collection VARIABLES that take their value in VALUES.
 * <br/><a href="http://www.emn.fr/x-info/sdemasse/gccat/Camong.html">gccat among</a>
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 8/02/14
 */
public class PropAmongGAC extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int nb_vars;        // number of decision variables (excludes the cardinality variable)
    private final int[] values;        // value set (array)
    private TIntHashSet setValues;    // value set (set)
    private ISet poss;                // variable set possibly assigned to a value in the value set
    private IStateInt nbSure;        // number of variables that are assigned to such value for sure

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Creates a propagator for Among:
     * Counts the number of decision variables which take a value in the input value set
     *
     * @param variables {decision variables, cardinality variable}
     * @param values    input value set
     */
    public PropAmongGAC(IntVar[] variables, int[] values) {
        super(variables, PropagatorPriority.LINEAR, true);
        nb_vars = variables.length - 1;
        IEnvironment environment = model.getEnvironment();
        this.setValues = new TIntHashSet(values);
        this.values = setValues.toArray();
        Arrays.sort(this.values);
		poss = SetFactory.makeStoredSet(SetType.BIPARTITESET, 0, model);
		nbSure = environment.makeInt(0);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int idx) {
        if (idx == nb_vars) {
            return IntEventType.boundAndInst();
        }
        return IntEventType.all();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            poss.clear();
            int nbMandForSure = 0;
            for (int i = 0; i < nb_vars; i++) {
                IntVar var = vars[i];
                int nb = 0;
                for (int j : values) {
                    if (var.contains(j)) {
                        nb++;
                    }
                }
                if (nb == var.getDomainSize()) {
                    nbMandForSure++;
                } else if (nb > 0) {
                    poss.add(i);
                }
            }
            nbSure.set(nbMandForSure);
        }
        filter();
    }

    @Override
    public void propagate(int vidx, int evtmask) throws ContradictionException {
        if (vidx != nb_vars && poss.contains(vidx)) {
            IntVar var = vars[vidx];
            int nb = 0;
            for (int j : values) {
                if (var.contains(j)) {
                    nb++;
                }
            }
            if (nb == var.getDomainSize()) {
                nbSure.add(1);
                poss.remove(vidx);
                vars[nb_vars].updateLowerBound(nbSure.get(), this);
            } else if (nb == 0) {
                poss.remove(vidx);
                vars[nb_vars].updateUpperBound(poss.size() + nbSure.get(), this);
            }
        }
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    private void filter() throws ContradictionException {
        int lb = nbSure.get();
        int ub = poss.size() + lb;
        vars[nb_vars].updateBounds(lb, ub, this);
        if (vars[nb_vars].isInstantiated() && lb < ub) {
            if (vars[nb_vars].getValue() == lb) {
                backPropRemPoss();
            } else if (vars[nb_vars].getValue() == ub) {
                backPropForcePoss();
            }
        }
    }

    private void backPropRemPoss() throws ContradictionException {
        ISetIterator iter = poss.iterator();
        while (iter.hasNext()) {
            int i = iter.nextInt();
            IntVar v = vars[i];
            if (v.hasEnumeratedDomain()) {
                for (int value : values) {
                    v.removeValue(value, this);
                }
                poss.remove(i);
            } else {
                int newLB = v.getLB();
                int newUB = v.getUB();
                for (int val = v.getLB(); val <= newUB; val = v.nextValue(val)) {
                    if (setValues.contains(val)) {
                        newLB = val + 1;
                    } else {
                        break;
                    }
                }
                for (int val = newUB; val >= newLB; val = v.previousValue(val)) {
                    if (setValues.contains(val)) {
                        newUB = val - 1;
                    } else {
                        break;
                    }
                }
                v.updateBounds(newLB, newUB, this);
                if (newLB > values[values.length - 1] || newUB < values[0]) {
                    poss.remove(i);
                }
            }
        }
    }

    private void backPropForcePoss() throws ContradictionException {
        ISetIterator iter = poss.iterator();
        while (iter.hasNext()) {
            int i = iter.nextInt();
            IntVar v = vars[i];
            if (v.hasEnumeratedDomain()) {
                for (int val = v.getLB(); val <= v.getUB(); val = v.nextValue(val)) {
                    if (!setValues.contains(val)) {
                        v.removeValue(val, this);
                    }
                }
                poss.remove(i);
                nbSure.add(1);
            } else {
                v.updateBounds(values[0], values[values.length - 1], this);
                int newLB = v.getLB();
                int newUB = v.getUB();
                for (int val = v.getLB(); val <= newUB; val = v.nextValue(val)) {
                    if (!setValues.contains(val)) {
                        newLB = val + 1;
                    } else {
                        break;
                    }
                }
                for (int val = newUB; val >= newLB; val = v.previousValue(val)) {
                    if (!setValues.contains(val)) {
                        newUB = val - 1;
                    } else {
                        break;
                    }
                }
                v.updateBounds(newLB, newUB, this);
                if (v.isInstantiated()) {
                    poss.remove(i);
                    nbSure.add(1);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int min = 0;
        int max = 0;
        int nbInst = vars[nb_vars].isInstantiated() ? 1 : 0;
        for (int i = 0; i < nb_vars; i++) {
            IntVar var = vars[i];
            if (var.isInstantiated()) {
                nbInst++;
                if (setValues.contains(var.getValue())) {
                    min++;
                    max++;
                }
            } else {
                int nb = 0;
                for (int j : values) {
                    if (var.contains(j)) {
                        nb++;
                    }
                }
                if (nb == var.getDomainSize()) {
                    min++;
                    max++;
                } else if (nb > 0) {
                    max++;
                }
            }
        }
        if (min > vars[nb_vars].getUB() || max < vars[nb_vars].getLB()) {
            return ESat.FALSE;
        }
        if (nbInst == nb_vars + 1) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AMONG(");
        sb.append("[");
        for (int i = 0; i < nb_vars; i++) {
            if (i > 0) sb.append(",");
            sb.append(vars[i].toString());
        }
        sb.append("],{");
        sb.append(Arrays.toString(values));
        sb.append("},");
        sb.append(vars[nb_vars].toString()).append(")");
        return sb.toString();
    }
}
