/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.binPacking;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.UnaryIntProcedure;

/**
 * Propagator for a Bin Packing constraint
 * Propagates item/bin allocations to bin loads
 * Reacts to item/bin allocation variables only
 *
 * Should be used together with PropLoadToItem
 *
 * @author Jean-Guillaume Fages
 */
public class PropItemToLoad extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int nbItems, nbAvailableBins, offset;
	private int[] itemSize;
	private IntVar[] binOfItem, binLoad;

	// backtrackable counters
	private IStateInt[] minLoad, maxLoad;

	// structure allowing iteration over removed values since last call
	private IIntDeltaMonitor[] monitors;

	// method to be called for each removed value
	private UnaryIntProcedure<Integer> procedure = new UnaryIntProcedure<Integer>() {
		int item;
		@Override
		public UnaryIntProcedure<Integer> set(Integer itemIdx) {
			item = itemIdx;
			return this;
		}
		@Override
		public void execute(int bin) throws ContradictionException {
			bin -= offset;
			if(bin>=0 && bin <nbAvailableBins) {
				maxLoad[bin].add(-itemSize[item]);
				binLoad[bin].updateUpperBound(maxLoad[bin].get(), PropItemToLoad.this);
			}
		}
	};

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************


	/**
	 * Incremental propagator for a Bin Packing constraint
	 * Propagates item/bin allocations to bin loads
	 *
	 * Should be used together with PropLoadToItem
	 *
	 * @param binOfItem bin of every item (possibly with offset)
	 * @param itemSize size of every item
	 * @param binLoad total load of every bin
	 * @param offset index offset: binOfItem[i] = k means item i is in bin k-offset
	 */
	public PropItemToLoad(IntVar[] binOfItem, int[] itemSize, IntVar[] binLoad, int offset) {
		super(binOfItem, PropagatorPriority.LINEAR, true);
		this.nbItems = binOfItem.length;
		this.nbAvailableBins = binLoad.length;
		this.itemSize = itemSize;
		this.binLoad = binLoad;
		this.binOfItem = binOfItem;
		this.offset = offset;
		monitors = new IIntDeltaMonitor[nbItems];
		for(int i=0; i<nbItems; i++){
			monitors[i] = binOfItem[i].monitorDelta(this);
		}
		minLoad = new IStateInt[nbAvailableBins];
		maxLoad = new IStateInt[nbAvailableBins];
		for(int b=0;b<nbAvailableBins;b++){
			minLoad[b] = model.getEnvironment().makeInt(0);
			maxLoad[b] = model.getEnvironment().makeInt(0);
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		// compute min/max loads according to instantiated/possible allocations
		for(int i=0; i<nbItems; i++){
			if(binOfItem[i].isInstantiated()){
				int val = binOfItem[i].getValue()-offset;
				if(val>=0 && val<nbAvailableBins) {
					minLoad[val].add(itemSize[i]);
					maxLoad[val].add(itemSize[i]);
				}
			}else{
				IntVar vr = binOfItem[i];
				int ub = vr.getUB();
				for(int val=vr.getLB(); val<=ub; val=vr.nextValue(val)){
					if(val>= offset && val<offset+nbAvailableBins) {
						maxLoad[val - offset].add(itemSize[i]);
					}
				}
			}
		}
		for(int b=0; b<nbAvailableBins; b++){
			binLoad[b].updateBounds(minLoad[b].get(), maxLoad[b].get(),this);
		}
		for(int i=0; i<nbItems; i++){
			monitors[i].unfreeze();
		}
	}

	@Override
	public void propagate(int item, int evtmask) throws ContradictionException {
		monitors[item].freeze();
		monitors[item].forEachRemVal(procedure.set(item));
		monitors[item].unfreeze();
		if(binOfItem[item].isInstantiated()){
			int bin = binOfItem[item].getValue()-offset;
			if(bin>=0 && bin <nbAvailableBins) {
				minLoad[bin].add(itemSize[item]);
				binLoad[bin].updateLowerBound(minLoad[bin].get(), this);
			}else{
				fails();
			}
		}
	}

	@Override
	public ESat isEntailed() {
		for(int i=0; i<nbItems; i++){
			if(binOfItem[i].isInstantiated()){
				int val = binOfItem[i].getValue();
				if(val<offset || val>=nbAvailableBins+offset){
					return ESat.FALSE;
				}
			}
		}
		for(int b=0; b<nbAvailableBins; b++){
			int min = 0;
			int max = 0;
			for(int i=0; i<nbItems; i++){
				if(binOfItem[i].contains(b+offset)){
					max += itemSize[i];
					if(binOfItem[i].isInstantiated()){
						min += itemSize[i];
					}
				}
			}
			if( min > binLoad[b].getUB() || max < binLoad[b].getLB()){
				return ESat.FALSE;
			}
		}
		if(isCompletelyInstantiated()){
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}
}
