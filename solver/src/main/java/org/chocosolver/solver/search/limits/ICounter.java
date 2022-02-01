/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.limits;

import org.chocosolver.util.criteria.Criterion;
import org.chocosolver.util.criteria.LongCriterion;



/**
 * An interface to define count smth during search process
 *
 * @author Charles Prud'homme
 * @see NodeCounter
 * @see BacktrackCounter
 * @see FailCounter
 * @see SolutionCounter
 * @since 15 juil. 2010
 */
public interface ICounter extends Criterion, LongCriterion{

    void init();

    void update();

    long getLimitValue();

    void overrideLimit(long newLimit);

    long currentValue();

    enum Impl implements ICounter{
        None {
            @Override
            public boolean isMet(long value) {
                return false;
            }

            @Override
            public boolean isMet() {
                return false;
            }

            @Override
            public void init() {

            }

            @Override
            public void update() {

            }

            @Override
            public long getLimitValue() {
                return 0;
            }

            @Override
            public void overrideLimit(long newLimit) {}

            @Override
            public long currentValue() {
                return 0;
            }
        }
    }

}
