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
package org.chocosolver.solver.expression.arithmetic;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.MathUtils;
import org.chocosolver.util.tools.StringUtils;
import org.chocosolver.util.tools.VariableUtils;

import java.util.Map;

/**
 * Binary arithmetic expression
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class BiArExpression implements ArExpression {

    /**
     * The model in which the expression is declared
     */
    Model model;

    /**
     * Lazy creation of the underlying variable
     */
    IntVar me = null;

    /**
     * Operator of the arithmetic expression
     */
    Operator op = null;

    /**
     * The first expression this expression relies on
     */
    private ArExpression e1;
    /**
     * The second expression this expression relies on
     */
    private ArExpression e2;

    /**
     * Builds a binary expression
     *
     * @param op an operator
     * @param e1 an expression
     * @param e2 an expression
     */
    public BiArExpression(Operator op, ArExpression e1, ArExpression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
        this.model = e1.getModel();
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public IntVar intVar() {
        if (me == null) {
            IntVar v1 = e1.intVar();
            IntVar v2 = e2.intVar();
            int[] bounds;
            switch (op) {
                case ADD:
                    bounds = VariableUtils.boundsForAddition(v1, v2);
                    me = model.intVar(StringUtils.randomName("sum_exp_"), bounds[0], bounds[1]);
                    model.arithm(v1, "+", v2, "=", me).post();
                    break;
                case SUB:
                    bounds = VariableUtils.boundsForSubstraction(v1, v2);
                    me = model.intVar(StringUtils.randomName("sub_exp_"), bounds[0], bounds[1]);
                    model.arithm(v1, "-", v2, "=", me).post();
                    break;
                case MUL:
                    bounds = VariableUtils.boundsForMultiplication(v1, v2);
                    me = model.intVar(StringUtils.randomName("mul_exp_"), bounds[0], bounds[1]);
                    model.times(v1, v2, me).post();
                    break;
                case DIV:
                    bounds = VariableUtils.boundsForDivision(v1, v2);
                    me = model.intVar(StringUtils.randomName("div_exp_"), bounds[0], bounds[1]);
                    model.div(v1, v2, me).post();
                    break;
                case MOD:
                    bounds = VariableUtils.boundsForModulo(v1, v2);
                    me = model.intVar(StringUtils.randomName("mod_exp_"), bounds[0], bounds[1]);
                    model.mod(v1, v2, me).post();
                    break;
                case POW: // todo as intension constraint
                    bounds = VariableUtils.boundsForPow(v1, v2);
                    me = model.intVar(StringUtils.randomName("pow_exp_"), bounds[0], bounds[1]);
                    model.table(new IntVar[]{v1, v2, me},
                            TuplesFactory.generateTuples(vs -> vs[2] == MathUtils.pow(vs[0], vs[1]),
                            true, v1, v2, me)).post();
                    break;
                case MIN:
                    bounds = VariableUtils.boundsForMinimum(v1, v2);
                    me = model.intVar(StringUtils.randomName("min_exp_"), bounds[0], bounds[1]);
                    model.min(me, v1, v2).post();
                    break;
                case MAX:
                    bounds = VariableUtils.boundsForMaximum(v1, v2);
                    me = model.intVar(StringUtils.randomName("max_exp_"), bounds[0], bounds[1]);
                    model.max(me, v1, v2).post();
                    break;
                default:
                    throw new UnsupportedOperationException("Binary arithmetic expressions does not support " + op.name());
            }
        }
        return me;
    }

    @Override
    public int eval(int[] values, Map<IntVar, Integer> map) {
        return op.eval(e1.eval(values, map), e2.eval(values, map));
    }

    @Override
    public ArExpression[] getExpressionChild() {
        return new ArExpression[]{e1, e2};
    }

    @Override
    public String toString() {
        return op.name() + "(" + e1.toString() + "," + e2.toString() + ")";
    }
}
