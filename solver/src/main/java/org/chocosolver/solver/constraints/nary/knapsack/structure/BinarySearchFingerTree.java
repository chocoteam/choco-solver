/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BinarySearchFingerTree extends FingerTree<InnerNode, KPItem> {

    public BinarySearchFingerTree(List<KPItem> sortedItems, Supplier<InnerNode> supplier) {
        super(sortedItems);
        setupTree(supplier);
    }

    /**
     * @param sortedWeights sorted items by efficiency !
     * @param sortedEnergy  sorted items by efficiency !
     */
    public BinarySearchFingerTree(int[] sortedWeights, int[] sortedEnergy, Supplier<InnerNode> supplier) {
        super();
        List<KPItem> sortedList = new ArrayList<>();
        for (int i = 0; i < sortedEnergy.length; ++i) {
            sortedList.set(i, new KPItem(sortedEnergy[i], sortedWeights[i]));
        }
        init(sortedList);
    }

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
     * desactivate leaf and updates inner nodes
     *
     * @param leafIndex global leaf index
     */
    public void removeLeaf(int leafIndex) {
        getLeaf(leafIndex).desactivate();
        updateTree(leafIndex);
    }

    /**
     * activate leaf and updates inner nodes
     *
     * @param leafIndex global leaf index
     */
    public void activateLeaf(int leafIndex) {
        getLeaf(leafIndex).activate();
        updateTree(leafIndex);
    }

    /**
     * updates inner nodes, when we activate/desactivate a leaf
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
     * Compute the minimum leaf global index starting from a given node.
     * If indexNode has a valid leaf, it will return a valid leaf index
     * 
     * @param indexNode starting node
     * @return a global leaf index
     */
    private int minLeafIndexFromInnerNode(int indexNode) {
        int minIdx = indexNode;
        while (isInnerNode(minIdx)) {
            minIdx = getLeftChild(minIdx);
        }
        return minIdx;
    }

    /**
     * Compute the maximum leaf global index starting from a given node.
     * If indexNode has a valid leaf, it will return a valid leaf index
     * 
     * @param indexNode starting node
     * @return a global leaf index
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
     * Binary search in the leaves, does not consider "removed" leaves
     * all informations in () are valid when right=false
     *
     * @param startIndex global starting leaf index
     * @param boundIndex global index that bounds the search
     * @param predicate  int -> boolean
     * @param right      true iff the search is left to right
     * @return minimum(maximum) index bigger(smaller) than startingIndex for which
     *         predicate(index) is true and index is
     *         smaller(bigger) than or equal to boundIndex.
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
                        // the current node accepts the predicate and we can explore the other child
                        index = childIndex;
                        descending = true;
                    } else if (index == 0) {
                        // the current node accepts the predicate but no more nodes can be inspected
                        return -1;
                    } else {
                        // the current node accepts the predicate but there are not in the search scope
                        // ie. behind the starting index
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

    public String toString() {
        String str = "digraph fingertree{\n";
        for (int i = 0; i < getInnerNodeTreeList().size(); ++i) {
            str += "" + i + " [label=\"" + i + getInnerNode(i) + "\"];\n";
            if (i != 0) {
                str += "" + getParentIndex(i) + "->" + i + ";\n";
            }
        }
        for (int i = 0; i < getLeafTreeList().size(); ++i) {
            str += "leaf" + i + " [label=\"" + leafToGlobalIndex(i) + "w=" + getLeafTreeList().get(i).getWeight()
                    + ",p=" + getLeafTreeList().get(i).getProfit() + "\"];\n";
            str += "" + getLeafParentIndex(i) + "-> leaf" + i + ";\n";
        }
        str += "}";
        return str;
    }

    /**
     * Create the dot graph of the tree, useful for debug
     */
    public void createDotFile(String filename) {
        try (FileWriter fWriter = new FileWriter(filename)) {
            fWriter.write(this.toString());
        } catch (Exception e) {
            System.err.println("Error trying to create DOT file : ");
            e.printStackTrace();
        }
    }
}
