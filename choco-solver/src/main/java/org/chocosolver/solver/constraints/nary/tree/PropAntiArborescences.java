/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.tree;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.dominance.AbstractLengauerTarjanDominatorsFinder;
import org.chocosolver.util.graphOperations.dominance.AlphaDominatorsFinder;
import org.chocosolver.util.graphOperations.dominance.SimpleDominatorsFinder;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * AntiArborescences propagation (simplification from tree constraint) based on dominators
 * loops (i.e., variables such that x[i]=i) are considered as roots
 * Can use the simple LT algorithm which runs in O(m.log(n)) worst case time
 * Or a slightly more sophisticated one, linear in theory but not necessarily faster in practice
 *
 * @author Jean-Guillaume Fages
 */
public class PropAntiArborescences extends Propagator<IntVar> {

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

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * AntiArborescences propagation (simplification from tree constraint) based on dominators
     *
     * @param succs array of integer variables
     * @param offSet int
     * @param linear boolean
     */
    public PropAntiArborescences(IntVar[] succs, int offSet, boolean linear) {
        super(succs, PropagatorPriority.LINEAR, false);
        this.n = succs.length;
        this.offSet = offSet;
        this.connectedGraph = new DirectedGraph(n + 1, SetType.BITSET, false);
        if (linear) {
            domFinder = new AlphaDominatorsFinder(n, connectedGraph);
        } else {
            domFinder = new SimpleDominatorsFinder(n, connectedGraph);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
		if (PropagatorEventType.isFullPropagation(evtmask)) {
			for (int i = 0; i < n; i++) {
				vars[i].updateLowerBound(offSet, aCause);
				vars[i].updateUpperBound(n - 1 + offSet, aCause);
			}
		}
        structuralPruning();
    }

    private void structuralPruning() throws ContradictionException {
        for (int i = 0; i < n + 1; i++) {
            connectedGraph.getSuccOf(i).clear();
            connectedGraph.getPredOf(i).clear();
        }
        for (int i = 0; i < n; i++) {
            int ub = vars[i].getUB();
            for (int y = vars[i].getLB(); y <= ub; y = vars[i].nextValue(y)) {
                if (i == y) { // can be a root node
                    connectedGraph.addArc(i, n);
                } else {
                    connectedGraph.addArc(i, y - offSet);
                }
            }
        }
        if (domFinder.findPostDominators()) {
            for (int x = 0; x < n; x++) {
                int ub = vars[x].getUB();
                for (int y = vars[x].getLB(); y <= ub; y = vars[x].nextValue(y)) {
                    if (x != y) {
                        if (domFinder.isDomminatedBy(y - offSet, x)) {
                            vars[x].removeValue(y, aCause);
                        }
                    }
                }
            }
        } else {
            // the source cannot reach all nodes
            fails();
        }
    }

    @Override
    public ESat isEntailed() {
        if (!isCompletelyInstantiated()) {
            return ESat.UNDEFINED;
        }
        ISet tmp = SetFactory.makeSwap(n, false);
        for (int i = 0; i < n; i++) {
            if (circuit(tmp, i)) {
                return ESat.FALSE;
            }
        }
        return ESat.TRUE;
    }

    private boolean circuit(ISet tmp, int i) {
        tmp.clear();
        int x = i;
        tmp.add(x);
        int y = vars[x].getValue();
        while (x != y) {
            x = y;
            if (tmp.contain(x)) {
                return true;
            }
            tmp.add(x);
            y = vars[x].getValue();
        }
        return false;
    }

}
