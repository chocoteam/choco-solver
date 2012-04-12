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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 30/01/12
 * Time: 17:09
 */

package solver.constraints.propagators.gary.tsp.heaps;

import java.util.BitSet;

/**
 * Trivial Heap to use for debugging
 * worst case running time for O(m) add/decrease key and O(n) pop = O(n*n+m)
 */
public class ArrayHeap implements Heap{

	BitSet in;
	double[] value;
	int[] mate;
	int size;

	public ArrayHeap(int n){
		in = new BitSet(n);
		value = new double[n];
		mate = new int[n];
		size = 0;
	}

	@Override
	public void add(int element, double element_key, int i) {
		if(!in.get(element)){
			in.set(element); size++;
			value[element] = element_key;
			mate[element] = i;
		}else if(element_key<value[element]){
			value[element] = element_key;
			mate[element] = i;
		}
	}

	@Override
	public void decreaseKey(int element, double new_element_key, int newMate) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int pop() {
		if(isEmpty()){
			throw new UnsupportedOperationException();
		}
		int min = in.nextSetBit(0);
		for(int i=in.nextSetBit(0);i>=0;i=in.nextSetBit(i+1)){
			if(value[i]<value[min]){
				min = i;
			}
		}
		in.clear(min);
		size--;
		return min;
	}

	@Override
	public void clear() {
		in.clear();
		size = 0;
	}

	@Override
	public boolean isEmpty() {
		return size==0;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public int getMate(int to) {
		return mate[to];
	}

//	@Override
	public void remove(int element) {
		throw new UnsupportedOperationException();
	}
}