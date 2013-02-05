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
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.setDataStructures.ISet;
import choco.kernel.memory.setDataStructures.SetFactory;
import choco.kernel.memory.setDataStructures.SetType;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.SetVar;
import solver.variables.delta.monitor.SetDeltaMonitor;

public class PropIntersection extends Propagator<SetVar>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int k;
	private SetDeltaMonitor[] sdm;
	private IntProcedure intersectionForced, intersectionRemoved, setForced, setRemoved;
	private ISet interRemToTreat,setAddToTreat;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropIntersection(SetVar[] sets, SetVar intersection, Solver solver, Constraint<SetVar, Propagator<SetVar>> c) {
		super(ArrayUtils.append(sets, new SetVar[]{intersection}), solver, c, PropagatorPriority.LINEAR);
		k = sets.length;
		sdm = new SetDeltaMonitor[k+1];
		for(int i=0;i<=k;i++){
			sdm[i] = this.vars[i].monitorDelta(this);
		}
		interRemToTreat = SetFactory.makeStoredSet(SetType.LINKED_LIST,0,environment);
		setAddToTreat = SetFactory.makeStoredSet(SetType.LINKED_LIST,0,environment);
		// PROCEDURES
		intersectionForced = new IntProcedure() {
			@Override
			public void execute(int element) throws ContradictionException {
				for(int i=0;i<k;i++){
					vars[i].addToKernel(element,aCause);
				}
			}
		};
		intersectionRemoved = new IntProcedure() {
			@Override
			public void execute(int element) throws ContradictionException {
				interRemToTreat.add(element);
			}
		};
		setForced = new IntProcedure() {
			@Override
			public void execute(int element) throws ContradictionException {
				if(!setAddToTreat.contain(element))
					setAddToTreat.add(element);
			}
		};
		setRemoved = new IntProcedure() {
			@Override
			public void execute(int element) throws ContradictionException {
				vars[k].removeFromEnvelope(element,aCause);
			}
		};
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ADD_TO_KER.mask+EventType.REMOVE_FROM_ENVELOPE.mask;
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		ISet set;
		SetVar intersection = vars[k];
		if((evtmask & EventType.FULL_PROPAGATION.mask)!=0){
			set = vars[0].getKernel();
			for(int j=set.getFirstElement(); j>=0; j=set.getNextElement()){
				boolean all = true;
				for(int i=1;i<k;i++){
					if(!vars[i].getKernel().contain(j)){
						all = false;break;
					}
				}
				if(all){
					intersection.addToKernel(j,aCause);
				}
			}
			set = intersection.getEnvelope();
			for(int j=set.getFirstElement(); j>=0; j=set.getNextElement()){
				if(intersection.getKernel().contain(j)){
					for(int i=0;i<k;i++){
						vars[i].addToKernel(j,aCause);
					}
				}else{
					for(int i=0;i<k;i++)
						if(!vars[i].getEnvelope().contain(j)){
							intersection.removeFromEnvelope(j,aCause);
							break;
						}
				}
			}
		}
		set = interRemToTreat;
		for(int j=set.getFirstElement();j>=0;j=set.getNextElement()){
			boolean all = true;
			for(int i=0;i<k;i++)
				if(vars[i].getEnvelope().contain(j)){
					all &= vars[i].getKernel().contain(j);
				}else{
					all = false;
					interRemToTreat.remove(j);
					break;
				}
			if(all){
				contradiction(vars[k],"");
			}
		}
		set = setAddToTreat;
		for(int j=set.getFirstElement();j>=0;j=set.getNextElement()){
			if(intersection.getEnvelope().contain(j) && !intersection.getKernel().contain(j)){
				boolean allKer = true;
				for(int i=0;i<k;i++){
					if(!vars[i].getEnvelope().contain(j)){
						setAddToTreat.remove(j);
						intersection.removeFromEnvelope(j,aCause);
						allKer = false;
						break;
					}
					if(!vars[i].getKernel().contain(j)){
						allKer = false;
						break;
					}
				}
				if(allKer){
					intersection.addToKernel(j,aCause);
					setAddToTreat.remove(j);
				}
			}
		}
		// ------------------
		if((evtmask & EventType.FULL_PROPAGATION.mask)!=0)
			for(int i=0;i<=k;i++)
				sdm[i].unfreeze();
	}

	@Override
	public void propagate(int idxVarInProp, int mask) throws ContradictionException {
		sdm[idxVarInProp].freeze();
		if(idxVarInProp<k){
			sdm[idxVarInProp].forEach(setForced,EventType.ADD_TO_KER);
			sdm[idxVarInProp].forEach(setRemoved,EventType.REMOVE_FROM_ENVELOPE);
		}else{
			sdm[idxVarInProp].forEach(intersectionForced,EventType.ADD_TO_KER);
			sdm[idxVarInProp].forEach(intersectionRemoved,EventType.REMOVE_FROM_ENVELOPE);
		}
		sdm[idxVarInProp].unfreeze();
		forcePropagate(EventType.CUSTOM_PROPAGATION);
	}

	@Override
	public ESat isEntailed() {
		ISet set;
		set = vars[k].getKernel();
		for(int j=set.getFirstElement();j>=0;j=set.getNextElement())
			for(int i=0;i<k;i++)
				if(!vars[i].getEnvelope().contain(j))
					return ESat.FALSE;
		set = vars[0].getKernel();
		for(int j=set.getFirstElement();j>=0;j=set.getNextElement()){
			if(!vars[k].getEnvelope().contain(j)){
				boolean all = true;
				for(int i=1;i<k;i++){
					if(!vars[i].getKernel().contain(j)){
						all = false;
						break;
					}
				}
				if(all){
					return ESat.FALSE;
				}
			}
		}
		if(isCompletelyInstantiated())return ESat.TRUE;
		return ESat.UNDEFINED;
	}
}
