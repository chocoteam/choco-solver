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

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;

/**
 * Restricts the set var not to be empty
 *
 * @author Jean-Guillaume Fages
 */
public class PropNotEmpty extends Propagator<SetVar> {

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropNotEmpty(SetVar set) {
		super(new SetVar[]{set}, PropagatorPriority.UNARY, false);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int e = vars[0].getUB().size();
		if(e==0){
			fails(); // TODO: could be more precise, for explanation purpose
		}else if(e==1){
			vars[0].force(vars[0].getUB().iterator().next(), this);
		}
		if(vars[0].getLB().size()>0){
			setPassive();
		}
	}

	@Override
	public ESat isEntailed() {
		if(vars[0].getUB().size()==0){
			return ESat.FALSE;
		}
		if(vars[0].getLB().size()>0){
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}

}
