/*
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

package solver.constraints.nary.circuit;

import gnu.trove.map.hash.THashMap;
import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
import util.graphOperations.dominance.AbstractLengauerTarjanDominatorsFinder;
import util.graphOperations.dominance.SimpleDominatorsFinder;
import util.objects.graphs.DirectedGraph;
import util.objects.setDataStructures.SetType;

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
        super(succs, PropagatorPriority.QUADRATIC, true);
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
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            for (int i = 0; i < n; i++) {
                vars[i].updateLowerBound(offSet, aCause);
                vars[i].updateUpperBound(n - 1 + offSet, aCause);
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

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        forcePropagate(EventType.FULL_PROPAGATION);
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
                                vars[x].removeValue(y, aCause);
                                vars[y - offSet].removeValue(y, aCause);
                            }
                        }
                    }
                }
            }
        } else {
            contradiction(vars[0], "the source cannot reach all nodes");
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

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.vars.length;
            IntVar[] aVars = new IntVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                aVars[i] = (IntVar) identitymap.get(this.vars[i]);
            }
            identitymap.put(this, new PropSubcircuit_AntiArboFiltering(aVars, this.offSet));
        }
    }
}
