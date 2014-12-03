/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.util.objects;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;

import java.io.Serializable;

/**
 * Implementation based on "Maintaining GAC on adhoc r-ary constraints", Cheng and Yap, CP12.
 * <p/>
 * <p/>
 * Created by cprudhom on 04/11/14.
 * Project: choco.
 */
public class StoredSparseSet implements Serializable{

    int[] sparse;
    int[] dense;
    IStateInt members;


    public StoredSparseSet(IEnvironment environment) {
        members = environment.makeInt(0);
        sparse = new int[16];
        dense = new int[16];
    }

    /**
     * Return if a value is contained.
     *
     * @param k the value to test
     * @return true if `k` is contained, false otherwise
     */
    public boolean contains(int k) {
        if (k < sparse.length) {
            int a = sparse[k];
            return a < members.get() && dense[a] == k;
        } else return false;
    }

    /**
     * Add the value `k` to the set.
     *
     * @param k value to add
     */
    public void add(int k) {
        ensureCapacity(k+1);
        int a = sparse[k];
        int b = members.get();
        if (a >= b || dense[a] != k) {
            sparse[k] = b;
            dense[b] = k;
            members.set(b + 1);
        }
    }

    private void ensureCapacity(int k) {
        if (k > sparse.length) {
            int[] tmp = sparse;
            int nsize = Math.max(k+1, tmp.length * 3 / 2 + 1);
            sparse = new int[nsize];
            sparse = new int[nsize];
            System.arraycopy(tmp, 0, sparse, 0, tmp.length);
            tmp = dense;
            dense = new int[nsize];
            System.arraycopy(tmp, 0, dense, 0, tmp.length);
        }

    }

}
