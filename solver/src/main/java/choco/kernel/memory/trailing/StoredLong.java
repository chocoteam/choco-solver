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

package choco.kernel.memory.trailing;

import choco.kernel.memory.IStateLong;
import choco.kernel.memory.trailing.trail.StoredLongTrail;

public final class StoredLong extends AbstractStoredObject implements IStateLong {


    private long currentValue;
    protected final StoredLongTrail myTrail;

    /**
     * Constructs a stored search with an unknown initial value.
     * Note: this constructor should not be used directly: one should instead
     * use the IEnvironment factory
     */

    public StoredLong(final EnvironmentTrailing env) {
        this(env, 0);
    }


    /**
     * Constructs a stored search with an initial value.
     * Note: this constructor should not be used directly: one should instead
     * use the IEnvironment factory
     */

    public StoredLong(final EnvironmentTrailing env, final long d) {
        super(env);
        myTrail = env.getLongTrail();
        currentValue = d;
    }


    @Override
    public long get() {
        return currentValue;
    }


    public void set(final long y) {
        if (y != currentValue) {
            final int wi = environment.getWorldIndex();
            if (this.worldStamp < wi) {
                myTrail.savePreviousState(this, currentValue, worldStamp);
                worldStamp = wi;
            }
            currentValue = y;
        }
    }

    public void add(final long delta) {
        set(currentValue + delta);
    }

    /**
     * Modifies the value without storing the former value on the trailing stack.
     *
     * @param y      the new value
     * @param wstamp the stamp of the world in which the update is performed
     */

    public void _set(final long y, final int wstamp) {
        currentValue = y;
        worldStamp = wstamp;
    }


    @Override
    public String toString() {
        return String.valueOf(currentValue);
    }


}
