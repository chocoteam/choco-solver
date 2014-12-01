/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.strategy.decision;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Identity;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.Deduction;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.variables.Variable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public abstract class Decision<V extends Variable> implements Identity, ICause, Comparable<Decision<V>> {

    public static int _ID = 0;

    final int id;

    protected V var;

    // 0: not applied yet, 1: applied once, 2: refute once
    protected int branch;

    int worldIndex; // indication on the world in which it has been selected

    protected Decision previous;

    protected boolean once;

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
     * @param decision previous decision
     */
    public void setPrevious(Decision decision) {
        this.previous = decision;
    }

    /**
     * Return the previous decision applied in the tree search
     *
     * @return the previous decision
     */
    public Decision getPrevious() {
        return previous;
    }

    public void setWorldIndex(int wi) {
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
        return branch < 2 && !once;
    }

    /**
     * Build the refutation, hasNext() must be called before
     */
    public void buildNext() {
        branch++;
    }

    /**
     * Should this decision be a one-shot decision, non refutable.
     * @param once a boolean
     */
    public void once(boolean once) {
        this.once = once;
    }

    protected void set(V var){
        this.var = var;
        branch = 0;
        this.once = false;
        this.setWorldIndex(var.getSolver().getEnvironment().getWorldIndex());
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

    public Decision<V> duplicate() {
        throw new UnsupportedOperationException();
    }


    @Override
    public void explain(Deduction d, Explanation e) {
        ExplanationEngine explainer = var.getSolver().getExplainer();
        if (branch == 1) {
            e.add(explainer.explain(getPositiveDeduction()));
        } else if (branch == 2) {
            e.add(explainer.explain(getNegativeDeduction()));
        } else {
            throw new SolverException("Cannot explain a decision which has not been applied or refuted");
        }
    }

    public Deduction getNegativeDeduction() {
        return var.getSolver().getExplainer().getDecision(this, false);
    }

    public Deduction getPositiveDeduction() {
        return var.getSolver().getExplainer().getDecision(this, true);
    }

    @Override
    public int compareTo(Decision<V> o) {
        if (o.getDecisionVariable().getId() == this.getDecisionVariable().getId()
                && o.getDecisionValue().equals(this.getDecisionValue())) {
            return 0;
        }
        return 1;
    }
}
