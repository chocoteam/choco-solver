/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.util.PoolManager;

/**
 * A decision based on a {@link RealVar}
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class RealDecision extends Decision<RealVar> {

    private static final long serialVersionUID = -4723411613242027280L;
    /**
     * The decision value
     */
    private double value;
    /**
     * Decision pool manager, to recycle decisions
     */
    transient private final PoolManager<RealDecision> poolManager;

    /**
     * Create an decision based on an {@link RealVar}
     * @param poolManager decision pool manager, to recycle decisions
     */
    public RealDecision(PoolManager<RealDecision> poolManager) {
        super(2);
        this.poolManager = poolManager;
    }

    @Override
    public Double getDecisionValue() {
        return value;
    }

    @Override
    public void apply() throws ContradictionException {
        if (branch == 1) {
            var.updateUpperBound(value, this);
        } else if (branch == 2) {
            var.updateLowerBound(value, this);
        }
    }

    /**
     * Instantiate this decision with the parameters
     * @param v a variable
     * @param value a value
     */
    public void set(RealVar v, double value) {
        super.set(v);
        this.value = value;
    }

    @Override
    public void free() {
        poolManager.returnE(this);
    }

    @Override
    public String toString() {
        return String.format("%s%s %s %s", (branch < 2 ? "" : "!"), var.getName(), "<=", value);
    }
}
