/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.real;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.RealEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.VariableUtils;

/**
 * A propagator for real variables. <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 18/07/12
 */
public class RealPropagator extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final IbexHandler ibex;
    protected final String functions;
    BoolVar reified;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Create a propagator on real variables, propagated using IBEX. <br/> A constraint is defined
     * using <code>functions</code>. A function is a string declared using the following format:
     * <br/>- the '{i}' tag defines a variable, where 'i' is an explicit index the array of
     * variables <code>vars</code>, <br/>- one or more operators :'+,-,*,/,=,<,>,<=,>=,exp( ),ln(
     * ),max( ),min( ),abs( ),cos( ), sin( ),...' <br/> A complete list is available in the
     * documentation of IBEX. <p> <p>
     * <blockquote><pre>
     * new RealConstraint("({0}*{1})+sin({0})=1.0;ln({0}+[-0.1,0.1])>=2.6", new RealVar[]{x,y}, new
     * String[]{""}, model);
     * </pre>
     * </blockquote>
     *
     * @param functions list of functions, separated by a semi-colon
     * @param vars      array of variables
     */
    public RealPropagator(String functions, Variable[] vars) {
        super(vars, PropagatorPriority.LINEAR, false);
        this.ibex = model.getIbexHandler();
        this.functions = functions;
        ibex.declare(this);
    }

    protected void reify(BoolVar r) {
        this.addVariable(r);
        reified = r;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (VariableUtils.isReal(vars[vIdx])) {
            return RealEventType.BOUND.getMask();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ibex.contract(this);
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }

    /**
     * Defines the ratio that real domains must be contract by ibex
     * to compute the constraint. A contraction is considered as significant
     * when at least {@param ratio} of a domain has been reduced.
     * If the contraction is not meet, then it is considered as insufficient
     * and therefore ignored.
     *
     * @param ratio defines the ratio that a domains must be contract to
     *              compute the constraint.
     */
    public void setContractionRatio(double ratio) {
        ibex.setContractionRatio(ratio);
    }

    public double getContractionRatio() {
        return ibex.getContractionRatio();
    }

    @Override
    public String toString() {
        return super.toString() + " ->(\"" + functions + "\")";
    }

}
