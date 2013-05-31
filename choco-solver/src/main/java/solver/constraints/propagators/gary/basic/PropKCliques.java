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

import gnu.trove.list.array.TIntArrayList;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.UndirectedGraphVar;
import util.ESat;
import util.graphOperations.connectivity.ConnectivityFinder;
import util.objects.setDataStructures.ISet;

import java.util.BitSet;

/**
 * Propagator that ensures that the final graph consists in K cliques
 *
 * @author Jean-Guillaume Fages
 */
public class PropKCliques extends Propagator {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private UndirectedGraphVar g;
    private IntVar k;
    private int n;
    private BitSet in;
    private BitSet inMIS;
    private int[] nbNeighbors;
    private TIntArrayList list;
    private ConnectivityFinder connectivityFinder;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropKCliques(UndirectedGraphVar graph, IntVar k) {
        super(new Variable[]{graph, k}, PropagatorPriority.LINEAR,false, true);
        g = (UndirectedGraphVar) vars[0];
        this.k = (IntVar) vars[1];
        n = g.getEnvelopGraph().getNbNodes();
        in = new BitSet(n);
        inMIS = new BitSet(n);
        nbNeighbors = new int[n];
        list = new TIntArrayList();
        connectivityFinder = new ConnectivityFinder(g.getKernelGraph());
    }

    //***********************************************************************************
    // FILTERING
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        connectivityFinder.findAllCC();
        int max = connectivityFinder.getNBCC();
        max += g.getEnvelopOrder() - g.getKernelOrder();
        k.updateUpperBound(max, aCause);
        if (max == k.getLB()) {
            ISet nodes = g.getKernelGraph().getActiveNodes();
            for (int i = 0; i < n; i++) {
                if (!nodes.contain(i)) {
                    ISet nei = g.getEnvelopGraph().getNeighborsOf(i);
                    for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                        g.removeArc(i, j, aCause);
                    }
                }
            }
        } else {
            int min = findMIS();
            k.updateLowerBound(min, aCause);
            if (min == k.getUB()) {
                filter();
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        propagate(0);
    }

    private int findMIS() {
        // prepare data structures
        in.clear();
        inMIS.clear();
        ISet nodes = g.getKernelGraph().getActiveNodes();
        int min = 0;
        for (int i = 0; i < n; i++) {
            nbNeighbors[i] = g.getEnvelopGraph().getNeighborsOf(i).getSize();
            if (!nodes.contain(i)) {
                in.set(i);
            }
        }
        // find MIS
        int idx = in.nextClearBit(0);
        while (idx >= 0 && idx < n) {
            for (int i = in.nextClearBit(idx + 1); i >= 0 && i < n; i = in.nextClearBit(i + 1)) {
                if (nbNeighbors[i] < nbNeighbors[idx]) {
                    idx = i;
                }
            }
            addToMIS(idx);
            min++;
            idx = in.nextClearBit(0);
        }
        return min;
    }

    private void addToMIS(int node) {
        ISet nei = g.getEnvelopGraph().getNeighborsOf(node);
        inMIS.set(node);
        in.set(node);
        list.clear();
        for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
            if (!in.get(j)) {
                in.set(j);
                list.add(j);
            }
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            nei = g.getEnvelopGraph().getNeighborsOf(list.get(i));
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                nbNeighbors[j]--;
            }
        }
    }

    private void filter() throws ContradictionException {
        ISet nei;
        in.clear();
        int mate;
        ISet nodes = g.getKernelGraph().getActiveNodes();
        for (int i = nodes.getFirstElement(); i >= 0; i = nodes.getNextElement()) {
            if (!inMIS.get(i)) {
                mate = -1;
                nei = g.getEnvelopGraph().getNeighborsOf(i);
                for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                    if (inMIS.get(j)) {
                        if (mate == -1) {
                            mate = j;
                        } else {
                            mate = -2;
                            break;
                        }
                    }
                }
                if (mate >= 0) {
                    g.enforceArc(i, mate, aCause);
                }
            }
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.ENFORCENODE.mask
                + EventType.DECUPP.mask + EventType.INCLOW.mask + EventType.INSTANTIATE.mask;
    }

    @Override
    public ESat isEntailed() {
        if (findMIS() > k.getUB()) {
            return ESat.FALSE;
        }
        if (g.instantiated()) {
            ConnectivityFinder cf = new ConnectivityFinder(g.getEnvelopGraph());
            cf.findAllCC();
            if (cf.getNBCC() <= k.getLB()) {
                return ESat.TRUE;// sous reserve que le graphe soit transitif (autre propagateur)
            }
        }
        return ESat.UNDEFINED;
    }
}
