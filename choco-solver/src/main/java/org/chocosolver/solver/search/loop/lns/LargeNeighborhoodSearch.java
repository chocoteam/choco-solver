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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 23/05/12
 * Time: 16:51
 */

package org.chocosolver.solver.search.loop.lns;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.loop.monitors.IMonitorInterruption;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;

/**
 * How to branch a Large Neighborhood Search ?
 * This class provides services to plug a LNS, it relies on a Neighbor computation, and enables fast restarts.
 */
public class LargeNeighborhoodSearch implements ICause, IMonitorSolution, IMonitorInterruption, IMonitorRestart {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected Solver solver;
    protected final INeighbor neighbor;
    protected boolean hasAppliedNeighborhood;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public LargeNeighborhoodSearch(final Solver solver, INeighbor neighbor, final boolean restartAfterEachSolution) {
        this.solver = solver;
        this.neighbor = neighbor;
        solver.plugMonitor((IMonitorSolution) () -> {
            if (restartAfterEachSolution) {
                solver.getSearchLoop().restart();
            }
        });
    }

    //***********************************************************************************
    // RECORD & RESTART
    //***********************************************************************************

    @Override
    public void onSolution() {
        // the fast restart policy is plugged when the first solution has been found
        if (solver.getMeasures().getSolutionCount() == 1) {
            neighbor.activeFastRestart();
        }
        neighbor.recordSolution();
    }

    @Override
    public void afterInterrupt() {
        if (hasAppliedNeighborhood
                && solver.getMeasures().getSolutionCount() > 0
<<<<<<< HEAD
=======
                && solver.getSearchLoop().canBeResumed()
>>>>>>> iss_304
                && !solver.getSearchLoop().hasReachedLimit()
                && !neighbor.isSearchComplete()) {
            neighbor.restrictLess();
            solver.getSearchLoop().forceAlive(true);
            solver.getSearchLoop().restart();
        }
    }

    //***********************************************************************************
    // FIX VARIABLES FOR NEXT LNS STEP
    //***********************************************************************************

    @Override
    public void beforeRestart() {
        hasAppliedNeighborhood = false;
    }

    @Override
    public void afterRestart() {
        if (solver.getMeasures().getSolutionCount() > 0) {
            try {
                neighbor.fixSomeVariables(this);
                hasAppliedNeighborhood = true;
                solver.getEngine().propagate();
            } catch (ContradictionException e) {
                solver.getEngine().flush();
                neighbor.restrictLess();
                solver.getSearchLoop().restart();
            }
        }
    }

}
