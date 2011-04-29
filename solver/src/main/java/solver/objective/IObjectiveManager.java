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

import solver.ICause;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.search.strategy.decision.Decision;
import solver.variables.EventType;

/**
 * An interface to deal with objective defined within a problem.
 * There are 3 types of resolutions: satisfaction (find one or more solutions), maximize and minimize.
 * The two last required the definition of an objective variable to optimize. All maximzing (resp.minimizing) long,
 * the different solutions give new informations on the lower (resp. upper) bound
 * of the objective vairable -- <code>update()</code>. This "best known value" help by cutting the search
 * -- <code>apply()</code> -- automatically setting the lower bound or upper bound to the best known value of the
 * objective variable, avoiding exploration of less quality leaves.
 * <br/>
 * In satisfaction, no objective variable is defined, the methods are empty.
 *
 * @author Charles Prud'homme
 * @since 27 juil. 2010
 * @see solver.objective.NoObjectiveManager
 * @see solver.objective.MaxObjectiveManager
 * @see solver.objective.MinObjectiveManager
 */
public abstract class IObjectiveManager implements ICause {

    public abstract int getBestValue();

    /**
     * Updates the best know value of the objective variable to its current value.
     */
    public abstract void update();

    /**
     * Updates the lower (or upper) bound of the objective variable, considering its best know value.
     * @param decision
     * @throws ContradictionException if this application leads to a contradiction  @param decision
     */
    public void apply(Decision decision) throws ContradictionException{
        decision.apply();
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
    public int getPropagationConditions() {
        return EventType.VOID.mask;
    }
}
