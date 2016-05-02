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

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.PoolManager;

/**
 * A decision based on a {@link SetVar}
 * @author Jean-Guillaume Fages
 * @since Jan. 2013
 */
public class SetDecision extends Decision<SetVar> {

    /**
     * The decision value
     */
    private int value;
    /**
     * The assignment operator
     */
    private DecisionOperator<SetVar> operator;
    /**
     * Decision pool manager, to recycle decisions
     */
    private final PoolManager<SetDecision> poolManager;

    /**
     * Create an decision based on an {@link SetVar}
     * @param poolManager decision pool manager, to recycle decisions
     */
    public SetDecision(PoolManager<SetDecision> poolManager) {
        super(2);
        this.poolManager = poolManager;
    }

    @Override
    public Integer getDecisionValue() {
        return value;
    }

    @Override
    public void apply() throws ContradictionException {
        if (branch == 1) {
            operator.apply(var, value, this);
        } else if (branch == 2) {
            operator.unapply(var, value, this);
        }
    }

    /**
     * Instantiate this decision with the parameters
     * @param v a variable
     * @param value a value
     */
    public void set(SetVar v, int value, DecisionOperator<SetVar> operator) {
        super.set(v);
        this.var = v;
        this.value = value;
        this.operator = operator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void reverse() {
        this.operator = operator.opposite();
    }

    @Override
    public void free() {
        poolManager.returnE(this);
    }

    @Override
    public String toString() {
        return String.format("%s%s %s %s", (branch < 2 ? "" : "!"), var.getName(), operator.toString(), value);
    }
}
