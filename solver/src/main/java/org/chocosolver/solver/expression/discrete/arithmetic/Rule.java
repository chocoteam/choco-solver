/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete.arithmetic;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Class that defines an expression's rewriting rule.
 * It is composed of a predicate that serves as a pattern detector
 * and a function to transform an expression into another one.
 * <br/>
 * It also provides predefined predicates and functions to limit verbosity.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/11/2022
 */
public class Rule<E extends ArExpression> {
    final Predicate<E> predicate;
    final Function<E, E> rewriter;

    public Rule(Predicate<E> predicate, Function<E, E> rewriter) {
        this.predicate = predicate;
        this.rewriter = rewriter;
    }

    /**
     * Predicate that take an operator as parameter
     *
     * @param op operator to match
     * @return an operator-based predicate
     */
    public static Predicate<ArExpression> isOperator(ExpOperator op) {
        return e -> e.getOperator().equals(op);
    }

    /**
     * Pre-defined to function to apply when: 1) the expression has two children and 2) both are equivalent.
     *
     * @param function function to apply to replace the pair by one expression involving only one occurrence
     * @return a function
     */
    public static Function<ArExpression, ArExpression> twoIdentical(Function<ArExpression, ArExpression> function) {
        return e -> {
            if (e.getNoChild() == 2) {
                ArExpression[] es = e.getExpressionChild();
                if (es[0].equals(es[1])) {
                    return function.apply(es[0]);
                }
            }
            return e;
        };
    }
}
