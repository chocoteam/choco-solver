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
 * Time: 17:10
 */

package solver.constraints.propagators.gary.tsp.heaps;

/**
 * Same worst case complexity but much better in practice
 * Especially when several nodes have same -infinity value 
 */
public class FastArrayHeap extends ArrayHeap{

	int[] best;
	int bestSize;
	double bestVal;

	public FastArrayHeap(int n){
		super(n);
		best = new int[n];
	}

	@Override
	public void add(int element, double element_key, int i) {
		if(isEmpty() || element_key<bestVal){
			bestVal = element_key;
			bestSize = 0;
			best[bestSize++]=element;
		}else if(element_key==bestVal && element_key<value[element]){
			best[bestSize++]=element;
		}
		super.add(element,element_key,i);
	}
	@Override
	public int pop() {
		if(bestSize>0){
			int min = best[bestSize-1];
			bestSize--;
			in.clear(min);
			size--;
			return min;
		}
		return super.pop();
	}
}