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
package solver.variables.view;

import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.AbstractVariable;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IntDelta;
import solver.variables.delta.NoDelta;

/**
 * An abstract class for common services of a view
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/12/11
 */
public abstract class AbstractView extends AbstractVariable<IntVar> implements IntVar, IView {

    protected IntDelta delta;

    protected HeuristicVal heuristicVal;

    protected boolean reactOnRemoval;

    protected AbstractView(String name, Solver solver) {
        super(name, solver);
        this.delta = NoDelta.singleton;
        this.reactOnRemoval = false;
    }

    protected AbstractView(Solver solver) {
        this(AbstractVariable.NO_NAME, solver);
    }

    public IntDelta getDelta() {
        return delta;
    }

    @Override
    public void setHeuristicVal(HeuristicVal heuristicVal) {
        this.heuristicVal = heuristicVal;
    }

    @Override
    public HeuristicVal getHeuristicVal() {
        return heuristicVal;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getType() {
        return VIEW;
    }

    @Override
    public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
        records.forEach(onContradiction.set(this, event, cause));
        solver.getEngine().fails(cause, this, message);
    }

    ///////////// SERVICES REQUIRED FROM CAUSE ////////////////////////////
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
        return 0;
    }

    @Override
    public void incFail() {
    }

    @Override
    public long getFails() {
        return 0;
    }
}
