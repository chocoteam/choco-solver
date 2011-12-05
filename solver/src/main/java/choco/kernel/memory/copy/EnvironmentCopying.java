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

package choco.kernel.memory.copy;

import choco.kernel.memory.*;
import choco.kernel.memory.structure.Operation;
import gnu.trove.TIntStack;

import static choco.kernel.memory.copy.RecomputableElement.*;

public class EnvironmentCopying extends AbstractEnvironment {


    /**
     * The current world number (should be less
     * than <code>maxWorld</code>).
     */

    private boolean newEl = false;

    protected final static TIntStack clonedWorldIdxStack;

    public static RcInt[] elementsI;
    public static RcVector[] elementsV;
    public static RcIntVector[] elementsIV;
    public static RcBool[] elementsB;
    public static RcLong[] elementsL;
    public static RcDouble[] elementsD;
    public static RcObject[] elementsO;

    public static int[] indices;
    private static RcSave save;

    public int nbCopy = 0;


    static {
        elementsI = new RcInt[64];
        elementsV = new RcVector[64];
        elementsIV = new RcIntVector[64];
        elementsB = new RcBool[64];
        elementsL = new RcLong[64];
        elementsD = new RcDouble[64];
        elementsO = new RcObject[64];
        indices = new int[NB_TYPE];
        clonedWorldIdxStack = new TIntStack();
    }


    public EnvironmentCopying() {
        for (int i = NB_TYPE; --i >= 0;) indices[i] = 0;
        clonedWorldIdxStack.clear();
        save = new RcSave(this);
    }

    public int getNbCopy() {
        return nbCopy;
    }

