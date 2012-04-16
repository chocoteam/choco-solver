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
package solver.constraints.ternary;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.ternary.PropDistanceXYZ;
import solver.exception.SolverException;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/04/12
 */
public class DistanceXYZ extends IntConstraint<IntVar> {

    public static enum Relop {
        EQ(0), LT(1), GT(2);
        final int pval;

        Relop(int pval) {
            this.pval = pval;
        }
    }

    Relop operator;


    public DistanceXYZ(IntVar x, IntVar y, Relop op, IntVar z, Solver solver) {
        super(new IntVar[]{x, y, z}, solver);
        this.operator = op;
        setPropagators(new PropDistanceXYZ(vars, op.pval, solver, this));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        if (operator == Relop.EQ) {
            return ESat.eval(Math.abs(tuple[0] - tuple[1]) == tuple[2]);
        } else if (operator == Relop.LT) {
            return ESat.eval(Math.abs(tuple[0] - tuple[1]) < tuple[2]);
        } else if (operator == Relop.GT) {
            return ESat.eval(Math.abs(tuple[0] - tuple[1]) > tuple[2]);
        } else {
            throw new SolverException("operator not known");
        }
    }
}
