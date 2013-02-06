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
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.graph.GraphVar;

/**
 * Propagator channeling between arcs of a graph and a boolean matrix
 *
 * @author Jean-Guillaume Fages
 */
public class PropBoolGraph extends Propagator<BoolVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected GraphVar graph;
    protected BoolVar[][] relations;
    protected int n;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public PropBoolGraph(GraphVar graph, BoolVar[][] rel, Solver solver, Constraint cstr) {
        super(ArrayUtils.flatten(rel), PropagatorPriority.QUADRATIC);
        this.graph = graph;
        relations = rel;
        n = rel.length;
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                updateGraph(i, j);
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        updateGraph(idxVarInProp / n, idxVarInProp % n);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INT_ALL_MASK();
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (relations[i][j].instantiated()) {
                    if (relations[i][j].getValue() == 1 && !graph.getEnvelopGraph().arcExists(i, j)) {
                        return ESat.FALSE;
                    }
                } else {
                    return ESat.UNDEFINED;
                }
            }
        }
        if (!graph.instantiated()) {
            return ESat.UNDEFINED;
        }
        return ESat.TRUE;
    }

    //***********************************************************************************
    // REACT ON BOOL MODIFICATION
    //***********************************************************************************

    private void updateGraph(int i, int j) throws ContradictionException {
        if (relations[i][j].instantiated()) {
            if (relations[i][j].getLB() == 0) {
                graph.removeArc(i, j, aCause);
            } else {
                graph.enforceArc(i, j, aCause);
            }
        }
    }
}
