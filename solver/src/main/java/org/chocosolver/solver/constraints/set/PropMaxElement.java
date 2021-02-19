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
 * Retrieves the maximum element of the set
 * the set must not be empty
 *
 * @author Jean-Guillaume Fages
 */
public class PropMaxElement extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar max;
    private SetVar set;
    private int offSet;
    private int[] weights;
    private final boolean notEmpty;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Retrieves the maximum element of the set
     * MAX{i | i in setVar} = max
     *
     * @param setVar set variable
     * @param max integer variable
     * @param notEmpty true : the set variable cannot be empty
     *                 false : the set may be empty (if so, the MAX constraint is not applied)
     */
    public PropMaxElement(SetVar setVar, IntVar max, boolean notEmpty) {
        this(setVar, null, 0, max, notEmpty);
    }

    /**
     * Retrieves the maximum element induced by set
     * MAX{weight[i-offset] | i in setVar} = max
     *
     * @param setVar set variable
     * @param weights array of int
     * @param offset int
     * @param max integer variable
     * @param notEmpty true : the set variable cannot be empty
     *                 false : the set may be empty (if so, the MAX constraint is not applied)
     */
    public PropMaxElement(SetVar setVar, int[] weights, int offset, IntVar max, boolean notEmpty) {
        super(new Variable[]{setVar, max}, PropagatorPriority.BINARY, false);
        this.max = (IntVar) vars[1];
        this.set = (SetVar) vars[0];
        this.weights = weights;
        this.offSet = offset;
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
            max.updateLowerBound(get(iter.nextInt()), this);
        }
        int maxVal = Integer.MIN_VALUE;
        int ub = max.getUB();
        iter = set.getUB().iterator();
        while (iter.hasNext()){
            int j = iter.nextInt();
            int k = get(j);
            if (k > ub) {
                set.remove(j, this);
            } else {
                if (maxVal < k) {
                    maxVal = k;
                }
            }
        }
        if (notEmpty || set.getLB().size() > 0) {
            max.updateUpperBound(maxVal, this);
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
        int lb = max.getLB();
        int ub = max.getUB();
        ISetIterator iter = set.getLB().iterator();
        while (iter.hasNext()){
            if (get(iter.nextInt()) > ub) {
                return ESat.FALSE;
            }
        }
        int maxVal = Integer.MIN_VALUE;
        iter = set.getUB().iterator();
        while (iter.hasNext()){
            int j = iter.nextInt();
            if (maxVal < get(j)) {
                maxVal = get(j);
            }
        }
        if (maxVal < lb && (notEmpty || set.getLB().size() > 0)) {
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
