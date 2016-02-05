/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

    final Ibex ibex;
    final String functions;
    final int option;
    final int contractorIdx;

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
        this.option = options;
        this.contractorIdx = ibex.add_contractor(vars.length, functions, option);
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
            case Ibex.NOT_SIGNIFICANT:
            default:
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
        if (result == Ibex.ENTAILED || isCompletelyInstantiated()) {
            for (int i = 0; i < vars.length; i++) {
                if (vars[i].getLB() < domains[2 * i] || vars[i].getUB() > domains[2 * i + 1]) {
                    if (domains[2 * i + 1] - domains[2 * i] >= vars[i].getPrecision()) {
                        return ESat.UNDEFINED;
                    } else {
                        return ESat.FALSE;
                    }
                }
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
