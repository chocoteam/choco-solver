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
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.search.measure.IMeasures;
import solver.variables.IntVar;

/**
 * An implementation of <code>IObjectiveManager</code> class for minimization problem.
 * The objective variable value has to be minimized, considering the constraints. All search long,
 * the upper bound is set to the best known value - 1, to avoid exploration of greater or equal "quality" leaves.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27 juil. 2010
 */
@SuppressWarnings({"unchecked"})
public class ObjectiveManager {

    final private ResolutionPolicy policy;

    final IntVar objective;
	private int bestKnownUpperBound;
	private int bestKnownLowerBound;

    IMeasures measures;

    public ObjectiveManager(IntVar objective, ResolutionPolicy policy) {
		this.policy = policy;
        this.objective = objective;
		this.bestKnownLowerBound = objective.getLB();
        this.bestKnownUpperBound = objective.getUB();
    }

    public void setMeasures(IMeasures measures) {
        this.measures = measures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBestValue() {
		switch (policy){
			case MINIMIZE:return bestKnownUpperBound;
			case MAXIMIZE:return bestKnownLowerBound;
			case SATISFACTION:
			default:throw new UnsupportedOperationException("There is no objective variable in satisfaction problems");
		}
    }

	public int getBestKnownLowerBound() {
		return bestKnownLowerBound;
	}

	public int getBestKnownUpperBound() {
		return bestKnownUpperBound;
	}

    /**
     * {@inheritDoc}
     */
    @Override
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

	public void updateLB(int lb) {
        this.bestKnownLowerBound = Math.max(bestKnownLowerBound,lb);
    }

	public void updateUB(int ub) {
        this.bestKnownUpperBound = Math.max(bestKnownUpperBound,ub);
    }

    @Override
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

    @Override
    public boolean isOptimization() {
        return policy!=ResolutionPolicy.SATISFACTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
		switch (policy){
			case MINIMIZE:return String.format("Minimize %s = [%d,%d]", this.objective.getName(), bestKnownLowerBound, bestKnownUpperBound);
			case MAXIMIZE:return String.format("Maximize %s = [%d,%d]", this.objective.getName(), bestKnownLowerBound, bestKnownUpperBound);
			case SATISFACTION:
			default:return "";
		}
    }

    @Override
    public Explanation explain(Deduction val) {
		if(policy==ResolutionPolicy.SATISFACTION){
			return null;
		}
		//TODO LB + UB
        return objective.explain(VariableState.UB);
    }
}
