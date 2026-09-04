/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

import java.util.List;

/**
 * Finger tree specialized for computing weight loss in knapsack filtering.
 * <p>
 * This class extends {@link BinarySearchFingerTree} to support the computation of
 * mandatory and forbidden items in the Katriel knapsack filtering algorithm.
 * It uses {@link InnerNodeSum} nodes which store the sum of weights and profits
 * in each subtree, enabling efficient computation of whether items can be
 * determined as mandatory or forbidden based on the Dantzig relaxation solution.
 * <p>
 * The tree provides methods to:
 * <ul>
 *   <li>Find the critical item (the item that cannot be fully included in the Dantzig solution)</li>
 *   <li>Compute limits for mandatory items (items that must be in any optimal solution)</li>
 *   <li>Compute limits for forbidden items (items that cannot be in any optimal solution)</li>
 * </ul>
 *
 * @author Nicolas PIERRE
 */
public class ComputingLossWeightTree extends BinarySearchFingerTree {
    // used to make mandatory and forbidden test return false on equality
    public static final double OFFSET = 1e-4;

    /**
     * Constructs a computing loss weight tree from a list of sorted knapsack items.
     *
     * @param sortedItems knapsack items sorted by decreasing efficiency
     */
    public ComputingLossWeightTree(List<KPItem> sortedItems) {
        super(sortedItems, InnerNodeSum::new);
    }

    /**
     * Returns the profit interface for a node at the given index.
     *
     * @param index the global index of the node
     * @return the profit interface for the node
     * @throws IndexOutOfBoundsException if the index is invalid
     */
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

    /**
     * Returns the profit of a node at the given index.
     *
     * @param index the global index of the node
     * @return the profit of the node
     */
    public int getNodeProfit(int index) {
        return getNodeProfitInterface(index).getProfit();
    }

    /**
     * Checks if the knapsack problem is trivial for a given capacity.
     * A problem is trivial if all items can fit in the knapsack.
     *
     * @param capacity the knapsack capacity
     * @return true if the total weight of all items is less than or equal to the capacity
     */
    public boolean isTrivial(int capacity) {
        // detect the trivial case where we can put every item in the KP
        return getNodeWeight(0) <= capacity;
    }

