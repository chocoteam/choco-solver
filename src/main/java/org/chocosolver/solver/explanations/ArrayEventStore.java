/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.explanations;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;

/**
 * A data structure which stores events during search, for lazy explanation purpose.
 * Created by cprudhom on 13/11/14.
 * Project: choco.
 * @author Charles Prud'homme
 * @since 13/11/14
 */
public class ArrayEventStore {

    /**
     * Default size of arrays
     */
    private static final int SIZE = 128;
    /**
     * to store variables, in chronological order
     */
    private IntVar[] varChunks;
    /**
     * to store causes, in chronological order
     */
    private ICause[] cauChunks;
    /**
     * to store masks, in chronological order
     */
    private IEventType[] masChunks;
    /**
     * to store first values, in chronological order
     */
    private int[] val1Chunks;
    /**
     * to store second values, in chronological order
     */
    private int[] val2Chunks;
    /**
     * to store thrid values, in chronological order
     */
    private int[] val3Chunks;
    /**
     * Number of event stored
     */
    private IStateInt size;

    /**
     * Create an event store
     * @param env backtracking environment
     */
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

    /**
     * Push an event on the top of this store
     * @param var modified variable
     * @param cause cause of the modification
     * @param mask modification mask
     * @param one an int
     * @param two an int
     * @param three an int
     */
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

    /**
     * Forget the last event pushed
     */
    public void forgetLast() {
        size.add(-1);
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

    /**
     * @return number of events stored in this
     */
    public int getSize() {
        return size.get();
    }

    /**
     * @param evt event index
     * @return the variable associated to the event in position <i>evt</i>
     */
    public IntVar getVariable(int evt) {
        return varChunks[evt];
    }

    /**
     * @param evt event index
     * @return the event type associated to the event in position <i>evt</i>
     */
    public IEventType getEventType(int evt) {
        return masChunks[evt];
    }

    /**
     * @param evt event index
     * @return the cause associated to the event in position <i>evt</i>
     */
    public ICause getCause(int evt) {
        return cauChunks[evt];
    }

    /**
     * @param evt event index
     * @return the first integer associated to the event in position <i>evt</i>
     */
    public int getFirstValue(int evt) {
        return val1Chunks[evt];
    }

    /**
     * @param evt event index
     * @return the second associated to the event in position <i>evt</i>
     */
    public int getSecondValue(int evt) {
        return val2Chunks[evt];
    }

    /**
     * @param evt event index
     * @return the third associated to the event in position <i>evt</i>
     */
    public int getThirdValue(int evt) {
        return val3Chunks[evt];
    }

}
