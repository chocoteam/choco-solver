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
package memory.buffer.type.unsafe;

import memory.IEnvironment;
import memory.buffer.EnvironmentBuffering;
import memory.buffer.type.ILongBuffering;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * A buffer fmwk for int
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/08/11
 */
public class LongBufferingUnsafe implements ILongBuffering {

    public static final int LONG_SIZE = 8; // Size of primitive int in bytes

    public static final int INIT_SIZE = 64; // initial size of buffer

    public static final int INIT_WORLD_CAPACITY = 128;

    public static final Unsafe UNSAFE;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private final EnvironmentBuffering environment; // link to the environment

    private long[] addresses; // previous addresses

    private int currentWorld;

    private long current; // current address

    private int size = INIT_SIZE;

    private int position; // current position in the current buffer -- differs from current.position() for dynamic adding

    public LongBufferingUnsafe(EnvironmentBuffering environmentBuffering) {
        this.environment = environmentBuffering;
        this.addresses = new long[INIT_WORLD_CAPACITY];
        this.current = build(size); // for 8 int
        this.position = 0;
    }

    private static long build(int size) {
        return UNSAFE.allocateMemory(size * LONG_SIZE);
    }

    /**
     * Return the current position in the current buffer
     *
     * @return position
     */
    public final int nextIdx() {
        int nidx = position;
        position += LONG_SIZE;
        ensureCapacity();
        return nidx;
    }

    private void clone(long from, long to) {
        UNSAFE.copyMemory(from, to, position);
    }

    /**
     * Resize the current buffer if necessary.
     */
    private void ensureCapacity() {
        if (size <= position) {
            long tmp = current;
            // double size of current buffer
            size *= 2;
            current = build(size);
            clone(tmp, current);
            UNSAFE.freeMemory(tmp);
        }
    }

    /**
     * Retrieve the value of the i^th int in the buffer
     *
     * @param i index of the int
     * @return its current value
     */
    public final long get(int i) {
        return UNSAFE.getLong(current + i);
    }

    /**
     * Write the new value of the i^th int in the buffer
     *
     * @param i index
     * @param v value
     */
    public final void save(int i, long v) {
        UNSAFE.putLong(current + i, v);
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
        long clone = build(size);
        clone(current, clone);
        if (addresses.length <= currentWorld) {
            long[] tmp = addresses;
            addresses = new long[addresses.length * 2];
            System.arraycopy(tmp, 0, addresses, 0, currentWorld);
        }
        addresses[currentWorld++] = clone;
    }

    @Override
    public void worldPop(int wi) {
        UNSAFE.freeMemory(current);
        current = addresses[--currentWorld];
    }

    @Override
    public void clear() {
        current = addresses[0];
        currentWorld = 0;
    }

    void print(long address) {
        for (int i = 0; i < position; i += LONG_SIZE) {
            System.out.printf("%d ", UNSAFE.getLong(address + i));
        }
        System.out.printf("\n");
    }

    public void print() {
        print(current);
    }
}
