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

package solver.search.strategy.decision;

import solver.constraints.Constraint;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.variables.EventType;
import solver.variables.Variable;

/**
 * A part implementation of <code>Deduction</code>. It implements the #setPrevious(Deduction)
 * and #getPrevious() methods
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19 aožt 2010
 */
public abstract class AbstractDecision<V extends Variable> extends Decision<V> {

    protected V var;

    protected Decision<V> assignment;

    protected int branch;

    long fails;

    protected Decision previous;

    public final void setPrevious(Decision decision) {
        this.previous = decision;
    }

    public final Decision getPrevious() {
        return previous;
    }

    @Override
    public final Constraint getConstraint() {
        return null;
    }

    public final void incFail() {
        fails++;
    }

    public final long getFails() {
        return fails;
    }

    @Override
    public final boolean isLeft() {
        return branch == 1;
    }

    @Override
    public final boolean isRight() {
        return branch == 2;
    }

    @Override
    public final boolean hasNext() {
        return branch < 2;
    }

    @Override
    public final void buildNext() {
        branch++;
    }

    @Override
    public final void rewind() {
        branch = 0;
    }

    @Override
    public final V getDecisionVariable() {
        return var;
    }


    @Override
    public final boolean reactOnPromotion() {
        return false;
    }

    @Override
    public final int getPropagationConditions(int vIdx) {
        return EventType.VOID.mask;
    }

    @Override
    public void explain(Deduction d, Explanation e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reverse() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Deduction getNegativeDeduction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Deduction getPositiveDeduction() {
        throw new UnsupportedOperationException();
    }
}
