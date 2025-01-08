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

public class InnerNodeSum implements InnerNode, ProfitInterface {
    private int sumWeight;
    private int sumProfit;

    public InnerNodeSum() {
        setup();
    }

    public void setup() {
        sumWeight = 0;
        sumProfit = 0;
    }

    public void updateValue(KPItem item) {
        if (item.isActive()) {
            sumWeight += item.getWeight();
            sumProfit += item.getProfit();
        }
    }

    public int getWeight() {
        return sumWeight;
    }

    public int getProfit() {
        return sumProfit;
    }

    public void updateValue(InnerNode node) {
        try {
            InnerNodeSum nodeSum = (InnerNodeSum) node;
            sumWeight += nodeSum.getWeight();
            sumProfit += nodeSum.getProfit();
        } catch (Exception e) {
            throw new RuntimeException("updateValue of InnerNode used with another type ");
        }
    }

    public boolean isActive() {
        return !(sumProfit == 0 && sumWeight == 0);
    }

    public String toString() {
        return "w=" + sumWeight + ",p=" + sumProfit;
    }

}
