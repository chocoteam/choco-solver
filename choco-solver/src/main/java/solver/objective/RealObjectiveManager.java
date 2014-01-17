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
import solver.variables.RealVar;

/**
 * Class that monitors the objective function and avoid exploring "worse" solutions
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class RealObjectiveManager extends ObjectiveManager<RealVar> {

	private double bestKnownUpperBound;
	private double bestKnownLowerBound;
	private final double precision;

	/**
	 * Creates an optimization manager
	 * Enables to cut "worse" solutions
	 *
	 * @param objective variable (represent the value of a solution)
	 * @param policy    SATISFACTION / MINIMIZATION / MAXIMIZATION
	 * @param precision the precision, substracted from the cut
	 * @param strict    enables to find same value solutions when set to false
	 * @param solver    the solver
	 */
	public RealObjectiveManager(RealVar objective, ResolutionPolicy policy, Solver solver, double precision, boolean strict) {
		super(objective, policy, solver.getMeasures(), strict);
		this.precision = precision;
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
	 * @param precision the precision, substracted from the cut
	 * @param solver    the solver
	 */
	public RealObjectiveManager(RealVar objective, ResolutionPolicy policy, Solver solver, double precision) {
		this(objective, policy, solver, precision, true);
	}

	@Override
	public Double getBestSolutionValue() {
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
	public double getBestLB() {
		return bestKnownLowerBound;
	}

	/**
	 * @return the best upper bound computed so far
	 */
	public double getBestUB() {
		return bestKnownUpperBound;
	}

	/**
	 * Informs the manager that a new solution has been found
	 */
	public void update() {
		if (policy == ResolutionPolicy.MINIMIZE) {
			this.bestKnownUpperBound = objective.getUB();
		} else if (policy == ResolutionPolicy.MAXIMIZE) {
			this.bestKnownLowerBound = objective.getLB();
		}
	}

	/**
	 * Prevent the solver from computing worse quality solutions
	 *
	 * @throws solver.exception.ContradictionException
	 *
	 */
	public void postDynamicCut() throws ContradictionException {
		int offset = 0;
		if (measures.getSolutionCount() > 0 && strict) {
			offset = 1;
		}
		if (policy == ResolutionPolicy.MINIMIZE) {
			this.objective.updateUpperBound(bestKnownUpperBound - (precision * offset), this);
			this.objective.updateLowerBound(bestKnownLowerBound, this);
		} else if (policy == ResolutionPolicy.MAXIMIZE) {
			this.objective.updateUpperBound(bestKnownUpperBound, this);
			this.objective.updateLowerBound(bestKnownLowerBound + (precision * offset), this);
		}
	}

	@Override
	public String toString() {
		switch (policy) {
			case MINIMIZE:
				return String.format("Minimize %s = %e", this.objective.getName(), bestKnownUpperBound);
			case MAXIMIZE:
				return String.format("Maximize %s = %e", this.objective.getName(), bestKnownLowerBound);
			case SATISFACTION:
				return "SAT";
			default:
				throw new UnsupportedOperationException("no objective manager");
		}
	}
}
