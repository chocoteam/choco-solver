/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.IView;

/**
 * A wrapper for integer variable to provide a boolean view.
 * This class is based on the <i>Lazy clause generation</i> technique.
 * <p>
 * The variable is considered as a boolean variable and the associated literal is created and managed by the SAT solver.
 * </p>
 *
 * @author Charles Prud'homme
 * @since 09/02/2024
 */
@Explained
public class BoolViewEagerLit extends BoolVarEagerLit implements IView<IntVar> {

    IntVar var;

    /**
     * Create a boolean variable for LCG.
     */
    public BoolViewEagerLit(IntVar var, int rel, int cste) {
        super("lit:" + var.getLit(cste, rel), var.getModel(), var.getLit(cste, rel));
        this.var = var;
        this.type = VAR;
    }

    @Override
    public IntVar[] getVariables() {
        return new IntVar[]{var};
    }

    @Override
    public int getNbObservedVariables() {
        return 1;
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        IView.super.notify(event, variableIdx);
    }
}
