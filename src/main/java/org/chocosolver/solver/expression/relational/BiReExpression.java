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
package org.chocosolver.solver.expression.relational;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.expression.arithmetic.ArExpression;
import org.chocosolver.solver.variables.IntVar;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Binary relational expression
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class BiReExpression implements ReExpression {

    /**
     * Operator of the arithmetic expression
     */
    ReExpression.Operator op = null;

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
    public BiReExpression(ReExpression.Operator op, ArExpression e1, ArExpression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public Constraint decompose() {
        IntVar v1 = e1.intVar();
        IntVar v2 = e2.intVar();
        Model model = v1.getModel();
        switch (op) {
            case LT:
                return model.arithm(v1, "<", v2);
            case LE:
                return model.arithm(v1, "<=", v2);
            case GE:
                return model.arithm(v1, ">=", v2);
            case GT:
                return model.arithm(v1, ">", v2);
            case NE:
                return model.arithm(v1, "!=", v2);
            case EQ:
                return model.arithm(v1, "=", v2);
        }
        throw new SolverException("Unexpected case");
    }

    @Override
    public Constraint extension() {
        HashSet<IntVar> avars = new LinkedHashSet<>();
        extractVar(avars, e1);
        extractVar(avars, e2);
        IntVar[] uvars = avars.stream().sorted().toArray(IntVar[]::new);
        Map<IntVar, Integer> map = IntStream.range(0, uvars.length).boxed().collect(Collectors.toMap(i -> uvars[i], i -> i));
        Tuples tuples = TuplesFactory.generateTuples(values -> eval(values, map), true, uvars);
//        System.out.printf("%d -> %d\n", VariableUtils.domainCardinality(uvars), tuples.nbTuples());
        return e1.getModel().table(uvars, tuples);
    }

    /**
     * Extract the variables from this expression
     * @param variables set of variables
     * @param ae expression to extract variables from
     */
    private static void extractVar(HashSet<IntVar> variables, ArExpression ae) {
        if (ae.isExpressionLeaf()) {
            variables.add((IntVar) ae);
        } else {
            for (ArExpression e : ae.getExpressionChild()) {
                extractVar(variables, e);
            }
        }
    }

    @Override
    public boolean eval(int[] values, Map<IntVar, Integer> map) {
        return op.eval(e1.eval(values, map), e2.eval(values, map));
    }

    @Override
    public String toString() {
        return op.name() + "(" + e1.toString() + "," + e2.toString() + ")";
    }
}
