/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary.element;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import java.util.Arrays;
import java.util.Comparator;

/**
 * VALUE = TABLE[INDEX-OFFSET], ensuring bound consistency on result and arc consistency on index.
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 23/04/26
 */
public class PropElementBound extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private final int[] table;
	private final IntVar index;
	private final IntVar result;
	private final int offset;

	/**
	 * sortedEntries[k][0] = table[k] (value)
	 * sortedEntries[k][1] = k        (original index)
	 * Sorted in ascending order
	 */
	private final int[][] sortedEntries;
	private final IStateInt minIndex;
	private final IStateInt maxIndex;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	/**
	 * Create a constraint ensuring that index = table[index-offset]
	 *
	 * @param result integer bounded variable
	 * @param table  int array
	 * @param index  integer enumerated variable
	 * @param offset facultative offset
	 */
	public PropElementBound(IntVar result, int[] table, IntVar index, int offset) {
		super(new IntVar[]{index, result}, PropagatorPriority.BINARY, false);
		this.table = table;
		this.index = index;
		this.result = result;
		this.offset = offset;

		if (!index.hasEnumeratedDomain()) {
			throw new IllegalArgumentException("PropElementBound only accepts enumerated index variables");
			// the result variable is recommended to be bounded but it is not mandatory
		}

		int n = table.length;
		this.minIndex = index.getModel().getEnvironment().makeInt(0);
		this.maxIndex = index.getModel().getEnvironment().makeInt(n - 1);
		this.sortedEntries = new int[n][2];
		for (int i = 0; i < n; i++) {
			sortedEntries[i][0] = table[i];
			sortedEntries[i][1] = i;
		}
		Arrays.sort(sortedEntries, Comparator.comparingInt(a -> a[0]));
	}

	//***********************************************************************************
	// FILTERING
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		index.updateBounds(offset, table.length - 1 + offset, this);
		if (index.isInstantiated()) {
			result.instantiateTo(table[index.getValue() - offset], this);
		} else {
			filterFromMin();
			filterFromMax();
		}
	}

	private void filterFromMin() throws ContradictionException {
		int minIdx = minIndex.get();
		int maxIdx = maxIndex.get();
		int minValue = result.getLB();
		for (int i = minIdx; i <= maxIdx; i++) {
			if (index.contains(sortedEntries[i][1] + offset) && result.contains(sortedEntries[i][0])) {
				minValue = sortedEntries[i][0];
				minIdx = i;
				break;
			} else {
				index.removeValue(sortedEntries[i][1] + offset, this);
			}
		}
		result.updateLowerBound(minValue, this);
		minIndex.set(minIdx);
	}

	private void filterFromMax() throws ContradictionException {
		int minIdx = minIndex.get();
		int maxIdx = maxIndex.get();
		int maxValue = result.getUB();
		for (int i = maxIdx; i >= minIdx; i--) {
			if (index.contains(sortedEntries[i][1] + offset) && result.contains(sortedEntries[i][0])) {
				maxValue = sortedEntries[i][0];
				maxIdx = i;
				break;
			} else {
				index.removeValue(sortedEntries[i][1] + offset, this);
			}
		}
		result.updateUpperBound(maxValue, this);
		maxIndex.set(maxIdx);
	}

	//***********************************************************************************
	// OTHER
	//***********************************************************************************

	@Override
	public ESat isEntailed() {
		if (index.isInstantiated() && result.isInstantiated()) {
			int ti = index.getValue() - offset;
			if (ti < 0 || ti >= table.length) {
				return ESat.FALSE;
			}
			return ESat.eval(table[ti] == result.getValue());
		}
		return ESat.UNDEFINED;
	}

	@Override
	public String toString() {
		return "elementBound(" + this.result + " = <...> [" + this.index + "])";
	}
}