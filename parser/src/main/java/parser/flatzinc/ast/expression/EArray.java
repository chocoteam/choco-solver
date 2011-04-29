/**
 *  Copyright (c) 2010, Ecole des Mines de Nantes
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

package parser.flatzinc.ast.expression;

import solver.Solver;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.SetVar;

import java.util.List;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 11 janv. 2010
* Since : Choco 2.1.1
*
* Class for array expressions based on flatzinc-like objects.
*/
public final class EArray extends Expression {

    public final List<Expression> what;

    public EArray(List<Expression> what) {
        super(EType.ARR);
        this.what = what;
    }

    public Expression getWhat_i(int i) {
        return what.get(i);
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder("[");
        st.append(what.get(0).toString());
        for (int i = 1; i < what.size(); i++) {
            st.append(',').append(what.get(i).toString());
        }
        return st.append(']').toString();
    }


    @Override
    public int[] toIntArray() {
        int[] arr = new int[what.size()];
        for (int i = 0; i < what.size(); i++) {
            arr[i] = what.get(i).intValue();
        }
        return arr;
    }

    @Override
    public BoolVar[] toBoolVarArray(Solver solver) {
        BoolVar[] arr = new BoolVar[what.size()];
        for (int i = 0; i < what.size(); i++) {
            arr[i] = what.get(i).boolVarValue(solver);
        }
        return arr;
    }

    @Override
    public IntVar[] toIntVarArray(Solver solver) {
        IntVar[] arr = new IntVar[what.size()];
        for (int i = 0; i < what.size(); i++) {
            arr[i] = what.get(i).intVarValue(solver);
        }
        return arr;
    }

    @Override
    public SetVar[] toSetVarArray(Solver solver) {
        SetVar[] arr = new SetVar[what.size()];
        for (int i = 0; i < what.size(); i++) {
            arr[i] = what.get(i).setVarValue(solver);
        }
        return arr;
    }
}
