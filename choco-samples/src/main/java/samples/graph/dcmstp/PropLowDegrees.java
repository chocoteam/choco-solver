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

package samples.graph.dcmstp;

import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.graph.UndirectedGraphVar;
import util.ESat;
import util.objects.setDataStructures.ISet;

import java.util.BitSet;

/**
 * Propagator filtering on low degrees:
 * if dMax(i) = dMax(j) = 1, then edge (i,j) is infeasible
 * if dMax(k) = 2 and (i,k) is already forced, then (k,j) is infeasible
 * ...
 *
 * @author Jean-Guillaume Fages
 */
public class PropLowDegrees extends Propagator<UndirectedGraphVar> {


    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n;
    private int[] counter, dMax;
    private BitSet oneNode;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropLowDegrees(UndirectedGraphVar vars, int[] maxDegrees) {
        super(new UndirectedGraphVar[]{vars}, PropagatorPriority.LINEAR, true);
        n = maxDegrees.length;
        oneNode = new BitSet(n);
        counter = new int[n];
        dMax = maxDegrees;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.ENFORCEARC.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        preprocessOneNodes();
        UndirectedGraphVar g = vars[0];
        if (oneNode.cardinality() < n) {
            for (int i = 0; i < n; i++) {
                ISet nei = g.getEnvelopGraph().getNeighborsOf(i);
                if (oneNode.get(i)) {
                    for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                        if (oneNode.get(j)) {
                            if (!g.getKernelGraph().edgeExists(i, j)) {
                                g.removeArc(i, j, this);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        propagate(0);
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }

    int[] list;

    private void preprocessOneNodes() throws ContradictionException {
        ISet nei;
        oneNode.clear();
        for (int i = 0; i < n; i++) {
            counter[i] = 0;
        }
        UndirectedGraphVar g = vars[0];
        int[] maxDegree = dMax;
        if (list == null) {
            list = new int[n];
        }
        int first = 0;
        int last = 0;
        for (int i = 0; i < n; i++) {
            if (maxDegree[i] == 1) {
                list[last++] = i;
                oneNode.set(i);
            }
        }
        while (first < last) {
            int k = list[first++];
            nei = g.getKernelGraph().getNeighborsOf(k);
            for (int s = nei.getFirstElement(); s >= 0; s = nei.getNextElement()) {
                if (!oneNode.get(s)) {
                    counter[s]++;
                    if (counter[s] > maxDegree[s]) {
                        contradiction(vars[0], "");
                    } else if (counter[s] == maxDegree[s] - 1) {
                        oneNode.set(s);
                        list[last++] = s;
                    }
                }
            }
        }
    }
}