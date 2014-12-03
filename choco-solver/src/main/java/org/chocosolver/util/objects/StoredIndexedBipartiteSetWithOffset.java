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

package org.chocosolver.util.objects;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.structure.IndexedObject;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Nov 5, 2009
 * Time: 12:40:42 PM
 */
public class StoredIndexedBipartiteSetWithOffset extends StoredIndexedBipartiteSet {

    int offset;

    public StoredIndexedBipartiteSetWithOffset(IEnvironment environment, int[] values) {
        super(environment, values);
    }

    public StoredIndexedBipartiteSetWithOffset(IEnvironment environment, IndexedObject[] values) {
        super(environment, values);
    }

    public StoredIndexedBipartiteSetWithOffset(IEnvironment environment, ArrayList<IndexedObject> values) {
        super(environment, values);
    }

    public StoredIndexedBipartiteSetWithOffset(IEnvironment environment, int nbValues) {
        super(environment, nbValues);
    }

    public void buildList(IEnvironment environment, int[] values) {
        this.list = values;
        int maxElt = 0;
        int minElt = Integer.MAX_VALUE;
        for (int value : values) {
            if (value > maxElt) maxElt = value;
            if (value < minElt) minElt = value;
        }
        this.offset = minElt;
        this.position = new int[maxElt - offset + 1];
        for (int i = 0; i < values.length; i++) {
            position[values[i] - offset] = i;
        }
        this.last = environment.makeInt(list.length - 1);

    }

    public boolean contain(int object) {
        return position[object - offset] <= last.get();
    }


    public void remove(int object) {
        if (contain(object)) {
            int idxToRem = position[object - offset];
            if (idxToRem == last.get()) {
                last.add(-1);
            } else {
                int temp = list[last.get()];
                list[last.get()] = object;
                list[idxToRem] = temp;
                position[object - offset] = last.get();
                position[temp - offset] = idxToRem;
                last.add(-1);
            }
        }
    }

    public final int getOffset() {
        return offset;
    }
}
