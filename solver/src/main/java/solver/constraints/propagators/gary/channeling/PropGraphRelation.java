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

package solver.constraints.propagators.gary.channeling;

import common.ESat;
import memory.setDataStructures.ISet;
import solver.Solver;
import solver.constraints.gary.relations.GraphRelation;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;

/**
 * Propagator channeling a graph and an array of variables through a Relation object
 *
 * @author Jean-Guillaume Fages
 */
public class PropGraphRelation<G extends GraphVar> extends Propagator<G> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private G g;
    private int n;
    private Variable[] nodeVars;
    private GraphRelation relation;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public PropGraphRelation(Variable[] vars, G graph, GraphRelation relation) {
        super((G[]) new GraphVar[]{graph}, PropagatorPriority.QUADRATIC);
        this.g = graph;
        this.nodeVars = vars;
        this.n = nodeVars.length;
        this.relation = relation;
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            checkVar(i);
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
        return EventType.ENFORCEARC.mask + EventType.ENFORCENODE.mask + EventType.REMOVEARC.mask + EventType.META.mask;
    }

    @Override
    public ESat isEntailed() {
        if (!g.instantiated()) {
            return ESat.UNDEFINED;
        }
        for (int i = 0; i < n; i++) {
            ISet nei = g.getEnvelopGraph().getSuccsOrNeigh(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (relation.isEntail(i, j) == ESat.FALSE) {
                    return ESat.FALSE;
                }
            }
        }
        return ESat.TRUE;
    }

    //***********************************************************************************
    // PROCEDURES
    //***********************************************************************************

    private void checkVar(int i) throws ContradictionException {
        ISet ker = g.getKernelGraph().getActiveNodes();
        for (int j = ker.getFirstElement(); j >= 0; j = ker.getNextElement()) {
            if (g.getKernelGraph().isArcOrEdge(i, j)) {
                relation.applyTrue(i, j, solver, aCause);
            } else {
                if (!g.getEnvelopGraph().isArcOrEdge(i, j)) {
                    if (ker.contain(i)) {
                        relation.applyFalse(i, j, solver, aCause);
                    }
                }
            }
        }
    }
}
