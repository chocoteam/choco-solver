/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package solver.propagation;

import solver.Solver;
import solver.propagation.hardcoded.SevenQueuesPropagatorEngine;
import solver.propagation.hardcoded.TwoBucketPropagationEngine;

/**
 * A factory to build a propagation engine.
 * There are two types of engines:
 * <br/>- hard coded ones ({@code VARIABLEDRIVEN}, {@code PROPAGATORDRIVEN}, ...),
 * <br/>- DSL based one ({@code DSLDRIVEN})
 * <br/>
 * The second type enable to declare a specific behavior: a propagation strategy
 *
 * @author Charles Prud'homme
 * @since 05/07/12
 */
public enum PropagationEngineFactory {

    /**
     * Create a seven queue dynamic propagator-oriented propagation engine
     */
    PROPAGATORDRIVEN_7QD() {
        @Override
        public IPropagationEngine make(Solver solver) {
            return new SevenQueuesPropagatorEngine(solver);
        }
    },

    /**
     * Create a propagation engine which handles both priority and separated coarse propagation.
     */
    TWOBUCKETPROPAGATIONENGINE() {
        @Override
        public IPropagationEngine make(Solver solver) {
            return new TwoBucketPropagationEngine(solver);
        }
    },

    DEFAULT() {
        @Override
        public IPropagationEngine make(Solver solver) {
            return TWOBUCKETPROPAGATIONENGINE.make(solver);
        }
    };

    public abstract IPropagationEngine make(Solver solver);
}
