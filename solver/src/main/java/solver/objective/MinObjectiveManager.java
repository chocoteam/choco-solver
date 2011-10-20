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

import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
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
public class MinObjectiveManager extends IObjectiveManager {

    private int bestKnownUpperBound;

    final IntVar objective;

    IMeasures measures;

    public MinObjectiveManager(IntVar objective) {
        this.objective = objective;
        this.bestKnownUpperBound = objective.getUB() + 1;
    }

    public void setMeasures(IMeasures measures) {
        this.measures = measures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBestValue() {
        return bestKnownUpperBound;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void update() {
        this.bestKnownUpperBound = objective.getValue();
        this.measures.setObjectiveValue(this.bestKnownUpperBound);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        this.objective.updateUpperBound(bestKnownUpperBound - 1, this, true);
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
        return String.format("Minimize %s = %d", this.objective.getName(), bestKnownUpperBound);
    }

    @Override
    public Explanation explain(Deduction val) {
        return null;  //TODO change body of implemented methods use File | Settings | File Templates.
    }
}
