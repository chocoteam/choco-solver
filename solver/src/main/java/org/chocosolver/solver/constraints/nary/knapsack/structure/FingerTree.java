/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.knapsack.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic finger search tree implementation for efficient tree traversal.
 * <p>
 * This class provides a binary tree structure with two key features:
 * <ol>
 *   <li><b>Complete binary tree</b>: All levels are fully filled except possibly the last level.</li>
 *   <li><b>Finger search</b>: Allows efficient traversal to adjacent nodes using precomputed "finger" links.</li>
 * </ol>
 * <p>
 * The tree consists of:
 * <ul>
 *   <li>An array of inner nodes (non-leaf nodes)</li>
 *   <li>An array of leaf nodes (items)</li>
 * </ul>
 * <p>
 * The finger search capability allows moving to the next/previous node at the same depth
 * in amortized constant time, which is crucial for the efficiency of the Katriel algorithm.
 *
 * @param <NodeType>  the type of inner nodes (e.g., {@link InnerNodeSum}, {@link InnerNodeMaxWeight})
 * @param <LeafType>  the type of leaf nodes (e.g., {@link KPItem})
 * @author Nicolas PIERRE
 */
public class FingerTree<NodeType, LeafType> {
    
    /**
     * List of inner nodes in the tree, stored in level-order traversal.
     * Index 0 is the root, index 1-2 are its children, etc.
     */
    private ArrayList<NodeType> innerNodeTreeList;
    
    /**
     * List of leaf nodes in the tree, stored in order.
     * Leaf indices start after all inner node indices.
     */
    private ArrayList<LeafType> leafTreeList;

    /**
     * Returns the list of inner nodes in this tree.
     *
     * @return the list of inner nodes
     */
    public ArrayList<NodeType> getInnerNodeTreeList() {
        return innerNodeTreeList;
    }

    /**
     * Returns the list of leaf nodes in this tree.
     *
     * @return the list of leaf nodes
     */
    public ArrayList<LeafType> getLeafTreeList() {
        return leafTreeList;
    }

    /**
     * Constructs a finger tree from a list of sorted items.
     * The items should be sorted by decreasing efficiency (profit/weight ratio),
     * with ties broken in favor of larger weights.
     *
     * @param sortedItems knapsack items sorted by decreasing efficiency, with ties broken by larger weight
     */
    public FingerTree(List<LeafType> sortedItems) {
        init(sortedItems);
    }

    /**
     * Constructs an empty finger tree.
     * This is used by subclasses that need to customize the initialization.
     */
    protected FingerTree() {
    }

    /**
     * Initializes the tree structure with the given sorted items.
     * Creates a complete binary tree with enough inner nodes to hold all leaves.
     * The tree is built as a perfect binary tree where all leaves are at the same depth.
     *
     * @param sortedItems the items to store in the tree leaves
     */
    protected void init(List<LeafType> sortedItems) {
        leafTreeList = new ArrayList<>(sortedItems);
        innerNodeTreeList = new ArrayList<>();
        // Calculate size for a complete binary tree: 2^ceil(log2(n)) - 1 inner nodes for n leaves
        int innerNodeSize = power2(1 + (int) (Math.log(sortedItems.size()) / Math.log(2))) - 1;
        innerNodeTreeList.ensureCapacity(innerNodeSize);
        for (int i = 0; i < innerNodeSize; i++) {
            innerNodeTreeList.add(null);
        }
    }

    /**
     * Returns the parent index of a leaf node.
     *
     * @param leafIndex the index of the leaf node
     * @return the index of the parent node
     */
    public int getLeafParentIndex(int leafIndex) {
        return getLeafParentIndex(leafIndex, true);
    }

    /**
     * Returns the parent index of a leaf node with an optional offset.
     * The offset accounts for the inner node list size when computing the parent.
     *
     * @param leafIndex the index of the leaf node (global index)
     * @param offset     if true, applies an offset for the inner node list size
     * @return the index of the parent node
     */
    public int getLeafParentIndex(int leafIndex, boolean offset) {
        return Math.floorDiv((offset ? this.innerNodeTreeList.size() : 0) + leafIndex - 1, 2);
    }

    /**
     * Returns the parent index of any node (leaf or inner).
     * For the root node (index 0), this returns 0.
     *
     * @param nodeIndex the index of the node (global index)
     * @return the index of the parent node
     * @throws IllegalArgumentException if the node index is invalid (neither leaf nor inner node)
     */
    public int getParentIndex(int nodeIndex) {
        if (nodeIndex != 0 && (isLeaf(nodeIndex) || isInnerNode(nodeIndex))) {
            return Math.floorDiv(nodeIndex - 1, 2);
        } else {
            throw new IllegalArgumentException("Getting parent of an invalid index : " + nodeIndex);
        }
    }

