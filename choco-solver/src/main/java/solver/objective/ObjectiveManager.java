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

import solver.ICause;
import solver.ResolutionPolicy;
import solver.exception.ContradictionException;
import solver.search.measure.IMeasures;
import solver.search.strategy.decision.Decision;

/**
 * interface to monitor the objective function and avoid exploring "worse" solutions
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public abstract class ObjectiveManager implements ICause {

	final protected ResolutionPolicy policy;
	final protected IMeasures measures;


	public ObjectiveManager(ResolutionPolicy policy, IMeasures measures) {
		this.policy = policy;
		this.measures = measures;
	}

    /**
     * Updates the lower (or upper) bound of the objective variable, considering its best know value.
     *
     * @param decision
     * @throws solver.exception.ContradictionException if this application leads to a contradiction  @param decision
     */
    public void apply(Decision decision) throws ContradictionException {
        decision.apply();
    }

    /**
     * Informs the manager that a new solution has been found
     */
    public abstract void update();

    /**
     * Prevent the solver from computing worse quality solutions
     *
     * @throws solver.exception.ContradictionException
     */
    public abstract void postDynamicCut() throws ContradictionException;

    /**
     * @return true iff the problem is an optimization problem
     */
    public boolean isOptimization() {
        return policy != ResolutionPolicy.SATISFACTION;
    }

    /**
     * @return the ResolutionPolicy of the problem
     */
    public ResolutionPolicy getPolicy() {
        return policy;
    }

	/**
	 * @return the best solution value found so far (returns the initial bound if no solution has been found yet)
	 */
	public abstract Number getBestSolutionValue();
}
