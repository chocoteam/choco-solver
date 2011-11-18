/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.variables.graph.graphStructure.adjacencyList.storedStructures;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import solver.variables.graph.graphStructure.adjacencyList.ArraySwapList_Array;

/**
 * Backtrable List of m elements based on Array int_swaping
 * add : O(1) only at root node!
 * testPresence: O(1)
 * remove: O(1)
 * iteration : O(m)
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 18/11/2011
 */
public class StoredArraySwapList_Array extends ArraySwapList_Array {

	protected IStateInt size;
	protected IEnvironment env;

	public StoredArraySwapList_Array(IEnvironment e, int n) {
		super(n);
		env = e;
		size = e.makeInt(0);
	}

	@Override
	public void add(int element) {
		if(env.getWorldIndex()!=0){
			System.out.println("cannot add elements in StoredArraySwapList after world 0");
			System.exit(0);
		}
		super.add(element);
	}

	protected int getSize(){
		return size.get();
	}
	protected void setSize(int s){
		size.set(s);
	}
	protected void addSize(int delta){
		size.add(delta);
	}
}
