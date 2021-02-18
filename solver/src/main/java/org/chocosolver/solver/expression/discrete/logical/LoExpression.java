/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete.logical;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;

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
    public enum Operator {
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
}
