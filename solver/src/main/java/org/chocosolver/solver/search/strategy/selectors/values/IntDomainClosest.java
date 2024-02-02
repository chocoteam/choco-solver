/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.IntVar;

/**
 * A value selector which selects the closest value to a given target value.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 10/07/2023
 */
public class IntDomainClosest implements IntValueSelector {

    final int target;

    /**
     * Creates an IntDomainClosest value selector.
     * The selected value is the closest to zero.
     */
    public IntDomainClosest() {
        this(0);
    }

    /**
     * Creates an IntDomainClosest value selector.
     * The selected value is the closest to {@code target}.
     *
     * @param target the target value
     */
    public IntDomainClosest(int target) {
        this.target = target;
    }

    @Override
    public int selectValue(IntVar var) {
        int pos = var.nextValue(target - 1);
        int neg = var.previousValue(target + 1);
        if (pos < Integer.MAX_VALUE) {
            if (neg > Integer.MIN_VALUE) {
                return pos + neg < 2 * target ? pos : neg;
            } else {
                return pos;
            }
        } else if (neg > Integer.MIN_VALUE) {
            return neg;
        } else {
            // no value left -- should not happen
            throw new UnsupportedOperationException();
        }
    }

}
