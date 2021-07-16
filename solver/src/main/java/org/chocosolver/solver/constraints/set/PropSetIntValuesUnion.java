/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Maintain a link between a set variable and the union of values taken by an array of
 * integer variables
 *
 * Not idempotent (use two of them)
 *
 * @author Jean-Guillaume Fages
 */
public class PropSetIntValuesUnion extends Propagator<Variable> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private IntVar[] X;
	private SetVar values;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropSetIntValuesUnion(IntVar[] X, SetVar values){
		super(ArrayUtils.append(X,new Variable[]{values}));
		this.X = X;
		this.values = values;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		ISetIterator iter = values.getUB().iterator();
		while (iter.hasNext()){
			int v = iter.nextInt();
			int support = -1;
			for(int i=0;i<X.length;i++){
				if(X[i].contains(v)){
					if(support==-1){
						support = i;
					}else{
						support = -2;
						break;
					}
				}
			}
			if(support == -1){
				values.remove(v, this);
			}else if(support!=-2 && values.getLB().contains(v)){
				X[support].instantiateTo(v, this);
			}
		}
		for(int i=0;i<X.length;i++){
			if(X[i].isInstantiated()){
				values.force(X[i].getValue(), this);
			}else {
				for (int v = X[i].getLB(); v <= X[i].getUB(); v = X[i].nextValue(v)) {
					if (!values.getUB().contains(v)) {
						X[i].removeValue(v, this);
					}
				}
			}
		}
	}

	@Override
	public ESat isEntailed() {
		ISetIterator iter = values.getLB().iterator();
		while (iter.hasNext()){
			int v = iter.nextInt();
			int support = -1;
			for(int i=0;i<X.length;i++){
				if(X[i].contains(v)){
					if(support==-1){
						support = i;
					}else{
						support = -2;
						break;
					}
				}
			}
			if(support == -1){
				return ESat.FALSE;
			}
		}
		for(IntVar x:X){
			if(x.isInstantiated() && !values.getUB().contains(x.getValue())){
				return ESat.FALSE;
			}
		}
		if(isCompletelyInstantiated()){
			return ESat.TRUE;
		}return ESat.UNDEFINED;
	}
}
