/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package memory;

import java.io.Serializable;

/**
 * An abstract class for backtrackable long.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/04/13
 */
public abstract class IStateLong implements Serializable {

    protected final IEnvironment environment;
    protected long currentValue;
    protected int timeStamp;


    public IStateLong(IEnvironment env, long i) {
        environment = env;
        currentValue = i;
        timeStamp = environment.getWorldIndex();
    }

    /**
     * Returns the current value.
     */
    public final long get() {
        return currentValue;
    }


    /**
     * Modifies the value and stores if needed the former value on the
     * trailing stack.
     */

    public abstract void set(long y);

    /**
     * modifying a StoredInt by an increment
     *
     * @param delta
     * @return the new value
     */
    public final long add(long delta) {
        long res = currentValue + delta;
        set(res);
        return res;
    }

    /**
     * Modifies the value without storing the former value on the trailing stack.
     *
     * @param y      the new value
     * @param wstamp the stamp of the world in which the update is performed
     */

    public void _set(final long y, final int wstamp) {
        currentValue = y;
        timeStamp = wstamp;
    }

    /**
     * Make a deep copy of this.
     *
     * @return a long
     */
    public final long deepCopy() {
        return currentValue;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void overrideTimeStamp(int aTimeStamp) {
        this.timeStamp = aTimeStamp;
    }

    /**
     * Retrieving the environment
     */
    public IEnvironment getEnvironment() {
        return environment;
    }


    @Override
    public String toString() {
        return String.valueOf(currentValue);
    }
}
