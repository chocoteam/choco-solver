/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.util.objects.setDataStructures;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.structure.IOperation;
import org.chocosolver.util.PoolManager;

import java.util.Iterator;

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
    private PoolManager<ListOP> operationPoolGC;
    private final static boolean ADD = true;
    private final static boolean REMOVE = false;
    // set (decorator design pattern)
    private ISet set;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

    public StdSet(IEnvironment environment, ISet set) {
        super();
        this.environment = environment;
        this.operationPoolGC = new PoolManager<>();
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
	public Iterator<Integer> iterator(){
		return set.iterator();
	}

    @Override
    public boolean add(int element) {
        if (set.add(element)) {
            ListOP op = operationPoolGC.getE();
            if (op == null) {
                op = new ListOP();
            }
            op.set(element, REMOVE);
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
            op.set(element, ADD);
            return true;
        }
        return false;
    }

    @Override
    public boolean contain(int element) {
        return set.contain(element);
    }

    @Override
    public int getSize() {
        return set.getSize();
    }

    @Override
    public void clear() {
        for (int i :set) {
            ListOP op = operationPoolGC.getE();
            if (op == null) {
                op = new ListOP();
            }
            op.set(i, ADD);
        }
        set.clear();
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
}
