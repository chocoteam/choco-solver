/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.structure.IOperation;
import org.chocosolver.solver.Model;
import org.chocosolver.util.PoolManager;

/**
 * Generic backtrable set for trailing
 *
 * @author Jean-Guillaume Fages
 * @since Nov 2012
 */
public class StdSet extends AbstractSet {

    private static final String HK_LIST_OP_PM = "HK_LIST_OP_PM";
    //***********************************************************************************
	// VARIABLES
	//***********************************************************************************

    // trailing
    private final IEnvironment environment;
    private PoolManager<ListOP> operationPoolGC;
    private final static boolean ADD = true;
    private final static boolean REMOVE = false;
    // set (decorator design pattern)
    private final ISet set;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

    public StdSet(Model model, ISet set) {
        super();
        this.environment = model.getEnvironment();
        //noinspection unchecked
        this.operationPoolGC = (PoolManager<ListOP>) model.getHook(HK_LIST_OP_PM);
        if(this.operationPoolGC == null){
            this.operationPoolGC = new PoolManager<>();
            model.addHook(HK_LIST_OP_PM, this.operationPoolGC);
        }
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
            ListOP op = operationPoolGC.getE();
            if (op == null) {
                op = new ListOP();
            }
            op.set(set, element, REMOVE);
            notifyObservingElementAdded(element);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(int element) {
        if (set.remove(element)) {
            ListOP op = operationPoolGC.getE();
            if (op == null) {
                op = new ListOP();
            }
            op.set(set, element, ADD);
            notifyObservingElementRemoved(element);
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
            ListOP op = operationPoolGC.getE();
            if (op == null) {
                op = new ListOP();
            }
            op.set(set, iter.nextInt(), ADD);
        }
        set.clear();
        notifyObservingCleared();
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

    //***********************************************************************************
    // TRAILING OPERATIONS
    //***********************************************************************************

    private class ListOP implements IOperation {
        private int element;
        private boolean addOrRemove;
        private ISet mset;

        @Override
        public void undo() {
            if (addOrRemove) {
                this.mset.add(element);
            } else {
                this.mset.remove(element);
            }
            operationPoolGC.returnE(this);
        }

        public void set(ISet set, int i, boolean add) {
            this.mset = set;
            this.element = i;
            this.addOrRemove = add;
            environment.save(this);
        }
    }

    @Override
	public SetType getSetType(){
		return set.getSetType();
	}
}
