/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nogood;

import org.chocosolver.solver.constraints.nary.sat.PropNogoods;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.util.ESat;

/**
 * Created by cprudhom on 21/01/15.
 * Project: choco.
 */
public class NogoodConstraint extends Constraint {

    private final PropNogoods nogoods;

    public NogoodConstraint(Model model) {
        super(ConstraintsName.NOGOODCONSTRAINT, new PropNogoods(model));
        nogoods = (PropNogoods) propagators[0];
    }

    @Override
    public ESat isSatisfied() {
        return nogoods.isEntailed();
    }

    public PropNogoods getPropNogoods() {
        return nogoods;
    }
}
