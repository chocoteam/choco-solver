/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.decision;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.real.IbexHandler;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;

/**
 * A decision that delegates the search process to Ibex <br/>
 *
 * @author Charles Prud'homme
 * @since 4.0.6
 */
public class IbexDecision extends Decision<Variable> {

    private static final long serialVersionUID = -4723411613242027369L;
    private IbexHandler handler;
    private Model model;
    /**
     * Create an decision based on an {@link RealVar}
     */
    public IbexDecision(Model model) {
        super(1);
        this.model = model;
        this.handler = model.getIbexHandler();
    }

    @Override
    public Object getDecisionValue() {
        return null;
    }

    @Override
    public void apply() throws ContradictionException {
        boolean init = branch == 1;
        if(handler.nextSolution(init)){
            max_branching++;
            handler.injectDomain();
        }else{
            // failure
            model.getSolver().throwsException(this, null, "ibex");
        }
    }

    @Override
    public void free() {
        // nothing to do
    }

    public boolean inUse(){
        return branch > 0;
    }

    @Override
    public String toString() {
        return "Delegate to Ibex ...";
    }
}
