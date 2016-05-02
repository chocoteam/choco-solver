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
package org.chocosolver.solver.constraints.nary.channeling;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.UnaryIntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

/**
 * X[i] = j+Ox <=> Y[j] = i+Oy
 * <p>
 * AC propagator for enumerated domain variables
 *
 * @author Jean-Guillaume Fages
 * @since Nov 2012
 */
public class PropInverseChannelAC extends Propagator<IntVar> {

    private int minX, minY;
    private int n;
    private IntVar[] X, Y;
    private RemProc rem_proc;
    private IIntDeltaMonitor[] idms;
    private ICause cause;

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
        this.cause = this;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            X[i].updateBounds(minX, n - 1 + minX, this);
            Y[i].updateBounds(minY, n - 1 + minY, this);
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
        idms[varIdx].forEachRemVal(rem_proc.set(varIdx));
        idms[varIdx].unfreeze();
    }

    private void enumeratedFilteringOfX(int var) throws ContradictionException {
        // X[i] = j+Ox <=> Y[j] = i+Oy
        int min = X[var].getLB();
        int max = X[var].getUB();
        for (int v = min; v <= max; v = X[var].nextValue(v)) {
            if (!Y[v - minX].contains(var + minY)) {
                X[var].removeValue(v, this);
            }
        }
    }

    private void enumeratedFilteringOfY(int var) throws ContradictionException {
        // X[i] = j+Ox <=> Y[j] = i+Oy
        int min = Y[var].getLB();
        int max = Y[var].getUB();
        for (int v = min; v <= max; v = Y[var].nextValue(v)) {
            if (!X[v - minY].contains(var + minX)) {
                Y[var].removeValue(v, this);
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
                Y[val - minX].removeValue(var + minY, cause);
            } else {
                X[val - minY].removeValue(var - n + minX, cause);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        boolean allInst = true;
        for (int i = 0; i < n; i++) {
            if (!(vars[i].isInstantiated() && vars[i + n].isInstantiated())) {
                allInst = false;
            }
            if (X[i].isInstantiated() && !Y[X[i].getValue() - minX].contains(i + minY)) {
                return ESat.FALSE;
            }
            if (Y[i].isInstantiated() && !X[Y[i].getValue() - minY].contains(i + minX)) {
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

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean nrules = ruleStore.addPropagatorActivationRule(this);
        int idx = 0;
        while (idx < 2 * n && vars[idx] != var) {
            idx++;
        }
        if (!IntEventType.isBound(evt.getMask())) {
            assert evt == IntEventType.REMOVE;
            if (idx < n) {
                nrules |= ruleStore.addRemovalRule(Y[value - minX], idx + minY);
            } else {
                idx -= n;
                nrules |= ruleStore.addRemovalRule(X[value - minY], idx + minX);
            }
        } /*else {
            // initial propagation: nothing can be explained
        }*/
        return nrules;
    }
}
