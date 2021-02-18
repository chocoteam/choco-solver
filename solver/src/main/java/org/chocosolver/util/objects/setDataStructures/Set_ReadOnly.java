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

/**
 * Generic read-only view of a set
 *
 * @author Jean-Guillaume Fages
 * @since 2016
 */
public class Set_ReadOnly implements ISet {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

    private ISet set;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

    public Set_ReadOnly(ISet set) {
        super();
        this.set = set;
    }

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

    @Override
    public ISetIterator newIterator() {
        return set.newIterator();
    }

	@Override
    public ISetIterator iterator(){
		return set.iterator();
	}

    @Override
    public boolean add(int element) {
        throw new UnsupportedOperationException("this set is read-only");
    }

    @Override
    public boolean remove(int element) {
        throw new UnsupportedOperationException("this set is read-only");
    }

    @Override
    public boolean contains(int element) {
        return set.contains(element);
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public void clear() {
		throw new UnsupportedOperationException("this set is read-only");
    }

    @Override
    public String toString() {
        return set.toString();
    }

	@Override
	public SetType getSetType(){
		return set.getSetType();
	}

	@Override
	public int min() {
		return set.min();
	}

	@Override
	public int max() {
		return set.max();
	}
}
