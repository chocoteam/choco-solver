/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 * must display the following acknowledgement:
 * This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public abstract class Decision<E> implements ICause {

    protected E var;

    protected int max_branching; // binary decision: 2
    // 0: not applied yet, 1: applied once, 2: refuted once, ...
    protected int branch;

    int worldIndex; // indication on the world in which it has been selected

    protected Decision previous;

    public Decision(int arity) {
        this.max_branching = arity;
    }

    /**
     * Set the previous decision applied in the tree search
     *
     * @param decision previous decision
     */
    public final void setPrevious(Decision decision) {
        this.previous = decision;
    }

    /**
     * Return the previous decision applied in the tree search
     *
     * @return the previous decision
     */
    public final Decision getPrevious() {
        return previous;
    }

    public final void setWorldIndex(int wi) {
        this.worldIndex = wi;
    }

    public final int getWorldIndex() {
        return worldIndex;
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
     * Indicate the number of possible branches from that decision
     * @param once set to true to disable refutation
     */
    public final void once(boolean once) {
        max_branching = once ? 1 : 2;
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
    public final void rewind() {
        branch = 0;
    }

    /**
     * Reuse the decision
     * @param var the decision object (commonly a variable)
     * @param wi the current world index
     */
    protected void set(E var, int wi) {
        this.var = var;
        branch = 0;
        this.setWorldIndex(wi);
    }

    /**
     * Return the variable object involves in the decision
     *
     * @return a variable V
     */
    public final E getDecisionVariables() {
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

    public Decision duplicate() {
        throw new UnsupportedOperationException();
    }
}
