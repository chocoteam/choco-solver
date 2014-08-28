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
package solver.variables.view;

import gnu.trove.map.hash.THashMap;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.variables.BoolVar;
import solver.variables.Variable;
import util.ESat;

/**
 * A specific view for equality on bool var
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/07/12
 */
public final class BoolEqView extends EqView implements BoolVar {

    protected final BoolVar var;

    public BoolEqView(BoolVar var, Solver solver) {
        super(var, solver);
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
        return new BoolEqView(this.var, this.getSolver());
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            this.var.duplicate(solver, identitymap);
            BoolEqView clone = new BoolEqView((BoolVar) identitymap.get(this.var), solver);
            identitymap.put(this, clone);
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
