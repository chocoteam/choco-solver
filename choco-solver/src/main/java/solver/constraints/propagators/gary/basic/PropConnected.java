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

package solver.constraints.propagators.gary.basic;

import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.graph.UndirectedGraphVar;
import util.ESat;
import util.graphOperations.connectivity.ConnectivityFinder;
import util.objects.setDataStructures.ISet;

import java.util.BitSet;

/**
 * Propagator checking that the graph is connected
 * can filter by forcing bridges but this is only activated
 * after the propagator failed once (to save time)
 *
 * @author Jean-Guillaume Fages
 */
public class PropConnected extends Propagator<UndirectedGraphVar> {


    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n;
    private BitSet visited;
    private int[] fifo;
    private UndirectedGraphVar g;
    private ConnectivityFinder env_CC_finder;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropConnected(UndirectedGraphVar graph) {
        super(new UndirectedGraphVar[]{graph}, PropagatorPriority.LINEAR, true);
        g = graph;
        n = graph.getEnvelopGraph().getNbNodes();
        visited = new BitSet(n);
        fifo = new int[n];
        env_CC_finder = new ConnectivityFinder(g.getEnvelopGraph());
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int maxOrder = g.getEnvelopOrder();
        if (maxOrder == g.getKernelOrder() && maxOrder > 1) {
            if (!env_CC_finder.isConnectedAndFindIsthma()) {
                contradiction(g, "");
            }
            int nbIsma = env_CC_finder.isthmusFrom.size();
            for (int i = 0; i < nbIsma; i++) {
                g.enforceArc(env_CC_finder.isthmusFrom.get(i), env_CC_finder.isthmusTo.get(i), aCause);
            }
        } else {
            if (!fastCheck()) {
                contradiction(g, "disconnected");
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        int maxOrder = g.getEnvelopOrder();
        if (solver.getMeasures().getFailCount() > 0
                && maxOrder == g.getKernelOrder() && maxOrder > 1) {
            if (!env_CC_finder.isConnectedAndFindIsthma()) {
                contradiction(g, "");
            }
            int nbIsma = env_CC_finder.isthmusFrom.size();
            for (int i = 0; i < nbIsma; i++) {
                g.enforceArc(env_CC_finder.isthmusFrom.get(i), env_CC_finder.isthmusTo.get(i), aCause);
            }
        } else {
            if (!fastCheck()) {
                contradiction(g, "disconnected");
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if (!fastCheck()) {
            return ESat.FALSE;
        }
        if (!g.instantiated()) {
            return ESat.UNDEFINED;
        }
        return ESat.TRUE;
    }

    private boolean fastCheck() {
        visited.clear();
        int first = 0;
        int last = 0;
        int nbNodes = g.getEnvelopGraph().getActiveNodes().getSize();
        if (nbNodes == 0) return true;//empty graph
        int i = g.getEnvelopGraph().getActiveNodes().getFirstElement();
        fifo[last++] = i;
        visited.set(i);
        int nbReached = 1;
        while (first < last && nbReached < nbNodes) {
            i = fifo[first++];
            ISet s = g.getEnvelopGraph().getNeighborsOf(i);
            for (int j = s.getFirstElement(); j >= 0; j = s.getNextElement()) {
                if (!visited.get(j)) {
                    visited.set(j);
                    fifo[last++] = j;
                    nbReached++;
                    if (nbReached == nbNodes) {
                        break;
                    }
                }
            }
        }
        return nbReached == nbNodes;
    }
}