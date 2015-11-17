/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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
package org.chocosolver.memory;

import org.chocosolver.memory.structure.BasicIndexedBipartiteSet;
import org.chocosolver.memory.structure.OneWordS32BitSet;
import org.chocosolver.memory.structure.OneWordS64BitSet;
import org.chocosolver.memory.structure.S64BitSet;

/**
 * Super class of all environments !
 */
public abstract class AbstractEnvironment implements IEnvironment {

    protected enum Type {
        FLAT, CHUNK, UNSAFE
    }

    protected final Type type;

    protected int currentWorld = 0;

    private static final int SIZE = 128;

    private ICondition condition = ICondition.FALSE;

    protected int timestamp;

    /**
     * Shared BitSet
     */
    public BasicIndexedBipartiteSet booleanSet;

    protected AbstractEnvironment(Type type) {
        this.type = type;
        this.timestamp = 0;
    }

    public final int getWorldIndex() {
        return currentWorld;
    }

    @Override
    public final int getTimeStamp() {
        return timestamp;
    }

    /**
     * Factory pattern: new IStateBitSet objects are created by the environment
     *
     * @param size initial size of the IStateBitSet
     * @return IStateBitSet
     */
    @Override
    public IStateBitSet makeBitSet(int size) {
        if (size < 32) {
            return new OneWordS32BitSet(this, size);
        } else if (size < 64) {
            return new OneWordS64BitSet(this, size);
        } else {
            return new S64BitSet(this, size);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void worldPopUntil(int w) {
        while (currentWorld > w) {
            worldPop();
        }
    }

    public final void createSharedBipartiteSet(int size) {
        booleanSet = new BasicIndexedBipartiteSet(this, size);
    }

    /**
     * Factory pattern : shared StoredBitSetVector objects is return by the environment
     *
     * @return a shared bipartite set
     */
    @Override
    public final BasicIndexedBipartiteSet getSharedBipartiteSetForBooleanVars() {
        if (booleanSet == null) {
            createSharedBipartiteSet(SIZE);
        }
        return booleanSet;
    }

    @Override
    public boolean fakeHistoryNeeded() {
        return condition.satisfied();
    }

    @Override
    public void buildFakeHistoryOn(ICondition condition) {
        this.condition = condition;
        this.condition.set(this);
    }
}
