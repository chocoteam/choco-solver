/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures;

import org.chocosolver.memory.IEnvironment;

/**
 * Generic backtrable set for trailing
 *
 * @author Jean-Guillaume Fages
 * @since Nov 2012
 */
public class StdSet implements ISet {

    //***********************************************************************************
	// VARIABLES
	//***********************************************************************************

    // trailing
    private final IEnvironment environment;
    // set (decorator design pattern)
    private ISet set;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

    public StdSet(IEnvironment environment, ISet set) {
        super();
        this.environment = environment;
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
        if (set.add(element)) {
            environment.save(()->set.remove(element));
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(int element) {
        if (set.remove(element)) {
            environment.save(()->set.add(element));
            return true;
        }
        return false;
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
        ISetIterator iter = iterator();
        while (iter.hasNext()) {
            environment.save(()->set.add(iter.nextInt()));
        }
        set.clear();
    }

    @Override
   	public int min() {
   		return set.min();
   	}

   	@Override
   	public int max() {
   		return set.max();
   	}

    @Override
    public String toString() {
        return set.toString();
    }

    @Override
	public SetType getSetType(){
		return set.getSetType();
	}
}
