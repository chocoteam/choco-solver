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

package solver.constraints.propagators.nary.channeling;

import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;
import util.ESat;
import util.procedure.UnaryIntProcedure;
import util.tools.ArrayUtils;

import java.util.Arrays;

/**
 * X[i] = j+Ox <=> Y[j] = i+Oy
 * <p/>
 * AC propagator for enumerated domain variables
 *
 * @author Jean-Guillaume Fages
 * @since Nov 2012
 */
public class PropInverseChannelAC extends Propagator<IntVar> {

    protected int minX, minY;
    protected int n;
    protected IntVar[] X, Y;
    protected RemProc rem_proc;
    protected IIntDeltaMonitor[] idms;
    private final static int AWAKENING_MASK = EventType.INT_ALL_MASK();

    public PropInverseChannelAC(IntVar[] X, IntVar[] Y, int minX, int minY) {
        super(ArrayUtils.append(X, Y), PropagatorPriority.LINEAR, true);
        for (int i = 0; i < this.vars.length; i++) {
            if (!vars[i].hasEnumeratedDomain()) {
                throw new UnsupportedOperationException("this propagator should be used with enumerated domain variables");
            }
        }
        this.X = Arrays.copyOfRange(this.vars, 0, X.length);
        this.Y = Arrays.copyOfRange(this.vars, X.length, vars.length);
        n = Y.length;
        this.minX = minX;
        this.minY = minY;
        rem_proc = new RemProc();
        this.idms = new IIntDeltaMonitor[this.vars.length];
        for (int i = 0; i < vars.length; i++) {
            idms[i] = this.vars[i].monitorDelta(this);
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return AWAKENING_MASK;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            X[i].updateLowerBound(minX, aCause);
            X[i].updateUpperBound(n - 1 + minX, aCause);
            Y[i].updateLowerBound(minY, aCause);
            Y[i].updateUpperBound(n - 1 + minY, aCause);
        }
        for (int i = 0; i < n; i++) {
            enumeratedFilteringOfX(i);
            enumeratedFilteringOfY(i);
        }
        for (int i = 0; i < vars.length; i++) {
            idms[i].unfreeze();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        idms[varIdx].freeze();
        idms[varIdx].forEach(rem_proc.set(varIdx), EventType.REMOVE);
        idms[varIdx].unfreeze();
    }

    private void enumeratedFilteringOfX(int var) throws ContradictionException {
        // X[i] = j+Ox <=> Y[j] = i+Oy
        int min = X[var].getLB();
        int max = X[var].getUB();
        for (int v = min; v <= max; v = X[var].nextValue(v)) {
            if (!Y[v - minX].contains(var + minY)) {
                X[var].removeValue(v, aCause);
            }
        }
    }

    private void enumeratedFilteringOfY(int var) throws ContradictionException {
        // X[i] = j+Ox <=> Y[j] = i+Oy
        int min = Y[var].getLB();
        int max = Y[var].getUB();
        for (int v = min; v <= max; v = Y[var].nextValue(v)) {
            if (!X[v - minY].contains(var + minX)) {
                Y[var].removeValue(v, aCause);
            }
        }
    }

    private class RemProc implements UnaryIntProcedure<Integer> {
        private int var;

        @Override
        public UnaryIntProcedure set(Integer idxVar) {
            this.var = idxVar;
            return this;
        }

        @Override
        public void execute(int val) throws ContradictionException {
            if (var < n) {
                Y[val - minX].removeValue(var + minY, aCause);
            } else {
                X[val - minY].removeValue(var - n + minX, aCause);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        boolean allInst = true;
        for (int i = 0; i < n; i++) {
            if (!(vars[i].instantiated() && vars[i + n].instantiated())) {
                allInst = false;
            }
            if (X[i].instantiated() && !Y[X[i].getValue() - minX].contains(i + minY)) {
                return ESat.FALSE;
            }
            if (Y[i].instantiated() && !X[Y[i].getValue() - minY].contains(i + minX)) {
                return ESat.FALSE;
            }
        }
        if (allInst) return ESat.TRUE;
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return "Inverse_AC({" + X[0] + "...}{" + Y[0] + "...})";
    }
}
