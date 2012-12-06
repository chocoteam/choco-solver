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

package solver.search.strategy.decision.graph;

import choco.kernel.common.util.PoolManager;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.search.strategy.assignments.GraphAssignment;
import solver.search.strategy.decision.AbstractDecision;
import solver.search.strategy.decision.Decision;
import solver.variables.EventType;
import solver.variables.graph.GraphVar;

public class GraphDecision extends AbstractDecision<GraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int branch;
    protected GraphAssignment assignment;
    protected int from, to;
    protected GraphVar g;
    protected final PoolManager<GraphDecision> poolManager;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public GraphDecision(PoolManager<GraphDecision> poolManager) {
        this.poolManager = poolManager;
    }

    @Override
    public GraphVar getDecisionVariable() {
        return g;
    }

    @Override
    public Object getDecisionValue() {
        if (to == -1) {
            return from;
        } else {
            return new int[]{from, to};
        }
    }

    public void setNode(GraphVar variable, int node, GraphAssignment graph_ass) {
        g = variable;
        this.from = node;
        this.to = -1;
        assignment = graph_ass;
        branch = 0;
    }

    public void setArc(GraphVar variable, int from, int to, GraphAssignment graph_ass) {
        g = variable;
        this.from = from;
        this.to = to;
        assignment = graph_ass;
        branch = 0;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean hasNext() {
        return branch < 2;
    }

    @Override
    public void buildNext() {
        branch++;
    }

    @Override
    public void buildPrevious() {
        branch--;
    }

    @Override
    public void apply() throws ContradictionException {
        if (branch == 1) {
            if (to == -1) {
                assignment.apply(g, from, this);
            } else {
                assignment.apply(g, from, to, this);
            }
        } else if (branch == 2) {
            if (to == -1) {
                assignment.unapply(g, from, this);
            } else {
                assignment.unapply(g, from, to, this);
            }
        }
    }

    @Override
    public Decision copy() {
        GraphDecision dec = poolManager.getE();
        if (dec == null) {
            dec = new GraphDecision(poolManager);
        }
        dec.g = this.g;
        dec.from = this.from;
        dec.to = this.to;
        dec.assignment = this.assignment;
        dec.branch = this.branch;
        return dec;
    }

    @Override
    public void opposite() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void free() {
        previous = null;
        poolManager.returnE(this);
    }

    @Override
    public String toString() {
        if (to == -1) {
            return " node " + from + assignment.toString();
        }
        return " arc (" + from + "," + to + ")" + assignment.toString();
    }

    @Override
    public void explain(Deduction d, Explanation e) {
        throw new UnsupportedOperationException("GraphDecision is not equipped for explanations");
    }

    @Override
    public boolean reactOnPromotion() {
        return false;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.VOID.mask;
    }

    @Override
    public Deduction getNegativeDeduction() {
        throw new UnsupportedOperationException(("GraphDecision is not equipped for explanations"));
    }

    @Override
    public Deduction getPositiveDeduction() {
        throw new UnsupportedOperationException(("GraphDecision is not equipped for explanations"));
    }
}
