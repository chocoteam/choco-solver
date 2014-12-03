/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
 * Date: 06/11/13
 * Time: 17:59
 */

package org.chocosolver.util.sort;

import java.util.Comparator;

public class ArraySort<T extends Object> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int[]	iint;
	private T[]		iobj;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public ArraySort(int nbMaxItems, boolean sortOjects, boolean sortInts){
		if(sortOjects){
			iobj = (T[]) new Object[nbMaxItems];
		}
		if(sortInts){
			iint = new int[nbMaxItems];
		}
	}

	//***********************************************************************************
	// OBJECT SORT
	//***********************************************************************************

	public void sort(T[] items, int size, Comparator<T> sort) {
		System.arraycopy(items, 0, iobj, 0, size);
		mergeSort(items, iobj, 0, size, sort);
		System.arraycopy(iobj, 0, items, 0, size);
	}

	private void mergeSort(T[] src, T[] dest, int low, int high, Comparator<T> c) {
		int length = high - low;
		// Insertion sort on smallest arrays
		if (length < 7) {
			for (int i = low; i < high; i++)
				for (int j = i; j > low && c.compare(dest[j-1], dest[j]) > 0; j--)
					swap(dest, j, j - 1);
			return;
		}
		// Recursively sort halves of dest into src
		int mid = (low + high) >>> 1;
		mergeSort(dest, src, low, mid, c);
		mergeSort(dest, src, mid, high, c);
		// If list is already sorted, just copy from src to dest.  This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (c.compare(src[mid-1], src[mid]) <= 0) {
			System.arraycopy(src, low, dest, low, length);
			return;
		}
		// Merge sorted halves (now in src) into dest
		for (int i = low, p = low, q = mid; i < high; i++) {
			if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0)
				dest[i] = src[p++];
			else
				dest[i] = src[q++];
		}
	}

	private void swap(T[] x, int a, int b) {
		T t = x[a];
		x[a] = x[b];
		x[b] = t;
	}

	//***********************************************************************************
	// INT SORT
	//***********************************************************************************

	public void sort(int[] items, int size, IntComparator sort) {
		System.arraycopy(items, 0, iint, 0, size);
		mergeSort(items, iint, 0, size, sort);
		System.arraycopy(iint, 0, items, 0, size);
	}

	private void mergeSort(int[] src, int[] dest, int low, int high, IntComparator c) {
		int length = high - low;
		// Insertion sort on smallest arrays
		if (length < 7) {
			for (int i = low; i < high; i++)
				for (int j = i; j > low && c.compare(dest[j-1], dest[j]) > 0; j--)
					swap(dest, j, j - 1);
			return;
		}
		// Recursively sort halves of dest into src
		int mid = (low + high) >>> 1;
		mergeSort(dest, src, low, mid, c);
		mergeSort(dest, src, mid, high, c);
		// If list is already sorted, just copy from src to dest.  This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (c.compare(src[mid-1], src[mid]) <= 0) {
			System.arraycopy(src, low, dest, low, length);
			return;
		}
		// Merge sorted halves (now in src) into dest
		for (int i = low, p = low, q = mid; i < high; i++) {
			if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0)
				dest[i] = src[p++];
			else
				dest[i] = src[q++];
		}
	}

	private static void swap(int[] x, int a, int b) {
		int t = x[a];
		x[a] = x[b];
		x[b] = t;
	}
}
