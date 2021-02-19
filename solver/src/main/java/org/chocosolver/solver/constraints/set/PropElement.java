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

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Propagator for element constraint over sets
 * states that
 * array[index-offSet] = set
 *
 * @author Jean-Guillaume Fages
 */
public class PropElement extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private TIntArrayList constructiveDisjunction;
    private IntVar index;
    private SetVar set;
    private SetVar[] array;
    private int offSet;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for element constraint over sets
     * states that array[index-offSet] = set
     *
     * @param index integer variable
     * @param array array of set variables
     * @param offSet int
     * @param set set variable
     */
    public PropElement(IntVar index, SetVar[] array, int offSet, SetVar set) {
        super(ArrayUtils.append(array, new Variable[]{set, index}), PropagatorPriority.LINEAR, false);
        this.index = (IntVar) vars[vars.length - 1];
        this.set = (SetVar) vars[vars.length - 2];
        this.array = new SetVar[array.length];
        for (int i = 0; i < array.length; i++) {
            this.array[i] = (SetVar) vars[i];
        }
        this.offSet = offSet;
        constructiveDisjunction = new TIntArrayList();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        index.updateBounds(offSet, array.length - 1 + offSet, this);
        if (index.isInstantiated()) {
            // filter set and array
            setEq(set, array[index.getValue() - offSet]);
            setEq(array[index.getValue() - offSet], set);
        } else {
            // filter index
            int ub = index.getUB();
            boolean noEmptyKer = true;
            for (int i = index.getLB(); i <= ub; i = index.nextValue(i)) {
                if (disjoint(set, array[i - offSet]) || disjoint(array[i - offSet], set)) {// array[i] != set
                    index.removeValue(i, this);
                } else {
                    if (array[i - offSet].getLB().size() == 0) {
                        noEmptyKer = false;
                    }
                }
            }
            ub = index.getUB();
            // filter set (constructive disjunction)
            if (noEmptyKer) {// from ker
                constructiveDisjunction.clear();
                ISetIterator iter = array[index.getLB() - offSet].getLB().iterator();
                while (iter.hasNext()){
                    int j = iter.nextInt();
                    if (!set.getLB().contains(j)) {
                        constructiveDisjunction.add(j);
                    }
                }
                for (int cd = constructiveDisjunction.size() - 1; cd >= 0; cd--) {
                    int j = constructiveDisjunction.get(cd);
                    for (int i = index.nextValue(index.getLB()); i <= ub; i = index.nextValue(i)) {
                        if (!array[i - offSet].getLB().contains(j)) {
                            constructiveDisjunction.remove(j);
                            break;
                        }
                    }
                }
                for (int cd = constructiveDisjunction.size() - 1; cd >= 0; cd--) {
                    int j = constructiveDisjunction.get(cd);
                    set.force(j, this);
                }
            }
            if (!set.isInstantiated()) {// from env
                ISetIterator iter = set.getUB().iterator();
                while (iter.hasNext()){
                    int j = iter.nextInt();
                    boolean valueExists = false;
                    for (int i = index.getLB(); i <= ub; i = index.nextValue(i)) {
                        if (array[i - offSet].getUB().contains(j)) {
                            valueExists = true;
                            break;
                        }
                    }
                    if (!valueExists) {
                        set.remove(j, this);
                    }
                }
            }
        }
    }

    private void setEq(SetVar s1, SetVar s2) throws ContradictionException {
        ISetIterator iter = s2.getLB().iterator();
        while (iter.hasNext()){
            s1.force(iter.nextInt(), this);
        }
        iter = s1.getUB().iterator();
        while (iter.hasNext()){
            int j = iter.nextInt();
            if (!s2.getUB().contains(j)) {
                s1.remove(j, this);
            }
        }
    }

    private boolean disjoint(SetVar s1, SetVar s2) {
        ISetIterator iter = s2.getLB().iterator();
        while (iter.hasNext()){
            if (!s1.getUB().contains(iter.nextInt())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ESat isEntailed() {
        if (index.isInstantiated()) {
            int i = index.getValue() - offSet;
            if (i < 0 || i >= array.length || disjoint(set, array[i]) || disjoint(array[i], set)) {
                return ESat.FALSE;
            } else {
                if (set.isInstantiated() && array[i].isInstantiated()) {
                    return ESat.TRUE;
                } else {
                    return ESat.UNDEFINED;
                }
            }
        }
        return ESat.UNDEFINED;
    }

}
