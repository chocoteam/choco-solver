/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.tools;

import static java.lang.reflect.Array.newInstance;

import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

/**
 * This class contains various methods for manipulating arrays.
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages, Rene Helmke
 * @since 17 sept. 2010
 */
public enum ArrayUtils {
    ;

    /**
     * Creates an array of ints of consecutive values from <i>lb</i> (inclusive) to <i>ub</i> (inclusive as well),
     * i.e. the range <i>[lb, ub]</i>,
     * For instance: {3,4,...,99,100}
     * @param lb first element in the array
     * @param ub last element in the array
     * @return an array of ints
     * @throws NegativeArraySizeException if ub<lb is negative
     */
    public static int[] array(int lb, int ub) {
        if (ub < lb) {
            throw new NegativeArraySizeException("Cannot create negative size array : "+lb+" should be <= "+ub);
        }
        final int[] r = new int[ub-lb+1];
        for (int i = 0; i < r.length; i++) {
            r[i] = i+lb;
        }
        return r;
    }

    /**
     * Create an array of size (<i>end</i> - <i>begin</i>)
     * and assigns to the element <i>i</i> the value (<i>i</i> + <i>begin</i>).
     *  <i>begin</i> must be greater or equal to  <i>end</i>.
     * @param begin first value
     * @param end last value
     * @return null if  <i>begin</i> >  <i>end</i>, an array of ints otherwise.
     * @throws NegativeArraySizeException if begin > end is negative
     */
    public static int[] linspace(int begin, int end) {
        if (end >= begin) {
            int[] r = new int[end - begin];
            for (int i = begin; i < end; i++) {
                r[i - begin] = i;
            }
            return r;
        } else {
            throw new NegativeArraySizeException("Cannot create negative size array");
        }
    }

    /**
     * Returns the column <i>c</i> extracted from matrix <i>array</i>.
     * @param array double entry matrix
     * @param c index of the column to get
     * @return the column <i>c</i> from <i>array</i>, or null if array is null or array.length is null,
     * or if c is negative or if array.length < c
     */
    @SuppressWarnings({"unchecked", "RedundantCast"})
    public static int[] getColumn(final int[][] array, final int c) {
        if (array != null && array.length > 0
                && c >= 0 && array[0].length > c) {
            int[] res = new int[array.length];
            for (int i = 0; i < array.length; i++) {
                res[i] = array[i][c];
            }
            return res;
        }
        return null;
    }

    /**
     * Returns the column <i>c</i> extracted from matrix <i>array</i>.
     * @param array double entry matrix
     * @param c index of the column to get
     * @return the column <i>c</i> from <i>array</i>, or null if array is null or array.length is null,
     * or if c is negative or if array.length < c
     */
    @SuppressWarnings({"unchecked", "RedundantCast"})
    public static double[] getColumn(final double[][] array, final int c) {
        if (array != null && array.length > 0
                && c >= 0 && array[0].length > c) {
            double[] res = new double[array.length];
            for (int i = 0; i < array.length; i++) {
                res[i] = array[i][c];
            }
            return res;
        }
        return null;
    }

    /**
     * Returns the column <i>c</i> extracted from matrix <i>array</i>.
     * @param array double entry matrix
     * @param c index of the column to get
     * @param <T> the class of the objects in the input matrix
     * @return the column <i>c</i> from <i>array</i>, or null if array is null or array.length is null,
     * or if c is negative or if array.length < c
     */
    @SuppressWarnings({"unchecked", "RedundantCast"})
    public static <T> T[] getColumn(final T[][] array, final int c) {
        return getColumn(array, c, (Class<? extends T[]>) array[0].getClass());
    }

