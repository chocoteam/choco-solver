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
        neighbor.recordSolution();
    }

    @Override
    public void afterInterrupt() {
        if (hasAppliedNeighborhood
                && solver.getMeasures().getSolutionCount() > 0
                && solver.getSearchLoop().canBeResumed()
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
//                neighbor.fixSomeVariables(this);
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
