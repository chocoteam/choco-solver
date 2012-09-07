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

package solver.constraints.propagators.gary;

import choco.kernel.ESat;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;

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

    private GraphVar g;
    private IntVar k;
    int n;
    BitSet in;
    BitSet inMIS;
    int[] nbNeighbors;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropKCliques(GraphVar graph, Solver solver, Constraint constraint, IntVar k) {
        super(new Variable[]{graph, k}, solver, constraint, PropagatorPriority.LINEAR);
        g = graph;
        this.k = k;
        n = g.getEnvelopGraph().getNbNodes();
        in = new BitSet(n);
        inMIS = new BitSet(n);
        nbNeighbors = new int[n];
    }

    //***********************************************************************************
    // FILTERING
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
//		int min = simpleSearch();
        int min = efficientSearch();
        k.updateLowerBound(min, aCause);
        if (min == k.getUB()) {
            filter();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        propagate(0);
    }

    private void filter() throws ContradictionException {
        IActiveNodes nodes = g.getKernelGraph().getActiveNodes();
        INeighbors nei;
        int mate;
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

    private int simpleSearch() {
        in.clear();
        inMIS.clear();
        int nb = 0;
        IActiveNodes nodes = g.getKernelGraph().getActiveNodes();
        for (int i = nodes.getFirstElement(); i >= 0; i = nodes.getNextElement()) {
            in.set(i);
            nb++;
        }
        int idx = -1;
        INeighbors nei;
        int min = 0;
        while (nb > 0) {
            idx = in.nextSetBit(idx + 1);
            nei = g.getEnvelopGraph().getNeighborsOf(idx);
            in.clear(idx);
            inMIS.set(idx);
            nb--;
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (in.get(j)) {
                    in.clear(j);
                    nb--;
                }
            }
            min++;
        }
        return min;
    }

    private int efficientSearch() {
        in.clear();
        inMIS.clear();
        int nb = 0;
        IActiveNodes nodes = g.getKernelGraph().getActiveNodes();
        for (int i = nodes.getFirstElement(); i >= 0; i = nodes.getNextElement()) {
            in.set(i);
            nbNeighbors[i] = g.getEnvelopGraph().getNeighborsOf(i).neighborhoodSize();
            nb++;
        }
        int idx;
        INeighbors nei;
        TIntArrayList list = new TIntArrayList();
        int min = 0;
        while (nb > 0) {
            idx = in.nextSetBit(0);
            for (int i = in.nextSetBit(idx + 1); i >= 0; i = in.nextSetBit(i + 1)) {
                if (nbNeighbors[i] < nbNeighbors[idx]) {
                    idx = i;
                }
            }
            nei = g.getEnvelopGraph().getNeighborsOf(idx);
            in.clear(idx);
            inMIS.set(idx);
            nb--;
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (in.get(j)) {
                    in.clear(j);
                    nb--;
                    list.add(j);
                }
            }
            for (int i = list.size() - 1; i >= 0; i--) {
                nei = g.getEnvelopGraph().getNeighborsOf(list.get(i));
                for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
//					if(in.get(j)){
                    nbNeighbors[j]--;
//					}
                }
            }
            list.clear();
            min++;
        }
        return min;
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
        //TODO
        return ESat.UNDEFINED;
    }
}
