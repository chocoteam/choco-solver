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

import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.search.measure.IMeasures;
import solver.variables.IntVar;

/**
 * An implementation of <code>IObjectiveManager</code> class for maximization problem.
 * The objective variable value has to be maximized, considering the constraints. All search long,
 * the lower bound is set to the best known value + 1, to avoid exploration of less or equal "quality" leaves.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27 juil. 2010
 */
@SuppressWarnings({"unchecked"})
public class MaxObjectiveManager extends IObjectiveManager {

    private int bestKnownLowerBound;

    final IntVar objective;

    IMeasures measures;

    public MaxObjectiveManager(IntVar objective) {
        this.objective = objective;
        this.bestKnownLowerBound = objective.getLB()-1;
    }

    public void setMeasures(IMeasures measures){
        this.measures = measures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBestValue() {
        return bestKnownLowerBound;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update() {
        this.bestKnownLowerBound = objective.getValue();
        this.measures.setObjectiveValue(this.bestKnownLowerBound);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        this.objective.updateLowerBound(bestKnownLowerBound+1, this);
    }

    @Override
    public boolean isOptimization() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("Maximize %s = %d", this.objective.getName(), bestKnownLowerBound);
    }


    @Override
    public Constraint getConstraint() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Explanation explain(Deduction val) {
        return objective.explain(VariableState.LB);
    }
}
