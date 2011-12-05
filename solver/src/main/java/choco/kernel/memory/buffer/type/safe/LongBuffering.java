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
package choco.kernel.memory.buffer.type.safe;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.buffer.EnvironmentBuffering;
import choco.kernel.memory.buffer.type.ILongBuffering;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

/**
 * A buffer fmwk for int
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/08/11
 */
public class LongBuffering implements ILongBuffering {

    public static final int LONG_SIZE = 8; // Size of primitive long in bytes

    public static final int INIT_SIZE = 64; // initial size of buffer

    public static final int INIT_WORLD_CAPACITY = 128;

    private final EnvironmentBuffering environment; // link to the environment

    private LongBuffer[] buffers; // previous buffers

    private int currentWorld;

    private LongBuffer current; // current buffer

    private int position; // current position in the current buffer -- differs from current.position() for dynamic adding

    public LongBuffering(EnvironmentBuffering environmentBuffering) {
        this.environment = environmentBuffering;
        this.buffers = new LongBuffer[INIT_WORLD_CAPACITY];
        this.current = build(INIT_SIZE); // for 8 long
        this.position = 0;
    }

    private static LongBuffer build(int size) {
        return ByteBuffer.allocateDirect(size * LONG_SIZE)
                .order(ByteOrder.nativeOrder())
                .asLongBuffer();
    }

    /**
     * Copy <code>original</code> into <code>clone</code>
     *
     * @param original buffer to copy
     * @param clone    the copy
     */
    public static void clone(LongBuffer original, LongBuffer clone) {
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
    }

    /**
     * Return the current position in the current buffer
     *
     * @return position
     */
    public final int nextIdx() {
        int nidx = position++;
        ensureCapacity();
        return nidx;
    }

    /**
     * Resize the current buffer if necessary.
     */
    private void ensureCapacity() {
        if (current.capacity() <= position) {
            LongBuffer tmp = current;
            // double size of current buffer
            current = build(tmp.capacity() * 2);
            // clone the current buffer
            clone(tmp, current);
            // update the limit
            current.limit(current.capacity());
        }
    }

    /**
     * Retrieve the value of the i^th long in the buffer
     *
     * @param i index of the long
     * @return its current value
     */
    public final long get(int i) {
        return current.get(i);
    }

    /**
     * Write the new value of the i^th long in the buffer
     *
     * @param i index
     * @param v value
     */
    public final void save(int i, long v) {
        current.put(i, v);
    }

    /**
     * Return the declaring environment
     *
     * @return environment
     */
    public IEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public void worldPush(int wi) {
        LongBuffer clone = build(current.capacity());
        clone(current, clone);
        if (buffers.length <= currentWorld) {
            LongBuffer[] tmp = buffers;
            buffers = new LongBuffer[buffers.length * 2];
            System.arraycopy(tmp, 0, buffers, 0, currentWorld);
        }
        buffers[currentWorld++] = clone;
    }

    @Override
    public void worldPop(int wi) {
        current = buffers[--currentWorld];
    }

    @Override
    public void clear() {
        current = buffers[0];
        currentWorld = 0;
    }

    public String toString() {
        StringBuilder st = new StringBuilder();
        for (int i = 0; i < current.capacity(); i++) {
            st.append(current.get(i)).append(" ");
        }
        return st.toString();
    }
}
