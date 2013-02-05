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

package solver.search.strategy.decision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Identity;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.ExplanationEngine;
import solver.variables.EventType;
import solver.variables.Variable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public abstract class Decision<V extends Variable> implements Identity, ICause {

    Logger LOGGER = LoggerFactory.getLogger(Decision.class);

    public static int _ID = 0;

    final int id;

    protected V var;

//    protected Decision<V> assignment; //WTF???

    protected int branch;

    long fails;

    int worldIndex; // indication on the world in which it has been selected

    protected Decision previous;

    public Decision() {
        id = _ID++;
    }

    @Override
    public final int getId() {
        return id;
    }

    /**
     * Set the previous decision applied in the tree search
     *
     * @param decision
     */
    public void setPrevious(Decision decision) {
        this.previous = decision;
    }

    /**
     * Return the previous decision applied in the tree search
     *
     * @return
     */
    public Decision getPrevious() {
        return previous;
    }

    protected void setWorldIndex(int wi) {
        this.worldIndex = wi;
    }

    public int getWorldIndex() {
        return worldIndex;
    }

    /**
     * Return true if the decision can be refuted
     *
     * @return true if the decision can be refuted, false otherwise
     */
    public boolean hasNext() {
        return branch < 2;
    }

    /**
     * Build the refutation, hasNext() must be called before
     */
    public void buildNext() {
        branch++;
    }

    /**
     * Apply the current decision
     *
     * @throws ContradictionException
     */
    public abstract void apply() throws ContradictionException;

    /**
     * Force the decision to be in its creation state.
     */
    public void rewind() {
        branch = 0;
    }

    /**
     * Return the variable object involves in the decision
     *
     * @return a variable V
     */
    public V getDecisionVariable() {
        return var;
    }

    /**
     * Return the value object involves in the decision
     *
     * @return a value object
     */
    public abstract Object getDecisionValue();

    /**
     * Free the decision, ie, it can be reused
     */
    public abstract void free();

    /**
     * Reverse the decision operator
     */
    public void reverse() {
        throw new UnsupportedOperationException();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public final boolean reactOnPromotion() {
        return false;
    }

    @Override
    public final int getPropagationConditions(int vIdx) {
        return EventType.VOID.mask;
    }

    @Override
    public void explain(Deduction d, Explanation e) {
        ExplanationEngine explainer = var.getSolver().getExplainer();
        if (branch < 2) {
            e.add(explainer.explain(getPositiveDeduction()));
        } else {
            e.add(explainer.explain(getNegativeDeduction()));
        }
    }

    public Deduction getNegativeDeduction() {
        return var.getSolver().getExplainer().getDecision(this, false);
    }

    public Deduction getPositiveDeduction() {
        return var.getSolver().getExplainer().getDecision(this, true);
    }

    public final Constraint getConstraint() {
        return null;
    }

    public final void incFail() {
        fails++;
    }

    public final long getFails() {
        return fails;
    }

}
