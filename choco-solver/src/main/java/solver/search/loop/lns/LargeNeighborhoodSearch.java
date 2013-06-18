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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 23/05/12
 * Time: 16:51
 */

package solver.search.loop.lns;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.search.loop.lns.neighbors.INeighbor;
import solver.search.loop.monitors.IMonitorInterruption;
import solver.search.loop.monitors.IMonitorRestart;
import solver.search.loop.monitors.IMonitorSolution;

/**
 * How to branch a Large Neighborhood Search ?
 * This class provides services to plug a LNS, it relies on a Neighbor computation, and enables fast restarts.
 */
public class LargeNeighborhoodSearch implements ICause, IMonitorSolution, IMonitorInterruption, IMonitorRestart {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected Solver solver;
    protected final boolean restartAfterEachSolution;
    protected final INeighbor neighbor;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public LargeNeighborhoodSearch(Solver solver, INeighbor neighbor, boolean restartAfterEachSolution) {
        solver.getSearchLoop().restartAfterEachSolution(true);
        this.solver = solver;
        this.neighbor = neighbor;
        this.restartAfterEachSolution = restartAfterEachSolution;
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
//        if (solver.getMeasures().getSolutionCount() == 0) {
//            System.out.println("research complete : no solution");
//            return;
//        }
//        if (solver.getSearchLoop().hasReachedLimit()) {
//            System.out.println("limit reached");
//            return;
//        }
//        if (neighbor.isSearchComplete()) {
//            System.out.println("optimality proved");
//            return;
//        }
        if (solver.getMeasures().getSolutionCount() > 0 && !solver.getSearchLoop().hasReachedLimit() && !neighbor.isSearchComplete()) {
            neighbor.restrictLess();

            solver.getSearchLoop().restartAfterEachSolution(restartAfterEachSolution);
            solver.getSearchLoop().forceAlive(true);
            solver.getSearchLoop().restart();
        }
    }

    //***********************************************************************************
    // FIX VARIABLES FOR NEXT LNS STEP
    //***********************************************************************************

    @Override
    public void beforeRestart() {
    }

    @Override
    public void afterRestart() {
        if (solver.getMeasures().getSolutionCount() > 0) {
            //System.out.println("SOLVER RESTARTED");
            solver.getSearchLoop().restartAfterEachSolution(restartAfterEachSolution);
            try {
                neighbor.fixSomeVariables(this);
                solver.getEngine().propagate();
            } catch (ContradictionException e) {
                //LOGGER.warn("fixing some variables raised a failure. Restart LNS to get a better fragment");
                solver.getEngine().flush();
                neighbor.restrictLess();
                solver.getSearchLoop().restart();
            }
        }
    }


    @Override
    public void explain(Deduction d, Explanation e) {
    }

}
