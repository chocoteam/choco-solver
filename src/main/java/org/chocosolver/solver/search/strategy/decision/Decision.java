/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;

/**
 * An abstract which defines a Decision
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 * @param <E> type of variable handle by this decision
 */
public abstract class Decision<E extends Variable> implements ICause {

    /**
     * Variable of this decision
     */
    protected E var;

    /**
     * Number of time a decision can be applied.
     * For unary decision set to 1 , for binary decision, set to 2, ...
     */
    protected int max_branching = 2;
    /**
     * Indication of the decision state.
     * 0: not yet applied
     * 1: applied once
     * 2: refuter once, ...
     */
    protected int branch;
    /**
     * Indicate the position of this decision in the decision path.
     */
    private int position;
    /**
     * Create a <i>arity</i>-decision.
     * @param arity how many time this decision can be applied (and then refuted)
     */
    public Decision(int arity) {
        this.max_branching = arity;
    }

    /**
     * Set the position of this decision in the decision path.
     * Note that the position a decision is unique, but some decisions taken together have the same rank.
     * @param p position of this decision in the decision path
     */
    public final void setPosition(int p) {
        this.position = p;
    }

    /**
     * Get the position of this decision in the decision path
     * Note that the position a decision is unique, but some decisions taken together have the same rank.
     * @return position of this decision in the decision path
     */
    public final int getPosition() {
        return position;
    }

    /**
     * Return true if the decision can be refuted
     *
     * @return true if the decision can be refuted, false otherwise
     */
    public final boolean hasNext() {
        return branch < max_branching;
    }

    /**
     * Build the refutation, hasNext() must be called before
     */
    public final void buildNext() {
        branch++;
    }

    /**
     * Return the number of branches left to try
     *
     * @return number of tries left
     */
    public final int triesLeft() {
        return max_branching - branch;
    }

    /**
     * Indicate whether or not this decision can be refuted
     * if set to false, if the decision(e.g. x=3) fails
     * instead of backtracking once and applying its negation (e.g. x!=3)
     * the resolver will backtrack twice and negates the previous decision.
     * @param isRefutable set to false to disable refutation
     */
    public final void setRefutable(boolean isRefutable) {
        max_branching = isRefutable ? 2 : 1;
    }

    /**
     * @return number of time a decision can be applied. For unary decision set to 1 , for binary decision, set to 2, ...
     */
    public final int getArity(){
        return max_branching;
    }

    /**
     * Apply the current decision
     *
     * @throws ContradictionException if the application of this decision fails
     */
    public abstract void apply() throws ContradictionException;

    /**
     * Force the decision to be in its creation state.
     */
    public final void rewind() {
        branch = 0;
    }

    /**
     * Reuse the decision
     * @param var the decision object (commonly a variable)
     */
    protected void set(E var) {
        this.var = var;
        branch = 0;
        max_branching = 2;
    }

    /**
     * Return the variable object involves in the decision
     *
     * @return a variable V
     */
    public final E getDecisionVariable() {
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

    /**
     * @return a copy of this decision
     */
    public Decision<E> duplicate() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param dec a decision
     * @return <tt>true</tt> if the two decisions are equivalent (same variable, same operator, same value)
     */
    public boolean isEquivalentTo(Decision dec){
        return false;
    }
}
