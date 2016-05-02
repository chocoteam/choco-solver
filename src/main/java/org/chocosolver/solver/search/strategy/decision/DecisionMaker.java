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

import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.PoolManager;

/**
 * A class that creates decisions on demand and maintains pool manager
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 14/03/2016.
 */
public class DecisionMaker {

    /**
     * object recycling management
     */
    private PoolManager<IntDecision> intDecisionPool;

    /**
     * object recycling management
     */
    private PoolManager<RealDecision> realDecisionPool;

    /**
     * object recycling management
     */
    private PoolManager<SetDecision> setDecisionPool;

    /**
     * Create a decision maker, that eases decision creation.
     */
    public DecisionMaker() {
        this.intDecisionPool = new PoolManager<>();
        this.realDecisionPool = new PoolManager<>();
        this.setDecisionPool = new PoolManager<>();
    }

    /**
     * Creates and returns an {@link IntDecision}: "{@code var} {@code dop} {@code value}".
     * @param var an integer variable
     * @param dop a decision operator
     * @param value a value
     * @return an IntDecision
     */
    public IntDecision makeIntDecision(IntVar var, DecisionOperator<IntVar> dop, int value) {
        IntDecision d = intDecisionPool.getE();
        if (d == null) {
            d = new IntDecision(intDecisionPool);
        }
        d.set(var, value, dop);
        return d;
    }

    /**
     * Creates and returns an {@link RealDecision}: "{@code var} &le; {@code value}".
     * @param var a real variable
     * @param value a value
     * @return an IntDecision
     */
    public RealDecision makeRealDecision(RealVar var, double value) {
        RealDecision d = realDecisionPool.getE();
        if (d == null) {
            d = new RealDecision(realDecisionPool);
        }
        d.set(var, value);
        return d;
    }

    /**
     * Creates and returns an {@link SetDecision}: "{@code var} {@code dop} {@code value}".
     * @param var a set variable
     * @param dop a decision operator
     * @param value a value
     * @return an SetDecision
     */
    public SetDecision makeSetDecision(SetVar var, DecisionOperator<SetVar> dop, int value) {
        SetDecision d = setDecisionPool.getE();
        if (d == null) {
            d = new SetDecision(setDecisionPool);
        }
        d.set(var, value, dop);
        return d;
    }

}
