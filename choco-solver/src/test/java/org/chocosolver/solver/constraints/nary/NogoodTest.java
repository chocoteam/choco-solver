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
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.BacktrackCounter;
import org.chocosolver.solver.search.loop.SLF;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.restart.MonotonicRestartStrategy;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/06/13
 */
public class NogoodTest {

    @Test(groups = "1s")
    public void test1() {
        final Solver solver = new Solver();
        IntVar[] vars = VF.enumeratedArray("vars", 3, 0, 2, solver);
        SMF.nogoodRecordingFromRestarts(solver);
        solver.set(ISF.random_value(vars, 29091981L));
        SLF.restart(solver,
                new BacktrackCounter(solver, 0),
                new MonotonicRestartStrategy(30), 3);
        SMF.limitTime(solver, 2000);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 27);
        Assert.assertEquals(solver.getMeasures().getBackTrackCount(), 51);
    }

    @Test(groups = "1s")
    public void test2() {
        final Solver solver = new Solver();
        IntVar[] vars = VF.enumeratedArray("vars", 3, 0, 3, solver);
        SMF.nogoodRecordingFromRestarts(solver);
        solver.set(ISF.random_value(vars, 29091981L));
        SLF.restart(solver,
                new BacktrackCounter(solver, 0),
                new MonotonicRestartStrategy(30), 1000);
        SMF.limitTime(solver, 2000);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 64);
        Assert.assertEquals(solver.getMeasures().getBackTrackCount(), 121);
    }

}
