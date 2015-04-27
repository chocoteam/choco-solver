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
package org.chocosolver.memory.trailing;


import org.chocosolver.memory.*;
import org.chocosolver.memory.structure.Operation;
import org.chocosolver.memory.trailing.trail.*;
import org.chocosolver.memory.trailing.trail.chunck.*;
import org.chocosolver.memory.trailing.trail.flatten.*;
import org.chocosolver.memory.trailing.trail.unsafe.UnsafeBoolTrail;
import org.chocosolver.memory.trailing.trail.unsafe.UnsafeDoubleTrail;
import org.chocosolver.memory.trailing.trail.unsafe.UnsafeIntTrail;
import org.chocosolver.memory.trailing.trail.unsafe.UnsafeLongTrail;

/**
 * The root class for managing memory and sessions.
 * <p/>
 * A environment is associated to each problem.
 * It is responsible for managing backtrackable data.
 */
public final class EnvironmentTrailing extends AbstractEnvironment {


    /**
     * The maximum numbers of worlds that a
     * {@link org.chocosolver.memory.IStorage} can handle.
     */
    private int maxWorld = 100; //1000;

    /**
     * The maximum numbers of updates that a
     * {@link org.chocosolver.memory.IStorage} can handle.
     */
    private static final int MaxHist = 5000;

    //Contains all the {@link IStorage} trails for
    // storing different kinds of data.

    private IStoredIntTrail intTrail;
    private IStoredBoolTrail boolTrail;
    private IStoredLongTrail longTrail;
    private IStoredDoubleTrail doubleTrail;
    private IOperationTrail operationTrail;

    private StoredIntVectorTrail intVectorTrail;
    private StoredDoubleVectorTrail doubleVectorTrail;

    /**
     * Contains all the {@link org.chocosolver.memory.IStorage} trails for
     * storing different kinds of data.
     */
    private ITrailStorage[] trails;
    private int trailSize;

    /**
     * Constructs a new <code>IEnvironment</code> with
     * the default stack sizes : 50000 and 1000.
     */

    public EnvironmentTrailing() {
        super(Type.FLAT);
        trails = new ITrailStorage[0];
        trailSize = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void worldPush() {
        timestamp++;
        //code optim.: replace loop by enumeration
        final int wi = currentWorld + 1;
        for (int i = 0; i < trailSize; i++) {
            trails[i].worldPush(wi);
        }
        currentWorld++;
        if (wi == maxWorld - 1) {
            resizeWorldCapacity(maxWorld * 3 / 2);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void worldPop() {
        timestamp++;
        //code optim.: replace loop by enumeration
        final int wi = currentWorld;
        for (int i = trailSize - 1; i >= 0; i--) {
            trails[i].worldPop(wi);
        }
        currentWorld--;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void worldCommit() {
        //code optim.: replace loop by enumeration;
        if (currentWorld == 0) {
            throw new IllegalStateException("Commit in world 0?");
        }
        final int wi = currentWorld;
        for (int i = trailSize; i >= 0; i--) {
            trails[i].worldCommit(wi);
        }
        currentWorld--;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateInt makeInt() {
        return makeInt(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateInt makeInt(final int initialValue) {
        return new StoredInt(this, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateBool makeBool(final boolean initialValue) {
        return new StoredBool(this, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateIntVector makeIntVector(final int size, final int initialValue) {
        return new StoredIntVector(this, size, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateDoubleVector makeDoubleVector(final int size, final double initialValue) {
        return new StoredDoubleVector(this, size, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateDouble makeFloat() {
        return makeFloat(Double.NaN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateDouble makeFloat(final double initialValue) {
        return new StoredDouble(this, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateLong makeLong() {
        return makeLong(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateLong makeLong(final long init) {
        return new StoredLong(this, init);
    }


    private void increaseTrail() {// TODO check resizing
        ITrailStorage[] tmp = trails;
        trails = new ITrailStorage[tmp.length + 1];
        System.arraycopy(tmp, 0, trails, 0, tmp.length);
    }

    public IStoredIntTrail getIntTrail() {
        if (intTrail == null) {
            switch (type) {
                case FLAT:
                    intTrail = new StoredIntTrail(MaxHist, maxWorld);
                    break;
                case CHUNK:
                    intTrail = new StoredIntChunckTrail(maxWorld);
                    break;
                case UNSAFE:
                    intTrail = new UnsafeIntTrail(MaxHist, maxWorld);
                    break;
            }
            increaseTrail();
            trails[trailSize++] = intTrail;
        }
        return intTrail;
    }

    public IStoredLongTrail getLongTrail() {
        if (longTrail == null) {
            switch (type) {
                case FLAT:
                    longTrail = new StoredLongTrail(MaxHist, maxWorld);
                    break;
                case CHUNK:
                    longTrail = new StoredLongChunckTrail(maxWorld);
                    break;
                case UNSAFE:
                    longTrail = new UnsafeLongTrail(MaxHist, maxWorld);
                    break;
            }

            increaseTrail();
            trails[trailSize++] = longTrail;
        }
        return longTrail;
    }

    public IStoredBoolTrail getBoolTrail() {
        if (boolTrail == null) {
            switch (type) {
                case FLAT:
                    boolTrail = new StoredBoolTrail(MaxHist, maxWorld);
                    break;
                case CHUNK:
                    boolTrail = new StoredBoolChunckTrail(maxWorld);
                    break;
                case UNSAFE:
                    boolTrail = new UnsafeBoolTrail(MaxHist, maxWorld);
                    break;
            }

            increaseTrail();
            trails[trailSize++] = boolTrail;
        }
        return boolTrail;
    }

    public IStoredDoubleTrail getDoubleTrail() {
        if (doubleTrail == null) {
            switch (type) {
                case FLAT:
                    doubleTrail = new StoredDoubleTrail(MaxHist, maxWorld);
                    break;
                case CHUNK:
                    doubleTrail = new StoredDoubleChunckTrail(maxWorld);
                    break;
                case UNSAFE:
                    doubleTrail = new UnsafeDoubleTrail(MaxHist, maxWorld);
                    break;
            }
            increaseTrail();
            trails[trailSize++] = doubleTrail;
        }
        return doubleTrail;
    }

    public IOperationTrail getOperationTrail() {
        if (operationTrail == null) {
            switch (type) {
                case FLAT:
                    operationTrail = new OperationTrail(MaxHist, maxWorld);
                    break;
                case CHUNK:
                case UNSAFE:
                    operationTrail = new OperationChunckTrail(maxWorld);
                    break;
            }
            increaseTrail();
            trails[trailSize++] = operationTrail;
        }
        return operationTrail;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SPECIFIC DATA STRUCTURES                                                                                       //
    // NOTE: this data structures should not be used...
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public StoredIntVectorTrail getIntVectorTrail() {
        if (intVectorTrail == null) {
            intVectorTrail = new StoredIntVectorTrail(this, MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = intVectorTrail;
        }
        return intVectorTrail;
    }

    public StoredDoubleVectorTrail getDoubleVectorTrail() {
        if (doubleVectorTrail == null) {
            doubleVectorTrail = new StoredDoubleVectorTrail(this, MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = doubleVectorTrail;
        }
        return doubleVectorTrail;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void resizeWorldCapacity(final int newWorldCapacity) {
        for (final ITrailStorage trail : trails) {
            trail.resizeWorldCapacity(newWorldCapacity);
        }
        maxWorld = newWorldCapacity;
    }


    public void save(Operation oldValue) {
        getOperationTrail().savePreviousState(oldValue);
    }
}

