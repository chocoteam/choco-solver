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

import parser.flatzinc.ast.expression.EAnnotation;
import parser.flatzinc.ast.expression.Expression;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.nary.cnf.Literal;
import solver.constraints.nary.cnf.Node;
import solver.variables.BoolVar;

import java.util.List;

/**
 * (&#8704; i &#8712; 1..N: as[i]) &#8660; r where n is the length of as
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/11
 */
public class ArrayBoolAndBuilder implements IBuilder {
    @Override
    public Constraint build(Solver solver, String name, List<Expression> exps, List<EAnnotation> annotations) {
        BoolVar[] as = exps.get(0).toBoolVarArray(solver);
        BoolVar r = exps.get(1).boolVarValue(solver);

        if (as.length == 0)
            return IntConstraintFactory.clauses(Literal.pos(r), solver);

        Literal[] l_as = new Literal[as.length];
        for (int i = 0; i < as.length; i++) {
            l_as[i] = Literal.pos(as[i]);
        }
        Literal l_r = Literal.pos(r);

        return IntConstraintFactory.clauses(Node.reified(l_r, Node.and(l_as)), solver);

    }
}
