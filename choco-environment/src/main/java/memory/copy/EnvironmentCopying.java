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

package memory.copy;

import memory.*;
import memory.copy.store.*;
import memory.copy.store.flatten.StoredBoolCopy;
import memory.copy.store.flatten.StoredDoubleCopy;
import memory.copy.store.flatten.StoredIntCopy;
import memory.copy.store.flatten.StoredLongCopy;
import memory.structure.Operation;

public class EnvironmentCopying extends AbstractEnvironment {

    private IStoredBoolCopy boolCopy;
    private IStoredIntCopy intCopy;
    private IStoredLongCopy longCopy;
    private IStoredDoubleCopy doubleCopy;
    private StoredOperationCopy operationCopy;

    private StoredIntVectorCopy intVectorCopy;
    private StoredDoubleVectorCopy doubleVectorCopy;
    private StoredObjectCopy objectCopy;

    private IStorage[] copies;
    private int copySize;

    public EnvironmentCopying() {
        super(Type.FLAT);
        copies = new IStorage[0];
        copySize = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void worldPush() {
        final int wi = currentWorld + 1;
        for (int i = 0; i < copySize; i++) {
            copies[i].worldPush(wi);
        }
        currentWorld++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void worldPop() {
        final int wi = currentWorld;
        for (int i = copySize - 1; i >= 0; i--) {
            copies[i].worldPop(wi);
        }
        currentWorld--;
    }

    @Override
    public void save(Operation operation) {
        getOperationCopy().savePreviousState(operation);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void worldCommit() {
        //TODO
        throw (new UnsupportedOperationException());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateInt makeInt() {
        return new RcInt(this, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateInt makeInt(int initialValue) {
        return new RcInt(this, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateBool makeBool(boolean initialValue) {
        return new RcBool(this, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateIntVector makeIntVector(int size, int initialValue) {
        return new RcIntVector(this, size, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateDoubleVector makeDoubleVector(int size, double initialValue) {
        return new RcDoubleVector(this, size, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateDouble makeFloat() {
        return new RcDouble(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateDouble makeFloat(double initialValue) {
        return new RcDouble(this, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateLong makeLong() {
        return new RcLong(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateLong makeLong(long init) {
        return new RcLong(this, init);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void increaseCopy() {// TODO check resizing
        IStorage[] tmp = copies;
        copies = new IStorage[tmp.length + 1];
        System.arraycopy(tmp, 0, copies, 0, tmp.length);
    }

    public IStoredIntCopy getIntCopy() {
        if (intCopy == null) {
            switch (type) {
                case FLAT:
                    intCopy = new StoredIntCopy();
                    break;
            }
            increaseCopy();
            copies[copySize++] = intCopy;
        }
        return intCopy;
    }

    public IStoredLongCopy getLongCopy() {
        if (longCopy == null) {
            switch (type) {
                case FLAT:
                    longCopy = new StoredLongCopy();
                    break;
            }

            increaseCopy();
            copies[copySize++] = longCopy;
        }
        return longCopy;
    }

    public IStoredBoolCopy getBoolCopy() {
        if (boolCopy == null) {
            switch (type) {
                case FLAT:
                    boolCopy = new StoredBoolCopy();
                    break;
            }

            increaseCopy();
            copies[copySize++] = boolCopy;
        }
        return boolCopy;
    }

    public IStoredDoubleCopy getDoubleCopy() {
        if (doubleCopy == null) {
            switch (type) {
                case FLAT:
                    doubleCopy = new StoredDoubleCopy();
                    break;
            }
            increaseCopy();
            copies[copySize++] = doubleCopy;
        }
        return doubleCopy;
    }

    public StoredOperationCopy getOperationCopy() {
        if (operationCopy == null) {
            switch (type) {
                case FLAT:
                    operationCopy = new StoredOperationCopy();
                    break;
            }
            increaseCopy();
            copies[copySize++] = operationCopy;
        }
        return operationCopy;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SPECIFIC DATA STRUCTURES                                                                                       //
    // NOTE: this data structures should not be used...
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public StoredIntVectorCopy getIntVectorCopy() {
        if (intVectorCopy == null) {
            intVectorCopy = new StoredIntVectorCopy(currentWorld);
            increaseCopy();
            copies[copySize++] = intVectorCopy;
        }
        return intVectorCopy;
    }

    public StoredDoubleVectorCopy getDoubleVectorCopy() {
        if (doubleVectorCopy == null) {
            doubleVectorCopy = new StoredDoubleVectorCopy(currentWorld);
            increaseCopy();
            copies[copySize++] = doubleVectorCopy;
        }
        return doubleVectorCopy;
    }

    public StoredObjectCopy getObjectCopy() {
        if (objectCopy == null) {
            objectCopy = new StoredObjectCopy(currentWorld);
            increaseCopy();
            copies[copySize++] = objectCopy;
        }
        return objectCopy;
    }

}