    public void add(RecomputableElement rc) {
        switch (rc.getType()) {
            case BOOL:
                int nB = indices[BOOL] + 1;
                if (nB > elementsB.length) {
                    int newSize = elementsB.length * 3 / 2 + 1;
                    while (nB >= newSize) {
                        newSize = (3 * newSize) / 2 + 1;
                    }
                    RcBool[] oldElements = elementsB;
                    elementsB = new RcBool[newSize];
                    System.arraycopy(oldElements, 0, elementsB, 0, oldElements.length);
                }
                elementsB[indices[BOOL]++] = (RcBool) rc;
                newEl = true;
                break;
            case INT:
                int nI = indices[INT] + 1;
                if (nI > elementsI.length) {
                    int newSize = elementsI.length * 3 / 2 + 1;
                    while (nI >= newSize) {
                        newSize = (3 * newSize) / 2 + 1;
                    }
                    RcInt[] oldElements = elementsI;
                    elementsI = new RcInt[newSize];
                    System.arraycopy(oldElements, 0, elementsI, 0, oldElements.length);
                }
                elementsI[indices[INT]++] = (RcInt) rc;
                newEl = true;
                break;
            case LONG:
                int nL = indices[LONG] + 1;
                if (nL > elementsL.length) {
                    int newSize = elementsL.length * 3 / 2 + 1;
                    while (nL >= newSize) {
                        newSize = (3 * newSize) / 2 + 1;
                    }
                    RcLong[] oldElements = elementsL;
                    elementsL = new RcLong[newSize];
                    System.arraycopy(oldElements, 0, elementsL, 0, oldElements.length);
                }
                elementsL[indices[LONG]++] = (RcLong) rc;
                newEl = true;
                break;
            case DOUBLE:
                int nD = indices[DOUBLE] + 1;
                if (nD > elementsD.length) {
                    int newSize = elementsD.length * 3 / 2 + 1;
                    while (nD >= newSize) {
                        newSize = (3 * newSize) / 2 + 1;
                    }
                    RcDouble[] oldElements = elementsD;
                    elementsD = new RcDouble[newSize];
                    System.arraycopy(oldElements, 0, elementsD, 0, oldElements.length);
                }
                elementsD[indices[DOUBLE]++] = (RcDouble) rc;
                newEl = true;
                break;
            case OBJECT:
                int nO = indices[OBJECT] + 1;
                if (nO > elementsO.length) {
                    int newSize = elementsO.length * 3 / 2 + 1;
                    while (nO >= newSize) {
                        newSize = (3 * newSize) / 2 + 1;
                    }
                    RcObject[] oldElements = elementsO;
                    elementsO = new RcObject[newSize];
                    System.arraycopy(oldElements, 0, elementsO, 0, oldElements.length);
                }
                elementsO[indices[OBJECT]++] = (RcObject) rc;
                newEl = true;
                break;
            case VECTOR:
                int nV = indices[VECTOR] + 1;
                if (nV > elementsV.length) {
                    int newSize = elementsV.length * 3 / 2 + 1;
                    while (nV >= newSize) {
                        newSize = (3 * newSize) / 2 + 1;
                    }
                    RcVector[] oldElements = elementsV;
                    elementsV = new RcVector[newSize];
                    System.arraycopy(oldElements, 0, elementsV, 0, oldElements.length);
                }
                elementsV[indices[VECTOR]++] = (RcVector) rc;
                newEl = true;
                break;
            case INTVECTOR:
                int nIV = indices[INTVECTOR] + 1;
                if (nIV > elementsIV.length) {
                    int newSize = elementsIV.length * 3 / 2 + 1;
                    while (nIV >= newSize) {
                        newSize = (3 * newSize) / 2 + 1;
                    }
                    RcIntVector[] oldElements = elementsIV;
                    elementsIV = new RcIntVector[newSize];
                    System.arraycopy(oldElements, 0, elementsIV, 0, oldElements.length);
                }
                elementsIV[indices[INTVECTOR]++] = (RcIntVector) rc;
                newEl = true;
                break;

        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void worldPush() {
        if (newEl) {
            save.currentElementB = new RcBool[indices[BOOL]];
            System.arraycopy(elementsB, 0, save.currentElementB, 0, indices[BOOL]);

            save.currentElementI = new RcInt[indices[INT]];
            System.arraycopy(elementsI, 0, save.currentElementI, 0, indices[INT]);

            save.currentElementL = new RcLong[indices[LONG]];
            System.arraycopy(elementsL, 0, save.currentElementL, 0, indices[LONG]);

            save.currentElementV = new RcVector[indices[VECTOR]];
            System.arraycopy(elementsV, 0, save.currentElementV, 0, indices[VECTOR]);

            save.currentElementIV = new RcIntVector[indices[INTVECTOR]];
            System.arraycopy(elementsIV, 0, save.currentElementIV, 0, indices[INTVECTOR]);

            save.currentElementD = new RcDouble[indices[DOUBLE]];
            System.arraycopy(elementsD, 0, save.currentElementD, 0, indices[DOUBLE]);

            save.currentElementO = new RcObject[indices[OBJECT]];
            System.arraycopy(elementsO, 0, save.currentElementO, 0, indices[OBJECT]);

            newEl = false;
        }
        this.saveEnv();
        currentWorld++;
    }

    private void saveEnv() {
        if (!(currentWorld != 0 && currentWorld == clonedWorldIdxStack.peek())) {

            nbCopy++;

            if (clonedWorldIdxStack.size() == 0)
                clonedWorldIdxStack.push(currentWorld);
            else if (clonedWorldIdxStack.peek() < currentWorld)
                clonedWorldIdxStack.push(currentWorld);

            save.save(currentWorld);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void worldPop() {
        save.restore(--currentWorld);
        clonedWorldIdxStack.pop();
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
    public IStateInt makeIntProcedure(IStateIntProcedure procedure,
                                      int initialValue) {
        return new RcIntProcedure(this, procedure, initialValue);
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
    public IStateIntVector makeIntVector() {
        return new RcIntVector(this);
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
    public IStateIntVector makeIntVector(int[] entries) {
        return new RcIntVector(this, entries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateDoubleVector makeDoubleVector() {
        return new RcDoubleVector(this);
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
    public IStateDoubleVector makeDoubleVector(double[] entries) {
        return new RcDoubleVector(this, entries);
    }

//    @Override
//	public IStateBitSet makeBitSet(int size) {
//		return new RcBitSet(this,size);
//	}

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

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> IStateVector<T> makeVector() {
        return new RcVector<T>(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateObject makeObject(Object obj) {
        return new RcObject(this, obj);
    }

    @Override
    public void save(Operation operation) {
        throw new UnsupportedOperationException();
    }
}

