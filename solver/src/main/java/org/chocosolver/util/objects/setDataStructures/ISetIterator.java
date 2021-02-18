/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Object to iterate over an ISet values using
 * <code>
 *     // more readable but includes autoboxing
 *     for(int value:set){
 *         ...
 *     }
 *
 *     // more verbose but without autoboxing
 *     ISetIterator iter = set.primitiveIterator();
 *     while(iter.hasNext()){
 *         int k = iter.nextInt();
 *         ...
 *     }
 * </code>
 *
 * @author Jean-Guillaume Fages
 */
public interface ISetIterator extends Iterator<Integer> {

	/**
	 * Reset iteration (to avoid creating a new ISetIterator for every iteration)
	 */
	void reset();

	/**
	 * Inform the iterator that value <code>item</code> has been removed
	 * (may require to update iterator structure)
	 * @param item removed value
	 */
	default void notifyRemoving(int item){
		// nothing to do by default
	}

	/**
	 * Returns the next int in the iteration.
	 *
	 * Beware : avoids autoboxing
	 *
	 * @return the next int in the iteration
	 * @throws NoSuchElementException if the iteration has no more elements
	 */
	int nextInt();

	@Override
	default Integer next() {
		return nextInt();
	}
}
