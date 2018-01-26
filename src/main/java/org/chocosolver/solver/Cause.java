/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/08/11
 */
public enum Cause implements ICause {
    Null{
        @Override
        public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
            return false;
        }
    }

}
