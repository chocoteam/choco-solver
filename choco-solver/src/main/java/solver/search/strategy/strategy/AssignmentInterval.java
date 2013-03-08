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

import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecisionReal;
import solver.search.strategy.selectors.RealValueIterator;
import solver.search.strategy.selectors.VariableSelector;
import solver.variables.RealVar;
import util.PoolManager;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class AssignmentInterval extends AbstractStrategy<RealVar> {

    VariableSelector<RealVar> varselector;

    RealValueIterator valueIterator;

    PoolManager<FastDecisionReal> decisionPool;

    public AssignmentInterval(RealVar[] vars, VariableSelector<RealVar> varselector, RealValueIterator valueIterator) {
        super(vars);
        this.varselector = varselector;
        this.valueIterator = valueIterator;
        decisionPool = new PoolManager<FastDecisionReal>();
    }

    @Override
    public void init() {
    }

    @Override
    public Decision<RealVar> computeDecision(RealVar variable) {
        if (variable == null || variable.instantiated()) {
            return null;
        }
        double value = valueIterator.selectValue(variable);
        FastDecisionReal d = decisionPool.getE();
        if (d == null) {
            d = new FastDecisionReal(decisionPool);
        }
        d.set(variable, value);
        return d;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Decision getDecision() {
        RealVar variable = null;
        if (varselector.hasNext()) {
            varselector.advance();
            variable = varselector.getVariable();
        }
        return computeDecision(variable);
    }
}
