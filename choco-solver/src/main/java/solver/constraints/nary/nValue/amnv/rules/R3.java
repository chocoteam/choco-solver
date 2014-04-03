/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package solver.constraints.nary.nValue.amnv.rules;

import memory.IEnvironment;
import solver.constraints.nary.nValue.amnv.graph.G;
import solver.constraints.nary.nValue.amnv.mis.F;
import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetFactory;
import util.objects.setDataStructures.SetType;

import java.util.BitSet;

/**
 * R3 filtering rule (back-propagation)
 *
 * @since 01/01/2014
 * @author Jean-Guillaume Fages
 */
public class R3 implements R {


	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int[] valToRem;
	private ISet[] learntEqualities;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public void filter(IntVar[] vars, G graph, F heur, Propagator aCause) throws ContradictionException{
		int n = vars.length-1;
		if(valToRem == null) {
			valToRem = new int[31];
			learntEqualities = new ISet[n];
			IEnvironment env = aCause.getSolver().getEnvironment();
			for(int i=0;i<n;i++){
				learntEqualities[i] = SetFactory.makeStoredSet(SetType.BITSET,n,env);
			}
		}
		BitSet mis = heur.getMIS();
		if(mis.cardinality()==vars[n].getUB()){
			ISet nei;
			for (int i = mis.nextClearBit(0); i>=0 && i < n; i = mis.nextClearBit(i + 1)) {
				int mate = -1;
				int last = 0;
				if(valToRem.length<vars[i].getDomainSize()){
					valToRem = new int[vars[i].getDomainSize()*2];
				}
				int ub = vars[i].getUB();
				int lb = vars[i].getLB();
				for (int k = lb; k <= ub; k = vars[i].nextValue(k)) {
					valToRem[last++] = k;
				}
				nei = graph.getNeighborsOf(i);
				for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
					if (mis.get(j)) {
						if (mate == -1) {
							mate = j;
						} else if (mate >= 0) {
							mate = -2;
						}
						for (int ik = 0; ik < last; ik++) {
							if (vars[j].contains(valToRem[ik])) {
								last--;
								if (ik < last) {
									valToRem[ik] = valToRem[last];
									ik--;
								}
							}
						}
						if(mate==-2 && last==0)break;
					}
				}
				if (mate >= 0) {
					enforceEq(i, mate, vars, aCause);
				} else {
					for (int ik = 0; ik < last; ik++) {
						vars[i].removeValue(valToRem[ik], aCause);
					}
				}
			}
		}
		for(int i=0;i<n;i++){
			for (int j = learntEqualities[i].getFirstElement(); j >= 0; j = learntEqualities[i].getNextElement()) {
				enforceEq(i, j, vars, aCause);
			}
		}
	}

	protected void enforceEq(int i, int j, IntVar[] vars, Propagator aCause) throws ContradictionException {
		if (i > j) {
			enforceEq(j, i, vars, aCause);
		} else {
			learntEqualities[i].add(j);
			learntEqualities[j].add(i);
			IntVar x = vars[i];
			IntVar y = vars[j];
			while (x.getLB() != y.getLB() || x.getUB() != y.getUB()) {
				x.updateLowerBound(y.getLB(), aCause);
				x.updateUpperBound(y.getUB(), aCause);
				y.updateLowerBound(x.getLB(), aCause);
				y.updateUpperBound(x.getUB(), aCause);
			}
			if (x.hasEnumeratedDomain() && y.hasEnumeratedDomain()) {
				int ub = x.getUB();
				for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
					if (!y.contains(val)) {
						x.removeValue(val, aCause);
					}
				}
				ub = y.getUB();
				for (int val = y.getLB(); val <= ub; val = y.nextValue(val)) {
					if (!x.contains(val)) {
						y.removeValue(val, aCause);
					}
				}
			}
		}
	}
}
