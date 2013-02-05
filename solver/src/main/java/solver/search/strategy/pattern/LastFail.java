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

package solver.search.strategy.pattern;

import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.IMonitorDownBranch;
import solver.search.loop.monitors.IMonitorRestart;
import solver.search.loop.monitors.IMonitorSolution;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.Variable;

/**
 * Last Fail pattern :
 * After a backtrack, the next decision to be computed should involve
 * the variable of the la decision (left branch, refutations are not considered)
 *
 * @author Jean-Guillaume Fages
 */
public class LastFail extends AbstractStrategy<Variable> implements IMonitorRestart, IMonitorSolution, IMonitorDownBranch {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected Variable lastVar;
    protected AbstractStrategy<Variable> mainStrategy;
    protected Solver solver;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public LastFail(Solver solver, AbstractStrategy<Variable> mainStrategy) {
        super(solver.getVars());
        this.solver = solver;
        this.mainStrategy = mainStrategy;
        solver.getSearchLoop().plugSearchMonitor(this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void init() throws ContradictionException {
    }

    @Override
    public Decision getDecision() {
        if (lastVar != null && !lastVar.instantiated()) {
            return mainStrategy.computeDecision(lastVar);
        }
        return null;
    }

    //***********************************************************************************
    // Monitor
    //***********************************************************************************

    @Override
    public void beforeDownLeftBranch() {
        lastVar = solver.getSearchLoop().decision.getDecisionVariable();
    }

    @Override
    public void afterDownLeftBranch() {
    }

    @Override
    public void beforeDownRightBranch() {
    }

    @Override
    public void afterDownRightBranch() {
    }

    @Override
    public void beforeRestart() {
    }

    @Override
    public void afterRestart() {
        lastVar = null;
    }

    @Override
    public void onSolution() {
        lastVar = null;
    }
}
