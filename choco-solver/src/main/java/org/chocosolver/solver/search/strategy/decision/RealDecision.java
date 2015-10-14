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
package org.chocosolver.solver.search.strategy.decision;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.util.PoolManager;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class RealDecision extends Decision<RealVar> {

    double value;

    final PoolManager<RealDecision> poolManager;

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

    public void set(RealVar v, double value) {
        super.set(v, v.getSolver().getEnvironment().getWorldIndex());
        this.value = value;
    }

    @Override
    public void free() {
        previous = null;
        poolManager.returnE(this);
    }

    @Override
    public String toString() {
        return String.format("%s%s %s %s (%d)", (branch < 2 ? "" : "!"), var.getName(), "<=", value, branch);
    }
}
