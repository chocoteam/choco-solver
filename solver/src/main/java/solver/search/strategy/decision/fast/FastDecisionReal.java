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

package solver.search.strategy.decision.fast;

import choco.kernel.common.util.PoolManager;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.ExplanationEngine;
import solver.search.strategy.decision.AbstractDecision;
import solver.search.strategy.decision.Decision;
import solver.variables.EventType;
import solver.variables.RealVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class FastDecisionReal extends AbstractDecision<RealVar> {

    RealVar var;

    double value;

    int branch;


    final PoolManager<FastDecisionReal> poolManager;

    public FastDecisionReal(PoolManager<FastDecisionReal> poolManager) {
        this.poolManager = poolManager;
    }

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
            var.updateUpperBound(value, this);
        } else if (branch == 2) {
            var.updateLowerBound(value, this);
        }
    }

    public void set(RealVar v, double value) {
        branch = 0;
        this.var = v;
        this.value = value;
    }

    @Override
    public void free() {
        previous = null;
        poolManager.returnE(this);
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
    public String toString() {
        return String.format("%s%s %s %s (%d)", (branch < 2 ? "" : "!"), var.getName(), "<=", value, branch);
    }


    @Override
    public Explanation explain(Deduction d) {
        Explanation expl = Explanation.build();
        ExplanationEngine explainer = var.getSolver().getExplainer();
        expl.add(branch < 2 ? explainer.explain(getPositiveDeduction()) : explainer.explain(getNegativeDeduction()));
        return expl;
    }

    @Override
    public Deduction getNegativeDeduction() {
        //return var.getSolver().getExplainer().getVariableRefutation(var, value, this);
        throw new UnsupportedOperationException();
    }

    @Override
    public Deduction getPositiveDeduction() {
//        return var.getSolver().getExplainer().getVariableAssignment(var, value);
        throw new UnsupportedOperationException();
    }

    @Override
    public Decision copy() {
        FastDecisionReal dec = poolManager.getE();
        if (dec == null) {
            dec = new FastDecisionReal(poolManager);
        }
        dec.var = this.var;
        dec.value = this.value;
        dec.branch = this.branch;
        return dec;
    }
}
