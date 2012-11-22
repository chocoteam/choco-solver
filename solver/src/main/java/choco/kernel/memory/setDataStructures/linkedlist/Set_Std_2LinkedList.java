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

package choco.kernel.memory.setDataStructures.linkedlist;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.structure.Operation;

import java.util.LinkedList;

/**
 * Backtrable linked list of m elements with double link (predecessor and successor)
 * add : O(1)
 * testPresence: O(m)
 * remove: O(m)
 * Enable deletion of the current item in O(1) (except for the last one)
 * iteration : O(m)
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 17/11/2011
 */
public class Set_Std_2LinkedList extends Set_2LinkedList {

	final IEnvironment environment;
	public final static boolean ADD = true;
	public final static boolean REMOVE = false;
	LinkedList<ListOP> operationPoolGC;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	public Set_Std_2LinkedList(IEnvironment environment) {
		super();
		this.environment = environment;
		operationPoolGC  = new LinkedList<ListOP>();
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean add(int element) {
		this._add(element);
		if(operationPoolGC.isEmpty()){
			new ListOP(element, REMOVE);
		}else{
			ListOP op = operationPoolGC.removeFirst();
			op.set(element,REMOVE);
		}
		return true;
	}

	protected void _add(int element) {
		super.add(element);
	}

	@Override
	public boolean remove(int element) {
		boolean done = this._remove(element);
		if (done) {
			if(operationPoolGC.isEmpty()){
				new ListOP(element, ADD);
			}else{
				ListOP op = operationPoolGC.removeFirst();
				op.set(element,ADD);
			}
		}
		return done;
	}

	protected boolean _remove(int element) {
		return super.remove(element);
	}

	@Override
	public void clear() {
		for(int i=getFirstElement(); i>=0; i=getNextElement()){
			if(operationPoolGC.isEmpty()){
				new ListOP(i, ADD);
			}else{
				ListOP op = operationPoolGC.removeFirst();
				op.set(i,ADD);
			}
		}
		super.clear();
	}

	//***********************************************************************************
	// TRAILING OPERATIONS
	//***********************************************************************************

	private class ListOP extends Operation{
		int element;
		boolean addOrRemove;
		public ListOP(int i,boolean add) {
			super();
			set(i, add);
		}
		@Override
		public void undo() {
			if(addOrRemove){
				_add(element);
			}else{
				_remove(element);
			}
			operationPoolGC.add(this);
		}
		public void set(int i,boolean add){
			element = i;
			addOrRemove = add;
			environment.save(this);
		}
	}
}