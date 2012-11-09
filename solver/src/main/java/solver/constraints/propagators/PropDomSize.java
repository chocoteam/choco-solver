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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 27/07/12
 * Time: 14:05
 */

package solver.constraints.propagators;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.UnaryIntProcedure;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;

public class PropDomSize extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    IStateInt[] size;
    int n;
    protected final IIntDeltaMonitor[] idms;
    private DirectedRemProc remProc;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropDomSize(IntVar[] vars, Constraint c, Solver s) {
        super(vars, s, c, PropagatorPriority.UNARY, false);
        n = vars.length;
        size = new IStateInt[n];
        for (int i = 0; i < n; i++) {
            size[i] = environment.makeInt(vars[i].getDomainSize());
        }
        this.idms = new IIntDeltaMonitor[this.vars.length];
        for (int i = 0; i < this.vars.length; i++) {
            idms[i] = this.vars[i].monitorDelta(this);
        }
        remProc = new DirectedRemProc();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        assert this.getNbPendingER() == 0;
        for (int i = 0; i < n; i++) {
            idms[i].unfreeze();
            size[i].set(vars[i].getDomainSize());
        }

    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        idms[varIdx].freeze();
        idms[varIdx].forEach(remProc.set(varIdx), EventType.REMOVE);
        idms[varIdx].unfreeze();
        if (size[varIdx].get() != vars[varIdx].getDomainSize()) {
            throw new UnsupportedOperationException(size[varIdx].get() + " != " + vars[varIdx].getDomainSize());
        }
        forcePropagate(EventType.FULL_PROPAGATION);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INT_ALL_MASK();
    }

    @Override
    public int getPropagationConditions() {
        return EventType.FULL_PROPAGATION.mask;
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }

    private class DirectedRemProc implements UnaryIntProcedure<Integer> {
        int idx;

        public void execute(int i) throws ContradictionException {
            size[idx].add(-1);
        }

        @Override
        public UnaryIntProcedure set(Integer idx) {
            this.idx = idx;
            return this;
        }
    }
}