    /**
     * Returns the column <i>c</i> extracted from matrix <i>array</i>.
     * @param array double entry matrix
     * @param c index of the column to get
     * @param newType the class of the copy to be returned
     * @param <T> the class of the objects in the input matrix
     * @return the column <i>c</i> from <i>array</i>, or null if array is null or array.length is null,
     * or if c is negative or if array.length < c
     */
    @SuppressWarnings({"unchecked", "RedundantCast"})
    public static <T> T[] getColumn(final T[][] array, final int c, Class<? extends T[]> newType) {
        if (array != null && array.length > 0
                && c >= 0 && array[0].length > c) {
            T[] res = ((Object) newType == (Object) Object[].class)
                    ? (T[]) new Object[array.length]
                    : (T[]) newInstance(newType.getComponentType(), array.length);
            for (int i = 0; i < array.length; i++) {
                res[i] = (T) array[i][c];
            }
            return res;
        }
        return null;
    }

    /**
     * Sum the lengths of <i>arrays</i>
     * @param arrays list of arrays
     * @param <T> the class of the objects in the input array
     * @return the sum of lengths of <i>arrays</i>
     */
    @SafeVarargs
    public static <T> int length(final T[]... arrays) {
        int length = 0;
        for (T[] array : arrays) {
            if (array != null) length += array.length;
        }
        return length;
    }

    /**
     *
     * @param array array of elements
     * @param obj element to find
     * @param <T> the class of the objects in the input array
     * @return <tt>true</tt> if <i>array</i> contains <i>obj</i>
     */
    public static <T> boolean contains(T[] array, T obj) {
        for (T elem : array) {
            if (elem.equals(obj)) return true;
        }
        return false;
    }

    /**
     * Returns the element in position <i>index</i> when considering all <i>arrays</i> appended altogether.
     * @param index position of the element to return
     * @param arrays list of arrays
     * @param <T> the class of the objects in the input arrays
     * @return the element in position <i>index</i> when considering all <i>arrays</i> appended altogether.
     */
    @SafeVarargs
    public static <T> T get(int index, final T[]... arrays) {
        int shift = index;
        for (T[] tab : arrays) {
            if (shift < tab.length) {
                return tab[shift];
            } else {
                shift -= tab.length;
            }
        }
        return null;
    }


    /**
     * Returns the element in position <i>index</i> when considering all <i>arrays</i> appended altogether.
     * @param index position of the element to return
     * @param arrays list of arrays
     * @param <T> the class of the objects in the input lists
     * @return the element in position <i>index</i> when considering all <i>arrays</i> appended altogether.
     */
    @SafeVarargs
    public static <T> T get(int index, final List<T>... arrays) {
        int shift = index;
        for (List<T> tab : arrays) {
            if (shift < tab.size()) {
                return tab.get(shift);
            } else {
                shift -= tab.size();
            }
        }
        return null;
    }

    /**
     * Append two Arrays
     *
     * @param toAppend array of arrays to append
     * @return a new Array composed of both given in parameters.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] append(T[]... toAppend) {
        int total = length(toAppend);
        T[] ret = (T[]) newInstance(toAppend.getClass().getComponentType().getComponentType(), total);
        int pos = 0;
        for (T[] tab : toAppend) {
            if (tab != null) {
                System.arraycopy(tab, 0, ret, pos, tab.length);
                pos += tab.length;
            }
        }
        return ret;
    }

    /**
     * Append two Arrays
     *
     * @param toAppend array of arrays to append
     * @return a new Array composed of both given in parameters.
     */
    @SuppressWarnings("unchecked")
    public static IntVar[] append(IntVar[]... toAppend) {
        int total = length(toAppend);
        IntVar[] ret = new IntVar[total];
        int pos = 0;
        for (IntVar[] tab : toAppend) {
            if (tab != null) {
                System.arraycopy(tab, 0, ret, pos, tab.length);
                pos += tab.length;
            }
        }
        return ret;
    }

