/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

/**
 * KPItem
 */
public class KPItem implements WeightInterface, ProfitInterface {

    private final int profit;
    private int weight;
    private boolean active;

    public KPItem(int profit, int weight) {
        this.profit = profit;
        this.weight = weight;
        this.active = true;
    }

    public void desactivate() {
        active = false;
    }

    public void activate() {
        active = true;
    }

    public int getProfit() {
        return active ? profit : 0;
    }

    public int getActivatedWeight() {
        return weight;
    }

    public int getActivatedProfit() {
        return profit;
    }

    public int getWeight() {
        return active ? weight : 0;
    }

    public boolean isActive() {
        return active;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public double getEfficiency() {
        return active ? (double) getProfit() / getWeight() : 0;
    }

}