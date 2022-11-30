/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete.logical;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.discrete.arithmetic.ExpOperator;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;

import java.util.List;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 04/04/2017.
 */
public abstract class LoExpression implements ReExpression {

    /**
     * List of available operator for relational expression
     */
    public enum Operator implements ExpOperator {
        /**
         * less than
         */
        AND {
            @Override
            boolean eval(boolean b1, boolean b2) {
                return b1 && b2;
            }
        },
        /**
         * less than or equal to
         */
        OR {
            @Override
            boolean eval(boolean b1, boolean b2) {
                return b1 || b2;
            }
        },
        XOR{
            @Override
            boolean eval(boolean b1, boolean b2) {
                return b1 != b2;
            }
        },
        IFF{
            @Override
            boolean eval(boolean b1, boolean b2) {
                return b1 == b2;
            }
        },
        IMP{
            @Override
            boolean eval(boolean b1, boolean b2) {
                return !b1 || b2;
            }
        },
        NOT{
            @Override
            boolean eval(boolean b1, boolean b2) {
                return !b1 || !b2;
            }
        };

        abstract boolean eval(boolean b1, boolean b2);
    }


    /**
     * The model in which the expression is declared
     */
    protected final Model model;

    /**
     * Lazy creation of the underlying variable
     */
    protected BoolVar me = null;

    /**
     * Operator of the arithmetic expression
     */
    protected final Operator op;

    public LoExpression(Model model, Operator op) {
        this.model = model;
        this.op = op;
    }

    /**
     * Return the associated model
     *
     * @return a Model object
     */
    public abstract Model getModel();

    /**
     * @return the logical expression as an {@link BoolVar}.
     * If necessary, it creates intermediary variable and posts intermediary constraints
     */
    public abstract BoolVar boolVar();

    /**
     * Replace the sub-expression at position <i>idx</i> in the expression by <i>e</i>.
     * @param idx index of the expression to replace
     * @param e the new expression
     * @implSpec This method is only supposed to be used by {{@link #rewrite(List)}
     */
    public abstract void substitute(int idx, ReExpression e);
}
