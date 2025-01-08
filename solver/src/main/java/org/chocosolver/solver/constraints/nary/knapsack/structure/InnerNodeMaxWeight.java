/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

public class InnerNodeMaxWeight implements InnerNode {
    private int maxWeight;

    public InnerNodeMaxWeight() {
        setup();
    }

    public void setup() {
        this.maxWeight = -1;
    }

    public void updateValue(KPItem item) {
        if (item.isActive()) {
            this.maxWeight = Math.max(item.getWeight(), maxWeight);
        }
    }

    public int getWeight() {
        return maxWeight;
    }

    public void updateValue(InnerNode node) {
        try {
            InnerNodeMaxWeight nodeMaxWeight = (InnerNodeMaxWeight) node;
            this.maxWeight = Math.max(nodeMaxWeight.getWeight(), maxWeight);
        } catch (Exception e) {
            throw new RuntimeException("updateValue of InnerNode used with another type ");
        }
    }

    public boolean isActive() {
        return maxWeight != -1;
    }

    public String toString() {
        return "w=" + maxWeight;
    }

}
