/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Finger tree with binary search capabilities for knapsack filtering.
 * <p>
 * This class extends {@link FingerTree} with the ability to perform efficient binary searches
 * on the tree structure. It maintains aggregate information (weight) at inner nodes to enable
 * efficient traversal and querying.
 * <p>
 * The tree supports activation and deactivation of leaf items, with automatic propagation
 * of changes up the tree hierarchy.
 *
 * @author Nicolas PIERRE
 */
public class BinarySearchFingerTree extends FingerTree<InnerNode, KPItem> {

    /**
     * Constructs a binary search finger tree from a list of sorted knapsack items.
     *
     * @param sortedItems the knapsack items sorted by decreasing efficiency
     * @param supplier    a supplier function that creates inner nodes of the appropriate type
     */
    public BinarySearchFingerTree(List<KPItem> sortedItems, Supplier<InnerNode> supplier) {
        super(sortedItems);
        setupTree(supplier);
    }

    /**
     * Returns the weight interface for a node at the given index.
     *
     * @param index the global index of the node
     * @return the weight interface for the node
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    private WeightInterface getNodeWeightInterface(int index) {
        if (isLeaf(index)) {
            return getLeaf(index);
        } else if (isInnerNode(index)) {
            return getInnerNode(index);
        } else {
            throw new IndexOutOfBoundsException(
                    "Looking for an index that corresponds to nothing in the tree (leaf outside of range)");
        }
    }

    /**
     * Returns the weight of a node at the given index.
     *
     * @param index the global index of the node
     * @return the weight of the node, or -1 if the index is invalid
     */
    public int getNodeWeight(int index) {
        if (isInnerNode(index) || isLeaf(index)) {
            return getNodeWeightInterface(index).getWeight();
        } else {
            return -1;
        }
    }

    private void setupTree(Supplier<InnerNode> supplier) {

        int innerNodeSize = getInnerNodeTreeList().size();
        for (int i = 0; i < innerNodeSize; ++i) {
            getInnerNodeTreeList().set(i, supplier.get());
        }
        for (int i = 0; i < getLeafTreeList().size(); ++i) {
            getInnerNode(getLeafParentIndex(i)).updateValue(getLeaf(innerNodeSize + i));
        }
        for (int i = innerNodeSize - 1; i > 0; --i) {
            getInnerNode(getParentIndex(i)).updateValue(getInnerNode(i));
        }
    }

    /**
     * Deactivates a leaf and updates inner nodes.
     * When a leaf is deactivated, it is treated as having zero weight and profit,
     * and all ancestor nodes are updated to reflect this change.
     *
     * @param leafIndex global leaf index
     */
    public void removeLeaf(int leafIndex) {
        getLeaf(leafIndex).deactivate();
        updateTree(leafIndex);
    }

    /**
     * Activates a leaf and updates inner nodes.
     * When a leaf is activated, its weight and profit values are restored,
     * and all ancestor nodes are updated to reflect this change.
     *
     * @param leafIndex global leaf index
     */
    public void activateLeaf(int leafIndex) {
        getLeaf(leafIndex).activate();
        updateTree(leafIndex);
    }

    /**
     * Updates inner nodes when a leaf is activated or deactivated.
     * This method propagates the change up the tree hierarchy, updating all
     * affected inner nodes.
     *
     * @param leafIndex global leaf index
     */
    private void updateTree(int leafIndex) {
        int index = getLeafParentIndex(leafIndex, false);
        getInnerNode(index).setup();
        getInnerNode(index).updateValue(getLeaf(leafIndex));
        if (getBrother(leafIndex) != leafIndex) {
            getInnerNode(index).updateValue(getLeaf(getBrother(leafIndex)));
        }
        while (index > 0) {
            int parentIndex = getParentIndex(index);
            if (getBrother(index) != index) {
                getInnerNode(parentIndex).setValue(getInnerNode(index), getInnerNode(getBrother(index)));
            } else {
                getInnerNode(parentIndex).setup();
                getInnerNode(parentIndex).updateValue(getInnerNode(index));
            }
            index = parentIndex;
        }
    }

    /**
     * Computes the minimum leaf global index starting from a given node.
     * If the starting node is an inner node, traverses to the leftmost leaf.
     *
     * @param indexNode starting node (global index)
     * @return a global leaf index (the leftmost leaf in the subtree)
     */
    private int minLeafIndexFromInnerNode(int indexNode) {
        int minIdx = indexNode;
        while (isInnerNode(minIdx)) {
            minIdx = getLeftChild(minIdx);
        }
        return minIdx;
    }

