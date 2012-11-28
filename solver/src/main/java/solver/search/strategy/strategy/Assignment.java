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

import choco.kernel.common.util.PoolManager;
import solver.Configuration;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.pattern.LastFail;
import solver.search.strategy.selectors.InValueIterator;
import solver.search.strategy.selectors.VariableSelector;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class Assignment extends AbstractStrategy<IntVar> {

    VariableSelector<IntVar> varselector;

    InValueIterator valueIterator;

    PoolManager<FastDecision> decisionPool;

    DecisionOperator assgnt = DecisionOperator.int_eq;

    public Assignment(IntVar[] vars, VariableSelector<IntVar> varselector, InValueIterator valueIterator) {
        super(vars);
        this.varselector = varselector;
        this.valueIterator = valueIterator;
        decisionPool = new PoolManager<FastDecision>();
    }

    public Assignment(IntVar[] vars, VariableSelector<IntVar> varselector, InValueIterator valueIterator,
                      DecisionOperator assgnt) {
        super(vars);
        this.varselector = varselector;
        this.valueIterator = valueIterator;
        decisionPool = new PoolManager<FastDecision>();
        this.assgnt = assgnt;
    }

    @Override
    public void init() {
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Decision getDecision() {
        if (varselector.hasNext()) {
            IntVar variable;
            int value;
			if(lastFail.canApply()){
				variable = lastFail.getVar();
			}else{
				varselector.advance();
                variable = varselector.getVariable();
				lastFail.setVar(variable);
			}
            value = valueIterator.selectValue(variable);
            FastDecision d = decisionPool.getE();
            if (d == null) {
                d = new FastDecision(decisionPool);
            }
            d.set(variable, value, assgnt);
            return d;
        }
        return null;
    }
}
