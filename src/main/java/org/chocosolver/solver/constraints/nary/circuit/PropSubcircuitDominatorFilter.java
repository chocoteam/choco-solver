/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.constraints.nary.circuit;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.dominance.AbstractLengauerTarjanDominatorsFinder;
import org.chocosolver.util.graphOperations.dominance.SimpleDominatorsFinder;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Random;

/**
 * Propagator for sub-circuit constraint based on dominators
 * Redundant propagator
 *
 * @author Jean-Guillaume Fages
 */
public class PropSubcircuitDominatorFilter extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	// flow graph
	private DirectedGraph connectedGraph;
	// number of nodes
	private int n;
	// dominators finder that contains the dominator tree
	private AbstractLengauerTarjanDominatorsFinder domFinder;
	// offset (usually 0 but 1 with MiniZinc)
	private int offSet;
	// random function
	private Random rd = new Random(0);
	// random function
	private int[] rootCandidates;
	// auto adapt whether to propagate or not
	private boolean adaptable;
	// counter for adaptive scheme
	private final static int MIN_COUNTER = 10;
	private final static int MAX_COUNTER = 1000;
	private int counter = MIN_COUNTER;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropSubcircuitDominatorFilter(IntVar[] succs, int offSet, boolean adaptable) {
		super(succs, PropagatorPriority.QUADRATIC, false);
		this.n = succs.length;
		this.offSet = offSet;
		this.connectedGraph = new DirectedGraph(n + 1, SetType.BITSET, false);
		domFinder = new SimpleDominatorsFinder(n, connectedGraph);
		rootCandidates = new int[n];
		this.adaptable = adaptable;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if (PropagatorEventType.isFullPropagation(evtmask)) {
			for (int i = 0; i < n; i++) {
				vars[i].updateBounds(offSet, n - 1 + offSet, this);
			}
		}
		counter ++;
		counter = Math.min(counter,MAX_COUNTER);
		counter = Math.max(counter,MIN_COUNTER);
		if((!adaptable) || rd.nextInt(counter)==0) {
			int size = 0;
			for (int i = 0; i < n; i++) {
				if (!vars[i].contains(i + offSet)) {
					rootCandidates[size++] = i;
				}
			}
			if (size > 0) {
				if (filterFromDom(rootCandidates[rd.nextInt(size)])
						|| reverseFilter(rootCandidates[rd.nextInt(size)])) {
					propagate(PropagatorEventType.CUSTOM_PROPAGATION.getMask());
				}
			}
		}
	}

	private boolean filterFromDom(int duplicatedNode) throws ContradictionException {
		clear();
		for (int i = 0; i < n; i++) {
			int ub = vars[i].getUB();
			for (int y = vars[i].getLB(); y <= ub; y = vars[i].nextValue(y)) {
				if (i == duplicatedNode || i == y-offSet) {
					connectedGraph.addArc(n, y - offSet);
				}else{
					connectedGraph.addArc(i, y - offSet);
				}
			}
		}
		boolean again = false;
		boolean hasFiltered = false;
		if (domFinder.findDominators()) {
			for (int x = 0; x < n; x++) {
				if (x != duplicatedNode) {
					int ub = vars[x].getUB();
					for (int y = vars[x].getLB(); y <= ub; y = vars[x].nextValue(y)) {
						if (x != y && domFinder.isDomminatedBy(x, y - offSet)) {
							hasFiltered |= vars[x].removeValue(y, this);
							// a dominator is not a loop
							if(vars[y-offSet].removeValue(y, this)){
								again = true;
							}
						}
					}
				}
			}
		} else {
			counter /= 2;
			// "the source cannot reach all nodes"
			fails();
		}
		hasFiltered |= again;
		counter += hasFiltered? -1:1;
		return again;
	}

	// anti-arborescence filtering
	private boolean reverseFilter(int duplicatedNode) throws ContradictionException {
		clear();
		for (int i = 0; i < n; i++) {
			int ub = vars[i].getUB();
			for (int y = vars[i].getLB(); y <= ub; y = vars[i].nextValue(y)) {
				if (y - offSet == duplicatedNode || i == y-offSet) {
					connectedGraph.addArc(n, i);
				}else{
					connectedGraph.addArc(y - offSet, i);
				}
			}
		}
		boolean again = false;
		boolean hasFiltered = false;
		if (domFinder.findDominators()) {
			for (int x = 0; x < n; x++) {
				int ub = vars[x].getUB();
				for (int y = vars[x].getLB(); y <= ub; y = vars[x].nextValue(y)) {
					if (x != y && y-offSet!=duplicatedNode && domFinder.isDomminatedBy(y - offSet,x)) {
						hasFiltered |= vars[x].removeValue(y, this);
						// a dominator is not a loop
						if(vars[x].removeValue(x+offSet, this)){
							again = true;
						}
					}
				}
			}
		} else {
			counter /= 2;
			// "the source cannot reach all nodes"
			fails();
		}
		hasFiltered |= again;
		counter += hasFiltered? -1:1;
		return again;
	}

	private void clear(){
		for (int i = 0; i < n + 1; i++) {
			connectedGraph.getSuccOf(i).clear();
			connectedGraph.getPredOf(i).clear();
		}
	}

	@Override
	public ESat isEntailed() {
		// redundant filtering
		return ESat.TRUE;
	}
}
