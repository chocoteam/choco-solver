/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
 * A propagator ensuring that |set| = card
 *
 * @author Jean-Guillaume Fages
 */
public class PropCardinality extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final IntVar card;
    private final SetVar set;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator ensuring that |setVar| = cardinality
     *
     * @param setVar a set variables
     * @param cardinality an integer variable
     */
    public PropCardinality(SetVar setVar, IntVar cardinality) {
        super(new Variable[]{setVar, cardinality}, PropagatorPriority.BINARY, false);
        this.set = (SetVar) vars[0];
        this.card = (IntVar) vars[1];
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
        int k = set.getLB().size();
        card.updateLowerBound(k, this);
        int e = set.getUB().size();
        card.updateUpperBound(e, this);
        if (card.isInstantiated()) {
            int c = card.getValue();
            if (c == k) {
                ISetIterator iter = set.getUB().iterator();
                while (iter.hasNext()){
                    int j = iter.nextInt();
                    if (!set.getLB().contains(j)) {
                        set.remove(j, this);
                    }
                }
            } else if (c == e) {
                ISetIterator iter = set.getUB().iterator();
                while (iter.hasNext()){
                    set.force(iter.nextInt(), this);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int k = set.getLB().size();
        int e = set.getUB().size();
        if (k > card.getUB() || e < card.getLB()) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
