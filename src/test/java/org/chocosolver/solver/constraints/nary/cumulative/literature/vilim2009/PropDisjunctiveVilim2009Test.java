/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature.vilim2009;

import org.chocosolver.solver.constraints.nary.cumulative.literature.AbstractDisjunctiveTest;
import org.chocosolver.solver.constraints.nary.cumulative.literature.DisjunctiveFilter;
import org.chocosolver.solver.variables.Task;

/**
 @author Arthur Godet <arth.godet@gmail.com>
 @since 23/05/2019
 */
public class PropDisjunctiveVilim2009Test extends AbstractDisjunctiveTest {

    @Override
    public DisjunctiveFilter propagator(Task[] tasks) {
        return new PropDisjunctiveVilim2009(tasks, true, true, true);
    }
}
