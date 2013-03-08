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

package solver.search.loop.monitors;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;

public abstract class Abstract_LNS_SearchMonitor implements ICause, IMonitorSolution, IMonitorInterruption, IMonitorClose, IMonitorRestart {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected Solver solver;
    protected final boolean restartAfterEachSolution;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public Abstract_LNS_SearchMonitor(Solver solver, boolean restartAfterEachSolution) {
        solver.getSearchLoop().restartAfterEachSolution(true);
        this.solver = solver;
        this.restartAfterEachSolution = restartAfterEachSolution;
    }

    //***********************************************************************************
    // RECORD & RESTART
    //***********************************************************************************

    @Override
    public void onSolution() {
        recordSolution();
    }

    @Override
    public void afterInterrupt() {
        //		if(solver.getMeasures().getSolutionCount()==0){
        //			System.out.println("research complete : no solution");return;
        //		}
        //		if(solver.getSearchLoop().getLimitsBox().isReached()){
        //			System.out.println("limit reached");return;
        //		}
        //		if(isSearchComplete()){
        //			System.out.println("optimality proved");return;
        //		}
        if (!(solver.getMeasures().getSolutionCount() == 0
                || solver.getSearchLoop().getLimits().isReached()
                || isSearchComplete())) {
            restrictLess();
            solver.getSearchLoop().restartAfterEachSolution(true);
            solver.getSearchLoop().forceAlive(true);
            solver.getSearchLoop().restart();
        }
    }

    @Override
    public void beforeClose() {
    }

    @Override
    public void afterClose() {
    }

    /**
     * @return true iff the search is in a complete mode (no fixed variable)
     */
    protected abstract boolean isSearchComplete();

    /**
     * Record values of decision variables to freeze some ones during the next LNS run
     */
    protected abstract void recordSolution();

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
                fixSomeVariables();
                solver.getEngine().propagate();
            } catch (Exception e) {
                //LOGGER.warn("fixing some variables raised a failure. Restart LNS to get a better fragment");
                solver.getEngine().flush();
                restrictLess();
                solver.getSearchLoop().restart();
            }
        }
    }

    /**
     * Freezes some variables in order to have a fast computation
     *
     * @throws ContradictionException if variables have been fixed to inconsistent values
     *                                this can happen if fixed variables cannot yield to a better solution than the last one
     *                                a contradiction is raised because a cut has been posted on the objective function
     *                                Notice that it could be used to generate a no-good
     */
    protected abstract void fixSomeVariables() throws ContradictionException;

    /**
     * Use less restriction at the beginning of a LNS run
     * in order to get better solutions
     * Called when no solution was found during a LNS run (trapped into a local optimum)
     */
    protected abstract void restrictLess();


    @Override
    public void explain(Deduction d, Explanation e) {
    }

    @Override
    public boolean reactOnPromotion() {
        return false;
    }

}
