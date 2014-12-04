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
package org.chocosolver.solver.explanations.store;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cprudhom on 13/11/14.
 * Project: choco.
 */
public class ChunkedEventStore implements IEventStore {

    private static final int CHUNK_SIZE = 128; // need to be a power of 2

    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkedEventStore.class);

    //*****************************************//
    // STRUCTURES DEDICATED TO EVENT RECORDING //
    //*****************************************//
    IntVar[][] varChunks;   // to store variables, in chronological order
    ICause[][] cauChunks;   // to store causes, in chronological order
    IEventType[][] masChunks;// to store masks, in chronological order
    int[][] val1Chunks;     // to store values, in chronological order
    int[][] val2Chunks;     // to store values, in chronological order
    int[][] val3Chunks;     // to store values, in chronological order

    IStateInt curChunk;
    IStateInt nextTop;
    IStateInt size;

    boolean up2date;  // to indicate if the database is up-to-date

    public ChunkedEventStore(IEnvironment env) {
        curChunk = env.makeInt(0);
        nextTop = env.makeInt(0);
        size = env.makeInt(0);

        varChunks = new IntVar[1][];
        varChunks[0] = new IntVar[CHUNK_SIZE];

        cauChunks = new ICause[1][];
        cauChunks[0] = new ICause[CHUNK_SIZE];

        masChunks = new IEventType[1][];
        masChunks[0] = new IEventType[CHUNK_SIZE];

        val1Chunks = new int[1][];
        val1Chunks[0] = new int[CHUNK_SIZE];

        val2Chunks = new int[1][];
        val2Chunks[0] = new int[CHUNK_SIZE];

        val3Chunks = new int[1][];
        val3Chunks[0] = new int[CHUNK_SIZE];

        up2date = false;
    }

    public void pushEvent(IntVar var, ICause cause, IEventType mask, int one, int two, int three) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("WRITE {} {} {} {} {}", var, cause, mask, one, two, three);
        }
        assert cause != Cause.Null : "cause null";
        int currentC = curChunk.get();
        int currentI = nextTop.add(1);
        if (currentI > CHUNK_SIZE) {
            currentC = curChunk.add(1);
            int l = varChunks.length;
            if (currentC == l) {
                increase(l);
            }
            nextTop.set(1);
            currentI = 0;
        } else {
            currentI--;
        }
        varChunks[currentC][currentI] = var;
        cauChunks[currentC][currentI] = cause;
        masChunks[currentC][currentI] = mask;
        val1Chunks[currentC][currentI] = one;
        val2Chunks[currentC][currentI] = two;
        val3Chunks[currentC][currentI] = three;
        size.add(1);
        up2date = false;
    }

    @Override
    public void popEvent() {

    }

    private void increase(int l) {
        IntVar[][] varBigger = new IntVar[l + 1][];
        System.arraycopy(varChunks, 0, varBigger, 0, l);
        varBigger[l] = new IntVar[CHUNK_SIZE];
        varChunks = varBigger;

        ICause[][] cauBigger = new ICause[l + 1][];
        System.arraycopy(cauChunks, 0, cauBigger, 0, l);
        cauBigger[l] = new ICause[CHUNK_SIZE];
        cauChunks = cauBigger;

        IEventType[][] masBigger = new IEventType[l + 1][];
        System.arraycopy(masChunks, 0, masBigger, 0, l);
        masBigger[l] = new IEventType[CHUNK_SIZE];
        masChunks = masBigger;

        int[][] valBigger = new int[l + 1][];
        System.arraycopy(val1Chunks, 0, valBigger, 0, l);
        valBigger[l] = new int[CHUNK_SIZE];
        val1Chunks = valBigger;

        valBigger = new int[l + 1][];
        System.arraycopy(val2Chunks, 0, valBigger, 0, l);
        valBigger[l] = new int[CHUNK_SIZE];
        val2Chunks = valBigger;

        valBigger = new int[l + 1][];
        System.arraycopy(val3Chunks, 0, valBigger, 0, l);
        valBigger[l] = new int[CHUNK_SIZE];
        val3Chunks = valBigger;
    }

    public boolean isUptodate() {
        return up2date;
    }

    public void setUptodate(boolean b) {
        this.up2date = b;
    }

    public int getSize() {
        return size.get();
    }

    private int getChunk(int evt) {
        return evt / CHUNK_SIZE;
    }

    private int getCell(int evt) {
        return evt & CHUNK_SIZE - 1;
    }

    public IntVar getVariable(int evt) {
        return varChunks[getChunk(evt)][getCell(evt)];
    }

    public IEventType getEventType(int evt) {
        return masChunks[getChunk(evt)][getCell(evt)];
    }

    public ICause getCause(int evt) {
        return cauChunks[getChunk(evt)][getCell(evt)];
    }

    public int getFirstValue(int evt) {
        return val1Chunks[getChunk(evt)][getCell(evt)];
    }

    public int getSecondValue(int evt) {
        return val2Chunks[getChunk(evt)][getCell(evt)];
    }

    public int getThirdValue(int evt) {
        return val3Chunks[getChunk(evt)][getCell(evt)];
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
