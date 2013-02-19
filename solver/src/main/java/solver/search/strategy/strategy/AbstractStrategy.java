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

package solver.search.strategy.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.exception.ContradictionException;
import solver.search.strategy.decision.Decision;
import solver.variables.Variable;

import java.io.Serializable;

/**
 * An <code>AbstractStrategy</code> does <b>not</b> implicitly advance to the next decision on
 * <code>getDecision()</code> (or <code>getOppositeDecision()</code>) invokation.
 * <br/>
 * See also Gamma et al. "Design Patterns: Elements of Reusable Object-Oriented Software",
 * Behavioral patterns : Command, Iterator.
 * todo: comment
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 juil. 2010
 */
public abstract class AbstractStrategy<V extends Variable> implements Serializable, IDecisionComputer<V> {

    protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractStrategy.class);

    public final V[] vars;

    protected AbstractStrategy(V[] variables) {
        this.vars = variables.clone();
    }

    /**
     * Prepare <code>this</code> to be used in a search loop
     */
    public abstract void init() throws ContradictionException;

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
     * @param var
     * @return a decision to be applied to variable var
     */
    public Decision<V> computeDecision(V var) {
        return null;
    }
}
