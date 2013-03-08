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

package memory.trailing;


import common.util.objects.setDataStructures.ISet;
import common.util.objects.setDataStructures.SetFactory;
import common.util.objects.setDataStructures.SetType;
import memory.*;
import memory.structure.Operation;
import memory.trailing.trail.*;

/**
 * The root class for managing memory and sessions.
 * <p/>
 * A environment is associated to each problem.
 * It is responsible for managing backtrackable data.
 */
public final class EnvironmentTrailing extends AbstractEnvironment {


    /**
     * The maximum numbers of worlds that a
     * {@link ITrailStorage} can handle.
     */
    private int maxWorld = 100; //1000;

    /**
     * The maximum numbers of updates that a
     * {@link ITrailStorage} can handle.
     */
    private static final int MaxHist = 5000;

    //Contains all the {@link ITrailStorage} trails for
    // storing different kinds of data.
    private StoredIntTrail intTrail;
    private StoredBoolTrail boolTrail;
    private StoredVectorTrail vectorTrail;
    private StoredLongTrail longTrail;
    private StoredIntVectorTrail intVectorTrail;
    private StoredDoubleVectorTrail doubleVectorTrail;
    private StoredDoubleTrail doubleTrail;
    private OperationTrail operationTrail;


    /**
     * Contains all the {@link ITrailStorage} trails for
     * storing different kinds of data.
     */
    private ITrailStorage[] trails;
    private int trailSize;

    /**
     * Constructs a new <code>IEnvironment</code> with
     * the default stack sizes : 50000 and 1000.
     */

    public EnvironmentTrailing() {
        trails = new ITrailStorage[0];
        trailSize = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void worldPush() {
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
        for (int i = trailSize; i >= 0; i--) {
            trails[i].worldCommit();
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
    public IStateInt makeIntProcedure(final IStateIntProcedure procedure,
                                      final int initialValue) {
        return new StoredIntProcedure(this, procedure, initialValue);
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
    public IStateIntVector makeIntVector() {
        return new StoredIntVector(this);
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
    public IStateIntVector makeIntVector(final int[] entries) {
        return new StoredIntVector(this, entries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateDoubleVector makeDoubleVector() {
        return new StoredDoubleVector(this);
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
    public IStateDoubleVector makeDoubleVector(final double[] entries) {
        return new StoredDoubleVector(this, entries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> IStateVector<T> makeVector() {
        return new StoredVector<T>(this);
    }

    //    @Override
    //	public AbstractStateBitSet makeBitSet(int size) {
    //		return new StoredBitSet(this, size);
    //	}

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

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateObject makeObject(final Object obj) {
        throw (new UnsupportedOperationException());
    }

    @Override
    public ISet makeSet(SetType type, int sizeMax) {
        return SetFactory.makeTrailedSet(type, sizeMax, this);
    }

    private void increaseTrail() {// TODO check resizing
        ITrailStorage[] tmp = trails;
        trails = new ITrailStorage[tmp.length + 1];
        System.arraycopy(tmp, 0, trails, 0, tmp.length);
    }

    public StoredIntTrail getIntTrail() {
        if (intTrail == null) {
            intTrail = new StoredIntTrail(this, MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = intTrail;
        }
        return intTrail;
    }

    public StoredLongTrail getLongTrail() {
        if (longTrail == null) {
            longTrail = new StoredLongTrail(MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = longTrail;
        }
        return longTrail;
    }

    public StoredBoolTrail getBoolTrail() {
        if (boolTrail == null) {
            boolTrail = new StoredBoolTrail(this, MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = boolTrail;
        }
        return boolTrail;
    }

    public StoredDoubleTrail getDoubleTrail() {
        if (doubleTrail == null) {
            doubleTrail = new StoredDoubleTrail(this, MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = doubleTrail;
        }
        return doubleTrail;
    }

    public StoredVectorTrail getVectorTrail() {
        if (vectorTrail == null) {
            vectorTrail = new StoredVectorTrail(this, MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = vectorTrail;
        }
        return vectorTrail;
    }

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

    public OperationTrail getOperationTrail() {
        if (operationTrail == null) {
            operationTrail = new OperationTrail(this, MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = operationTrail;
        }
        return operationTrail;
    }

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

