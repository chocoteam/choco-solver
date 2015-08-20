/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.explanations.store;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;

/**
 * Created by cprudhom on 13/11/14.
 * Project: choco.
 */
public class ArrayEventStore implements IEventStore {

    private static final int SIZE = 128;

    //*****************************************//
    // STRUCTURES DEDICATED TO EVENT RECORDING //
    //*****************************************//
    IntVar[] varChunks;   // to store variables, in chronological order
    ICause[] cauChunks;   // to store causes, in chronological order
    IEventType[] masChunks;// to store masks, in chronological order
    int[] val1Chunks;     // to store values, in chronological order
    int[] val2Chunks;     // to store values, in chronological order
    int[] val3Chunks;     // to store values, in chronological order

    IStateInt size;

    public ArrayEventStore(IEnvironment env) {
        size = env.makeInt(0);
        size._set(0, 0); // to force history manually -- required when created during the search

        varChunks = new IntVar[SIZE];
        cauChunks = new ICause[SIZE];
        masChunks = new IEventType[SIZE];
        val1Chunks = new int[SIZE];
        val2Chunks = new int[SIZE];
        val3Chunks = new int[SIZE];
    }

    public void pushEvent(IntVar var, ICause cause, IEventType mask, int one, int two, int three) {
        //        assert cause != Cause.Null : "cause null";
        int idx = size.get();
        if (idx >= varChunks.length) {
            increase();
        }
        varChunks[idx] = var;
        cauChunks[idx] = cause;
        masChunks[idx] = mask;
        val1Chunks[idx] = one;
        val2Chunks[idx] = two;
        val3Chunks[idx] = three;
        size.add(1);
    }

    private void increase() {
        int oldCapacity = varChunks.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);

        IntVar[] varBigger = new IntVar[newCapacity];
        System.arraycopy(varChunks, 0, varBigger, 0, oldCapacity);
        varChunks = varBigger;

        ICause[] cauBigger = new ICause[newCapacity];
        System.arraycopy(cauChunks, 0, cauBigger, 0, oldCapacity);
        cauChunks = cauBigger;

        IEventType[] masBigger = new IEventType[newCapacity];
        System.arraycopy(masChunks, 0, masBigger, 0, oldCapacity);
        masChunks = masBigger;

        int[] valBigger = new int[newCapacity];
        System.arraycopy(val1Chunks, 0, valBigger, 0, oldCapacity);
        val1Chunks = valBigger;

        valBigger = new int[newCapacity];
        System.arraycopy(val2Chunks, 0, valBigger, 0, oldCapacity);
        val2Chunks = valBigger;

        valBigger = new int[newCapacity];
        System.arraycopy(val3Chunks, 0, valBigger, 0, oldCapacity);
        val3Chunks = valBigger;
    }

    public int getSize() {
        return size.get();
    }

    public IntVar getVariable(int evt) {
        return varChunks[evt];
    }

    public IEventType getEventType(int evt) {
        return masChunks[evt];
    }

    public ICause getCause(int evt) {
        return cauChunks[evt];
    }

    public int getFirstValue(int evt) {
        return val1Chunks[evt];
    }

    public int getSecondValue(int evt) {
        return val2Chunks[evt];
    }

    public int getThirdValue(int evt) {
        return val3Chunks[evt];
    }

}
