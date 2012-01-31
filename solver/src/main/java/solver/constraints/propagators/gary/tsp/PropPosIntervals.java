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
 * Date: 03/10/11
 * Time: 19:56
 */

package solver.constraints.propagators.gary.tsp;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 */
public class PropPosIntervals extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	int n;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropPosIntervals(IntVar[] vars, Constraint constraint, Solver solver) {
		super(vars, solver, constraint, PropagatorPriority.BINARY,true);
		this.n = vars.length;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int i=0;i<n;i++){
			for(int j=i+1;j<n;j++){
				check(i,j);
			}
		}
	}

	private void check(int i, int j) throws ContradictionException {
		int first = Math.min(vars[i].getLB(),vars[j].getLB());
		int last  = Math.max(vars[i].getUB(),vars[j].getUB())+1;
		if(first>last){
			throw new UnsupportedOperationException();
		}
		int q = 0;
		int tot = 0;
		for(int k=0;k<n;k++){
			tot ++;
			if(vars[k].getLB()>=first && vars[k].getUB()+1<=last){
				q++;
			}
		}

		if(tot>vars[n-1].getUB()+1){
			contradiction(vars[n-1],"");
		}
		int mandFirst = last-q;
		int mandLast  = first+q;
//		System.out.println(first+" + "+q+" -> "+last);
		if(mandFirst<mandLast){
			for(int k=0;k<n;k++){
				if(vars[k].getLB()<first || vars[k].getUB()+1>last){
//					checkEnergy(k,mandFirst,mandLast);
					checkEnergy(k,first,last,q,mandFirst,mandLast);
				}
			}
		}
	}

	private void checkEnergy(int i, int first, int last , int dur, int mandFirst , int mandLast) throws ContradictionException {
		// can be done after the box
		boolean right= vars[i].getUB()>=mandLast;
		// cannot be done inside the box
		if(1>last-first-dur){
			// can be done before the box
			if(vars[i].getLB()+1<=mandFirst){
				if(!right){
					vars[i].updateUpperBound(mandFirst-1,this);
				}
			}
			// cannot be done before the box
			else{
				if(right){
						vars[i].updateLowerBound(mandLast, this);
				}else{
					contradiction(vars[i],"sweep");
				}
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		forcePropagate(EventType.FULL_PROPAGATION);
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.FULL_PROPAGATION.mask+EventType.INSTANTIATE.mask+EventType.DECUPP.mask+EventType.INCLOW.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}
}
