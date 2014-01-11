/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.variables.view;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.VF;
import solver.variables.Variable;
import util.ESat;

/**
 * A constant view specific to boolean variable
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations",
 * C. Schulte and G. Tack
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public class BoolConstantView extends ConstantView implements BoolVar {

    private BoolVar not;

    public BoolConstantView(String name, int constant, Solver solver) {
        super(name, constant, solver);
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
        if (not == null) {
            not = VF.not(this);
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
    public boolean isNot() {
        return constante == 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
