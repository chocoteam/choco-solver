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

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;

/**
 * Created by cprudhom on 13/11/14.
 * Project: choco.
 */
public class QueueEventStore implements IEventStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueEventStore.class);

    //*****************************************//
    // STRUCTURES DEDICATED TO EVENT RECORDING //
    //*****************************************//
    ArrayDeque<IntVar> varChunks;   // to store variables, in chronological order
    ArrayDeque<ICause> cauChunks;   // to store causes, in chronological order
    ArrayDeque<IEventType> masChunks;// to store masks, in chronological order
    ArrayDeque<Integer> val1Chunks;     // to store values, in chronological order
    ArrayDeque<Integer> val2Chunks;     // to store values, in chronological order
    ArrayDeque<Integer> val3Chunks;     // to store values, in chronological order
    volatile int size;

    IntVar var;
    ICause cause;
    IEventType mask;
    int v1, v2, v3;


    boolean up2date;  // to indicate if the database is up-to-date

    public QueueEventStore() {
        varChunks = new ArrayDeque<>();
        cauChunks = new ArrayDeque<>();
        masChunks = new ArrayDeque<>();
        val1Chunks = new ArrayDeque<>();
        val2Chunks = new ArrayDeque<>();
        val3Chunks = new ArrayDeque<>();
        up2date = false;
        size = 0;
    }

    public synchronized void pushEvent(IntVar var, ICause cause, IEventType mask, int one, int two, int three) {
//        if (LOGGER.isDebugEnabled()) {
//            LOGGER.debug("WRITE {} {} {} {} {}", var, cause, mask, one, two, three);
//        }
        assert cause != Cause.Null : "cause null";
        varChunks.addLast(var);
        cauChunks.addLast(cause);
        masChunks.addLast(mask);
        val1Chunks.addLast(one);
        val2Chunks.addLast(two);
        val3Chunks.addLast(three);
        up2date = false;
        size++;
    }

    public synchronized void popEvent() {
        var = varChunks.removeFirst();
        mask = masChunks.removeFirst();
        cause = cauChunks.removeFirst();
        v1 = val1Chunks.removeFirst();
        v2 = val2Chunks.removeFirst();
        v3 = val3Chunks.removeFirst();
        size--;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("READ {} {} {} {} {}", var, cause, mask, v1, v2, v3);
        }
    }

    public boolean isUptodate() {
        return up2date;
    }

    public void setUptodate(boolean b) {
        this.up2date = b;
    }

    public synchronized int getSize() {
        return size;
    }

    public IntVar getVariable(int evt) {
        return var;
    }

    public IEventType getEventType(int evt) {
        return mask;
    }

    public ICause getCause(int evt) {
        return cause;
    }

    public int getFirstValue(int evt) {
        return v1;
    }

    public int getSecondValue(int evt) {
        return v2;
    }

    public int getThirdValue(int evt) {
        return v3;
    }

    @Override
    public void clear() {
        varChunks.clear();
        cauChunks.clear();
        masChunks.clear();
        val1Chunks.clear();
        val2Chunks.clear();
        val3Chunks.clear();
    }
}