    /**
     * Finds the finger neighbor of a node (adjacent node at the same depth).
     * This implements the finger search capability that gives the tree its name.
     * The neighbor must be at the same depth and adjacent in the tree structure.
     *
     * @param nodeIndex the index of the starting node (global index)
     * @param right     true to find the right neighbor, false to find the left neighbor
     * @return the index of the neighbor node, or -1 if it doesn't exist
     */
    public int getFingerNeighboor(int nodeIndex, boolean right) {
        // checks if we are the last element to the border
        if (!isPowerOfTwo(right ? (nodeIndex + 2) : (nodeIndex + 1))) {
            int index = nodeIndex + (right ? 1 : -1);
            if (isLeaf(index) || isInnerNode(index)) {
                return index;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Returns the left child of a node.
     *
     * @param nodeIndex the index of the parent node
     * @return the index of the left child
     */
    public int getLeftChild(int nodeIndex) {
        return 2 * nodeIndex + 1;
    }

    /**
     * Returns the brother node (sibling with the same parent).
     * For the root node (index 0), returns 0 since it has no sibling.
     * For even indices, returns the left sibling (index-1).
     * For odd indices, returns the right sibling (index+1) if it exists.
     *
     * @param nodeIndex the index of a node (global index)
     * @return the index of the brother node, or nodeIndex itself if no brother exists
     */
    public int getBrother(int nodeIndex) {
        // return nodeIndex if there is no brother
        if (nodeIndex == 0) {
            return 0;
        } else if (nodeIndex % 2 == 0) {
            // right to left
            return nodeIndex - 1;
        } else if (nodeIndex == getInnerNodeTreeList().size() + getLeafTreeList().size() - 1) {
            // left to right but right does not exist (only happens on leaves)
            return nodeIndex;
        } else {
            // nodeIndex % 2 == 1
            // left to right otherwise
            return nodeIndex + 1;
        }
    }

    /**
     * Returns the right child of a node.
     *
     * @param nodeIndex the index of the parent node
     * @return the index of the right child
     */
    public int getRightChild(int nodeIndex) {
        return 2 * nodeIndex + 2;
    }

    /**
     * Checks if a given index corresponds to an inner node.
     *
     * @param index the index to check
     * @return true if the index is a valid inner node, false otherwise
     */
    public boolean isInnerNode(int index) {
        return index < innerNodeTreeList.size() && index >= 0;
    }

    /**
     * Returns the inner node at a given index.
     *
     * @param index the index of the inner node
     * @return the inner node at that index
     */
    public NodeType getInnerNode(int index) {
        return innerNodeTreeList.get(index);
    }

    /**
     * Checks if a given index corresponds to a leaf node.
     *
     * @param index the index to check
     * @return true if the index is a valid leaf node, false otherwise
     */
    public boolean isLeaf(int index) {
        return index >= innerNodeTreeList.size() && index < innerNodeTreeList.size() + leafTreeList.size();
    }

    /**
     * Returns the leaf node at a given global index.
     *
     * @param index the global index of the leaf (must be a leaf index)
     * @return the leaf node at that index
     */
    public LeafType getLeaf(int index) {
        return leafTreeList.get(index - innerNodeTreeList.size());
    }

    /**
     * Finds the next node to explore in the specified direction.
     * This is the core of the finger search: it moves up the tree until it can move
     * right/left, then returns the finger neighbor.
     *
     * @param startingIndex the index of the starting node
     * @param right         true to search to the right, false to search to the left
     * @return the index of the next node to explore, or -1 if none exists
     */
    public int getNextNode(int startingIndex, boolean right) {
        int index = startingIndex;
        while (index != 0 && index % 2 == (right ? 0 : 1)) {
            index = getParentIndex(index);
        }
        if (index == 0) {
            return -1;
        } else {
            return getFingerNeighboor(index, right);
        }
    }

    /**
     * Converts a leaf index (0-based in the leaf list) to a global index.
     *
     * @param index the leaf index (0 to numLeaves-1)
     * @return the global index of the leaf
     */
    public int leafToGlobalIndex(int index) {
        return index + getInnerNodeTreeList().size();
    }

    /**
     * Converts a global leaf index to a leaf index (0-based in the leaf list).
     *
     * @param index the global index of the leaf
     * @return the leaf index (0 to numLeaves-1)
     */
    public int globalToLeaf(int index) {
        return index - getInnerNodeTreeList().size();
    }

    /**
     * Returns the total number of nodes in the tree (inner nodes + leaves).
     *
     * @return the total number of nodes
     */
    public int getNumberNodes() {
        return getLeafTreeList().size() + getInnerNodeTreeList().size();
    }

    /**
     * Returns the number of leaf nodes in the tree.
     *
     * @return the number of leaves
     */
    public int getNumberLeaves() {
        return getLeafTreeList().size();
    }

    /**
     * Checks if a number is a power of two.
     *
     * @param x the number to check
     * @return true if x is a power of two, false otherwise
     */
    public static boolean isPowerOfTwo(int x) {
        return x != 0 && ((x & (x - 1)) == 0);
    }

    /**
     * Computes 2 raised to the power of the given exponent.
     *
     * @param exponant the exponent
     * @return 2^exponent
     */
    public static int power2(int exponant) {
        return 1 << exponant;
    }

}