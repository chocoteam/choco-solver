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

import java.util.List;

public class ComputingLossWeightTree extends BinarySearchFingerTree {
    // used to make mandatory and forbidden test return false on equality
    public static final double OFFSET = 1e-4;

    /**
     * @param sortedItems sorted items by efficiency !
     */
    public ComputingLossWeightTree(List<KPItem> sortedItems) {
        super(sortedItems, InnerNodeSum::new);
    }

    private ProfitInterface getNodeProfitInterface(int index) {
        if (isLeaf(index)) {
            return getLeaf(index);
        } else if (isInnerNode(index)) {
            // the constructor ensure that every inner node is type InnerNodeSum,
            // which extends ProfitInterface
            return (ProfitInterface) getInnerNode(index);
        } else {
            throw new IndexOutOfBoundsException(
                    "Looking for an index that corresponds to nothing in the tree (leaf outside of range)");
        }
    }

    public int getNodeProfit(int index) {
        return getNodeProfitInterface(index).getProfit();
    }

    public boolean isTrivial(int capacity) {
        // detect the trivial case where we can put every item in the KP
        return getNodeWeight(0) <= capacity;
    }

    /**
     * Computes the index of the critical item and the solutions informations
     *
     * @param capacity capacity of the KP to consider
     * @return Info object with informations of the solution
     */
    public Info findCriticalItem(int capacity) {
        int remainingCapacity = capacity;
        double criticalProfit = 0;
        int criticalIdx = 0;
        if (capacity < 0) {
            return new Info(getNumberNodes() - getNumberLeaves(), 0, 0, 0);
        }
        if (isTrivial(capacity)) {
            return new Info(getNumberNodes(), getNodeProfit(0), getNodeProfit(0), getNodeWeight(0));
        }
        while (isInnerNode(criticalIdx)) {
            int leftChild = getLeftChild(criticalIdx);
            int rightChild = getRightChild(criticalIdx);
            if (isLeaf(leftChild) || isInnerNode(leftChild)) {
                if (getNodeWeight(leftChild) >= remainingCapacity) {
                    criticalIdx = leftChild;
                } else {
                    criticalIdx = rightChild;
                    remainingCapacity -= getNodeWeight(leftChild);
                    criticalProfit += getNodeProfit(leftChild);
                }
            } else {
                throw new RuntimeException("Finding a critical item led to an empty Item, but kp is not trivial");
            }
        }
        criticalProfit += getLeaf(criticalIdx).getEfficiency() * remainingCapacity;
        return new Info(criticalIdx, criticalProfit, capacity - remainingCapacity, capacity);
    }

    /**
     * @param criticalInfos     infos about the Dantzig solution
     * @param itemIndex         global item index to decide on
     * @param startingIndex     global item index to start the search
     * @param profitAccumulated since the beggining of the whole search
     * @param weightAccumulated since the beggining of the whole search
     * @param allowedProfitLoss delta between optimal solution and profit lower
     *                          bound
     * @param startItemWeight   weight to consider for the startingIndex item
     * @return
     */
    public SearchInfos computeLimitWeightMandatory(Info criticalInfos,
                                                   int itemIndex, int startingIndex, double profitAccumulated,
                                                   double weightAccumulated, double allowedProfitLoss,
                                                   double startItemWeight) {
        assert !isInnerNode(startingIndex);
        boolean decision = false;
        if (criticalInfos.index == getNumberNodes()) {
            // if the KP optimal solution is trivial
            // we can have every item in the solution
            decision = getNodeProfit(itemIndex) > allowedProfitLoss + ComputingLossWeightTree.OFFSET;
            return new SearchInfos(decision, startingIndex, profitAccumulated, weightAccumulated, startItemWeight);
        }
        if (getNodeWeight(itemIndex) == 0) {
            // we just see if removing the profit of the item is allowed,
            // then it is NOT mandatory
            decision = getNodeProfit(itemIndex) > allowedProfitLoss + ComputingLossWeightTree.OFFSET;
            return new SearchInfos(decision, startingIndex, profitAccumulated, weightAccumulated, startItemWeight);
        }
        double itemWeight = getNodeWeight(itemIndex);

        if (!isLeaf(startingIndex)) {
            // no node left to add
            decision = itemWeight * getLeaf(itemIndex).getEfficiency() - profitAccumulated > allowedProfitLoss
                    + ComputingLossWeightTree.OFFSET;
            return new SearchInfos(decision, startingIndex, profitAccumulated, weightAccumulated, startItemWeight);
        }

        int index = startingIndex;
        double itemEfficiency = getLeaf(itemIndex).getEfficiency();
        double profit = profitAccumulated;
        double weight = weightAccumulated;
        double nextWeight = startItemWeight;
        double nextProfit = startItemWeight * getLeaf(startingIndex).getEfficiency();
        // we are looking for the node that contains the "exceeding" item
        while (profit + nextProfit - (weight + nextWeight) * itemEfficiency >= -allowedProfitLoss) {
            weight += nextWeight;
            profit += nextProfit;
            index = getNextNode(index, true);
            // there is no node left and we know that weight < itemWeight
            if (index == -1) {
                // we must give up all of the item without additionnal profit
                decision = weight < itemWeight &&
                        itemWeight * itemEfficiency - profit > allowedProfitLoss + ComputingLossWeightTree.OFFSET;
                return new SearchInfos(decision, -1, profit, weight, 0);
            }
            nextProfit = getNodeProfit(index);
            nextWeight = getNodeWeight(index);
        }
        // now we dive into the subtree to find the "exceeding" item
        while (isInnerNode(index)) {
            int leftChild = getLeftChild(index);
            nextProfit = getNodeProfit(leftChild);
            nextWeight = getNodeWeight(leftChild);
            if (profit + nextProfit - (weight + nextWeight) * itemEfficiency <= -allowedProfitLoss) {
                index = leftChild;
            } else {
                weight += nextWeight;
                profit += nextProfit;
                index = getRightChild(index);
            }
        }
        double remainingWeightEndItem = 0;
        // Special case where we went to the end of the tree and the leaf does not
        // exists, thus we must give up the rest without additionnal profit
        if (!isLeaf(index)) {
            decision = itemWeight * itemEfficiency - profit > allowedProfitLoss + ComputingLossWeightTree.OFFSET;
        } else {
            // we have to compute the exact part of this item that can be used
            // TODO index efficiency == itemEfficiency == 0
            double portionWeight = (weight * itemEfficiency - profit - allowedProfitLoss)
                    / (getLeaf(index).getEfficiency() - itemEfficiency);
            weight += portionWeight;
            profit += portionWeight * getLeaf(index).getEfficiency();
            if (index == startingIndex) {
                remainingWeightEndItem = startItemWeight - portionWeight;
            } else {
                remainingWeightEndItem = getNodeWeight(index) - portionWeight;
            }
            decision = weight + ComputingLossWeightTree.OFFSET < itemWeight;
            if (Math.abs(weight * itemEfficiency - profit - allowedProfitLoss) > 0.01) {
                throw new RuntimeException("Limit Weight found is not correct");
            }
        }
        return new SearchInfos(decision, index, profit, weight, remainingWeightEndItem);
    }

