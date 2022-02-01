/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.learn;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.clauses.ClauseStore;

/**
 * An abstract class that defines required methods for an explanation.
 * An explanation tells about the reason of a failure.
 * It gives information relatives to:
 * <ul>
 *     <li>decision to backtrack to</li>
 *     <li>additional constraint to add to the model</li>
 * </ul>
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 27/01/2017.
 */
public abstract class IExplanation {

    /**
     * Extract and post the nogood related to this explanation
     * @param mModel the model to post the nogood in
     */
    public abstract void extractConstraint(Model mModel, ClauseStore ngstore);

    /**
     * Recycle this explanation when it is not used anymore.
     */
    public abstract void recycle();
}
