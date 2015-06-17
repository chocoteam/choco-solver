/*
 * Copyright (c) 1999-2015, Ecole des Mines de Nantes
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
package org.chocosolver.solver.variables.impl.scheduler;

import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.iterators.EvtScheduler;

/**
 * Created by cprudhom on 17/06/15.
 * Project: choco.
 */
public class IntEvtScheduler implements EvtScheduler<IntEventType> {

    final int[] dis = new int[]{-1, -1, -1, -1};
    int i = 0;

    public void init(IntEventType evt) {
        i = 0;
        switch (evt) {
            case REMOVE: // rem
                dis[0] = 4; // included
                dis[1] = 5; // excluded
                dis[2] = dis[3] = -1;
                break;
            case INCLOW: // lb
                dis[0] = 1; // included
                dis[1] = 2; // excluded
                dis[2] = 3; // included
                dis[3] = 5; // excluded
                break;
            case DECUPP: // ub
                dis[0] = 2; // included
                dis[1] = 5; // excluded
                dis[2] = dis[3] = -1;
                break;
            case INSTANTIATE: // inst
                dis[0] = 0; // included
                dis[1] = 5; // excluded
                dis[2] = dis[3] = -1;
                break;
        }
    }

    @Override
    public int select(int mask) {
        switch (mask) {
            case 8: // instantiate
                return 0;
            case 10: // lb or more
                return 1;
            case 12: // ub or more
                return 2;
            case 14: // bound or more
                return 3;
            case 15: // all
            case 255: // all
                return 4;
            default:
                throw new UnsupportedOperationException("Unknown case");
        }
    }

    @Override
    public boolean hasNext() {
        return i < 4 && dis[i] > -1;
    }

    @Override
    public int next() {
        return dis[i++];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