    /**
     * @param criticalInfos     infos about the Dantzig solution
     * @param itemIndex         global item index to decide on
     * @param startingIndex     global item index to start the search
     * @param profitAccumulated since the beggining of the whole search
     * @param weightAccumulated since the beggining of the whole search
     * @param allowedProfitLoss delta between optimal solution and profit lower
     *                          bound
     * @param startItemWeight   weight to consider for the startingIndex item
     * @return
     */
    public SearchInfos computeLimitWeightForbidden(Info criticalInfos,
                                                   int itemIndex, int startingIndex, double profitAccumulated,
                                                   double weightAccumulated, double allowedProfitLoss,
                                                   double startItemWeight) {
        assert !isInnerNode(startingIndex);
        boolean decision = false;
        double itemWeight = getNodeWeight(itemIndex);
        if (!isLeaf(startingIndex)) {
            // no node left to add
            decision = profitAccumulated - itemWeight * getLeaf(itemIndex).getEfficiency() > allowedProfitLoss
                    + ComputingLossWeightTree.OFFSET;
            return new SearchInfos(decision, startingIndex, profitAccumulated, weightAccumulated, startItemWeight);
        }

        int index = startingIndex;
        double itemEfficiency = getLeaf(itemIndex).getEfficiency();
        double profit = profitAccumulated;
        double weight = weightAccumulated;
        double nextWeight = startItemWeight;
        double nextProfit = startItemWeight * getLeaf(startingIndex).getEfficiency();
        // we are looking for the node that contains the "exceeding" item
        while ((weight + nextWeight) * itemEfficiency - profit - nextProfit >= -allowedProfitLoss) {
            weight += nextWeight;
            profit += nextProfit;
            index = getNextNode(index, false);
            // there is no node left and we know that weight < itemWeight
            if (index == -1) {
                decision = weight < itemWeight
                        && itemWeight * itemEfficiency - profit + ComputingLossWeightTree.OFFSET < allowedProfitLoss;
                return new SearchInfos(decision, -1, profit, weight, 0);
            }
            nextProfit = getNodeProfit(index);
            nextWeight = getNodeWeight(index);
        }
        // now we dive into the subtree to find the "exceeding" item
        while (isInnerNode(index)) {
            int rightChild = getRightChild(index);
            nextProfit = getNodeProfit(rightChild);
            nextWeight = getNodeWeight(rightChild);
            if ((weight + nextWeight) * itemEfficiency - profit - nextProfit <= -allowedProfitLoss) {
                index = rightChild;
            } else {
                weight += nextWeight;
                profit += nextProfit;
                index = getLeftChild(index);
            }
        }
        double remainingWeightEndItem = 0;
        // Special case where we went to the end of the tree and the leaf does not
        // exists, thus we must give up the rest without additionnal profit
        if (!isLeaf(index)) {
            decision = itemWeight * itemEfficiency - profit + ComputingLossWeightTree.OFFSET < allowedProfitLoss;
        } else {
            // we have to compute the exact part of this item that can be used
            // TODO index efficiency == itemEfficiency == 0
            double portionWeight = (profit - allowedProfitLoss - weight * itemEfficiency)
                    / (itemEfficiency - getLeaf(index).getEfficiency());
            weight += portionWeight;
            profit += portionWeight * getLeaf(index).getEfficiency();
            if (index == startingIndex) {
                remainingWeightEndItem = startItemWeight - portionWeight;
            } else {
                remainingWeightEndItem = getNodeWeight(index) - portionWeight;
            }
            decision = weight + ComputingLossWeightTree.OFFSET < itemWeight;
            if (Math.abs(profit - weight * itemEfficiency - allowedProfitLoss) > 0.01) {
                throw new RuntimeException("Limit Weight found is not correct");
            }
        }
        return new SearchInfos(decision, index, profit, weight, remainingWeightEndItem);
    }

}
