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
package parser.flatzinc.ast.constraints;

import parser.flatzinc.ast.Datas;
import parser.flatzinc.ast.Exit;
import parser.flatzinc.ast.expression.EAnnotation;
import parser.flatzinc.ast.expression.ESetBounds;
import parser.flatzinc.ast.expression.Expression;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.LogicalConstraintFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;

import java.util.List;

/**
 * (a &#8712; b) &#8660; r
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/07/12
 */
public class SetInReifBuilder implements IBuilder {

    @Override
    public Constraint[] build(Solver solver, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
        IntVar a = exps.get(0).intVarValue(solver);
        Constraint[] cs = new Constraint[2];
        if (exps.get(1).getTypeOf().equals(Expression.EType.SET_L)) {
            int[] values = exps.get(1).toIntArray();
            cs[0] = IntConstraintFactory.member(a, values);
            cs[1] = IntConstraintFactory.not_member(a, values);
        } else if (exps.get(1).getTypeOf().equals(Expression.EType.SET_B)) {
            int low = ((ESetBounds) exps.get(1)).getLow();
            int upp = ((ESetBounds) exps.get(1)).getUpp();
            cs[0] = IntConstraintFactory.member(a, low, upp);
            cs[1] = IntConstraintFactory.not_member(a, low, upp);
        } else {
            Exit.log("SetVar unavailable");
            return new Constraint[0];
        }
        BoolVar r = exps.get(2).boolVarValue(solver);
        return new Constraint[]{LogicalConstraintFactory.ifThenElse(r, cs[0], cs[1])};

    }
}