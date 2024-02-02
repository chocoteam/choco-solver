/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

public interface InnerNode extends WeightInterface {

    void setup();

    void updateValue(KPItem item);

    void updateValue(InnerNode item);

    default void setValue(KPItem item1, KPItem item2) {
        setup();
        updateValue(item1);
        updateValue(item2);
    }

    default void setValue(InnerNode item1, InnerNode item2) {
        setup();
        updateValue(item1);
        updateValue(item2);
    }

    // todo remove this method
    boolean isActive();
}
