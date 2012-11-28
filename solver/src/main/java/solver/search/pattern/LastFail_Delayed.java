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
 * Date: 28/11/12
 * Time: 16:25
 */

package solver.search.pattern;

import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.measure.IMeasures;
import solver.variables.Variable;

public class LastFail_Delayed<V extends Variable> extends LastFail<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	// Delays the Last Fail pattern to the second decision
	private V tmp;
	private long failStamp;
	private IMeasures measures;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public LastFail_Delayed(Solver solver){
		super(solver);
		measures = solver.getMeasures();
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean canApply() {
		return super.canApply()&&tmp!=null;
	}

	@Override
	public void setVar(V v) {
		tmp = v;
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
		if(failStamp!=measures.getFailCount()){
			failStamp = measures.getFailCount();
			lastVar = tmp;
			tmp = null;
		}
	}
}