    /**
     * Computes the maximum leaf global index starting from a given node.
     * If the starting node is an inner node, traverses to the rightmost valid leaf.
     * Skips subtrees that are marked as inactive.
     *
     * @param indexNode starting node (global index)
     * @return a global leaf index (the rightmost valid leaf in the subtree)
     */
    private int maxLeafIndexFromInnerNode(int indexNode) {
        int maxIdx = indexNode;
        while (isInnerNode(maxIdx)) {
            maxIdx = getRightChild(maxIdx);
            if (isInnerNode(maxIdx) && !getInnerNode(maxIdx).isActive()) {
                // we are going down a path leading to invalid leaves
                maxIdx = getBrother(maxIdx);
            }
        }
        return maxIdx;
    }

    /**
     * Performs a binary search in the leaves, skipping removed/inactive leaves.
     * <p>
     * When searching right (true), finds the minimum index greater than startIndex where
     * predicate is true and index is less than or equal to boundIndex.
     * When searching left (false), finds the maximum index less than startIndex where
     * predicate is true and index is greater than or equal to boundIndex.
     *
     * @param startIndex global starting leaf index
     * @param boundIndex global index that bounds the search
     * @param predicate  predicate to test on node indices
     * @param right      true if searching left to right, false if searching right to left
     * @return minimum (maximum) index bigger (smaller) than startingIndex for which
     *         predicate(index) is true and index is smaller (bigger) than or equal to boundIndex.
     *         -1 if no such index exists.
     */
    public int binarySearch(int startIndex, int boundIndex, Predicate<Integer> predicate,
            boolean right) {
        assert isLeaf(startIndex);
        int index = startIndex;
        boolean comingFromRightLeaf = false;
        boolean descending = false;
        while ((index != 0 || predicate.test(index))
                && (index == startIndex || isInnerNode(index) || !predicate.test(index))
                && (right ? minLeafIndexFromInnerNode(index) <= boundIndex
                        : maxLeafIndexFromInnerNode(index) >= boundIndex)) {
            if (descending) {
                // We are going down
                int firstIndex = right ? getLeftChild(index) : getRightChild(index);
                int secondIndex = right ? getRightChild(index) : getLeftChild(index);
                if (predicate.test(firstIndex)) {
                    index = firstIndex;
                } else {
                    index = secondIndex;
                }
            } else {
                // we are going up
                int childIndex = right ? getRightChild(index) : getLeftChild(index);

                if (predicate.test(index) && isInnerNode(index)) {
                    if ((right != comingFromRightLeaf)
                            && predicate.test(childIndex)) {
                        // the current node accepts the predicate, and we can explore the other child
                        index = childIndex;
                        descending = true;
                    } else if (index == 0) {
                        // the current node accepts the predicate but no more nodes can be inspected
                        return -1;
                    } else {
                        // the current node accepts the predicate but there are not in the search scope
                        // i.e. behind the starting index
                        comingFromRightLeaf = index % 2 == 0;
                        index = getParentIndex(index);
                    }
                } else {
                    comingFromRightLeaf = index % 2 == 0;
                    index = getParentIndex(index);
                }
            }
        }
        if (isLeaf(index) && (right ? index <= boundIndex : index >= boundIndex)) {
            return index;
        } else {
            // end of the tree or index >(<) boundIndex
            return -1;
        }
    }

    /**
     * Returns a DOT format string representation of the tree.
     * This can be used to visualize the tree structure using Graphviz.
     *
     * @return a DOT format string
     */
    public String toString() {
        StringBuilder str = new StringBuilder("digraph FingerTree{\n");
        for (int i = 0; i < getInnerNodeTreeList().size(); ++i) {
            str.append(i).append(" [label=\"").append(getInnerNode(i)).append("\"];\n");
            if (i != 0) {
                str.append(getParentIndex(i)).append("->").append(i).append(";\n");
            }
        }
        for (int i = 0; i < getLeafTreeList().size(); ++i) {
            str.append("leaf").append(i).append(" [label=\"")
                    /*.append(leafToGlobalIndex(i))*/
                    .append("w=")
                    .append(getLeafTreeList().get(i).getWeight())
                    .append(",p=")
                    .append(getLeafTreeList().get(i).getProfit())
                    .append("\"];\n");
            str.append(getLeafParentIndex(i)).append("-> leaf").append(i).append(";\n");
        }
        str.append("}");
        return str.toString();
    }
}

