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
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/07/12
 */
public class IntToRealPropagator extends Propagator<IntVar> {

    final Ibex ibex;
    final int contractorIdx;

    /**
     * Create a propagator informing Ibex of variable to discretize
     *
     * @param ibex continuous solver
     * @param cIdx index of the propagator in Ibex
     * @param vars array of integer variables
     */
    public IntToRealPropagator(Ibex ibex, int cIdx, IntVar[] vars) {
        super(vars, PropagatorPriority.LINEAR);
        this.ibex = ibex;
        this.contractorIdx = cIdx;
        this.ibex.add_int_ctr(vars.length);
    }


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
                    vars[i].updateLowerBound((int) domains[2 * i], aCause);
                    vars[i].updateUpperBound((int) domains[2 * i + 1], aCause);
                }
                return;
            case Ibex.ENTAILED:
                return;
            case Ibex.NOT_SIGNIFICANT:
            default:
                return;
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        forcePropagate(EventType.FULL_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; //CPRU: we assume IBEX correctly contract domains
    }
}
