/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package util.objects.setDataStructures;

import memory.structure.Operation;
import memory.trailing.EnvironmentTrailing;
import util.PoolManager;

/**
 * Backtrable set
 *
 * @author Jean-Guillaume Fages
 * @since Nov 2012
 */
public class Set_Trail implements ISet {

    // trailing
    private final EnvironmentTrailing environment;
    private PoolManager<ListOP> operationPoolGC;
    private final static boolean ADD = true;
    private final static boolean REMOVE = false;
    // set (decorator design pattern)
    private ISet set;

    public Set_Trail(EnvironmentTrailing environment, ISet set) {
        super();
        this.environment = environment;
        this.operationPoolGC = new PoolManager<ListOP>();
        this.set = set;
    }

    @Override
    public boolean add(int element) {
        if (set.add(element)) {
            ListOP op = operationPoolGC.getE();
            if (op == null) {
                new ListOP(element, REMOVE);
            } else {
                op.set(element, REMOVE);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(int element) {
        if (set.remove(element)) {
            ListOP op = operationPoolGC.getE();
            if (op == null) {
                new ListOP(element, ADD);
            } else {
                op.set(element, ADD);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean contain(int element) {
        return set.contain(element);
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public int getSize() {
        return set.getSize();
    }

    @Override
    public void clear() {
        for (int i = getFirstElement(); i >= 0; i = getNextElement()) {
            ListOP op = operationPoolGC.getE();
            if (op == null) {
                new ListOP(i, ADD);
            } else {
                op.set(i, ADD);
            }
        }
        set.clear();
    }

    @Override
    public int getFirstElement() {
        return set.getFirstElement();
    }

    @Override
    public int getNextElement() {
        return set.getNextElement();
    }

    @Override
    public String toString() {
        return "set stored by trailing " + set.toString();
    }

    //***********************************************************************************
    // TRAILING OPERATIONS
    //***********************************************************************************

    private class ListOP extends Operation {
        private int element;
        private boolean addOrRemove;

        public ListOP(int i, boolean add) {
            super();
            set(i, add);
        }

        @Override
        public void undo() {
            if (addOrRemove) {
                set.add(element);
            } else {
                set.remove(element);
            }
            operationPoolGC.returnE(this);
        }

        public void set(int i, boolean add) {
            element = i;
            addOrRemove = add;
            environment.save(this);
        }
    }

	@Override
	public SetType getSetType(){
		return set.getSetType();
	}

	@Override
	public int[] toArray() {
		return set.toArray();
	}

	@Override
	public int getMaxSize() {
		return set.getMaxSize();
	}
}