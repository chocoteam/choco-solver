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
import solver.variables.BoolVar;
import solver.variables.IntVar;

import java.util.List;

/**
 * (a = b)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/11
 */
public class Bool2IntBuilder implements IBuilder {

    private static Constraint[] NO_CSTR = new Constraint[0];
    private static final String defines_var = "def";//"defines_var";

    @Override
    public Constraint[] build(Solver solver, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
        BoolVar bVar = exps.get(0).boolVarValue(solver);
        IntVar iVar = exps.get(1).intVarValue(solver);
        /*// 1. an annotation specify which variable is a defined one
        if (annotations.size() > 0 && annotations.get(0).id.value.startsWith(defines_var)) {
            IntVar defvar = datas.getVariable(annotations.get(0).exps.get(0).toString());
            if (defvar != null && defvar.getNbProps() == 0) {
                if (defvar == iVar) {
                    // then iVar can be removed and bVar now refers to iVar
                    substitute(iVar, bVar, solver, datas);
                    return NO_CSTR;
                } else if (defvar == bVar) {
                    // then bVar can be removed and iVar now refers to bVar
                    substitute(bVar, iVar, solver, datas);
                    return NO_CSTR;
                }
            }
        }
        // otherwise, well post the constraint*/
        return new Constraint[]{ICF.arithm(bVar, "=", iVar)};
    }

    private void substitute(IntVar REMOVE, IntVar KEEP, Solver SOLVER, Datas datas) {
        SOLVER.unassociates(REMOVE);
        datas.register(REMOVE.getName(), KEEP);
        if (REMOVE.isInstantiated()) {
            SOLVER.post(ICF.member(KEEP, REMOVE.getValue(), REMOVE.getValue()));
        }
    }

}
