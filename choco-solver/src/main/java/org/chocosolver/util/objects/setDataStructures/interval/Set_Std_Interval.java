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
package org.chocosolver.util.objects.setDataStructures.interval;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Iterator;

/**
 * Interval set of the form [min, max]
 * BEWARE: Cannot add/remove elements other than bounds
 *
 * @author Jean-Guillaume Fages
 */
public class Set_Std_Interval implements ISet {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private IStateInt lb, ub;
	private ISetIterator iter = newIterator();

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Creates a set of integers encoded as an interval [min, max]
	 * Initially empty
	 * @param env backtracking environment
	 */
	public Set_Std_Interval(IEnvironment env) {
		this(env,0,-1);
	}

	/**
	 * Creates a set of integers encoded as an interval [min, max]
	 * @param env backtracking environment
	 * @param min lowest value in the set
	 * @param max highest value in the set
	 */
	public Set_Std_Interval(IEnvironment env, int min, int max) {
		this.lb = env.makeInt(min);
		this.ub = env.makeInt(max);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean add(int element) {
		int s = getSize();
		if(lb.get()-1 == element){
			lb.add(-1);
		}
		if(ub.get()+1 == element){
			ub.add(1);
		}
		return s!=getSize();
	}

	@Override
	public boolean remove(int element) {
		int s = getSize();
		if(lb.get() == element){
			lb.add(1);
		}
		if(ub.get() == element){
			ub.add(-1);
		}
		return s!=getSize();
	}

	@Override
	public boolean contain(int element) {
		return lb.get()<=element && element<=ub.get();
	}

	@Override
	public int getSize() {
		return ub.get()-lb.get()+1;
	}

	@Override
	public void clear() {
		lb.set(0);
		ub.set(-1);
	}

	@Override
	public SetType getSetType(){
		return SetType.INTERVAL;
	}

	@Override
	public String toString(){
		return "["+lb.get()+","+ub.get()+"]";
	}

	//***********************************************************************************
	// ITERATOR
	//***********************************************************************************

	@Override
	public Iterator<Integer> iterator(){
		iter.reset();
		return iter;
	}

	@Override
	public ISetIterator newIterator(){
		return new ISetIterator() {
			int value = lb.get();
			@Override
			public void reset() {
				value = lb.get();
			}
			@Override
			public void notifyRemoved(int item) {}
			@Override
			public boolean hasNext() {
				return value <= ub.get();
			}
			@Override
			public Integer next() {
				value++;
				return value-1;
			}
		};
	}
}
