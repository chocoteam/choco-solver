/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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




