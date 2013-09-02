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
package solver.constraints.binary;

import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.Operator;
import solver.exception.SolverException;
import solver.variables.IntVar;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * Ensures: <br/>
 * |X-Y| OP C
 * <br/>
 * where OP can take its value among {=, >, <, =/=}
 *
 * @author Charles Prud'homme
 * @since 21/03/12
 */
public class DistanceXYC extends IntConstraint<IntVar> {

    final int cste;
    final Operator operator;


    public DistanceXYC(IntVar X, IntVar Y, Operator operator, int cste, Solver solver) {
        super(ArrayUtils.toArray(X, Y), solver);
        if (operator != Operator.EQ && operator != Operator.GT && operator != Operator.LT && operator != Operator.NQ) {
            throw new SolverException("Unexpected operator for distance");
        }
        this.cste = cste;
        this.operator = operator;
        setPropagators(new PropDistanceXYC(vars, operator, cste));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        if (operator == Operator.EQ) {
            return ESat.eval(Math.abs(tuple[0] - tuple[1]) == cste);
        } else if (operator == Operator.LT) {
            return ESat.eval(Math.abs(tuple[0] - tuple[1]) < cste);
        } else if (operator == Operator.GT) {
            return ESat.eval(Math.abs(tuple[0] - tuple[1]) > cste);
        } else if (operator == Operator.NQ) {
            return ESat.eval(Math.abs(tuple[0] - tuple[1]) != cste);
        } else {
            throw new SolverException("operator not known");
        }
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("|").append(vars[0].getName()).append(" - ").append(vars[1].getName()).append("|");
        switch (operator) {
            case EQ:
                st.append("=");
                break;
            case GT:
                st.append(">");
                break;
            case LT:
                st.append("<");
                break;
            case NQ:
                st.append("=/=");
                break;
        }
        st.append(cste);
        return st.toString();
    }
}
