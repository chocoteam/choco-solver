/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 27/10/12
 * Time: 01:56
 */

package common.util.objects.setDataStructures;

import common.util.objects.setDataStructures.linkedlist.Set_2LinkedList;
import common.util.objects.setDataStructures.linkedlist.Set_LinkedList;
import common.util.objects.setDataStructures.linkedlist.Set_Std_2LinkedList;
import common.util.objects.setDataStructures.linkedlist.Set_Std_LinkedList;
import common.util.objects.setDataStructures.matrix.Set_Array;
import common.util.objects.setDataStructures.matrix.Set_BitSet;
import common.util.objects.setDataStructures.matrix.Set_Std_Array;
import common.util.objects.setDataStructures.swapList.Set_Std_Swap_Array;
import common.util.objects.setDataStructures.swapList.Set_Std_Swap_Hash;
import common.util.objects.setDataStructures.swapList.Set_Swap_Array;
import common.util.objects.setDataStructures.swapList.Set_Swap_Hash;
import memory.IEnvironment;
import memory.buffer.EnvironmentBuffering;
import memory.copy.EnvironmentCopying;
import memory.copy.Set_Copy;
import memory.structure.Set_Std_BitSet;
import memory.trailing.EnvironmentTrailing;

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

    public final static boolean HARD_CODED = true;

    /**
     * Make a stored set of integers in the range [0,maximumSize-1]
     * Such a set is restored after a backtrack
     *
     * @param type        of set data structure
     * @param maximumSize of the set (maximum value -1)
     * @param environment solver environment
     * @return a new set which can be restored during search, after some backtracks
     */
    public static ISet makeStoredSet(SetType type, int maximumSize, IEnvironment environment) {
        if (HARD_CODED)
            switch (type) {
                case SWAP_ARRAY:
                    return new Set_Std_Swap_Array(environment, maximumSize);
                case SWAP_HASH:
                    return new Set_Std_Swap_Hash(environment, maximumSize);
                case LINKED_LIST:
                    return new Set_Std_LinkedList(environment);
                case DOUBLE_LINKED_LIST:
                    return new Set_Std_2LinkedList(environment);
                case BITSET:
                    new Set_Std_BitSet(environment, maximumSize);
                case BOOL_ARRAY:
                    return new Set_Std_Array(environment, maximumSize);
            }
        return environment.makeSet(type, maximumSize);
    }

    public static ISet makeTrailedSet(SetType type, int sizeMax, EnvironmentTrailing environmentCopying) {
        return new Set_Trail(environmentCopying, makeSet(type, sizeMax));
    }

    public static ISet makeCopiedSet(SetType type, int sizeMax, EnvironmentCopying environmentCopying) {
        return new Set_Copy(environmentCopying, makeSet(type, sizeMax));
    }

    public static ISet makeBufferedSet(SetType type, int sizeMax, EnvironmentBuffering environmentCopying) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    //***********************************************************************************
    // FACTORY - SET
    //***********************************************************************************

    /**
     * Make a set of integers in the range [0,maximumSize-1]
     *
     * @param type        of set data structure
     * @param maximumSize of the set (maximum value -1)
     * @return a new set
     */
    public static ISet makeSet(SetType type, int maximumSize) {
        switch (type) {
            case SWAP_ARRAY:
                return makeSwap(maximumSize, false);
            case SWAP_HASH:
                return makeSwap(maximumSize, true);
            case LINKED_LIST:
                return makeLinkedList(false);
            case DOUBLE_LINKED_LIST:
                return makeLinkedList(true);
            case BITSET:
                return makeBitSet(maximumSize);
            case BOOL_ARRAY:
                return makeArray(maximumSize);
        }
        throw new UnsupportedOperationException("unknown SetType");
    }

    /**
     * Creates a set based on a linked list
     * appropriate when the set has only a few elements
     *
     * @param doubleLink
     * @return a new set
     */
    public static ISet makeLinkedList(boolean doubleLink) {
        if (doubleLink) {
            return new Set_2LinkedList();
        } else {
            return new Set_LinkedList();
        }
    }

    /**
     * Creates a stored set based on a BitSet
     *
     * @param n maximal size of the set
     * @return a new set
     */
    public static ISet makeBitSet(int n) {
        return new Set_BitSet(n);
    }

    /**
     * Creates a set based on a boolean array
     *
     * @param n maximal size of the set
     * @return a new set
     */
    public static ISet makeArray(int n) {
        return new Set_Array(n);
    }

    /**
     * Creates a set that will ALWAYS contain all values from 0 to n-1
     *
     * @param n size of the set
     * @return a new set that must always be full
     */
    public static ISet makeFullSet(int n) {
        return new Set_Full(n);
    }

    /**
     * Creates a set based on swaps
     * Optimal complexity
     *
     * @param n    maximal size of the set
     * @param hash lighter in memory by slower (false is recommended)
     * @return a new set
     */
    public static ISet makeSwap(int n, boolean hash) {
        if (hash) {
            return new Set_Swap_Hash(n);
        } else {
            return new Set_Swap_Array(n);
        }
    }

}
