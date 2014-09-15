/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
 * Last Conflict heuristic
 * Composite heuristic which hacks a mainStrategy by forcing the
 * use of variables involved in recent conflicts
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 */
public class LastConflict extends AbstractStrategy<Variable> implements IMonitorRestart, IMonitorSolution, IMonitorContradiction {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

	protected Solver solver;
    protected AbstractStrategy<Variable> mainStrategy;
	protected boolean active;
	protected int nbCV;
	protected Variable[] conflictingVariables;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public LastConflict(Solver solver, AbstractStrategy<Variable> mainStrategy, int k) {
        super(mainStrategy.vars);
		assert k>0 : "parameter K of last conflict must be strictly positive!";
        this.solver = solver;
        this.mainStrategy = mainStrategy;
        solver.getSearchLoop().plugSearchMonitor(this);
		conflictingVariables = new Variable[k];
		nbCV = 0;
		active = false;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void init() throws ContradictionException {
		mainStrategy.init();
    }

    @Override
    public Decision getDecision() {
		if(active){
			Variable decVar = firstNotInst();
			if (decVar != null) {
				Decision d = mainStrategy.computeDecision(decVar);
				if(d != null){
					return d;
				}
			}
		}
		active = true;
		return mainStrategy.getDecision();
    }

    //***********************************************************************************
    // Monitor
    //***********************************************************************************


    @Override
    public void onContradiction(ContradictionException cex) {
        Variable curDecVar = solver.getSearchLoop().getLastDecision().getDecisionVariable();
		if(nbCV>0 && conflictingVariables[nbCV-1]==curDecVar)return;
		if(inScope(curDecVar)){
			if(nbCV<conflictingVariables.length){
				conflictingVariables[nbCV++] = curDecVar;
			}else{
				assert nbCV==conflictingVariables.length;
				for(int i=0;i<nbCV-1;i++){
					conflictingVariables[i] = conflictingVariables[i+1];
				}
				conflictingVariables[nbCV-1] = curDecVar;
			}
		}
    }

    @Override
    public void beforeRestart() {}

    @Override
    public void afterRestart() {
		active = false;
    }

    @Override
    public void onSolution() {
		active = false;
    }

    //***********************************************************************************
    //***********************************************************************************

    private Variable firstNotInst() {
		for(int i=nbCV-1;i>=0;i--){
			if(!conflictingVariables[i].isInstantiated()){
				return conflictingVariables[i];
			}
		}
        return null;
    }

	private boolean inScope(Variable target){
		Variable[] scope = mainStrategy.vars;
		for(int v=0; v<scope.length; v++){
			if(scope[v].getId()==target.getId()){
				return true;
			}
		}
		return false;
	}
}
