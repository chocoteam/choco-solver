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

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.binary.PropElement;
import solver.constraints.propagators.nary.channeling.PropElementV;
import solver.variables.IntVar;

/**
 * VALUE = TABLE[INDEX]
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20 sept. 2010
 */
public class Element extends IntConstraint<IntVar> {

    final int[] values;
    final int offset;

    /**
     * Build ELMENT constraint: VALUE = TABLE[INDEX]
     *
     * @param value  VALUE
     * @param values TABLE
     * @param index  INDEX
     * @param offset offset matching INDEX.LB and TABLE[0]
     * @param solver the attached solver
     */
    public Element(IntVar value, int[] values, IntVar index, int offset, Solver solver) {
        this(value, values, index, offset, "none", solver);
    }

    /**
     * Build ELMENT constraint: VALUE = TABLE[INDEX]
     *
     * @param value  VALUE
     * @param values TABLE
     * @param index  INDEX
     * @param offset offset matching INDEX.LB and TABLE[0]
     * @param sort   "asc","desc", detect" : values are sorted wrt <code>sort</code>
     * @param solver the attached solver
     */
    public Element(IntVar value, int[] values, IntVar index, int offset, String sort, Solver solver) {
        super(ArrayUtils.toArray(value, index), solver);
        this.values = values;
        this.offset = offset;
        setPropagators(new PropElement(vars[0], values, vars[1], offset, PropElement.Sort.valueOf(sort), solver, this));
    }

    /**
     * Build ELMENT constraint: VALUE = TABLE[INDEX]
     *
     * @param value  VALUE
     * @param values TABLE
     * @param index  INDEX
     * @param solver the attached solver
     */
    public Element(IntVar value, int[] values, IntVar index, Solver solver) {
        this(value, values, index, 0, "none", solver);
    }

    /**
     * Build ELMENT constraint: VALUE = TABLE[INDEX]
     *
     * @param value  VALUE
     * @param values TABLE
     * @param index  INDEX
     * @param sort   "asc","desc", detect" : values are sorted wrt <code>sort</code>
     * @param solver the attached solver
     */
    public Element(IntVar value, int[] values, IntVar index, String sort, Solver solver) {
        this(value, values, index, 0, sort, solver);
    }

    public Element(IntVar value, IntVar[] values, IntVar index, int offset, Solver solver) {
        super(ArrayUtils.append(new IntVar[]{value, index}, values), solver);
        this.values = new int[0];
        this.offset = offset;
        //CPRU  double to simulate idempotency
        setPropagators(new PropElementV(value, values, index, offset, solver, this),
                new PropElementV(value, values, index, offset, solver, this));
    }


    @Override
    public ESat isSatisfied(int[] tuple) {
        if (values.length == 0) {
            return ESat.eval(tuple[tuple[1] + offset] == tuple[0]);
        }
        return ESat.eval(
                !(tuple[1] - this.offset >= values.length || tuple[1] - this.offset < 0)
                        && this.values[tuple[1] - this.offset] == tuple[0]
        );
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append(this.vars[0]).append(" = ");
        sb.append("<");
        if (values.length == 0) {
            int i = 2;
            for (; i < Math.min(this.vars.length - 1, 5); i++) {
                sb.append(this.vars[i]).append(", ");
            }
            if (i < this.vars.length - 1) {
                sb.append("..., ");
            }
            sb.append(this.vars[vars.length - 1]);
        } else {
            int i = 0;
            for (; i < Math.min(this.values.length - 1, 5); i++) {
                sb.append(this.values[i]).append(", ");
            }
            if (i == 5 && this.values.length - 1 > 5) sb.append("..., ");
            sb.append(this.values[values.length - 1]);
        }
        sb.append(">[").append(this.vars[1]).append(']');
        return sb.toString();
    }
}