    /**
     * Append two Arrays
     *
     * @param toAppend array of arrays to append
     * @return a new Array composed of both given in parameters.
     */
    @SuppressWarnings("unchecked")
    public static BoolVar[] append(BoolVar[]... toAppend) {
        int total = length(toAppend);
        BoolVar[] ret = new BoolVar[total];
        int pos = 0;
        for (BoolVar[] tab : toAppend) {
            if (tab != null) {
                System.arraycopy(tab, 0, ret, pos, tab.length);
                pos += tab.length;
            }
        }
        return ret;
    }

    /**
     * Append <i>elements</i> at the end of another <i>array</i>
     *
     * @param array array of arrays to append
     * @param elements elements to append
     * @return a new Array composed of both given in parameters.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] concat(T[] array, T... elements) {
        return append(array, elements);
    }

    /**
     * Append <i>elements</i> at the end of another <i>array</i>
     *
     * @param array array of arrays to append
     * @param elements elements to append
     * @return a new Array composed of both given in parameters.
     */
    @SuppressWarnings("unchecked")
    public static IntVar[] concat(IntVar[] array, IntVar... elements) {
        return append(array, elements);
    }

    /**
     * Append <i>elements</i> at the end of another <i>array</i>
     *
     * @param array array of arrays to append
     * @param elements elements to append
     * @return a new Array composed of both given in parameters.
     */
    @SuppressWarnings("unchecked")
    public static BoolVar[] concat(BoolVar[] array, BoolVar... elements) {
        return append(array, elements);
    }

    /**
     * Append int arrays
     *
     * @param toAppend array of arrays to append
     * @return a new Array composed of those given in parameters.
     */
    @SuppressWarnings("unchecked")
    public static int[] append(int[]... toAppend) {
        int total = 0;
        for (int[] tab : toAppend) {
            if (tab != null) {
                total += tab.length;
            }
        }
        int[] ret = new int[total];
        int pos = 0;
        for (int[] tab : toAppend) {
            if (tab != null) {
                System.arraycopy(tab, 0, ret, pos, tab.length);
                pos += tab.length;
            }
        }
        return ret;
    }

    /**
     * Append <i>elements</i> at the end of another <i>array</i>
     *
     * @param array array of arrays to append
     * @param elements elements to append
     * @return a new Array composed of both given in parameters.
     */
    @SuppressWarnings("unchecked")
    public static int[] concat(int[] array, int... elements) {
        return append(array, elements);
    }

    /**
     * Reverse all signs of the a given int table.
     *
     * @param tab array to inverse
     */
    public static void inverseSign(int[] tab) {
        for (int i = 0; i < tab.length; i++) {
            tab[i] = -tab[i];
        }
    }

    /**
     * Turns back to from the elements of <i>tab</i> from the middle position.
     * @param tab array to reverse
     */
    public static void reverse(int[] tab) {
        int tmp;
        final int n = tab.length;
        for (int i = 0; i < n / 2; i++) {
            tmp = tab[i];
            tab[i] = tab[n - i - 1];
            tab[n - i - 1] = tmp;
        }
    }

    /**
     * Turns back to from the elements of <i>tab</i> from the middle position.
     * @param tab array to reverse
     * @param <T> the class of the objects in the input array
     */
    public static <T> void reverse(T[] tab) {
        T tmp;
        final int n = tab.length - 1;
        for (int i = 0; i <= n / 2; i++) {
            tmp = tab[i];
            tab[i] = tab[n - i];
            tab[n - i] = tmp;
        }
    }

    /**
     * Permutes elements from <i>tab</i> wrt to <i>permutuation</i>: tab[i] = tab[permutation[i]].
     * @param permutation permutation
     * @param tab array of ints
     * @param <T> the class of the objects in the input array
     */
    @SuppressWarnings("unchecked")
    public static <T> void permutation(int[] permutation, T[] tab) {
        T[] tmp = tab.clone();
        for (int i = 0; i < tab.length; i++) {
            tab[i] = tmp[permutation[i]];
        }
    }

