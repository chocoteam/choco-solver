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
package solver.constraints.nary.lex;

import common.ESat;
import common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.nary.lex.PropLex;
import solver.variables.IntVar;

/**
 * Enforce a lexicographic ordering on two vectors of integer
 * variables x <_lex y with x = <x_0, ..., x_n>, and y = <y_0, ..., y_n>.
 * ref : Global Constraints for Lexicographic Orderings (Frisch and al)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 10/08/11
 */
public class Lex extends IntConstraint<IntVar> {

    public final int n;
    public final boolean strict;

    public Lex(IntVar[] X, IntVar[] Y, boolean strict, Solver solver) {
        super(ArrayUtils.append(X, Y), solver);
        this.strict = strict;
        this.n = X.length;
        setPropagators(new PropLex(X, Y, strict, solver, this));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        for (int i = 0; i < n; i++) {
            int xi = tuple[i];
            int yi = tuple[i + n];
            if (xi < yi) {
                return ESat.TRUE;
            } else if (xi > yi) {
                return ESat.FALSE;
            }//else xi == yi
        }
        if (strict) {
            return ESat.FALSE;
        } else {
            return ESat.eval(tuple[n - 1] == tuple[n - 1 + n]);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(vars[i].getName());
        }
        sb.append("} <");
        if (!strict) {
            sb.append("=");
        }
        sb.append("_lex {");
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(vars[i + n].getName());
        }
        sb.append("}");
        return sb.toString();
    }
}
