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

package solver.search.strategy.strategy;

import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.IMonitorContradiction;
import solver.search.loop.monitors.IMonitorRestart;
import solver.search.loop.monitors.IMonitorSolution;
import solver.search.strategy.decision.Decision;
import solver.variables.Variable;

/**
 * @author Jean-Guillaume Fages, Charles Prud'homme
 */
@Deprecated
public class LastConflict_old extends AbstractStrategy<Variable> implements IMonitorRestart, IMonitorSolution, IMonitorContradiction {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected Solver solver;
    protected AbstractStrategy<Variable> mainStrategy;
    protected Variable candidate;
    protected final Variable[] testingSet;
    protected final int k;
    protected int cIdx; // current index
    protected final boolean dynamic;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public LastConflict_old(Solver solver, AbstractStrategy<Variable> mainStrategy, int k, boolean dynamic) {
        super(solver.getVars());
        this.solver = solver;
        this.mainStrategy = mainStrategy;
        solver.getSearchLoop().plugSearchMonitor(this);
        this.k = k;
        this.testingSet = new Variable[k];
        this.cIdx = 0;
        this.dynamic = dynamic;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void init() throws ContradictionException {
    }

    @Override
    public Decision getDecision() {
        Variable decVar = firstNotInst(testingSet, 0, cIdx);
        if (decVar == null) {
            if (candidate != null && !candidate.isInstantiated()) {
                testingSet[cIdx++] = candidate;
                decVar = candidate;
            } else {
                cIdx = 0;
            }
            candidate = null;
        }
        if (decVar != null) {
            return mainStrategy.computeDecision(decVar);
        } else {
            return null;
        }
    }

    //***********************************************************************************
    // Monitor
    //***********************************************************************************


    @Override
    public void onContradiction(ContradictionException cex) {
        Variable curDecVar = solver.getSearchLoop().getLastDecision().getDecisionVariable();
        if (candidate == null && cIdx < k && !search(testingSet, 0, cIdx, curDecVar)) {
			boolean inScope = false;
			for(Variable v:mainStrategy.vars){
				if(v.getId()==curDecVar.getId()){
					inScope = true;
					break;
				}
			}if(inScope){
				candidate = curDecVar;
			}
        }
    }

    @Override
    public void beforeRestart() {
    }

    @Override
    public void afterRestart() {
        candidate = null;
    }

    @Override
    public void onSolution() {
        candidate = null;
    }

    //***********************************************************************************
    //***********************************************************************************


    private static Variable firstNotInst(Variable[] a, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (!a[i].isInstantiated()) {
                return a[i];
            }
        }
        return null;
    }

    private static boolean search(Variable[] a, int fromIndex, int toIndex, Variable key) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (a[i].getId() == key.getId()) {
                return true;
            }
        }
        return false;
    }
}
