/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.penalty;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Apr 27, 2010
 * Time: 11:31:07 AM
 */
public class IsoPenaltyFunction extends AbstractPenaltyFunction {

    private int factor;

    public IsoPenaltyFunction() {
        this(1);
    }

    public IsoPenaltyFunction(int factor) {
        this.factor = factor;
    }

    @Override
    public final int penalty(int value) {
        return value * factor;
    }

    public final int getFactor() {
        return factor;
    }
}
