/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.min_max;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 15/12/2013
 */
public class PropBoolMax extends Propagator<BoolVar> {

    final int n;
    int x1, x2;

    public PropBoolMax(BoolVar[] variables, BoolVar maxVar) {
        super(ArrayUtils.append(variables, new BoolVar[]{maxVar}), PropagatorPriority.UNARY, true);
        n = variables.length;
        x1 = -1;
        x2 = -1;
        assert n > 0;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        x1 = -1;
        x2 = -1;
        for (int i = 0; i < n; i++) {
            if (!vars[i].isInstantiated()) {
                if (x1 == -1) {
                    x1 = i;
                } else if (x2 == -1) {
                    x2 = i;
                }
            } else if (vars[i].getValue() == 1) {
                if (vars[n].instantiateTo(1, aCause)) {
                    setPassive();
                    return;
                }
            }
        }
        filter();
    }

    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == n) {
            filter();
        } else {
            if (vars[idxVarInProp].isInstantiatedTo(1)) {
                if (vars[n].instantiateTo(1, aCause)) {
                    setPassive();
                }
            } else if (idxVarInProp == x1 || idxVarInProp == x2) {
                if (idxVarInProp == x1) {
                    x1 = x2;
                }
                x2 = -1;
                for (int i = 0; i < n; i++) {
                    if (i != x1 && !vars[i].isInstantiated()) {
                        x2 = i;
                        break;
                    }
                }
                filter();
            }
        }
    }

    public void filter() throws ContradictionException {
        if (x1 == -1) {
            if (vars[n].instantiateTo(0, aCause)) {
                setPassive();
                return;
            }
        }
        if (x2 == -1 && vars[n].isInstantiatedTo(1)) {
            if (vars[x1].instantiateTo(1, aCause)) {
                setPassive();
                return;
            }
        }
        if (vars[n].isInstantiatedTo(0)) {
            for (int i = 0; i < n; i++) {
                vars[i].instantiateTo(0, aCause);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int ub = vars[n].getUB();
        for (int i = 0; i < n; i++) {
            if (vars[i].getLB() > ub) {
                return ESat.FALSE;
            }
        }
        for (int i = 0; i < n; i++) {
            if (vars[i].getUB() > ub) {
                return ESat.UNDEFINED;
            }
        }
        if (vars[n].isInstantiated()) {
            for (int i = 0; i < n; i++) {
                if (vars[i].isInstantiatedTo(ub)) {
                    return ESat.TRUE;
                }
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PropBoolMin ");
        sb.append(vars[n]).append(" = min({");
        sb.append(vars[0]);
        for (int i = 1; i < n; i++) {
            sb.append(", ");
            sb.append(vars[i]);
        }
        sb.append("})");
        return sb.toString();

    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.vars.length - 1;
            BoolVar[] aVars = new BoolVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                aVars[i] = (BoolVar) identitymap.get(this.vars[i]);
            }
            this.vars[size].duplicate(solver, identitymap);
            BoolVar M = (BoolVar) identitymap.get(this.vars[size]);
            identitymap.put(this, new PropBoolMax(aVars, M));
        }
    }
}