    /**
     * Computes the index of the critical item and the Dantzig relaxation solution information.
     * <p>
     * The Dantzig relaxation is computed by greedily selecting items in order of decreasing
     * efficiency (profit/weight ratio) until the capacity is reached. The critical item is the
     * first item that cannot be fully included without exceeding the capacity.
     * <p>
     * The method traverses the tree, at each inner node deciding whether to go left (if the
     * left subtree's weight exceeds remaining capacity) or right (adding the left subtree's
     * weight and profit and continuing with the remaining capacity).
     *
     * @param capacity capacity of the knapsack to consider
     * @return Info object containing the critical item index, total profit, weight without
     *         critical item, and total weight
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
     * Computes whether an item can be determined as mandatory (must be in any optimal solution).
     * <p>
     * An item is mandatory if excluding it would reduce the profit below the lower bound
     * (critical profit minus allowed profit loss). This method performs a search through the tree
     * to find the maximum weight of items that can replace the item under consideration, and
     * determines if the profit loss would be acceptable.
     *
     * @param criticalInfos     information about the Dantzig relaxation solution
     * @param itemIndex         global index of the item to check for mandatoriness
     * @param startingIndex     global index to start the search from
     * @param accumulatedProfit profit accumulated during the search so far
     * @param accumulatedWeight weight accumulated during the search so far
     * @param allowedProfitLoss maximum allowed profit loss from the optimal solution
     * @param startItemWeight   weight to consider for the starting index item
     * @return SearchInfos object containing the decision (true if mandatory), last item index,
     *         accumulated profit and weight, and remaining weight of the last item
     */
    public SearchInfos computeLimitWeightMandatory(Info criticalInfos,
                                                   int itemIndex, int startingIndex, double accumulatedProfit,
                                                   double accumulatedWeight, double allowedProfitLoss,
                                                   double startItemWeight) {
        assert !isInnerNode(startingIndex);
        boolean decision = false;
        if (criticalInfos.index() == getNumberNodes()) {
            // if the KP optimal solution is trivial
            // we can have every item in the solution
            decision = getNodeProfit(itemIndex) > allowedProfitLoss + ComputingLossWeightTree.OFFSET;
            return new SearchInfos(decision, startingIndex, accumulatedProfit, accumulatedWeight, startItemWeight);
        }
        if (getNodeWeight(itemIndex) == 0) {
            // we just see if removing the profit of the item is allowed,
            // then it is NOT mandatory
            decision = getNodeProfit(itemIndex) > allowedProfitLoss + ComputingLossWeightTree.OFFSET;
            return new SearchInfos(decision, startingIndex, accumulatedProfit, accumulatedWeight, startItemWeight);
        }
        double itemWeight = getNodeWeight(itemIndex);

        if (!isLeaf(startingIndex)) {
            // no node left to add
            decision = itemWeight * getLeaf(itemIndex).getEfficiency() - accumulatedProfit > allowedProfitLoss
                    + ComputingLossWeightTree.OFFSET;
            return new SearchInfos(decision, startingIndex, accumulatedProfit, accumulatedWeight, startItemWeight);
        }

        int index = startingIndex;
        double itemEfficiency = getLeaf(itemIndex).getEfficiency();
        double profit = accumulatedProfit;
        double weight = accumulatedWeight;
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
        double remainingWeight = 0;
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
                remainingWeight = startItemWeight - portionWeight;
            } else {
                remainingWeight = getNodeWeight(index) - portionWeight;
            }
            decision = weight + ComputingLossWeightTree.OFFSET < itemWeight;
            if (Math.abs(weight * itemEfficiency - profit - allowedProfitLoss) > 0.01) {
                throw new RuntimeException("Limit Weight found is not correct");
            }
        }
        return new SearchInfos(decision, index, profit, weight, remainingWeight);
    }

    /**
     * Computes whether an item can be determined as forbidden (cannot be in any optimal solution).
     * <p>
     * An item is forbidden if including it would require excluding items whose combined profit
     * exceeds the allowed profit loss from the optimal solution. This method performs a search
     * through the tree to find items that can be excluded to make room for the item under
     * consideration, and determines if the profit loss would be too great.
     *
     * @param criticalInfos     information about the Dantzig relaxation solution
     * @param itemIndex         global index of the item to check for being forbidden
     * @param startingIndex     global index to start the search from
     * @param accumulatedProfit profit accumulated during the search so far
     * @param accumulatedWeight weight accumulated during the search so far
     * @param allowedProfitLoss maximum allowed profit loss from the optimal solution
     * @param startItemWeight   weight to consider for the starting index item
     * @return SearchInfos object containing the decision (true if forbidden), last item index,
     *         accumulated profit and weight, and remaining weight of the last item
     */
    public SearchInfos computeLimitWeightForbidden(Info criticalInfos,
                                                   int itemIndex, int startingIndex, double accumulatedProfit,
                                                   double accumulatedWeight, double allowedProfitLoss,
                                                   double startItemWeight) {
        assert !isInnerNode(startingIndex);
        boolean decision = false;
        double itemWeight = getNodeWeight(itemIndex);
        if (!isLeaf(startingIndex)) {
            // no node left to add
            decision = accumulatedProfit - itemWeight * getLeaf(itemIndex).getEfficiency() > allowedProfitLoss
                    + ComputingLossWeightTree.OFFSET;
            return new SearchInfos(decision, startingIndex, accumulatedProfit, accumulatedWeight, startItemWeight);
        }

        int index = startingIndex;
        double itemEfficiency = getLeaf(itemIndex).getEfficiency();
        double profit = accumulatedProfit;
        double weight = accumulatedWeight;
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
        double remainingWeight = 0;
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
                remainingWeight = startItemWeight - portionWeight;
            } else {
                remainingWeight = getNodeWeight(index) - portionWeight;
            }
            decision = weight + ComputingLossWeightTree.OFFSET < itemWeight;
            if (Math.abs(profit - weight * itemEfficiency - allowedProfitLoss) > 0.01) {
                throw new RuntimeException("Limit Weight found is not correct");
            }
        }
        return new SearchInfos(decision, index, profit, weight, remainingWeight);
    }

}
