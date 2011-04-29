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

package solver.constraints.binary;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.binary.PropEqualX_YC;
import solver.variables.IntVar;

/**
 * X = Y + C
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
public class EqualX_YC extends IntConstraint<IntVar> {

    final IntVar x;
    final IntVar y;
    final int cste;


    public EqualX_YC(IntVar x, IntVar y, int c, Solver solver) {
        this(x, y, c, solver, _DEFAULT_THRESHOLD);
    }

    public EqualX_YC(IntVar x, IntVar y, int c, Solver solver,
                     PropagatorPriority storeThreshold) {
        super(ArrayUtils.toArray(x, y), solver, storeThreshold);
        this.x = x;
        this.y = y;
        this.cste = c;
        setPropagators(new PropEqualX_YC(vars, c, solver.getEnvironment(), this));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        return ESat.eval(tuple[0] == tuple[1] + this.cste);
    }

    public String toString() {
        StringBuilder s = new StringBuilder("");
        s.append(x).append(" = ").append(y).append(" + ").append(cste);
        return s.toString();
    }
}
