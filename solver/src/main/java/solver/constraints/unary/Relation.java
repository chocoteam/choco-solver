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
package solver.constraints.unary;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.binary.PropEqualXY;
import solver.constraints.propagators.binary.PropGreaterOrEqualXY;
import solver.constraints.propagators.binary.PropNotEqualXY;
import solver.variables.IntVar;
import solver.variables.view.Views;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/08/11
 */
public class Relation extends IntConstraint<IntVar> {
    public enum R{
        EQ, NQ, GQ, LQ, GT, LT
    }

    private final R r;
    private final int c;

    public Relation(IntVar var, R relation, int cste, Solver solver) {
        super(ArrayUtils.toArray(var), solver);
        this.r = relation;
        this.c = cste;
        switch (r){
            case EQ:
                setPropagators(new PropEqualXY(var, Views.fixed(cste, solver), solver, this));
                break;
            case NQ:
                setPropagators(new PropNotEqualXY(var, Views.fixed(cste, solver), solver, this));
                break;
            case GQ:
                setPropagators(new PropGreaterOrEqualXY(var, Views.fixed(cste, solver), solver, this));
                break;
            case LQ:
                setPropagators(new PropGreaterOrEqualXY(Views.fixed(cste, solver), var, solver, this));
                break;
            case GT:
                setPropagators(new PropGreaterOrEqualXY(var, Views.fixed(cste+1, solver), solver, this));
                break;
            case LT:
                setPropagators(new PropGreaterOrEqualXY(Views.fixed(cste-1, solver),var, solver, this));
                break;
        }
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        switch (r){
            case EQ:
                return ESat.eval(tuple[0] == c);
            case NQ:
                return ESat.eval(tuple[0] != c);
            case GQ:
                return ESat.eval(tuple[0] >= c);
            case LQ:
                return ESat.eval(tuple[0] <= c);
            case GT:
                return ESat.eval(tuple[0] > c);
            case LT:
                return ESat.eval(tuple[0] < c);
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder(vars[0].getName());
        switch (r){
            case EQ:
                st.append("=");
                break;
            case NQ:
                st.append("=/=");
                break;
            case GQ:
                st.append(">=");
                break;
            case LQ:
                st.append("<=");
                break;
            case GT:
                st.append(">");
                break;
            case LT:
                st.append("<");
                break;
        }
        st.append(c);
        return st.toString();
    }
}
