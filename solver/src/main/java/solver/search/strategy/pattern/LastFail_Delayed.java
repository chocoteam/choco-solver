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
import solver.search.strategy.decision.Decision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.Variable;

/**
 * restricted Last Fail :
 * the last fail is not applied directly after a backtrack
 * It is delayed to the second decision after the backtrack
 * @author Jean-Guillaume Fages
 */
public class LastFail_Delayed extends LastFail{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	// Delays the Last Fail pattern to the second decision
	private Variable tmp;
	private long failStamp;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public LastFail_Delayed(Solver solver, AbstractStrategy<Variable> mainStrategy){
		super(solver,mainStrategy);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public Decision getDecision() {
		if(lastVar!=null && !lastVar.instantiated()
		&& tmp!=null){
			return mainStrategy.computeDecision(lastVar);
		}
		return null;
	}

	public void afterOpenNode() {
		tmp = solver.getSearchLoop().decision.getDecisionVariable();
	}

	@Override
	public void afterRestart() {
		lastVar = null;
		tmp = null;
	}

	@Override
	public void onSolution() {
		lastVar = null;
		tmp = null;
	}

	public void onContradiction(ContradictionException cex) {
		if(failStamp!=solver.getMeasures().getFailCount()){
			failStamp = solver.getMeasures().getFailCount();
			lastVar = tmp;
			tmp = null;
		}
	}
}
