/**
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
package solver.constraints.nary.nValue;

import memory.IStateBitSet;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
import util.objects.graphs.UndirectedGraph;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetType;
import util.tools.ArrayUtils;

import java.util.BitSet;
import java.util.Random;

/**
 * Propagator for atMostNValues with difference constraints
 * Uses R^k to compute maximum independent sets
 *
 * @author Jean-Guillaume Fages
 */
public class AMNV_Gci_R_R13 extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	// graph model
	private int n;
	private UndirectedGraph cliques;
	// required data structure
	private BitSet in, inMIS;
	private int[] misValues;

	private int nbIter;
	private Random rd = new Random(0);
	private Differences diff;
	private IStateBitSet[] eqs;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public AMNV_Gci_R_R13(IntVar[] variables, IntVar nValues, Differences diff, int nbIter) {
		super(ArrayUtils.append(variables, new IntVar[]{nValues}), PropagatorPriority.QUADRATIC, true);
		n = variables.length;
		cliques = new UndirectedGraph(solver.getEnvironment(), n, SetType.LINKED_LIST, false);
		in = new BitSet(n);
		inMIS = new BitSet(n);
		this.nbIter = nbIter;
		int max = 0;
		int min = Integer.MAX_VALUE / 2;
		for (IntVar v : variables) {
			max = Math.max(max, v.getUB());
			min = Math.min(min, v.getLB());
		}
		misValues = new int[max + 1 - min];
		this.diff = diff;
		eqs = new IStateBitSet[n];
		for (int i = 0; i < n; i++) {
			eqs[i] = environment.makeBitSet(n);
		}
	}

	//***********************************************************************************
	// ALGORITHMS
	//***********************************************************************************

	private void buildDigraph() {
		for (int i = 0; i < n; i++) {
			cliques.getNeighborsOf(i).clear();
		}
		for (int i = 0; i < n; i++) {
			for (int i2 = i + 1; i2 < n; i2++) {
				if (!diff.mustBeDifferent(i, i2)) {
					if (intersect(i, i2)) {
						cliques.addEdge(i, i2);
					}
				}
			}
		}
	}

	private boolean intersect(int i, int j) {
		IntVar x = vars[i];
		IntVar y = vars[j];
		if (x.getLB() > y.getUB() || y.getLB() > x.getUB()) {
			return false;
		}
		int ub = x.getUB();
		for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
			if (y.contains(val)) {
				return true;
			}
		}
		return false;
	}

	private int findMIS() {
		// prepare data structures
		in.clear();
		inMIS.clear();
		int min = 0;
		// find MIS
		while (in.cardinality() < n) {
			int nb = rd.nextInt(n - in.cardinality());
			int idx = in.nextClearBit(0);
			for (int i = idx; i >= 0 && i < n && nb >= 0; i = in.nextClearBit(i + 1)) {
				idx = i;
				nb--;
			}
			addToMIS(idx);
			min++;
		}
		if (min != inMIS.cardinality()) {
			throw new UnsupportedOperationException();
		}
		return min;
	}

	private void addToMIS(int node) {
		inMIS.set(node);
		in.set(node);
		ISet nei = cliques.getNeighborsOf(node);
		for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
			in.set(j);
		}
	}

	private void filter() throws ContradictionException {
		ISet nei;
		for (int i = inMIS.nextClearBit(0); i >= 0 && i < n; i = inMIS.nextClearBit(i + 1)) {
			int mate = -1;
			int last = 0;
			int ub = vars[i].getUB();
			int lb = vars[i].getLB();
			for (int k = lb; k <= ub; k = vars[i].nextValue(k)) {
				misValues[last++] = k;
			}
			nei = cliques.getNeighborsOf(i);
			for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
				if (inMIS.get(j)) {
					if (mate == -1) {
						mate = j;
					} else if (mate >= 0) {
						mate = -2;
					}
					for (int ik = 0; ik < last; ik++) {
						if (vars[j].contains(misValues[ik])) {
							last--;
							if (ik < last) {
								misValues[ik] = misValues[last];
								ik--;
							}
						}
					}
				}
			}
			if (mate >= 0) {
				enforce(i, mate);
			} else {
				for (int ik = 0; ik < last; ik++) {
					vars[i].removeValue(misValues[ik], aCause);
				}
			}
		}
	}

	private void enforce(int i, int j) throws ContradictionException {
		if (i > j) {
			eqs[i].set(j);
			enforce(j, i);
		} else {
			eqs[i].set(j);
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

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if ((evtmask &= EventType.FULL_PROPAGATION.mask) != 0) {
			buildDigraph();
		}
		for (int i = 0; i < nbIter; i++) {
			int min = findMIS();
			vars[n].updateLowerBound(min, aCause);
			if (min == vars[n].getUB()) {
				filter();
			}
		}
	}

	@Override
	public void propagate(int idxVarInProp, int mask) throws ContradictionException {
		if (idxVarInProp < n) {
			ISet nei = cliques.getNeighborsOf(idxVarInProp);
			for (int v = nei.getFirstElement(); v >= 0; v = nei.getNextElement()) {
				if (eqs[idxVarInProp].get(v)) {
					enforce(idxVarInProp, v);
				} else if (!intersect(idxVarInProp, v)) {
					cliques.removeEdge(idxVarInProp, v);
				}
			}
		}
		forcePropagate(EventType.CUSTOM_PROPAGATION);
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.INT_ALL_MASK();
	}

	@Override
	public ESat isEntailed() {
		return ESat.TRUE;//redundant propagator
	}
}
