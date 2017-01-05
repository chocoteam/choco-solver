/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete.relational;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
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
