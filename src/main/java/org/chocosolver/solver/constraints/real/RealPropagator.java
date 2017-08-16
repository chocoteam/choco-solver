/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.real;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.events.RealEventType;
import org.chocosolver.util.ESat;

/**
 * A propagator for real variables.
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 18/07/12
 */
public class RealPropagator extends Propagator<RealVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final Ibex ibex;
    private final String functions;
    private final int contractorIdx;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Create a propagator on real variables, propagated using IBEX.
     * <br/>
     * A constraint is defined using <code>functions</code>.
     * A function is a string declared using the following format:
     * <br/>- the '{i}' tag defines a variable, where 'i' is an explicit index the array of variables <code>vars</code>,
     * <br/>- one or more operators :'+,-,*,/,=,<,>,<=,>=,exp( ),ln( ),max( ),min( ),abs( ),cos( ), sin( ),...'
     * <br/> A complete list is available in the documentation of IBEX.
     * <p>
     * <p>
     * <blockquote><pre>
     * new RealConstraint("({0}*{1})+sin({0})=1.0;ln({0}+[-0.1,0.1])>=2.6", new RealVar[]{x,y}, new String[]{""}, model);
     * </pre>
     * </blockquote>
     *
     * @param functions list of functions, separated by a semi-colon
     * @param vars      array of variables
     * @param options   list of options to give to IBEX
     */
    public RealPropagator(String functions, RealVar[] vars, int options) {
        super(vars, PropagatorPriority.LINEAR, false);
        this.ibex = model.getIbex();
        this.functions = functions;
        this.contractorIdx = ibex.add_contractor(vars.length, functions, options);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return RealEventType.BOUND.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        double domains[] = new double[2 * vars.length];
        for (int i = 0; i < vars.length; i++) {
            domains[2 * i] = vars[i].getLB();
            domains[2 * i + 1] = vars[i].getUB();
        }
        int result = ibex.contract(contractorIdx, domains);
        switch (result) {
            case Ibex.FAIL:
                 // "Ibex failed"
                fails();
                break;
            case Ibex.CONTRACT:
                for (int i = 0; i < vars.length; i++) {
                    vars[i].updateBounds(domains[2 * i], domains[2 * i + 1], this);
                }
                break;
            case Ibex.ENTAILED:
                for (int i = 0; i < vars.length; i++) {
                    vars[i].updateBounds(domains[2 * i], domains[2 * i + 1], this);
                }
                setPassive();
                break;
            case Ibex.NOT_SIGNIFICANT: break;
            default: break;
        }
    }

    @Override
    public ESat isEntailed() {
        double domains[] = new double[2 * vars.length];
        for (int i = 0; i < vars.length; i++) {
            domains[2 * i] = vars[i].getLB();
            domains[2 * i + 1] = vars[i].getUB();
        }
        int result = ibex.contract(contractorIdx, domains);
        if (result == Ibex.FAIL) {
            return ESat.FALSE;
        }
        for (int i = 0; i < vars.length; i++) {
            // if current domain is larger than domain after contraction
            if ((vars[i].getLB() < domains[2 * i] || vars[i].getUB() > domains[2 * i + 1]) && !vars[i].isInstantiated()) {
                return ESat.UNDEFINED;
            }
        }
        return ESat.TRUE;
    }

    @Override
    public String toString() {
        return super.toString()+" ->(\""+functions + "\")";
    }
}
