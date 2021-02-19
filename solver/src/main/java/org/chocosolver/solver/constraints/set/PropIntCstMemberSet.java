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
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;

/**
 * Propagator for Member constraint: int cst is in set
 *
 * @author Jean-Guillaume Fages
 */
public class PropIntCstMemberSet extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int cst;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for Member constraint: cst is in setVar
     *
     * @param setVar a set variable
     * @param cst a constant (int)
     */
    public PropIntCstMemberSet(SetVar setVar, int cst) {
        super(new SetVar[]{setVar}, PropagatorPriority.UNARY, false);
        this.cst = cst;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
		vars[0].force(cst,this);
    }

    @Override
    public ESat isEntailed() {
		if(vars[0].getLB().contains(cst)){
			return ESat.TRUE;
		}else if(vars[0].getUB().contains(cst)){
			return ESat.UNDEFINED;
		}else{
			return ESat.FALSE;
		}
    }
}
