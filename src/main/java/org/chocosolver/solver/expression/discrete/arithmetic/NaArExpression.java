/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.expression.discrete.arithmetic;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.StringUtils;
import org.chocosolver.util.tools.VariableUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * Binary arithmetic expression
 * <p/>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class NaArExpression implements ArExpression {

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
     * The expressions this expression relies on
     */
    private ArExpression[] es;

    /**
     * Builds a n-ary expression
     *
     * @param op an operator
     * @param es some expressions
     */
    public NaArExpression(Operator op, ArExpression e, ArExpression... es) {
        this.op = op;
        this.es = ArrayUtils.append(new ArExpression[]{e}, es);
        this.model = e.getModel();
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public IntVar intVar() {
        if (me == null) {
            IntVar[] vs = Arrays.stream(es).map(e -> e.intVar()).toArray(IntVar[]::new);
            int[] bounds;
            switch (op) {
                case ADD:
                    bounds = VariableUtils.boundsForAddition(vs);
                    me = model.intVar(StringUtils.randomName("sum_exp_"), bounds[0], bounds[1]);
                    model.sum(vs, "=", me).post();
                    break;
                case MUL:
                    bounds = VariableUtils.boundsForMultiplication(vs[0], vs[1]);
                    me = model.intVar(StringUtils.randomName("mul_exp_0_"), bounds[0], bounds[1]);
                    model.times(vs[0], vs[1], me).post();
                    for (int i = 2; i < vs.length; i++) {
                        IntVar pre = me;
                        bounds = VariableUtils.boundsForMultiplication(pre, vs[i]);
                        me = model.intVar(StringUtils.randomName("mul_exp_0_"), bounds[0], bounds[1]);
                        model.times(pre, vs[i], me).post();
                    }
                    break;
                case MIN:
                    bounds = VariableUtils.boundsForMinimum(vs);
                    me = model.intVar(StringUtils.randomName("min_exp_"), bounds[0], bounds[1]);
                    model.min(me, vs).post();
                    break;
                case MAX:
                    bounds = VariableUtils.boundsForMaximum(vs);
                    me = model.intVar(StringUtils.randomName("max_exp_"), bounds[0], bounds[1]);
                    model.max(me, vs).post();
                    break;
                default:
                    throw new UnsupportedOperationException("Binary arithmetic expressions does not support " + op.name());
            }
        }
        return me;
    }

    @Override
    public int eval(int[] values, Map<IntVar, Integer> map) {
        int r = es[0].eval(values, map);
        return Arrays.stream(es)
                .mapToInt(e -> e.eval(values, map))
                .reduce(0, (e1, e2) -> op.eval(e1, e2));
    }

    @Override
    public ArExpression[] getExpressionChild() {
        return es;
    }

    @Override
    public String toString() {
        return op.name() + "(" + es[0].toString() + ",... ," + es[es.length - 1].toString() + ")";
    }
}
