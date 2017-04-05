/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete.logical;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.Map;

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
        };

        abstract boolean eval(boolean b1, boolean b2);
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
     * Post the decomposition of this expression in the solver
     */
    public void post() {
        decompose().post();
    }

    /**
     * @return the topmost constraint representing the expression. If needed, a call to this method
     * creates additional variables and posts additional constraints.
     */
    public abstract Constraint decompose();

    @Override
    public final Constraint extension() {
        throw new UnsupportedOperationException("LoExpression does not support \"to extension\" transformation yet");
    }

    @Override
    public boolean eval(int[] values, Map<IntVar, Integer> map) {
        throw new UnsupportedOperationException("LoExpression does not support \"eval\" yet");
    }
}
