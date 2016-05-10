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



/**
 * An abstract class for backtrackable boolean.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/04/13
 */
public abstract class IStateBool  {

    protected final IEnvironment environment;
    protected boolean currentValue;
    protected int timeStamp;


    public IStateBool(IEnvironment env, boolean i) {
        environment = env;
        currentValue = i;
        timeStamp = environment.getWorldIndex();
    }

    /**
     * Returns the current value.
     */
    public final boolean get() {
        return currentValue;
    }


    /**
     * Modifies the value and stores if needed the former value on the
     * trailing stack.
     */
    public abstract void set(boolean y);

    /**
     * Modifies the value without storing the former value on the trailing stack.
     *
     * @param y      the new value
     * @param wstamp the stamp of the world in which the update is performed
     */
    public void _set(final boolean y, final int wstamp) {
        currentValue = y;
        timeStamp = wstamp;
    }

    /**
     * Make a deep copy of this.
     *
     * @return a boolean
     */
    public final boolean deepCopy() {
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
