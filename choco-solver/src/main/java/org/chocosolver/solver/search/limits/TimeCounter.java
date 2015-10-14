/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.limits;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorOpenNode;

/**
 * A limit over run time.
 * It acts as a monitor, to be up-to-date when the search loop asks for limit reaching.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class TimeCounter extends ACounter implements IMonitorOpenNode {

    private Solver solver;

    private long offset;

    /**
     * @param solver a solver
     * @param timeLimit in millisecond
     */
    public TimeCounter(Solver solver, long timeLimit) {
        super(timeLimit);
        this.solver = solver;
    }


    @Override
    public void init() {
        solver.getMeasures().updateTime();
        long time = (long) (solver.getMeasures().getTimeCount()*1000f);
        offset = System.currentTimeMillis() - time;
    }

    @Override
    public void beforeOpenNode() {
        setCounter(System.currentTimeMillis() - offset);
    }

    @Override
    public void afterOpenNode() {
    }

}
