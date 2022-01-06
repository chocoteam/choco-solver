/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.limits;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.measure.IMeasures;

/**
 * Set a limit over the number of found solutions allowed during the search.
 * When this limit is reached, the search loop is informed and the resolution is stopped.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15 juil. 2010
 */
public class SolutionCounter extends ACounter{

    public SolutionCounter(Model model, long solutionlimit) {
        this(model.getSolver().getMeasures(), solutionlimit);
    }

    public SolutionCounter(IMeasures measures, long solutionlimit) {
        super(measures, solutionlimit);
    }

    @Override
    public long currentValue() {
        return measures.getSolutionCount();
    }
}
