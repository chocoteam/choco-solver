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
public class Set_Interval implements ISet {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int lb, ub;
	private ISetIterator iter = newIterator();

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Creates a set of integers encoded as an interval [min, max]
	 * Initially empty
	 */
	public Set_Interval() {
		this(0,-1);
	}

	/**
	 * Creates a set of integers encoded as an interval [min, max]
	 * @param min lowest value in the set
	 * @param max highest value in the set
	 */
	public Set_Interval(int min, int max) {
		this.lb = min;
		this.ub = max;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean add(int element) {
		int s = getSize();
		if(lb-1 == element){
			lb--;
		}
		if(ub+1 == element){
			ub++;
		}
		return s!=getSize();
	}

	@Override
	public boolean remove(int element) {
		int s = getSize();
		if(lb == element){
			lb++;
		}
		if(ub == element){
			ub--;
		}
		return s!=getSize();
	}

	@Override
	public boolean contain(int element) {
		return lb<=element && element<=ub;
	}

	@Override
	public int getSize() {
		return ub-lb+1;
	}

	@Override
	public void clear() {
		lb = 0;
		ub = -1;
	}

	@Override
	public SetType getSetType(){
		return SetType.INTERVAL;
	}

	@Override
	public String toString(){
		return "["+lb+","+ub+"]";
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
			int value = lb;
			@Override
			public void reset() {
				value = lb;
			}
			@Override
			public boolean hasNext() {
				return value <= ub;
			}
			@Override
			public Integer next() {
				value++;
				return value-1;
			}
		};
	}
}
