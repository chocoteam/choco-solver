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
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.Variable;

import java.io.Serializable;

/**
 * A search strategy provides decisions to go down in the search space.
 * The main method is {@link #computeDecision(Variable)} which returns the next decision to apply.
 *
 * @author Charles Prud'homme
 * @since 1 juil. 2010
 */
public abstract class AbstractStrategy<V extends Variable> implements Serializable {

    protected final V[] vars;

    protected AbstractStrategy(V... variables) {
        this.vars = variables.clone();
    }

    /**
     * Prepare <code>this</code> to be used in a search loop
     * The initialization can detect inconsistency, in that case, it returns false
     */
    public boolean init(){return true;}

    /**
     * Provides access to the current decision in the strategy.
     * If there are no more decision to provide, it returns <code>null</code>.
     *
     * @return the current decision
     */
    public abstract Decision<V> getDecision();

    /**
     * Creates a <code>String</code> object containing a pretty print of the current variables.
     *
     * @return a <code>String</code> object
     */
    public String toString() {
        StringBuilder s = new StringBuilder(32);
        for (Variable v : vars) {
            s.append(v).append(' ');
        }
        return s.toString();
    }

    /**
     * Computes a decision to be applied to variable var
     * This method should be implemented in order to use search patterns
     *
     * @param var a variable
     * @return a decision to be applied to variable var
     */
    protected Decision<V> computeDecision(V var) {
        return null;
    }

    /**
     * @return array of variables
     */
    public V[] getVariables() {
        return vars;
    }
}
