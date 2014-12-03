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


package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.Variable;

/**
 * Enables to switch from one heuristic to another once a solution has been found
 * @author Jean-Guillaume Fages
 * @since 07/11/13
 * @param <V>
 */
public class FindAndProve<V extends Variable> extends AbstractStrategy<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	AbstractStrategy find, prove;
	Solver solver;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Heuristic which switches from one heuristic (heurToFindASol) to another (heurToProveOpt)
	 * once a solution has been found
	 *
	 * @param vars				variables to branch on
	 * @param heurToFindASol	a heuristic to branch on vars, to find a (good) solution easily
	 * @param heurToProveOpt	a heuristic to branch on vars, to prove the optimality of the solution
	 */
	public FindAndProve(V[] vars, AbstractStrategy<V> heurToFindASol, AbstractStrategy<V> heurToProveOpt) {
		super(vars);
		this.find = heurToFindASol;
		this.prove= heurToProveOpt;
		this.solver = vars[0].getSolver();
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void init() throws ContradictionException {
		find.init();
		prove.init();
	}

	@Override
	public Decision getDecision() {
		if (solver.getMeasures().getSolutionCount() == 0) {
			return find.getDecision();
		}
		return prove.getDecision();
	}

	@Override
	public Decision<V> computeDecision(V variable) {
		if (solver.getMeasures().getSolutionCount() == 0) {
			return find.computeDecision(variable);
		}
		return prove.computeDecision(variable);
	}
}
