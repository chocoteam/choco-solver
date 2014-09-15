/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.real;

import gnu.trove.map.hash.THashMap;
import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.RealVar;
import util.ESat;

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
     * <p/>
     * <p/>
     * <blockquote><pre>
     * new RealConstraint("({0}*{1})+sin({0})=1.0;ln({0}+[-0.1,0.1])>=2.6", new RealVar[]{x,y}, new String[]{""}, solver);
     * </pre>
     * </blockquote>
     *
     * @param functions list of functions, separated by a semi-colon
     * @param vars      array of variables
     * @param options   list of options to give to IBEX
     */
    public RealPropagator(String functions, RealVar[] vars, int options) {
        super(vars, PropagatorPriority.LINEAR, false);
		this.ibex = solver.getIbex();
        this.functions = functions;
        this.option = options;
        this.contractorIdx = ibex.add_contractor(vars.length, functions, option);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.BOUND.mask;
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
                contradiction(null, "Ibex failed");
            case Ibex.CONTRACT:
                for (int i = 0; i < vars.length; i++) {
                    vars[i].updateBounds(domains[2 * i], domains[2 * i + 1], aCause);
                }
                break;
            case Ibex.ENTAILED:
                for (int i = 0; i < vars.length; i++) {
                    vars[i].updateBounds(domains[2 * i], domains[2 * i + 1], aCause);
                }
                setPassive();
                break;
            case Ibex.NOT_SIGNIFICANT:
            default:
        }
	}

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        propagate(0);
    }

    @Override
    public ESat isEntailed() {
		double domains[] = new double[2 * vars.length];
		for (int i = 0; i < vars.length; i++) {
			domains[2 * i] = vars[i].getLB();
			domains[2 * i + 1] = vars[i].getUB();
		}
		int result = ibex.contract(contractorIdx, domains);
		if(result==Ibex.FAIL){
			return ESat.FALSE;
		}
		if(result==Ibex.ENTAILED || isCompletelyInstantiated()){
			for (int i = 0; i < vars.length; i++) {
				if(vars[i].getLB()<domains[2*i] || vars[i].getUB()>domains[2*i+1]){
					return ESat.UNDEFINED;
				}
			}
			return ESat.TRUE;
		}
        return ESat.UNDEFINED;
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = vars.length;
            RealVar[] rvars = new RealVar[size];
            for (int i = 0; i < size; i++) {
                vars[i].duplicate(solver, identitymap);
                rvars[i] = (RealVar) identitymap.get(vars[i]);
            }
            identitymap.put(this, new RealPropagator(functions, rvars, option));
        }
    }
}
