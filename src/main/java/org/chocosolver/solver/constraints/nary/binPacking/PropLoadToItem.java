/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.binPacking;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import java.util.BitSet;


/**
 * Incremental propagator for a Bin Packing constraint
 * Propagates bin loads to item/bin allocations
 * Reacts to load modifications AND allocation assignments
 *
 * Should be used together with PropItemToLoad
 * @author Jean-Guillaume Fages
 */
public class PropLoadToItem extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private final int nbItems, nbAvailableBins, offset;
	private final int[] itemSize;
	private final IntVar[] binOfItem, binLoad;
	private BitSet binToProcess = new BitSet();

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Incremental propagator for a Bin Packing constraint
	 * Propagates bin loads to item/bin allocations
	 *
	 * Should be used together with PropItemToLoad
	 *
	 * @param binOfItem bin of every item (possibly with offset)
	 * @param itemSize size of every item
	 * @param binLoad total load of every bin
	 * @param offset index offset: binOfItem[i] = k means item i is in bin k-offset
	 */
	public PropLoadToItem(IntVar[] binOfItem, int[] itemSize, IntVar[] binLoad, int offset) {
		super(ArrayUtils.append(binLoad,binOfItem), PropagatorPriority.LINEAR, true);
		this.nbItems = binOfItem.length;
		this.nbAvailableBins = binLoad.length;
		this.itemSize = itemSize;
		this.binLoad = binLoad;
		this.binOfItem = binOfItem;
		this.offset = offset;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for (int i = 0; i < nbItems; i++) {
			binOfItem[i].updateBounds(offset,offset+nbAvailableBins-1,this);
		}
		binToProcess.set(0,nbAvailableBins);
		while (binToProcess.nextSetBit(0)>=0){
			processBin(binToProcess.nextSetBit(0));
		}
	}

	@Override
	public void propagate(int vIdx, int evtmask) throws ContradictionException {
		int bin = vIdx<nbAvailableBins?vIdx:vars[vIdx].getValue()-offset;
		binToProcess.clear();
		binToProcess.set(bin);
		while (binToProcess.nextSetBit(0)>=0){
			processBin(binToProcess.nextSetBit(0));
		}
	}

	private void processBin(int bin) throws ContradictionException {
		binToProcess.clear(bin);
		int minLoad = 0;
		for (int i = 0; i < nbItems; i++) {
			if (binOfItem[i].isInstantiatedTo(bin + offset)) {
				minLoad += itemSize[i];
			}
		}
		binLoad[bin].updateLowerBound(minLoad, PropLoadToItem.this);
		int maxLoad = binLoad[bin].getUB();
		for (int i = 0; i < nbItems; i++) {
			if (minLoad + itemSize[i] > maxLoad && !binOfItem[i].isInstantiated()) {
				binOfItem[i].removeValue(bin + offset, this);
				if(binOfItem[i].isInstantiated()){
					binToProcess.set(binOfItem[i].getValue() - offset);
				}
			}
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		if(vIdx<nbAvailableBins)return IntEventType.boundAndInst();
		return IntEventType.instantiation();
	}

	@Override
	public ESat isEntailed() {
		// same checker as PropItemToLoad, no need to implement it twice
		return ESat.TRUE;
	}
}