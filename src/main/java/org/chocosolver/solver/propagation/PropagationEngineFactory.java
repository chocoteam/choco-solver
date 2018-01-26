/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.propagation;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.propagation.hardcoded.SevenQueuesPropagatorEngine;
import org.chocosolver.solver.propagation.hardcoded.TwoBucketPropagationEngine;

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
        public IPropagationEngine make(Model model) {
            return new SevenQueuesPropagatorEngine(model);
        }
    },

    /**
     * Create a propagation engine which handles both priority and separated coarse propagation.
     */
    TWOBUCKETPROPAGATIONENGINE() {
        @Override
        public IPropagationEngine make(Model model) {
            return new TwoBucketPropagationEngine(model);
        }
    },

    DEFAULT() {
        @Override
        public IPropagationEngine make(Model model) {
            return PROPAGATORDRIVEN_7QD.make(model);
        }
    };

    public abstract IPropagationEngine make(Model model);
}
