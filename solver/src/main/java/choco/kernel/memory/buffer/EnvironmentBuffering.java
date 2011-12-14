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
package choco.kernel.memory.buffer;

import choco.kernel.memory.*;
import choco.kernel.memory.buffer.type.IBoolBuffering;
import choco.kernel.memory.buffer.type.IBuffering;
import choco.kernel.memory.buffer.type.IIntBuffering;
import choco.kernel.memory.buffer.type.ILongBuffering;
import choco.kernel.memory.buffer.type.safe.BoolBuffering;
import choco.kernel.memory.buffer.type.safe.IntBuffering;
import choco.kernel.memory.buffer.type.safe.LongBuffering;
import choco.kernel.memory.buffer.type.unsafe.BoolBufferingUnsafe;
import choco.kernel.memory.buffer.type.unsafe.IntBufferingUnsafe;
import choco.kernel.memory.buffer.type.unsafe.LongBufferingUnsafe;
import choco.kernel.memory.structure.Operation;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/08/11
 */
public class EnvironmentBuffering extends AbstractEnvironment {

    IIntBuffering intB; // a buffer dedicated to int
    ILongBuffering longB; // a buffer dedicated to int
    IBoolBuffering booleanB; // a buffer dedicated to boolean

    private IBuffering[] buffers; // array of IBuffering in use

    private int bSize; // number of IBuffering in use

    private final boolean unsafe;

    public EnvironmentBuffering() {
        this(true);
    }

    public EnvironmentBuffering(boolean unsafe) {
        buffers = new IBuffering[4];
        bSize = 0;
        this.unsafe = unsafe;
    }

    @Override
    public void worldPush() {
        final int wi = currentWorld + 1;
        for (int i = 0; i < bSize; i++) {
            buffers[i].worldPush(wi);
        }
        currentWorld++;
        //if (wi == maxWorld - 1) {
        //    resizeWorldCapacity(maxWorld * 3 / 2);
        //}
    }

    @Override
    public void worldPop() {
        final int wi = currentWorld;
        for (int i = bSize - 1; i >= 0; i--) {
            buffers[i].worldPop(wi);
        }
        currentWorld--;
    }

    @Override
    public void worldCommit() {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        for (int i = 0; i < bSize; i++) {
            buffers[i].clear();
        }
    }

    @Override
    public IStateInt makeInt() {
        return new BuffInt(this, 0);
    }

    @Override
    public IStateInt makeInt(int initialValue) {
        return new BuffInt(this, initialValue);
    }

    @Override
    public IStateInt makeIntProcedure(IStateIntProcedure procedure, int initialValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IStateBool makeBool(boolean initialValue) {
        return new BuffBoolean(this, initialValue);
    }

    @Override
    public IStateIntVector makeIntVector() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IStateIntVector makeIntVector(int size, int initialValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IStateIntVector makeIntVector(int[] entries) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IStateDoubleVector makeDoubleVector() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IStateDoubleVector makeDoubleVector(int size, double initialValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IStateDoubleVector makeDoubleVector(double[] entries) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> IStateVector<T> makeVector() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IStateDouble makeFloat() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IStateLong makeLong() {
        return new BuffLong(this, 0);
    }

    @Override
    public IStateLong makeLong(long init) {
        return new BuffLong(this, init);
    }

    @Override
    public IStateDouble makeFloat(double initialValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IStateObject makeObject(Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(Operation operation) {
        throw new UnsupportedOperationException();
    }

    ///////////////////////////

    private void increaseBuff() {
        IBuffering[] tmp = buffers;
        buffers = new IBuffering[tmp.length + 1];
        System.arraycopy(tmp, 0, buffers, 0, tmp.length);
    }

    public IIntBuffering getIntBuffering() {
        if (intB == null) {
            intB = (unsafe ? new IntBufferingUnsafe(this) : new IntBuffering(this));
            increaseBuff();
            buffers[bSize++] = intB;
        }
        return intB;
    }

    public ILongBuffering getLongBuffering() {
        if (longB == null) {
            longB = (unsafe ? new LongBufferingUnsafe(this) : new LongBuffering(this));
            increaseBuff();
            buffers[bSize++] = longB;
        }
        return longB;
    }

    public IBoolBuffering getBooleanBuffering() {
        if (booleanB == null) {
            booleanB = (unsafe ? new BoolBufferingUnsafe(this) : new BoolBuffering(this));
            increaseBuff();
            buffers[bSize++] = booleanB;
        }
        return booleanB;
    }
}
