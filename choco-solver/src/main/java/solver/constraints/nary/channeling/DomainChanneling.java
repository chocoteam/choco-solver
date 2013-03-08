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
package solver.constraints.nary.channeling;

import common.ESat;
import common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.nary.channeling.PropDomainChanneling;
import solver.variables.BoolVar;
import solver.variables.IntVar;

/**
 * Constraints that map the boolean assignments variables (bvars) with the standard assignment variables (var).
 * var = i -> bvars[i] = 1
 * <br/>
 *
 * @author Xavier Lorca
 * @author Hadrien Cambazard
 * @author Fabien Hermenier
 * @author Charles Prud'homme
 * @since 04/08/11
 */
public class DomainChanneling extends IntConstraint<IntVar> {

    public DomainChanneling(BoolVar[] bs, IntVar x, Solver solver) {
        super(ArrayUtils.append(bs, new IntVar[]{x}), solver);
        setPropagators(new PropDomainChanneling(bs, x));

    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        int val = tuple[tuple.length - 1];
        for (int i = 0; i < tuple.length - 1; i++) {
            if (i != val && tuple[i] != 0) {
                return ESat.FALSE;
            } else if (i == val && tuple[i] != 1) {
                return ESat.FALSE;
            }
        }
        return ESat.eval(!(val < 0 || val > tuple.length - 1));
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append(vars[vars.length - 1].getName()).append(" = i => <");
        int i = 0;
        for (; i < Math.min(3, vars.length - 2); i++) {
            st.append(vars[i].getName()).append(", ");
        }
        if (i < vars.length - 2) {
            st.append("..., ");
        }
        st.append(vars[vars.length - 2].getName()).append(">[i] = 1");
        return st.toString();
    }
}
