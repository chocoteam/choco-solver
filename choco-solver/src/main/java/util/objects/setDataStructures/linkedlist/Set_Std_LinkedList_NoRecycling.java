/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package util.objects.setDataStructures.linkedlist;

import memory.IEnvironment;
import memory.structure.Operation;

/**
 * Backtrable LinkedList of m elements
 * add : O(1)
 * testPresence: O(m)
 * remove: O(m)
 * iteration : O(m)
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages, chameau
 * Date: 10 fevr. 2011
 */
public class Set_Std_LinkedList_NoRecycling extends Set_LinkedList {

	final IEnvironment environment;
	protected final static boolean ADD = true;
	protected final static boolean REMOVE = false;

	public Set_Std_LinkedList_NoRecycling(IEnvironment environment) {
		super();
		this.environment = environment;
	}

	@Override
	public boolean add(int element) {
		this._add(element);
		makeOperation(element, REMOVE);
		return true;
	}

	protected void _add(int element) {
		super.add(element);
	}

	@Override
	public boolean remove(int element) {
		boolean done = this._remove(element);
		if (done) {
			makeOperation(element, ADD);
		}
		return done;
	}

	protected boolean _remove(int element) {
		return super.remove(element);
	}

	@Override
	public void clear() {
		for (int i = getFirstElement(); i >= 0; i = getNextElement()) {
			makeOperation(i, ADD);
		}
		super.clear();
	}

	protected void makeOperation(int element, boolean addOrRem){
		if(environment.getWorldIndex()>0) {
			new ListOP(element, addOrRem);
		}
	}

	protected void free(ListOP op){}

	//***********************************************************************************
	// TRAILING OPERATIONS
	//***********************************************************************************

	protected class ListOP extends Operation {
		int element;
		boolean addOrRemove;

		public ListOP(int i, boolean add) {
			super();
			set(i, add);
		}

		@Override
		public void undo() {
			if (addOrRemove) {
				_add(element);
			} else {
				_remove(element);
			}
			free(this);
		}

		public void set(int i, boolean add) {
			element = i;
			addOrRemove = add;
			environment.save(this);
		}
	}
}