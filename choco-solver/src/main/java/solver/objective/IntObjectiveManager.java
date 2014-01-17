/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.objective;

import solver.ResolutionPolicy;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

/**
 * Class that monitors the objective function and avoid exploring "worse" solutions
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class IntObjectiveManager extends ObjectiveManager<IntVar> {

	int bestKnownUpperBound;
	int bestKnownLowerBound;

	/**
	 * Creates an optimization manager
	 * Enables to cut "worse" solutions
	 *
	 * @param objective variable (represent the value of a solution)
	 * @param policy    SATISFACTION / MINIMIZATION / MAXIMIZATION
	 * @param solver
	 * @param strict    enables to find same value solutions when set to false
	 */
	public IntObjectiveManager(final IntVar objective, ResolutionPolicy policy, Solver solver, boolean strict) {
		super(objective,policy, solver.getMeasures(),strict);
		if (policy != ResolutionPolicy.SATISFACTION) {
			this.bestKnownLowerBound = objective.getLB();
			this.bestKnownUpperBound = objective.getUB();
		}
	}

	/**
	 * Creates an optimization manager
	 * Enables to cut "worse" solutions
	 *
	 * @param objective variable (represent the value of a solution)
	 * @param policy    SATISFACTION / MINIMIZATION / MAXIMIZATION
	 * @param solver
	 */
	public IntObjectiveManager(final IntVar objective, ResolutionPolicy policy, Solver solver) {
		this(objective, policy, solver, true);
	}

	@Override
	public Integer getBestSolutionValue() {
		if (policy == ResolutionPolicy.MINIMIZE) {
			return bestKnownUpperBound;
		}
		if (policy == ResolutionPolicy.MAXIMIZE) {
			return bestKnownLowerBound;
		}
		throw new UnsupportedOperationException("There is no objective variable in satisfaction problems");
	}

	/**
	 * @return the best lower bound computed so far
	 */
	public int getBestLB() {
		return bestKnownLowerBound;
	}

	/**
	 * @return the best upper bound computed so far
	 */
	public int getBestUB() {
		return bestKnownUpperBound;
	}

	/**
	 * States that lb is a global lower bound on the problem
	 *
	 * @param lb lower bound
	 */
	public void updateBestLB(int lb) {
		bestKnownLowerBound = Math.max(bestKnownLowerBound, lb);
	}

	/**
	 * States that ub is a global upper bound on the problem
	 *
	 * @param ub upper bound
	 */
	public void updateBestUB(int ub) {
		bestKnownUpperBound = Math.min(bestKnownUpperBound, ub);
	}

	/**
	 * Informs the manager that a new solution has been found
	 */
	public void update() {
		if (policy == ResolutionPolicy.MINIMIZE) {
			this.bestKnownUpperBound = objective.getValue();
		} else if (policy == ResolutionPolicy.MAXIMIZE) {
			this.bestKnownLowerBound = objective.getValue();
		}
	}

	/**
	 * Prevent the solver from computing worse quality solutions
	 *
	 * @throws ContradictionException
	 */
	public void postDynamicCut() throws ContradictionException {
		int offset = 0;
		if (measures.getSolutionCount() > 0 && strict) {
			offset = 1;
		}
		if (policy == ResolutionPolicy.MINIMIZE) {
			this.objective.updateUpperBound(bestKnownUpperBound - offset, this);
			this.objective.updateLowerBound(bestKnownLowerBound, this);
		} else if (policy == ResolutionPolicy.MAXIMIZE) {
			this.objective.updateUpperBound(bestKnownUpperBound, this);
			this.objective.updateLowerBound(bestKnownLowerBound + offset, this);
		}
	}

	@Override
	public String toString() {
		switch (policy) {
			case MINIMIZE:
				return String.format("Minimize %s = %d", this.objective.getName(), bestKnownUpperBound);
			case MAXIMIZE:
				return String.format("Maximize %s = %d", this.objective.getName(), bestKnownLowerBound);
			case SATISFACTION:
				return "SAT";
			default:
				throw new UnsupportedOperationException("no objective manager");
		}
	}
}
