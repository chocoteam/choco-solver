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

import common.ESat;
import memory.setDataStructures.ISet;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;

/**
 * Propagator that ensures that K loops belong to the final graph
 *
 * @author Jean-Guillaume Fages
 */
public class PropKLoops extends Propagator {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private GraphVar g;
    private IntVar k;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropKLoops(GraphVar graph, IntVar k) {
        super(new Variable[]{graph, k}, PropagatorPriority.LINEAR);
        this.g = graph;
        this.k = k;
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int min = 0;
        int max = 0;
        ISet nodes = g.getEnvelopGraph().getActiveNodes();
        for (int i = nodes.getFirstElement(); i >= 0; i = nodes.getNextElement()) {
            if (g.getKernelGraph().isArcOrEdge(i, i)) {
                min++;
                max++;
            } else if (g.getEnvelopGraph().isArcOrEdge(i, i)) {
                max++;
            }
        }
        k.updateLowerBound(min, aCause);
        k.updateUpperBound(max, aCause);
        if (min == max) {
            setPassive();
        } else if (k.instantiated()) {
            if (k.getValue() == max) {
                for (int i = nodes.getFirstElement(); i >= 0; i = nodes.getNextElement()) {
                    if (g.getEnvelopGraph().isArcOrEdge(i, i)) {
                        g.enforceArc(i, i, aCause);
                    }
                }
                setPassive();
            }
            if (k.getValue() == min) {
                for (int i = nodes.getFirstElement(); i >= 0; i = nodes.getNextElement()) {
                    if (!g.getKernelGraph().isArcOrEdge(i, i)) {
                        g.removeArc(i, i, aCause);
                    }
                }
                setPassive();
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        propagate(0);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask
                + EventType.INCLOW.mask + EventType.DECUPP.mask + EventType.INSTANTIATE.mask;
    }

    @Override
    public ESat isEntailed() {
        int min = 0;
        int max = 0;
        ISet env = g.getEnvelopGraph().getActiveNodes();
        for (int i = env.getFirstElement(); i >= 0; i = env.getNextElement()) {
            if (g.getKernelGraph().isArcOrEdge(i, i)) {
                min++;
                max++;
            } else if (g.getEnvelopGraph().isArcOrEdge(i, i)) {
                max++;
            }
        }
        if (k.getLB() > max || k.getUB() < min) {
            return ESat.FALSE;
        }
        if (min == max) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
