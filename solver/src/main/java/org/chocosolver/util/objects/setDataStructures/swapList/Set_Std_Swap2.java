/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2026, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.swapList;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;

/**
 * Set of integers based on BipartiteSet implementation for small sets (arraylist inside)
 * BEWARE : CANNOT BOTH ADD AND REMOVE ELEMENTS DURING SEARCH
 *
 * @author : Charles Prud'homme, Jean-Guillaume FAGES (fix remove)
 */
public class Set_Std_Swap2 extends Set_Swap2 {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

    private final IStateInt size;

    //***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	/**
	 * Creates an empty bipartite set
	 * @param e backtracking environment
	 */
	public Set_Std_Swap2(IEnvironment e){
        super();
		size = e.makeInt(0);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

    @Override
    public int size() {
        return size.get();
    }

    @Override
    protected void setSize(int s) {
        size.set(s);
    }

    @Override
    protected void addSize(int delta) {
        size.add(delta);
    }
}