    /**
     * Returns a list composed of elements from <i>array</i>.
     * @param array array of elements
     * @param <T> the class of the objects in the input array
     * @return a list composed of elements from <i>array</i>.
     */
    public static <T> List<T> toList(T[] array) {
        return Arrays.asList(array);
    }

    /**
     * Returns an array composed of elements from <i>list</i>.
     * @param c the class of the copy to be returned
     * @param list list of elements
     * @param <T> the class of the objects in the input array
     * @return an array composed of elements from <i>list</i>.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Class c, List<T> list) {
        //        T[] array = (T[])Array.newInstance(c, list.size());
        //        return list.toArray(array);
        return list.toArray((T[]) newInstance(c, list.size()));
    }

    /**
     * Creates an array from vargs <i>elt</i>.
     * @param elt elements to put in an array
     * @param <T> the class of the objects in the returned array
     * @return an array from vargs <i>elt</i>
     */
    @SafeVarargs
    public static <T> T[] toArray(T... elt) {
        return elt;
    }

    /**
     * Creates an array from elements in <i>list</i>.
     * @param list elements to put in an array
     * @param <T> the class of the objects in the returned array
     * @return an array from element in <i>list</i>
     */
    public static <T> T[] toArray(List<T> list) {
        return toArray(list.get(0).getClass(), list);
    }

    /**
     * Transposes a matrix M[n][m] in a matrix M<sup>T</sup>[m][n] such that M<sup>T</sup>[i][j] = M[j][i]
     * @param matrix matrix to transpose
     * @param <T> the class of the objects in the input matrix
     * @return a matrix
     */
    @SuppressWarnings("unchecked")
    public static <T> T[][] transpose(T[][] matrix) {
        T[][] ret = (T[][]) newInstance(matrix.getClass().getComponentType(), matrix[0].length);
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (T[]) newInstance(matrix[0].getClass().getComponentType(), matrix.length);
        }

        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                ret[j][i] = matrix[i][j];

