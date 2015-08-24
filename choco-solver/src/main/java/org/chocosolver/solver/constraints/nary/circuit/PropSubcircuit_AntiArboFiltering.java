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

public class PropSubcircuit_AntiArboFiltering extends Propagator<IntVar> {

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
    private int[] rootCandidates;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropSubcircuit_AntiArboFiltering(IntVar[] succs, int offSet) {
        super(succs, PropagatorPriority.QUADRATIC, false);
        this.n = succs.length;
        this.offSet = offSet;
        this.connectedGraph = new DirectedGraph(n + 1, SetType.LINKED_LIST, false);
        domFinder = new SimpleDominatorsFinder(n, connectedGraph);
        rootCandidates = new int[n];
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            for (int i = 0; i < n; i++) {
                vars[i].updateLowerBound(offSet, this);
                vars[i].updateUpperBound(n - 1 + offSet, this);
            }
        }
        int size = 0;
        for (int i = 0; i < n; i++) {
            if (!vars[i].contains(i + offSet)) {
                rootCandidates[size++] = i;
            }
        }
        if (size > 0) {
            filterFromPostDom(rootCandidates[rd.nextInt(size)]);
        }
    }

    private void filterFromPostDom(int duplicatedNode) throws ContradictionException {
        for (int i = 0; i < n + 1; i++) {
            connectedGraph.getSuccOf(i).clear();
            connectedGraph.getPredOf(i).clear();
        }
        for (int i = 0; i < n; i++) {
            if (i == duplicatedNode || vars[i].contains(i + offSet)) {
                connectedGraph.addArc(i, n);
            } else {
                int ub = vars[i].getUB();
                for (int y = vars[i].getLB(); y <= ub; y = vars[i].nextValue(y)) {
                    connectedGraph.addArc(i, y - offSet);
                }
            }
        }
        if (domFinder.findPostDominators()) {
            for (int x = 0; x < n; x++) {
                if (x != duplicatedNode) {
                    int ub = vars[x].getUB();
                    for (int y = vars[x].getLB(); y <= ub; y = vars[x].nextValue(y)) {
                        if (x != y) {
                            if (domFinder.isDomminatedBy(y - offSet, x)) {
                                vars[x].removeValue(y, this);
                                vars[y - offSet].removeValue(y, this);
                            }
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
        // redundant filtering
        if (!isCompletelyInstantiated()) {
            return ESat.UNDEFINED;
        }
        return ESat.TRUE;
    }

}
