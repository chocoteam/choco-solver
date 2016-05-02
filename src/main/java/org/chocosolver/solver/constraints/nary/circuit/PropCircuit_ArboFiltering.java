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

public class PropCircuit_ArboFiltering extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // flow graph
    protected DirectedGraph connectedGraph;
    // number of nodes
    protected int n;
    // dominators finder that contains the dominator tree
    protected AbstractLengauerTarjanDominatorsFinder domFinder;
    // offset (usually 0 but 1 with MiniZinc)
    protected int offSet;
    // random function
    protected Random rd;
    protected CircuitConf conf;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropCircuit_ArboFiltering(IntVar[] succs, int offSet, CircuitConf conf) {
        super(succs, PropagatorPriority.QUADRATIC, false);
        this.conf = conf;
        this.n = succs.length;
        this.offSet = offSet;
        this.connectedGraph = new DirectedGraph(n + 1, SetType.BITSET, false);
        domFinder = new SimpleDominatorsFinder(n, connectedGraph);
        if (conf == CircuitConf.RD) {
            rd = new Random(0);
        }
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
        switch (conf) {
            case FIRST:
                filterFromDom(0);
                break;
            default:
            case RD:
                filterFromDom(rd.nextInt(n));
                break;
            case ALL:
                for (int i = 0; i < n; i++) {
                    filterFromDom(i);
                }
                break;
        }
    }

    protected void filterFromDom(int duplicatedNode) throws ContradictionException {
        for (int i = 0; i < n + 1; i++) {
            connectedGraph.getSuccOf(i).clear();
            connectedGraph.getPredOf(i).clear();
        }
        for (int i = 0; i < n; i++) {
            int ub = vars[i].getUB();
            for (int y = vars[i].getLB(); y <= ub; y = vars[i].nextValue(y)) {
                if (i == duplicatedNode) {
                    connectedGraph.addArc(n, y - offSet);
                } else {
                    connectedGraph.addArc(i, y - offSet);
                }
            }
        }
        if (domFinder.findDominators()) {
            for (int x = 0; x < n; x++) {
                if (x != duplicatedNode) {
                    int ub = vars[x].getUB();
                    for (int y = vars[x].getLB(); y <= ub; y = vars[x].nextValue(y)) {
                        if (domFinder.isDomminatedBy(x, y - offSet)) {
                            vars[x].removeValue(y, this);
                        }
                    }
                }
            }
        } else {
            // "the source cannot reach all nodes"
            fails();
        }
    }

    @Override
    public ESat isEntailed() {
        // redundant filtering
        return ESat.TRUE;
    }

}
