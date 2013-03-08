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
package solver.constraints.propagators.real;

import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.real.Ibex;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.RealVar;
import solver.variables.Variable;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * A propagator for real variable.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/07/12
 */
public class RealReifiedPropagator extends Propagator<Variable> {

    final Ibex ibex;
    final String functions;
    final int option;
    final int contractorIdx;
    final BoolVar bvar;
    final RealVar[] rvars;

    /**
     * Create a propagator on real variables, propagated using IBEX.
     * <br/>
     * A constraint is defined using <code>functions</code>.
     * A function is a string declared using the following format:
     * <br/>- the '{i}' tag defines a variable, where 'i' is an explicit index the array of variables <code>vars</code>,
     * <br/>- one or more operators :'+,-,*,/,=,<,>,<=,>=,exp( ),ln( ),max( ),min( ),abs( ),cos( ), sin( ),...'
     * <br/> A complete list is available in the documentation of IBEX.
     * <p/>
     *
     * @param ibex      continuous solver
     * @param functions list of functions, separated by a semi-colon
     * @param bvar      a boolean variable
     * @param variables array of real variables
     * @param options   list of options to give to IBEX
     */
    public RealReifiedPropagator(Ibex ibex, int cIdx, String functions, BoolVar bvar, RealVar[] variables, int options) {
        super(ArrayUtils.<Variable>append(new BoolVar[]{bvar}, variables), PropagatorPriority.LINEAR, false);
        this.contractorIdx = cIdx;
        this.ibex = ibex;
        this.functions = functions;
        this.option = options;
        this.ibex.add_ctr(vars.length, functions, option);
        rvars = new RealVar[variables.length];
        for (int i = 0; i < variables.length; i++) {
            rvars[i] = (RealVar) vars[i + 1];
        }
        this.bvar = (BoolVar) vars[0];
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.BOUND.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        double domains[] = new double[2 * rvars.length];
        for (int i = 0; i < rvars.length; i++) {
            domains[2 * i] = rvars[i].getLB();
            domains[2 * i + 1] = rvars[i].getUB();
        }
        int bstatus = (bvar.instantiated() ? (bvar.getValue() == 1 ? Ibex.TRUE : Ibex.FALSE) : Ibex.FALSE_OR_TRUE);

        int result = ibex.contract(contractorIdx, domains, bstatus);

        switch (bstatus) {
            case Ibex.FALSE: // the reified boolean variable is set to FALSE
                switch (result) {
                    case Ibex.FAIL: // and the constraint failed (~ always unsatisfied)
                        update(domains);
                        this.setPassive();
                        return;

                    case Ibex.ENTAILED: // and the constraint is entailed (~ always satisfied)
                        contradiction(null, "Ibex failed on reification");
                        return;

                    case Ibex.CONTRACT: // and the constraint filters but cannot state on its status
                        update(domains);
                        return;

                    default:
                    case Ibex.NOT_SIGNIFICANT: // and the constraint does not filter nor state on its status
                        // nothing to do
                        return;

                }
            case Ibex.TRUE:  // the reified boolean variable is set to TRUE
                switch (result) {
                    case Ibex.FAIL: // and the constraint failed (~ always unsatisfied)
                        contradiction(null, "Ibex failed on reification");
                        return;

                    case Ibex.ENTAILED: // and the constraint is entailed (~ always satisfied)
                        update(domains);
                        this.setPassive();
                        return;

                    case Ibex.CONTRACT: // and the constraint filters but cannot state on its status
                        update(domains);
                        return;

                    default:
                    case Ibex.NOT_SIGNIFICANT:  // and the constraint does not filter nor state on its status
                        // nothing to do
                        return;

                }
            case Ibex.FALSE_OR_TRUE:  // the reified boolean variable is UNKNOWN
                switch (result) {
                    case Ibex.FAIL: // and the constraint failed (~ always unsatisfied)
                        bvar.setToFalse(aCause);
                        this.setPassive();
                        return;

                    case Ibex.ENTAILED: // and the constraint is entailed (~ always satisfied)
                        bvar.setToTrue(aCause);
                        this.setPassive();
                        return;

                    case Ibex.CONTRACT: // and the constraint filters but cannot state on its status
                        //  => unexpected!!
                        assert false : "RealReifiedPropagator: the constraint filters although the boolean is unknown";
                    default:
                    case Ibex.NOT_SIGNIFICANT: // and the constraint does not filter nor state on its status
                        // nothing to do

                }
        }
    }

    private void update(double[] domains) throws ContradictionException {
        for (int i = 0; i < vars.length; i++) {
            rvars[i].updateBounds(domains[2 * i], domains[2 * i + 1], aCause);
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        forcePropagate(EventType.FULL_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; //we assume IBEX correctly contracts domains
    }
}
