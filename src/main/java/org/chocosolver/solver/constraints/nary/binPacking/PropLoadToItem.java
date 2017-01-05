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
import org.chocosolver.util.ESat;


/**
 * Incremental propagator for a Bin Packing constraint
 * Propagates bin loads to item/bin allocations
 * Reacts to load modifications only
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
		super(binLoad, PropagatorPriority.LINEAR, true);
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
		for (int b = 0; b < nbAvailableBins; b++) {
			propagate(b, 0);
		}
	}

	@Override
	public void propagate(int bin, int evtmask) throws ContradictionException {
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
			}
		}
	}

	@Override
	public ESat isEntailed() {
		// same checker as PropItemToLoad
		// no need to implement it twice
		return ESat.TRUE;
	}
}




