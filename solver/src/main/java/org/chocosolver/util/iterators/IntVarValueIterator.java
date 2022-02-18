/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.iterators;

import java.util.NoSuchElementException;
import org.chocosolver.solver.variables.IntVar;

import java.util.Iterator;

/**
 * Object to iterate over an IntVar values using
 * <code>
 *     for(int value:var){
 *         ...
 *     }
 * </code>
 * that is equivalent to
 * <code>
 *     int ub = var.getUB();
 *     for(int value = var.getLB(); values <= ub; value = var.nextValue(value)){
 *         ...
 *     }
 * </code>
 *
 * @author Jean-Guillaume Fages
 */
public class IntVarValueIterator implements Iterator<Integer> {

	/**
	 * Variable to iterate on
	 */
	private final IntVar var;
    /**
     * current returned value
     */
	private int value;
    /**
     * upper bound of {@link #var}
     */
	private int ub;

	/**
	 * Creates an object to iterate over an IntVar values using
	 * <code>
	 *     for(int value:var){
	 *         ...
	 *     }
	 * </code>
	 * that is equivalent to
	 * <code>
	 *     int ub = var.getUB();
	 *     for(int value = var.getLB(); values <= ub; value = var.nextValue(value)){
	 *         ...
	 *     }
	 * </code>
	 * @param v an integer variables
	 */
	public IntVarValueIterator(IntVar v){
		this.var = v;
	}

	/**
	 * Reset iteration (to avoid creating a new IntVarValueIterator() for every iteration)
	 * Stores the current upper bound
	 */
	public void reset(){
		value = var.getLB()-1;
		ub = var.getUB();
	}

	@Override
	public boolean hasNext() {
		return var.nextValue(value) <= ub;
	}

	@Override
	public Integer next() {
		value = var.nextValue(value);
		if(value > ub) {
			throw new NoSuchElementException("IntVarValueIterator for IntVar "+var+" has no more element");
		}
		return value;
	}
}