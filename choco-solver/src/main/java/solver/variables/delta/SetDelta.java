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

package solver.variables.delta;

import solver.Configuration;
import solver.ICause;
import solver.search.loop.SearchLoop;

/**
 * @author Jean-Guillaume Fages
 * @since Oct 2012
 */
public class SetDelta implements ISetDelta {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************
    private IEnumDelta[] delta;
    private long timestamp;
    private final SearchLoop loop;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public SetDelta(SearchLoop loop) {
        this.loop = loop;
        delta = new IEnumDelta[2];
        delta[0] = new EnumDelta(loop);
        delta[1] = new EnumDelta(loop);
        timestamp = loop.timeStamp;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    @Override
    public void clear() {
        delta[0].clear();
        delta[1].clear();
        timestamp = loop.timeStamp;
    }

	@Override
    public int getSize(int kerOrEnv) {
        return delta[kerOrEnv].size();
    }

	@Override
    public void add(int element, int kerOrEnv, ICause cause) {
        if (Configuration.LAZY_UPDATE) {
            lazyClear();
        }
        delta[kerOrEnv].add(element, cause);
    }

	@Override
    public void lazyClear() {
        if (timestamp != loop.timeStamp) {
            clear();
        }
    }

	@Override
    public int get(int index, int kerOrEnv) {
        return delta[kerOrEnv].get(index);
    }

	@Override
    public ICause getCause(int index, int kerOrEnv) {
        return delta[kerOrEnv].getCause(index);
    }

    @Override
    public SearchLoop getSearchLoop() {
        return loop;
    }

    @Override
    public boolean timeStamped() {
        return timestamp == loop.timeStamp;
    }
}
