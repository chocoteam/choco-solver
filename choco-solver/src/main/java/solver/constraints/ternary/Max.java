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

package solver.constraints.ternary;

import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.IntConstraintFactory;
import solver.variables.IntVar;
import solver.variables.fast.IntervalIntVarImpl;
import util.ESat;
import util.tools.StringUtils;

/**
 * X = MAX(Y,Z)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class Max extends IntConstraint {

    IntVar X, Y, Z;

    public Max(IntVar X, IntVar Y, IntVar Z, Solver solver) {
        super(new IntVar[]{X, Y, Z}, solver);
        this.X = X;
        this.Y = Y;
        this.Z = Z;
//        setPropagators(new PropMax(X, Y, Z, solver, this));
        setPropagators(new PropMaxBC(X, Y, Z));
    }

    public static IntVar var(IntVar a, IntVar b) {
        if (a.getLB() >= b.getUB()) {
            return a;
        } else if (b.getLB() >= a.getUB()) {
            return b;
        } else {
            Solver solver = a.getSolver();
            IntVar z = new IntervalIntVarImpl(StringUtils.randomName(),
                    Math.max(a.getLB(), b.getLB()), Math.max(a.getUB(), b.getUB()), solver);
            solver.post(IntConstraintFactory.maximum(z, a, b));
            return z;
        }
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        return ESat.eval(tuple[0] == Math.max(tuple[1], tuple[2]));
    }

    @Override
    public String toString() {
        return String.format("%s = MAX(%s, %s)", X.getName(), Y.getName(), Z.getName());
    }
}
