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
package solver.constraints.extension.nary;

import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.constraints.extension.Tuples;
import solver.variables.IntVar;
import util.ESat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public abstract class PropLargeCSP<R extends LargeRelation> extends Propagator<IntVar> {

    protected final R relation;

    protected PropLargeCSP(IntVar[] vars, Tuples tuples) {
        super(vars, PropagatorPriority.QUADRATIC, true);
        this.relation = makeRelation(tuples, vars);
    }

    protected abstract R makeRelation(Tuples tuples, IntVar[] vars);


    public final LargeRelation getRelation() {
        return relation;
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int[] tuple = new int[vars.length];
            for (int i = 0; i < vars.length; i++) {
                tuple[i] = vars[i].getValue();
            }
            return ESat.eval(relation.isConsistent(tuple));
        }
        return ESat.UNDEFINED;
//        return ESat.TRUE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CSPLarge({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(vars[i]).append(", ");
        }
        sb.append("})");
        return sb.toString();
    }
}
