/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.IntVar;

import java.util.function.ToDoubleFunction;

/**
 * Selects the middle of the current bounds of the variable.
 * <br/>
 * On an enumerated domain, it returns the mean of the bounds, floored or ceiled according to the rounding policy.
 * This value may not belong to the domain, which is fine with a split decision (<code>x &le; v</code>), where it
 * is only a bound. With an assignment (<code>x = v</code>) or a removal (<code>x &ne; v</code>) decision, the decision
 * is made on the value of the domain closest to it
 * (see {@link org.chocosolver.solver.search.strategy.decision.DecisionMaker#makeIntDecision}).
 * On a bounded domain, it returns the lower bound (floor) or the upper bound (ceil).
 * <br/>
 * <p>
 * BEWARE: should not be used with assignment decisions over bounded variables (because the decision negation
 * would result in no inference)
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 2 juil. 2010
 */
public class IntDomainMiddle implements IntValueSelector {

    // VARIABLES
    public final static boolean FLOOR = true;
    private final boolean roundingPolicy;

    /**
     * Selects the middle value
     *
     * @param roundingPolicy should be either FLOOR or !FLOOR (ceil)
     */
    public IntDomainMiddle(boolean roundingPolicy) {
        this.roundingPolicy = roundingPolicy;
    }

    /**
     * Selects the middle value
     *
     * @param middle         ignored: the middle is always the mean of the bounds
     * @param roundingPolicy should be either FLOOR or !FLOOR (ceil)
     * @deprecated <i>middle</i> is ignored, use {@link #IntDomainMiddle(boolean)} instead
     */
    @Deprecated(forRemoval = true)
    public IntDomainMiddle(ToDoubleFunction<IntVar> middle, boolean roundingPolicy) {
        this(roundingPolicy);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("PointlessBooleanExpression")
    @Override
    public int selectValue(IntVar var) {
        int value;
        if (var.hasEnumeratedDomain()) {
            int min = var.getLB();
            int max = var.getUB();
            if (roundingPolicy == FLOOR) {
                value = min + (max - min ) / 2;
            } else {
                value = min + (max - min + 1) / 2;
            }
        } else {
            if (roundingPolicy == FLOOR) {
                value = var.getLB();
            } else {
                value = var.getUB();
            }
        }
        return value;
    }
}
