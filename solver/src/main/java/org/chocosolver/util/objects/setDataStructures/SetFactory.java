/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 27/10/12
 * Time: 01:56
 */

package org.chocosolver.util.objects.setDataStructures;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.bitset.Set_BitSet;
import org.chocosolver.util.objects.setDataStructures.bitset.Set_Std_BitSet;
import org.chocosolver.util.objects.setDataStructures.constant.Set_CstInterval;
import org.chocosolver.util.objects.setDataStructures.constant.Set_FixedArray;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.linkedlist.Set_LinkedList;
import org.chocosolver.util.objects.setDataStructures.swapList.Set_Std_Swap;
import org.chocosolver.util.objects.setDataStructures.swapList.Set_Std_Swap2;
import org.chocosolver.util.objects.setDataStructures.swapList.Set_Swap;
import org.chocosolver.util.objects.setDataStructures.swapList.Set_Swap2;

/**
 * Factory for creating sets
 *
 * @author Jean-Guillaume Fages
 * @since Oct 2012
 */
public class SetFactory {

    //***********************************************************************************
    // FACTORY - STORED SET
    //***********************************************************************************

    public static boolean HARD_CODED = true;

    /**
     * Creates a stored set of integers greater or equal than <code>offSet</code>
     * Such a set is restored after a backtrack
     * @param type      of set data structure
     * @param offSet	smallest value allowed in the set (possibly < 0)
     * @param model		model providing the backtracking environment
     * @return a new set which can be restored upon backtrack
     */
    public static ISet makeStoredSet(SetType type, int offSet, Model model) {
        IEnvironment environment = model.getEnvironment();
        if (HARD_CODED) {
            if (type == SetType.SMALLBIPARTITESET) {
                return new Set_Std_Swap2(environment);
            }else if (type == SetType.BIPARTITESET) {
                return new Set_Std_Swap(environment, offSet);
            }else if (type == SetType.BITSET) {
                return new Set_Std_BitSet(environment, offSet);
            }
        }
        return new StdSet(model,makeSet(type,offSet));
    }


    //***********************************************************************************
    // FACTORY - SET
    //***********************************************************************************

    /**
     * Creates an empty set of integers greater or equal than <code>offSet</code>
     * @param type      	implementation type
     * @param offSet		smallest value allowed in the set (possibly < 0)
     * @return a new set
     */
    public static ISet makeSet(SetType type, int offSet) {
        switch (type) {
            case RANGESET:
                return makeRangeSet();
            case BIPARTITESET:
                return makeBipartiteSet(offSet);
            case SMALLBIPARTITESET:
                return makeSmallBipartiteSet();
            case LINKED_LIST:
                return makeLinkedList();
            case BITSET:
                return makeBitSet(offSet);
            case FIXED_ARRAY: throw new UnsupportedOperationException("Please use makeConstantSet method to create a "+SetType.FIXED_ARRAY+" set");
            case FIXED_INTERVAL: throw new UnsupportedOperationException("Please use makeConstantSet method to create a "+SetType.FIXED_INTERVAL+" set");
            default:throw new UnsupportedOperationException("Unsupported SetType "+type);
        }
    }

    /**
     * Creates a set based on an ordered list of ranges
     * @return a new set
     */
    public static ISet makeRangeSet() {
        return new IntIterableRangeSet();
    }

    // --- List

    /**
     * Creates a set based on a linked list
     * appropriate when the set has only a few elements
     * @return a new set
     */
    public static ISet makeLinkedList() {
        return new Set_LinkedList();
    }

    // --- Bit Set

    /**
     * Creates a set of integers, based on an offseted BitSet,
     * Supports integers greater or equal than <code>offSet</code>
     * @param offSet	smallest value allowed in the set (possibly < 0)
     * @return a new set
     */
    public static ISet makeBitSet(int offSet) {
        return new Set_BitSet(offSet);
    }

    // --- Bipartite Set

    /**
     * Creates a set of integers, based on an offseted bipartite set,
     * Supports integers greater or equal than <code>offSet</code>
     * Optimal complexity
     * @param offSet	smallest value allowed in the set (possibly < 0)
     * @return a new bipartite set
     */
    public static ISet makeBipartiteSet(int offSet) {
        return new Set_Swap(offSet);
    }

    /**
     * Creates a set of integers, based on an offseted bipartite set, for small sets
     * (arraylist inside to consume less memory)
     * @return a new bipartite set
     */
    public static ISet makeSmallBipartiteSet() {
        return new Set_Swap2();
    }

    // --- Constant Set

    /**
     * Creates a fixed set of integers, equal to <code>cst</code>
     * @param cst	set value
     * @return a fixed set
     */
    public static ISet makeConstantSet(int[] cst) {
        return new Set_FixedArray(cst);
    }

    /**
     * Creates a constant set of integers represented with an interval [lb, ub]
     * @return a new interval (constant)
     */
    public static ISet makeConstantSet(int lb, int ub) {
        return new Set_CstInterval(lb, ub);
    }
}
