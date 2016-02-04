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
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.StringUtils;

/**
 * A constant view specific to boolean variable
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations",
 * C. Schulte and G. Tack
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public class FixedBoolVarImpl extends FixedIntVarImpl implements BoolVar {

    private static final long serialVersionUID = 1L;
    private BoolVar not;

    public FixedBoolVarImpl(String name, int constant, Solver solver) {
        super(name, constant, solver);
        assert constant == 0 || constant == 1 : "FixedBoolVarImpl value should be taken in {0,1}";
    }

    @Override
    public int getTypeAndKind() {
        return Variable.BOOL | Variable.CSTE;
    }

    @Override
    public ESat getBooleanValue() {
        return ESat.eval(constante == 1);
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
    public BoolVar not() {
        if (!hasNot()) {
            not = solver.boolNotView(this);
            not._setNot(this);
        }
        return not;
    }

    @Override
    public void _setNot(BoolVar not) {
        this.not = not;
    }

    @Override
    public boolean isLit() {
        return true;
    }

    @Override
    public boolean hasNot() {
        return not != null;
    }

    @Override
    public boolean isNot() {
        return constante == 0;
    }

    @Override
    public void setNot(boolean isNot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return name + " = " + String.valueOf(constante);
    }

    @Override
    public IntVar duplicate() {
        return solver.intVar(StringUtils.randomName(), this.constante);
    }

}
