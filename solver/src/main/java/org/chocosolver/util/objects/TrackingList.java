/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects;

import org.chocosolver.memory.IEnvironment;

/**
 * The Tracking list is similar to a doubly linked list
 * for which each element has a predecessor and a successor, implemented by arrays
 * An artifical source node is added at the beginning of the list
 * An artificial sink node is added at the end of the list
 * The tracking list allows to call the functions TrackPrev and TrackNext that are specific to this data structure
 * Furthermore, it is possible to distinguish the elements removed/reinserted to the list and to the universe in case the tracking list is used in a dynamic environment
 * @author Sulian Le Bozec-Chiffoleau
 * @since 17 Oct. 2024
 */

public class TrackingList {
    private int[] successor;
    private int[] predecessor;
    private int[] stackRemoved;
    private int topRemoved;
    private boolean[] present;
    private final int source;
    private final int sink;
    private final int minValue;
    private final int maxValue;
    private final int maxSize;
    private int universeSize;

    public TrackingList(int a, int b) {
        this.minValue = a;
        this.maxValue = b;
        this.maxSize = maxValue - minValue + 1;
        this.source = minValue -1;
        this.sink = maxValue + 1;
        this.successor = new int[maxSize+1]; // Position 0 indicates the successor of the source node
        this.predecessor = new int[maxSize+1]; // Position maxSize indicates the predecessor of the sink node
        for (int i = 0; i < maxSize+1; i++) {
            this.successor[i] = i;
            this.predecessor[i] = i-1;
        }
        this.universeSize = maxSize;
        this.stackRemoved = new int[maxSize];
        this.topRemoved = 0;
        this.present = new boolean[maxSize];
        for (int i = 0; i < maxSize; i++) {present[i] = true;}
    }


    public int getSize() {return universeSize - topRemoved;}

    public int getUniverseSize() {return universeSize;}

    /**
     * Returns the next element in the in-list
     * Starts by converting the element into its equivalent index and then gets the next index in the in-list
     * Ends by converting the found index into the actual element and returns it
     */
    public int getNext(int e) {return convertToValue(successor[convertToIndex(e) +1]);}

    /**
     * Returns the previous element in the in-list
     * Starts by converting the element into its equivalent index and then gets the previous index in the in-list
     * Ends by converting the found index into the actual element and returns it
     */
    public int getPrevious(int e) {return convertToValue(predecessor[convertToIndex(e)]);}

    /**
     * Returns the artificial source element
     */
    public int getSource(){return source;}

    /**
     * Returns the artificial sink element
     */
    public int getSink(){return sink;}

    public boolean isEmpty(){return topRemoved == universeSize;}

    /**
     * Returns true iff the element is present in the in-list
     */
    public boolean isPresent(int e) {
        return  present[convertToIndex(e)];
    }

    /**
     * Returns true iff the element is not the first element in the in-list
     */
    public boolean hasPrevious(int e) {
        return (getPrevious(e) != source);
    }

    /**
     * Returns true iff the element is not the last element in the in-list
     */
    public boolean hasNext(int e) {
        return (getNext(e) != sink);
    }

    /**
     * Removes an element from the in-list
     * @Warning You can only remove an element that is present in the in-list and that is not the source nor the sink
     */
    public void remove(int e) {
        assert(isPresent(e));
        int i = convertToIndex(e);
        successor[predecessor[i] + 1] = successor[i + 1];
        predecessor[successor[i + 1]] = predecessor[i];
        stackRemoved[topRemoved] = i;
        topRemoved++;
        present[i] = false;
    }

    /**
     * This method allows to reduce the universe so that it is not an interval anymore
     * @Warning This method can be called only if every element of the universe is present in the in-list
     */
    public void removeFromUniverse(int e) {
        assert(topRemoved == 0);
        assert(isPresent(e));
        int i = convertToIndex(e);
        int pi = predecessor[i];
        int si = successor[i + 1];
        successor[pi + 1] = si;
        predecessor[si] = pi;
        universeSize--;
        present[i] = false;
    }

    /**
     * This method is used when using the Tracking List as a backtrackable structure in Choco
     * @Warning This method can be called only if every element of the universe is present in the in-list
     */
    public void removeFromUniverse(int e, IEnvironment env) {
        assert(topRemoved == 0);
        assert(isPresent(e));
        int i = convertToIndex(e);
        int pi = predecessor[i];
        int si = successor[i + 1];
        successor[pi + 1] = si;
        predecessor[si] = pi;
        universeSize--;
        present[i] = false;

        // Here we store the operations to call during the backtrack
        env.save(() -> {
            successor[pi + 1] = i;
            predecessor[si] = i;
            universeSize++;
            present[i] = true;
        });
    }

    /**
     * Refills the tracking list with all the elements of the universe
     */
    public void refill() {
        while (topRemoved != 0) {
            int i = stackRemoved[topRemoved - 1];
            successor[predecessor[i] + 1] = i;
            predecessor[successor[i + 1]] = i;
            topRemoved--;
            present[i] = true;
        }
    }

    /**
     * Special elementary function of the tracking list
     * Returns the first element in the in-list from a given element and toward the predecessors
     */
    public int trackLeft(int e) {
        int i = convertToIndex(e);
        int indexSource = convertToIndex(source);
        int indexSink = convertToIndex(sink);
        while (i != indexSource && i != indexSink && !present[i]) {
            i = predecessor[i];
        }
        return convertToValue(i);
    }

    @Override
    public String toString() {
        int node = getSource();
        String printedString = ""; 
        while (hasNext(node)) {
            node = getNext(node);
            printedString += node + " ";
        }
        return printedString;
    }

    //////////////////////////////////////////
    // Private functions of the Tracking List
    //////////////////////////////////////////

    private int convertToIndex(int e) {return e - minValue;}

    private int convertToValue(int i) {return i + minValue;}

}
