/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 18:46
 */

package solver.search.strategy.strategy.set;

import choco.kernel.common.util.PoolManager;
import solver.exception.ContradictionException;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecisionSet;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.SetVar;

/**
 * Strategy for branching on set variables
 * Lexicographic element enforcing by default
 */
public class SetSearchStrategy extends AbstractStrategy<SetVar> {

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    protected PoolManager<FastDecisionSet> pool;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public SetSearchStrategy(SetVar[] variables) {
        super(variables);
        pool = new PoolManager<FastDecisionSet>();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void init() throws ContradictionException {
    }

    @Override
    public Decision<SetVar> getDecision() {
        for (SetVar s : vars) {
            Decision<SetVar> d = computeDecision(s);
            if (d != null) return d;
        }
        return null;
    }

    @Override
    public Decision<SetVar> computeDecision(SetVar s) {
        if (!s.instantiated()) {
            for (int i = s.getEnvelope().getFirstElement(); i >= 0; i = s.getEnvelope().getNextElement()) {
                if (!s.getKernel().contain(i)) {
                    FastDecisionSet d = pool.getE();
                    if (d == null) {
                        d = new FastDecisionSet(pool);
                    }
                    d.set(s, i, DecisionOperator.set_force);
                    return d;
                }
            }
        }
        return null;
    }
}
