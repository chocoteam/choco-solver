/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
 */

package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;

/**
 * Retrieves the minimum element of the set
 * the set must not be empty
 *
 * @author Jean-Guillaume Fages
 */
public class PropMinElement extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar min;
    private SetVar set;
    private int offSet;
    private int[] weights;
    private final boolean notEmpty;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Retrieves the minimum element of the set
     * MIN(i | i in setVar) = min
     *
     * @param setVar set variable
     * @param min integer variable
     * @param notEmpty true : the set variable cannot be empty
     *                 false : the set may be empty (if so, the MIN constraint is not applied)
     */
    public PropMinElement(SetVar setVar, IntVar min, boolean notEmpty) {
        this(setVar, null, 0, min, notEmpty);
    }

    /**
     * Retrieves the minimum element induced by setVar
     * MIN{weights[i-offSet] | i in setVar} = min
     *
     * @param setVar set variable
     * @param weights array of int
     * @param offSet int
     * @param min integer variable
     * @param notEmpty true : the set variable cannot be empty
     *                 false : the set may be empty (if so, the MIN constraint is not applied)
     */
    public PropMinElement(SetVar setVar, int[] weights, int offSet, IntVar min, boolean notEmpty) {
        super(new Variable[]{setVar, min}, PropagatorPriority.BINARY, false);
        this.min = (IntVar) vars[1];
        this.set = (SetVar) vars[0];
        this.weights = weights;
        this.offSet = offSet;
        this.notEmpty = notEmpty;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) return SetEventType.all();
        else return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ISetIterator iter = set.getLB().iterator();
        while (iter.hasNext()){
            min.updateUpperBound(get(iter.nextInt()), this);
        }
        int minVal = Integer.MAX_VALUE;
        int lb = min.getLB();
        iter = set.getUB().iterator();
        while (iter.hasNext()){
            int j = iter.nextInt();
            int k = get(j);
            if (k < lb) {
                set.remove(j, this);
            } else {
                if (minVal > k) {
                    minVal = k;
                }
            }
        }
        if (notEmpty || set.getLB().size() > 0) {
            min.updateLowerBound(minVal, this);
        }
    }

    @Override
    public ESat isEntailed() {
        if (set.getUB().size() == 0) {
            if (notEmpty) {
                return ESat.FALSE;
            } else {
                return ESat.TRUE;
            }
        }
        int lb = min.getLB();
        int ub = min.getUB();
        ISetIterator iter = set.getLB().iterator();
        while (iter.hasNext()){
            if (get(iter.nextInt()) < lb) {
                return ESat.FALSE;
            }
        }
        int minVal = Integer.MAX_VALUE;
        iter = set.getUB().iterator();
        while (iter.hasNext()){
            int j = iter.nextInt();
            if (minVal > get(j)) {
                minVal = get(j);
            }
        }
        if (minVal > ub && (notEmpty || set.getLB().size() > 0)) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    private int get(int j) {
        return (weights == null) ? j : weights[j - offSet];
    }

}
