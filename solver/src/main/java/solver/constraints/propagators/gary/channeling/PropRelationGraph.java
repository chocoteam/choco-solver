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

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.relations.GraphRelation;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;

/**
 * Propagator channeling a graph and an array of variables
 *
 * @author Jean-Guillaume Fages
 */
public class PropRelationGraph extends Propagator {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private GraphVar g;
    private int n;
    private Variable[] nodeVars;
    private GraphRelation relation;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public PropRelationGraph(Variable[] vars, GraphVar graph, Solver solver, Constraint cons, GraphRelation relation) {
        super(vars, solver, cons, PropagatorPriority.LINEAR);
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
        checkVar(idxVarInProp);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INT_ALL_MASK(); // TODO ALL_Events
    }

    @Override
    public ESat isEntailed() {
        INeighbors nei;
        for (int i = 0; i < n; i++) {
            nei = g.getEnvelopGraph().getSuccessorsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (g.getKernelGraph().arcExists(i, j) && relation.isEntail(i, j) == ESat.FALSE) {
                    return ESat.FALSE;
                }
            }
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (relation.isEntail(i, j) == ESat.UNDEFINED && !g.getKernelGraph().arcExists(i, j)) {
                    return ESat.UNDEFINED;
                }
            }
        }
        return ESat.TRUE;
    }

    //***********************************************************************************
    // PROCEDURE
    //***********************************************************************************

    private void checkVar(int i) throws ContradictionException {
        IActiveNodes ker = g.getKernelGraph().getActiveNodes();
        INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
        for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
            if (!g.getKernelGraph().arcExists(i, j)) {
                switch (relation.isEntail(i, j)) {
                    case TRUE:
                        if (ker.isActive(i) && ker.isActive(j)) {
                            g.enforceArc(i, j, aCause);
                        }
                        break;
                    case FALSE:
                        g.removeArc(i, j, aCause);
                        break;
                }
            }
        }
    }
}
