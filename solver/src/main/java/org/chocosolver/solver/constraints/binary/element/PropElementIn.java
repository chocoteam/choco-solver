/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary.element;

import org.chocosolver.memory.IStateBool;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSet;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * VALUE IN TABLE[INDEX-OFFSET], ensuring arc consistency on result and index.
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 23/04/26
 */
@Explained(ignored = true, comment = "Not explained")
public class PropElementIn extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	/**
	 * Table of values
	 */
	private final IntIterableSet[] values;

	/**
	 * Stores whether the index is in the index domain
	 */
	private final IStateBool[] indexInDomain;

	/**
	 * For each position, a support value
	 */
	private final Integer[] watchLiterals;

	/**
	 * Index variable
	 */
	private final IntVar index;

	/**
	 * Resulting variable
	 */
	private final IntVar result;

	/**
	 * Set of forbidden indices
	 */
	private final IntIterableBitSet fidx;

	/**
	 * Set of possible values
	 */
	private final IntIterableBitSet pVals;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	/**
	 * Create a propagator which ensures that VALUE IN TABLE[INDEX] holds.
	 *
	 * @param value  integer variable
	 * @param values array of int sets, those sets must be DISJOINT
	 * @param index  integer variable
	 */
	public PropElementIn(IntVar value, IntIterableSet[] values, IntVar index) {
		super(ArrayUtils.toArray(value, index), PropagatorPriority.LINEAR, true);
		this.values = values;
		this.watchLiterals = new Integer[values.length];
		this.index = index;
		this.result = value;
		this.fidx = new IntIterableBitSet();
		this.fidx.setOffset(index.getLB());
		this.pVals = new IntIterableBitSet();
		this.indexInDomain = new IStateBool[values.length];
		for (int i = 0; i < indexInDomain.length; i++) {
			indexInDomain[i] = index.getModel().getEnvironment().makeBool(true);
		}
		if (!value.hasEnumeratedDomain() || !index.hasEnumeratedDomain()) {
			throw new IllegalArgumentException("PropElementIn only accepts enumerated domains");
		}
	}

	//***********************************************************************************
	// FILTERING
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if (result.isInstantiated()) {
			int nextIndex = -1;
			int value = result.getValue();
			for (int i = index.getLB(); i <= index.getUB(); i = index.nextValue(i)) {
				if (values[i].contains(value)) {
					nextIndex = i;
					break;
				}
			}
			index.instantiateTo(nextIndex, this);
			setPassive();
		} else if (PropagatorEventType.isFullPropagation(evtmask)) {
			firstPropagation();
		} else {
			nextPropagation();
		}
	}

	@Override
	public void propagate(int idxVarInProp, int mask) throws ContradictionException {
		forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
	}

	private void firstPropagation() throws ContradictionException {
		index.updateBounds(0, values.length - 1, this);
		int iub = index.getUB();
		pVals.clear();
		fidx.clear();
		for (int i = index.getLB(); i <= iub; i = index.nextValue(i)) {
			IntIterableSet set = values[i];
			Integer support = getSupport(set, watchLiterals[i]);
			if (support == null) {
				fidx.add(i);
				indexInDomain[i].set(false);
			} else {
				indexInDomain[i].set(true);
				watchLiterals[i] = support;
				pVals.addAll(set);
			}
		}
		result.removeAllValuesBut(pVals, this);
		if (!fidx.isEmpty()) {
			index.removeValues(fidx, this);
		}
		passivate();
	}

	private void nextPropagation() throws ContradictionException {
		fidx.clear();
		for (int i = 0; i < indexInDomain.length; i++) {
			if (indexInDomain[i].get()) {
				IntIterableSet set = values[i];
				if (index.contains(i)) {
					Integer support = getSupport(set, watchLiterals[i]);
					if (support == null) {
						fidx.add(i);
						// sets disjoint, so if the index is wrong, then so are the associated values
						result.removeValues(set, this);
						indexInDomain[i].set(false);
					} else {
						watchLiterals[i] = support;
					}
				} else {
					indexInDomain[i].set(false);
					// sets disjoint, so if the index is wrong, then so are the associated values
					result.removeValues(set, this);
				}
			}
		}
		if (!fidx.isEmpty()) {
			index.removeValues(fidx, this);
		}
		passivate();
	}

	private void passivate() {
		if (index.isInstantiated() && !result.isInstantiated() && result.hasEnumeratedDomain()) {
			setPassive();
		}
	}

	private Integer getSupport(IntIterableSet set, Integer watcher) {
		if (watcher != null && result.contains(watcher)) {
			return watcher;
		}
		if (result.getDomainSize() < set.size()) {
			return getSupportFromResult(set, watcher);
		} else {
			return getSupportFromSet(set, watcher);
		}
	}

	private Integer getSupportFromResult(IntIterableSet set, Integer watcher) {
		int iub = result.getUB();
		if (watcher == null) {
			watcher = result.getLB();
		}
		if (!result.contains(watcher)) {
			watcher = result.nextValue(watcher);
		}
		// starts from watcher
		for (int v = watcher; v <= iub; v = result.nextValue(v)) {
			if (set.contains(v)) {
				return v;
			}
		}
		// if no support has been found, check from left
		for (int v = result.getLB(); v < watcher; v = result.nextValue(v)) {
			if (set.contains(v)) {
				return v;
			}
		}
		return null;
	}

	private Integer getSupportFromSet(IntIterableSet set, Integer watcher) {
		int min = set.min();
		int max = set.max();
		if (watcher == null) {
			watcher = min;
		}
		// starts from watcher
		for (int v = watcher; v <= max; v = set.nextValue(v)) {
			if (result.contains(v)) {
				return v;
			}
		}
		// if no support has been found, check from left
		for (int v = min; v < watcher; v = set.nextValue(v)) {
			if (result.contains(v)) {
				return v;
			}
		}
		return null;
	}

	//***********************************************************************************
	// OTHER
	//***********************************************************************************

	@Override
	public ESat isEntailed() {
		if (index.getUB() < 0 || index.getLB() >= values.length) {
			return ESat.FALSE;
		}
		if (isCompletelyInstantiated()) {
			IntIterableSet set = values[index.getValue()];
			int value = result.getValue();
			return ESat.eval(set.contains(value));
		} else if (result.isInstantiated()) {
			int val = result.getValue();
			int indexUB = Math.min(index.getUB(), values.length - 1);
			for (int i = Math.max(0, index.getLB()); i <= indexUB; i = index.nextValue(i)) {
				if (values[i].contains(val)) {
					return ESat.UNDEFINED;
				}
			}
			return ESat.FALSE;
		} else if (index.isInstantiated()) {
			int idx = index.getValue();
			if (getSupport(values[idx], watchLiterals[idx]) != null) {
				return ESat.UNDEFINED;
			}
			return ESat.FALSE;
		}
		return ESat.UNDEFINED;
	}

	@Override
	public String toString() {
		return "elementIn(" + this.result + " = <{},{}...> [" + this.index + "])";
	}
}
