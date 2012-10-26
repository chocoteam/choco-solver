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

package solver.objective;

import choco.kernel.ResolutionPolicy;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.search.measure.IMeasures;
import solver.search.strategy.decision.Decision;
import solver.variables.EventType;
import solver.variables.IntVar;

@SuppressWarnings({"unchecked"})
public class ObjectiveManager implements ICause{

    final private ResolutionPolicy policy;
    final IntVar objective;
	private int bestKnownUpperBound;
	private int bestKnownLowerBound;

    IMeasures measures;

	/**
	 * Creates an optimization manager
	 * Enables to cut "worse" solutions
	 * @param objective variable (represent the value of a solution)
	 * @param policy SATISFACTION / MINIMIZATION / MAXIMIZATION
	 * @param solver
	 */
	public ObjectiveManager(IntVar objective, ResolutionPolicy policy, Solver solver) {
		this.policy = policy;
		this.measures = solver.getMeasures();
		this.objective = objective;
		if(policy!=ResolutionPolicy.SATISFACTION){
			this.bestKnownLowerBound = objective.getLB();
			this.bestKnownUpperBound = objective.getUB();
		}
    }

	/**
	 * @return the best objective value found so far (returns the initial bound if no solution has been found yet)
	 */
	public int getBestValue() {
		switch (policy){
			case MINIMIZE:return bestKnownUpperBound;
			case MAXIMIZE:return bestKnownLowerBound;
			case SATISFACTION:
			default:throw new UnsupportedOperationException("There is no objective variable in satisfaction problems");
		}
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
     * Updates the lower (or upper) bound of the objective variable, considering its best know value.
     *
     * @param decision
     * @throws ContradictionException if this application leads to a contradiction  @param decision
     */
    public void apply(Decision decision) throws ContradictionException {
        decision.apply();
    }

	/**
	 * Informs the manager that a new solution has been found
	 */
	public void update() {
		switch (policy){
			case MINIMIZE:
				this.bestKnownUpperBound = objective.getValue();
				this.measures.setObjectiveValue(this.bestKnownUpperBound);
				break;
			case MAXIMIZE:
				this.bestKnownLowerBound = objective.getValue();
				this.measures.setObjectiveValue(this.bestKnownLowerBound);
		}
    }

	/**
	 * Improve the lower bound on the problem
	 * @param lb a valid lower bound
	 */
	public void updateLB(int lb) {
        this.bestKnownLowerBound = Math.max(bestKnownLowerBound,lb);
    }

	/**
	 * Improve the upper bound on the problem
	 * @param ub a valid upper bound
	 */
	public void updateUB(int ub) {
        this.bestKnownUpperBound = Math.max(bestKnownUpperBound,ub);
    }

	/**
	 * Prevent the solver from computing worse quality solutions
	 * @throws ContradictionException
	 */
	public void postDynamicCut() throws ContradictionException {
		int offset = 0;
		if(measures.getSolutionCount()>0){
			offset = 1;
		}
		switch (policy){
			case MINIMIZE:
				this.objective.updateUpperBound(bestKnownUpperBound-offset, this);
				this.objective.updateLowerBound(bestKnownLowerBound, this);
				break;
			case MAXIMIZE:
				this.objective.updateUpperBound(bestKnownUpperBound, this);
				this.objective.updateLowerBound(bestKnownLowerBound+offset, this);
		}
    }

	/**
	 * @return true iff the problem is an optimization problem
	 */
	public boolean isOptimization() {
        return policy!=ResolutionPolicy.SATISFACTION;
    }

	/**
	 * @return the ResolutionPolicy of the problem 
	 */
	public ResolutionPolicy getPolicy() {
        return policy;
    }

    @Override
    public String toString() {
		switch (policy){
			case MINIMIZE:return String.format("Minimize %s = [%d,%d]", this.objective.getName(), bestKnownLowerBound, bestKnownUpperBound);
			case MAXIMIZE:return String.format("Maximize %s = [%d,%d]", this.objective.getName(), bestKnownLowerBound, bestKnownUpperBound);
			case SATISFACTION:
			default:return "";
		}
    }

	public Explanation explain(Deduction val) {
		if(policy==ResolutionPolicy.SATISFACTION){
			return null;
		}
		//TODO LB + UB
        return objective.explain(VariableState.UB);
    }

	@Override
	public Constraint getConstraint() {
		return null;
	}

	@Override
	public boolean reactOnPromotion() {
		return false;
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.VOID.getMask();
	}
}
