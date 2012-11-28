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

import choco.kernel.common.util.tools.ArrayUtils;
import solver.exception.ContradictionException;
import solver.search.strategy.decision.Decision;
import solver.variables.Variable;

/**
 * A <code>StrategiesSequencer</code> is class for <code>AbstractStrategy</code> composition.
 * <code>this</code> is created with a list of <code>AbstractStrategy</code>, and calling
 * <code>getDecision()</code> retrieves the current active <code>AbstractStrategy</code> and
 * calls the delegate <code>getDecision()</code> method.
 * <br/>
 * A <code>AbstractStrategy</code> becomes "inactive" when no more decision can be computed,
 * ie every decisions have been computed and used.
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 5 June 2012
 */
public class StaticStrategiesSequencer extends AbstractStrategy<Variable> {

    protected AbstractStrategy[] strategies;

    private static Variable[] make(AbstractStrategy... strategies) {
        Variable[] vars = new Variable[0];
        for (int i = 0; i < strategies.length; i++) {
            vars = ArrayUtils.append(vars, strategies[i].vars);
        }
        return vars;
    }

    public StaticStrategiesSequencer(AbstractStrategy... strategies) {
        super(make(strategies));
        this.strategies = strategies;
    }

    @Override
    public void init() throws ContradictionException {
        for (int i = 0; i < strategies.length; i++) {
            strategies[i].init();
        }
    }

	@Override
	public Decision<Variable> computeDecision(Variable variable){
		if(variable==null || variable.instantiated()){
			return null;
		}
		int idx = 0;
        Decision decision = strategies[idx].computeDecision(variable);
        while (decision == null && idx < strategies.length - 1) {
            decision = strategies[++idx].computeDecision(variable);
        }
		return decision;
	}

    /**
     * {@inheritDoc}
     * Iterates over the declared sub-strategies and gets the overall current decision.
     */
    @Override
    public Decision getDecision() {
        int idx = 0;
        Decision decision = strategies[idx].getDecision();
        while (decision == null && idx < strategies.length - 1) {
            decision = strategies[++idx].getDecision();
        }
        return decision;
    }

    /**
     * {@inheritDoc}
     * This is based on the <code>print()</code> method of every sub-strategies.
     */
    @Override
    public String toString() {
        StringBuilder st = new StringBuilder("Static sequence of:\n");
        for (int i = 0; i < strategies.length; i++) {
            st.append("\t").append(strategies[i].toString()).append("\n");
        }
        return st.toString();
    }
}
