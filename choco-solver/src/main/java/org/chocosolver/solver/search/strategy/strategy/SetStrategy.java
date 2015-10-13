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
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.fast.FastDecisionSet;
import org.chocosolver.solver.search.strategy.selectors.SetValueSelector;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.PoolManager;

/**
 * Strategy for branching on set variables
 *
 * @author Jean-Guillaume Fages
 * @since 6/10/13
 */
public class SetStrategy extends AbstractStrategy<SetVar> {

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    protected PoolManager<FastDecisionSet> pool;
    protected VariableSelector<SetVar> varSelector;
    protected SetValueSelector valSelector;
    protected DecisionOperator<SetVar> operator;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Generic strategy to branch on set variables
     *
     * @param scope        SetVar array to branch on
     * @param varS         variable selection strategy
     * @param valS         integer  selection strategy
     * @param enforceFirst branching order true = enforce first; false = remove first
     */
    public SetStrategy(SetVar[] scope, VariableSelector<SetVar> varS, SetValueSelector valS, boolean enforceFirst) {
        super(scope);
        varSelector = varS;
        valSelector = valS;
        operator = enforceFirst ? DecisionOperator.set_force : DecisionOperator.set_remove;
        pool = new PoolManager<>();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean init(){
        return true;
    }

    @Override
    public Decision<SetVar> getDecision() {
        SetVar variable = varSelector.getVariable(vars);
        return computeDecision(variable);
    }

    @Override
    public Decision<SetVar> computeDecision(SetVar s) {
        if (s == null) {
            return null;
        }
        assert !s.isInstantiated();
        FastDecisionSet d = pool.getE();
        if (d == null) {
            d = new FastDecisionSet(pool);
        }
        d.set(s, valSelector.selectValue(s), operator);
        return d;
    }
}
