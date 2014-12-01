/**
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package org.chocosolver.solver.constraints.nary.channeling;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Constraints that map the boolean assignments variables (bvars) with the standard assignment variables (var).
 * var = i <-> bvars[i-offSet] = true
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 22/05/13
 */
public class PropEnumDomainChanneling extends Propagator<IntVar> {

    protected final int n;
    protected final IntProcedure rem_proc;
    protected final IIntDeltaMonitor idm;
    protected final int offSet;

    public PropEnumDomainChanneling(BoolVar[] bvars, IntVar aVar, final int offSet) {
        super(ArrayUtils.append(bvars, new IntVar[]{aVar}), PropagatorPriority.UNARY, true);
        assert aVar.hasEnumeratedDomain();
        this.n = bvars.length;
        this.offSet = offSet;
        this.idm = this.vars[n].monitorDelta(this);
        this.rem_proc = i -> vars[i - offSet].instantiateTo(0, aCause);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        vars[n].updateLowerBound(offSet, aCause);
        vars[n].updateUpperBound(n - 1 + offSet, aCause);
        for (int i = 0; i < n; i++) {
            if (vars[i].isInstantiated()) {
                if (vars[i].getValue() == 0) {
                    vars[n].removeValue(i + offSet, aCause);
                } else {
                    vars[n].instantiateTo(i + offSet, aCause);
                }
            } else if (!vars[n].contains(i + offSet)) {
                vars[i].instantiateTo(0, aCause);
            }
        }
        if (vars[n].isInstantiated()) {
            int v = vars[n].getValue() - offSet;
            vars[v].instantiateTo(1, aCause);
            for (int i = 0; i < n; i++) {
                if (i != v) {
                    vars[i].instantiateTo(0, aCause);
                }
            }
        }
        idm.unfreeze();
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (varIdx == n) {
            idm.freeze();
            idm.forEachRemVal(rem_proc);
            idm.unfreeze();
        } else {
            if (vars[varIdx].getValue() == 1) {
                vars[n].instantiateTo(varIdx + offSet, aCause);
                for (int i = 0; i < n; i++) {
                    if (i != varIdx) {
                        vars[i].instantiateTo(0, aCause);
                    }
                }
            } else {
                vars[n].removeValue(varIdx + offSet, aCause);
            }
        }
        if (vars[n].isInstantiated()) {
            vars[vars[n].getValue() - offSet].instantiateTo(1, aCause);
        }
    }

    @Override
    public ESat isEntailed() {
        if (vars[n].getLB() > n - 1 + offSet || vars[n].getUB() < offSet) {
            return ESat.FALSE;
        }
        for (int i = 0; i < n; i++) {
            if (vars[i].isInstantiated()) {
                if (vars[i].getValue() == 1 && !vars[n].contains(i + offSet)) {
                    return ESat.FALSE;
                }
            }
        }
        if (vars[n].isInstantiated()) {
            int v = vars[n].getValue() - offSet;
            if (!vars[v].contains(1)) {
                return ESat.FALSE;
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.vars.length - 1;
            BoolVar[] bVars = new BoolVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                bVars[i] = (BoolVar) identitymap.get(this.vars[i]);
            }
            this.vars[size].duplicate(solver, identitymap);
            IntVar aVar = (IntVar) identitymap.get(this.vars[size]);
            identitymap.put(this, new PropEnumDomainChanneling(bVars, aVar, this.offSet));
        }
    }
}
