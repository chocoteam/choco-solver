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
package choco.solver.search.enumerations.values;

public class Concat<A> extends ValueIterator<A> {
	ValueIterator<ValueIterator<A>> p;
	int[] cumulatedLengths;
	Concat(ValueIterator<ValueIterator<A>> p1) {
		p = p1;
		cumulatedLengths = new int[p.length()];
		cumulatedLengths[0] = p1.get(0).length(); 
		for (int i=1; i<cumulatedLengths.length; i++) {
			cumulatedLengths[i] = cumulatedLengths[i-1]+p1.get(i).length(); 
		}
	}
	public A get(int i) {
		int j=0;
		while (i>=cumulatedLengths[j]) {
			j++;
		}
		if (j==0) {
			return p.get(j).get(i);
		} else {
			return p.get(j).get(i-cumulatedLengths[j-1]);
		}
	}
	public int length() {
		int result = 0;
		for (int i=0;i<p.length();i++) {
			result += p.get(i).length();
		}
		return result;
	}
	public String toString() {
		return "Concat(" + p + ")";
	}
}
