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

package parser.flatzinc.ast.constraints;

import parser.flatzinc.ast.Datas;
import parser.flatzinc.ast.expression.EAnnotation;
import parser.flatzinc.ast.expression.Expression;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.constraints.IntConstraintFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VF;

import java.util.List;

/**
 * (&#8721; i &#8712; 1..n: as[i].bs[i] &#8800; c) &#8660; r where n is the common length of as and bs
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 26/01/11
 */
public class IntLinNeReifBuilder implements IBuilder {

    @Override
    public Constraint[] build(Solver solver, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
        int[] as = exps.get(0).toIntArray();
        IntVar[] bs = exps.get(1).toIntVarArray(solver);
        int c = exps.get(2).intValue();
        BoolVar r = exps.get(3).boolVarValue(solver);

        Constraint cstr = null;
        if (as.length == 1) {
            if (as[0] == 1) {
                cstr = ICF.arithm(bs[0], "!=", c);
            } else if (as[0] == -1) {
                cstr = ICF.arithm(bs[0], "!=", -c);
            }
        } else if (as.length == 2) {
            if (as[0] == 1 && as[1] == 1) {
                cstr = ICF.arithm(bs[0], "+", bs[1], "!=", c);
            } else if (as[0] == 1 && as[1] == -1) {
                cstr = ICF.arithm(bs[0], "-", bs[1], "!=", c);
            } else if (as[0] == -1 && as[1] == 1) {
                cstr = ICF.arithm(bs[1], "-", bs[0], "!=", c);
            } else if (as[0] == -1 && as[1] == -1) {
                cstr = ICF.arithm(bs[0], "+", bs[1], "!=", -c);
            }
        }
        if (cstr == null) {
            cstr = IntConstraintFactory.scalar(bs, as, "!=", VF.fixed(c, solver));
        }
        cstr.reifyWith(r);
        return new Constraint[0];
    }
}
