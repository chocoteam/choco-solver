/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.solver.search.limits;

import org.chocosolver.solver.search.loop.monitors.IMonitorInitialize;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29 juil. 2010
 */
public abstract class ACounter implements ICounter, IMonitorInitialize {

    protected long max, current;
    protected ICounterAction action;

    public ACounter(long limit) {
        max = limit;
        current = 0;
        this.action = ActionCounterFactory.none();
    }

    @Override
    public void init() {
    }

    @Override
    public void update() {
    }

    @Override
    public final void overrideLimit(long newLimit) {
        max = newLimit;
    }

    @Override
    public void reset() {
        current = 0;
        init();
    }

    @Override
    public final boolean isReached() {
        return max - current <= 0;
    }

    @Override
    public long getLimitValue() {
        return max;
    }

    protected final void incCounter() {
        current++;
        if (isReached()) {
            action.onLimitReached();
        }
    }

    protected final void setCounter(long value) {
        current = value;
        if (isReached()) {
            action.onLimitReached();
        }
    }

    @Override
    public void beforeInitialize() {
    }

    @Override
    public void afterInitialize() {
        this.init();
    }

    @Override
    public void setAction(ICounterAction action) {
        this.action = action;
    }
}
