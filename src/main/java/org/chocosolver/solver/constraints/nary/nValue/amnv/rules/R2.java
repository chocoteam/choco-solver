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
package org.chocosolver.solver.constraints.nary.nValue.amnv.rules;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.nValue.amnv.mis.F;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;

import java.util.BitSet;

/**
 * R2 filtering rule (back-propagation)
 *
 * @since 01/01/2014
 * @author Jean-Guillaume Fages
 */
public class R2 implements R {

	private BitSet valInMIS;

	public void filter(IntVar[] vars, UndirectedGraph graph, F heur, Propagator aCause) throws ContradictionException{
		int n = vars.length-1;
		BitSet mis = heur.getMIS();
		if(mis.cardinality()==vars[n].getUB()){
			if(valInMIS == null) valInMIS = new BitSet();
			valInMIS.clear();
			for (int i = mis.nextSetBit(0); i >= 0; i = mis.nextSetBit(i + 1)) {
				int ub = vars[i].getUB();
				for (int k = vars[i].getLB(); k <= ub; k = vars[i].nextValue(k)) {
					valInMIS.set(k);
				}
			}
			for (int i = mis.nextClearBit(0); i < n; i = mis.nextClearBit(i + 1)) {
				int ub = vars[i].getUB();
				for (int k = vars[i].getLB(); k <= ub; k = vars[i].nextValue(k)) {
					if (!valInMIS.get(k)) {
						vars[i].removeValue(k, aCause);
					}
				}
			}
		}
	}
}
