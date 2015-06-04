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
package org.chocosolver.solver.variables.view;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

/**
 * A specific view for equality on bool var
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/07/12
 */
public final class BoolEqView extends EqView implements BoolVar {

    protected final BoolVar var;

    public BoolEqView(BoolVar var) {
        super(var);
        this.var = var;
    }

    @Override
    public ESat getBooleanValue() {
        return var.getBooleanValue();
    }

    @Override
    public boolean setToTrue(ICause cause) throws ContradictionException {
        return instantiateTo(1, cause);
    }

    @Override
    public boolean setToFalse(ICause cause) throws ContradictionException {
        return instantiateTo(0, cause);
    }

    @Override
    public BoolVar duplicate() {
        return new BoolEqView(this.var);
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            this.var.duplicate(solver, identitymap);
            BoolEqView clone = new BoolEqView((BoolVar) identitymap.get(this.var));
            identitymap.put(this, clone);
            for (int i = mIdx - 1; i >= 0; i--) {
                monitors[i].duplicate(solver, identitymap);
            }
        }
    }

    @Override
    public BoolVar not() {
        return var.not();
    }

    @Override
    public boolean hasNot() {
        return var.hasNot();
    }

    @Override
    public void _setNot(BoolVar not) {
        throw new SolverException("Unexpected call to BoolEqView._setNot()");
    }

    @Override
    public boolean isLit() {
        return true;
    }

    @Override
    public boolean isNot() {
        return var.isNot();
    }

    @Override
    public void setNot(boolean isNot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTypeAndKind() {
        return Variable.VIEW | Variable.BOOL;
    }
}
