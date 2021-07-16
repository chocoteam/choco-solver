/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.swapList;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;

/**
 * Set of integers based on BipartiteSet implementation
 * BEWARE : CANNOT BOTH ADD AND REMOVE ELEMENTS DURING SEARCH
 * (add only or remove only)
 *
 * add : O(1)
 * testPresence: O(1)
 * remove: O(1)
 * iteration : O(m)
 *
 * @author : Jean-Guillaume Fages
 */
public class Set_Std_Swap extends Set_Swap {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

    protected IStateInt size;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	/**
	 * Creates an empty bipartite set having numbers greater or equal than <code>offSet</code> (possibly < 0)
	 * @param e backtracking environment
	 * @param offSet smallest allowed value in this set (possibly < 0)
	 */
	public Set_Std_Swap(IEnvironment e, int offSet){
		super(offSet);
		size = e.makeInt(0);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

    @Override
    public int size() {
        return size.get();
    }

    protected void setSize(int s) {
        size.set(s);
    }

    protected void addSize(int delta) {
        size.add(delta);
    }
}
