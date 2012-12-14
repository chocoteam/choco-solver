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
import solver.exception.ContradictionException;
import solver.variables.Variable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public abstract class Decision<V extends Variable> implements ICause {

    Logger LOGGER = LoggerFactory.getLogger(Decision.class);

//    public Decision() {
//        super(Type.Dec);
//    }

    /**
     * Return the variable object involves in the decision
     *
     * @return a variable V
     */
    public abstract V getDecisionVariable();

    /**
     * Return the value object involves in the decision
     *
     * @return a value object
     */
    public abstract Object getDecisionValue();

    public abstract boolean isLeft();

    public abstract boolean isRight();

    /**
     * Return true if the decision can be refuted
     *
     * @return true if the decision can be refuted, false otherwise
     */
    public abstract boolean hasNext();

    /**
     * Build the refutation, hasNext() must be called before
     */
    public abstract void buildNext();

    /**
     * Force the decision to be in its creation state.
     */
    public abstract void rewind();

    /**
     * Apply the current decision
     *
     * @throws ContradictionException
     */
    public abstract void apply() throws ContradictionException;

    /**
     * Set the previous decision applied in the tree search
     *
     * @param decision
     */
    public abstract void setPrevious(Decision decision);

    /**
     * Return the previous decision applied in the tree search
     *
     * @return
     */
    public abstract Decision getPrevious();

    /**
     * Free the decision, ie, it can be reused
     */
    public abstract void free();

    /**
     * Make a copy of the current decision and reverse it
     */
    public abstract void reverse();
}
