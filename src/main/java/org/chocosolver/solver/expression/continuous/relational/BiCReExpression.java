/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.expression.continuous.relational;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.expression.continuous.arithmetic.CArExpression;
import org.chocosolver.solver.variables.RealVar;

/**
 * Binary relational expression over continuous expressions
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class BiCReExpression implements CReExpression {

    /**
     * Operator of the arithmetic expression
     */
    Operator op = null;

    /**
     * The first expression this expression relies on
     */
    private CArExpression e1;
    /**
     * The second expression this expression relies on
     */
    private CArExpression e2;

    /**
     * Builds a binary expression
     *
     * @param op an operator
     * @param e1 an expression
     * @param e2 an expression
     */
    public BiCReExpression(Operator op, CArExpression e1, CArExpression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public Constraint ibex(double p) {
        RealVar v1 = e1.realVar(p);
        RealVar v2 = e2.realVar(p);
        Model model = v1.getModel();
        switch (op) {
            case LT:
                return model.realIbexGenericConstraint("{0}<{1}", v1, v2);
            case LE:
                return model.realIbexGenericConstraint("{0}<={1}", v1, v2);
            case GE:
                return model.realIbexGenericConstraint("{0}>={1}", v1, v2);
            case GT:
                return model.realIbexGenericConstraint("{0}>{1}", v1, v2);
            case EQ:
                return model.realIbexGenericConstraint("{0}={1}", v1, v2);
        }
        throw new SolverException("Unexpected case");
    }


    @Override
    public String toString() {
        return op.name() + "(" + e1.toString() + "," + e2.toString() + ")";
    }
}