        return ret;

    }

    /**
     * Transposes a matrix M[n][m] in a matrix M<sup>T</sup>[m][n] such that M<sup>T</sup>[i][j] = M[j][i]
     * @param matrix matrix to transpose
     * @return a matrix
     */
    public static int[][] transpose(int[][] matrix) {
        int[][] ret = (int[][]) newInstance(matrix.getClass().getComponentType(), matrix[0].length);
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (int[]) newInstance(matrix[0].getClass().getComponentType(), matrix.length);
        }

        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                ret[j][i] = matrix[i][j];

        return ret;

    }

    /**
     * Flattens a matrix M[n][m] in an array F[n*m] such that F[i*n+j] = M[i][j].
     * @param matrix matrix to flatten
     * @param <T> the class of the objects in the input matrix
     * @return a matrix
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] flatten(T[][] matrix) {
        int sz = 0;
        for (T[] t : matrix) sz += t.length;
        T[] ret = (T[]) newInstance(matrix[0].getClass().getComponentType(), sz);
        int k = 0;
        for (T[] ta : matrix) {
            for (T t : ta)
                ret[k++] = t;
        }
        return ret;
    }

    /**
     * Flattens a matrix M[n][m][p] in an array F[n*m*p] such that F[(i*n*m) + (j*m) + k] = M[i][j][k].
     * @param matrix matrix to flatten
     * @param <T> the class of the objects in the input matrix
     * @return a matrix
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] flatten(T[][][] matrix) {
        List<T> elt = new ArrayList<>();
        for (T[][] t : matrix) {
            for (T[] tt : t) {
                elt.addAll(Arrays.asList(tt));
            }
        }
        T[] ret = (T[]) newInstance(matrix[0][0].getClass().getComponentType(), elt.size());
        return elt.toArray(ret);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] flattenSubMatrix(int iMin, int iLength, int jMin, int jLength, T[][] matrix) {
        T[] ret = (T[]) newInstance(matrix[0].getClass().getComponentType(), iLength * jLength);
        for (int i = 0, k = 0; i < iLength; i++, k += jLength)
            System.arraycopy(matrix[iMin + i], jMin, ret, k, jLength);
        return ret;
    }

    /**
     * Flattens a matrix M[n][m] in an array F[n*m] such that F[i*n+j] = M[i][j].
     * @param matrix matrix to flatten
     * @return a matrix
     */
    public static int[] flatten(int[][] matrix) {
        int sz = 0;
        for (int[] t : matrix) sz += t.length;
        final int[] ret = new int[sz];
        int k = 0;
        for (int[] ta : matrix) {
            for (int t : ta)
                ret[k++] = t;
        }
        return ret;
    }


    public static int[] createNonRedundantSortedValues(TIntArrayList values) {
        values.sort();
        int offset = 1;
        while (offset < values.size()) {
            while (values.get(offset - 1) == values.get(offset)) {
                values.remove(offset);
                if (offset == values.size()) {
                    break;
                }
            }
            offset++;
        }
        return values.toArray();
    }


    /**
     * Create an array of elements in <i>set</i> and sort them using {@link Arrays#sort(Object[])}
     * @param set set of elements
     * @param <T> the class of the objects in the input set.
     * @return an array of sorted elements from <i>set</i>
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> T[] sort(Set<T> set) {
        final LinkedList<T> tmpl = new LinkedList<>(set);
        if (tmpl.isEmpty()) {
            return null;
        } else {
            T[] tmpa = (T[]) newInstance(tmpl.getFirst().getClass(), tmpl.size());
            tmpl.toArray(tmpa);
            Arrays.sort(tmpa);
            return tmpa;
        }
    }

    /**
     * Duplicates <i>arr</i> and returns the new copy
     * @param arr matrix to duplicate
     * @return a copy of <i>arr</i>
     */
    public static int[][][][] swallowCopy(int[][][][] arr) {
        int s0 = arr.length;
        int[][][][] copy = new int[s0][][][];
        for (int i = s0 - 1; i >= 0; i--) {
            int s1 = arr[i].length;
            copy[i] = new int[s1][][];
            for (int j = s1 - 1; j >= 0; j--) {
                int s2 = arr[i][j].length;
                copy[i][j] = new int[s2][];
                for (int k = s2 - 1; k >= 0; k--) {
                    int s3 = arr[i][j][k].length;
                    copy[i][j][k] = new int[s3];
                    System.arraycopy(arr[i][j][k], 0, copy[i][j][k], 0, s3);
                }
            }
        }
        return copy;

    }

    /**
     * Duplicates <i>arr</i> and returns the new copy
     * @param arr matrix to duplicate
     * @return a copy of <i>arr</i>
     */
    public static int[][][] swallowCopy(int[][][] arr) {
        int s0 = arr.length;
        int[][][] copy = new int[s0][][];
        for (int i = s0 - 1; i >= 0; i--) {
            int s1 = arr[i].length;
            copy[i] = new int[s1][];
            for (int j = s1 - 1; j >= 0; j--) {
                int s2 = arr[i][j].length;
                copy[i][j] = new int[s2];

                System.arraycopy(arr[i][j], 0, copy[i][j], 0, s2);
            }
        }
        return copy;

    }

    /**
     * Creates and returns an array of ints composed of unique values from 0 (inclusive) to nb (exclusive), in random order.
     * @param nb upper value (exclusive)
     * @return an array of ints composed of unique values from 0 (inclusive) to nb (exclusive), in random order.
     */
    public static int[] zeroToNShuffle(int nb) {
        return zeroToNShuffle(nb, System.nanoTime());
    }

    /**
     * Creates and returns an array of ints composed of unique values from 0 (inclusive) to nb (exclusive), in random order.
     * @param nb upper value (exclusive)
     * @param seed seed for randomness
     * @return an array of ints composed of unique values from 0 (inclusive) to nb (exclusive), in random order.
     */
    public static int[] zeroToNShuffle(int nb, long seed) {
        Random r = new Random(seed);
        int[] ret = new int[nb];
        ArrayList<Integer> tmp = new ArrayList<>();
        for (int i = 0; i < nb; i++) tmp.add(i);
        Collections.shuffle(tmp);
        return tmp.stream().mapToInt(i -> i).toArray();

    }

    /**
     * Permutes randomly elements from <i>tab</i>
     * @param tab array of ints
     * @param r randomness generator
     */
    public static void randomPermutations(int[] tab, Random r) {
        int l = tab.length;
        for (int i = 0; i < l; i++) {
            int j = r.nextInt(l);
            int tmp = tab[i];
            tab[i] = tab[j];
            tab[j] = tmp;
        }
    }

    /**
     * Permutes randomly elements from <i>tab</i>
     * @param tab array of ints
     * @param seed seed for randomness
     */
    public static void randomPermutations(int[] tab, long seed) {
        randomPermutations(tab, new Random(seed));
    }

    /**
     * Permutes randomly elements from <i>tab</i>
     * @param tab array of ints
     * @param r randomness generator
     * @param <E> the class of the objects in the input array
     */
    public static <E> void randomPermutations(E[] tab, Random r) {
        int l = tab.length;
        for (int i = 0; i < l; i++) {
            int j = r.nextInt(l);
            E tmp = tab[i];
            tab[i] = tab[j];
            tab[j] = tmp;
        }
    }

    /**
     * Permutes randomly elements from <i>tab</i>
     * @param tab array of ints
     * @param seed seed for randomness
     * @param <E> the class of the objects in the input array
     */
    public static <E> void randomPermutations(E[] tab, long seed) {
        randomPermutations(tab, new Random(seed));
    }

    /**
     * Adapted from java.util.Arrays#binarySearch0(int[], int, int, int) ,
     * it returns the value greater or equal to key in an increasing order value array
     * If the key exists in a, it returns the index of key in a,
     * otherwise it returns the index of the closest value greater than key when gq is set to true,
     * or the index of the closest value smaller than key when gq is set to false.
     *
     * @param a         the values, increasingly ordered
     * @param fromIndex starting index (inclusive)
     * @param toIndex   ending index (exclusive)
     * @param key       value to look for
     * @param gq        set to true to look for the value greater or equal to key,
     *                  false to look for the value smaller or equal to the key
     */
    public static int binarySearchInc(int[] a, int fromIndex, int toIndex, int key, boolean gq) {
        int p = Arrays.binarySearch(a, fromIndex, toIndex, key);
        if (p >= 0) {
            return p;
        } else {
            p = -(p + 1);
            p -= (gq ? 0 : 1);
            return p;
        }
    }

    /**
     * Sorts the input array if it is not already sorted,
     * and removes multiple occurrences of the same value
     *
     * @param values array of values
     * @return a sorted array containing each value of values exactly once
     */
    public static int[] mergeAndSortIfNot(int[] values) {
        int n = values.length;
        boolean sorted = true;
        boolean noDouble = true;
        for (int i = 0; i < n - 1 && sorted; i++) {
            if (values[i] > values[i + 1]) {
                sorted = false;
                noDouble = false;// cannot be sure
            }
            if (values[i] == values[i + 1]) {
                noDouble = false;
            }
        }
        if (!sorted) {
            Arrays.sort(values);
        }
        if (!noDouble) {
            int nbVals = 1;
            for (int i = 0; i < n - 1; i++) {
                assert values[i] <= values[i + 1];
                if (values[i] < values[i + 1]) {
                    nbVals++;
                }
            }
            if (nbVals < n) {
                int[] correctValues = new int[nbVals];
                int idx = 0;
                for (int i = 0; i < n - 1; i++) {
                    if (values[i] < values[i + 1]) {
                        correctValues[idx++] = values[i];
                    }
                }
                correctValues[idx] = values[n - 1];
                return correctValues;
            }
        }
        return values;
    }
}
