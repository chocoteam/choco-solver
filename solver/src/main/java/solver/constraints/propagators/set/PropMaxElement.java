/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
 */

package solver.constraints.propagators.set;

import choco.kernel.ESat;
import choco.kernel.memory.setDataStructures.ISet;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.*;

/**
 * Retrieves the maximum element of the set
 * MAXIMUM_ELEMENT_OF(set) = max
 * @author Jean-Guillaume Fages
 */
public class PropMaxElement extends Propagator<Variable>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private IntVar max;
	private SetVar set;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Retrieves the maximum element of the set
	 * MAXIMUM_ELEMENT_OF(setVar) = max
	 * @param setVar
	 * @param max
	 * @param solver
	 * @param c
	 */
	public PropMaxElement(SetVar setVar, IntVar max, Solver solver, Constraint c) {
		super(new Variable[]{setVar,max}, solver, c, PropagatorPriority.BINARY);
		this.max = max;
		this.set = setVar;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ADD_TO_KER.mask+EventType.REMOVE_FROM_ENVELOPE.mask
				+EventType.INSTANTIATE.mask+EventType.DECUPP.mask+EventType.INCLOW.mask;
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		ISet tmp = set.getKernel();
		for(int j=tmp.getFirstElement();j>=0;j=tmp.getNextElement()){
			max.updateLowerBound(j,aCause);
		}
		tmp = set.getEnvelope();
		int maxVal = tmp.getFirstElement();
		int ub = max.getUB();
		for(int j=maxVal;j>=0;j=tmp.getNextElement()){
			if(j>ub){
				set.removeFromEnvelope(j,aCause);
			}else{
				if(maxVal<j){
					maxVal = j;
				}
			}
		}
		max.updateUpperBound(maxVal,aCause);
	}

	@Override
	public void propagate(int i, int mask) throws ContradictionException {
		propagate(0);
	}

	@Override
	public ESat isEntailed() {
		int lb = max.getLB();
		int ub = max.getUB();
		ISet tmp = set.getKernel();
		for(int j=tmp.getFirstElement();j>=0;j=tmp.getNextElement()){
			if(j>ub){
				return ESat.FALSE;
			}
		}
		tmp = set.getEnvelope();
		int maxVal = tmp.getFirstElement();
		for(int j=maxVal;j>=0;j=tmp.getNextElement()){
			if(maxVal<j){
				maxVal = j;
			}
		}
		if(maxVal<lb){
			return ESat.FALSE;
		}
		if(isCompletelyInstantiated()){
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}
}
