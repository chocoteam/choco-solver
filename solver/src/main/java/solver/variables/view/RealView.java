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
package solver.variables.view;


import solver.ICause;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.*;
import solver.variables.delta.NoDelta;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/07/12
 */
public class RealView extends AbstractVariable<NoDelta, RealVar>
        implements IView<NoDelta>, RealVar {

    protected final IntVar var;

    protected final double precision;

    public RealView(IntVar var, double precision) {
        super(var.getSolver());
        this.var = var;
        this.precision = precision;
        this.var.subscribeView(this);
        this.solver.associates(this);
    }

    @Override
    public IntVar getVariable() {
        return var;
    }

    @Override
    public void transformEvent(EventType evt, ICause cause) throws ContradictionException {
        if (evt == EventType.INSTANTIATE) {
            evt = EventType.BOUND;
        } else if (evt == EventType.REMOVE) {
            return;
        }
        notifyPropagators(evt, this);
    }

    @Override
    public void recordMask(int mask) {
        super.recordMask(mask);
        var.recordMask(mask);
    }

    ///////////// SERVICES REQUIRED FROM CAUSE ////////////////////////////

    @Override
    public boolean reactOnPromotion() {
        return false;
    }

    @Override
    public double getLB() {
        return var.getLB();
    }

    @Override
    public double getUB() {
        return var.getUB();
    }

    @Override
    public boolean updateLowerBound(double value, ICause cause) throws ContradictionException {
        return var.updateLowerBound((int) value, this);
    }

    @Override
    public boolean updateUpperBound(double value, ICause cause) throws ContradictionException {
        return var.updateUpperBound((int) value, this);
    }

    @Override
    public boolean updateBounds(double lowerbound, double upperbound, ICause cause) throws ContradictionException {
        return var.updateLowerBound((int) lowerbound, this) & var.updateUpperBound((int) upperbound, this);
    }

    @Override
    public double getPrecision() {
        return precision;
    }

    @Override
    public boolean instantiated() {
        double lb = var.getLB();
        double ub = var.getUB();
        if (ub - lb < precision) return true;
        return nextValue(lb) >= ub;   // TODO a confirmer aupres de Gilles
    }

    private double nextValue(double x) {
        if (x < 0) {
            return Double.longBitsToDouble(Double.doubleToLongBits(x) - 1);
        } else if (x == 0) {
            return Double.longBitsToDouble(1);
        } else if (x < Double.POSITIVE_INFINITY) {
            return Double.longBitsToDouble(Double.doubleToLongBits(x) + 1);
        } else {
            return x; // nextValue(infty) = infty
        }
    }

    @Override
    public NoDelta getDelta() {
        return NoDelta.singleton;
    }

    @Override
    public void createDelta() {
    }

    public void notifyPropagators(EventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        notifyMonitors(event);
        if ((modificationEvents & event.mask) != 0) {
            solver.getEngine().onVariableUpdate(this, event, cause);
        }
        notifyViews(event, cause);
    }

    public void notifyMonitors(EventType event) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event);
        }
    }

    @Override
    public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
        solver.getEngine().fails(cause, this, message);
    }

    @Override
    public int getTypeAndKind() {
        return Variable.VIEW + var.getTypeAndKind();
    }

    @Override
    public RealVar duplicate() {
        return VariableFactory.real(this.var, this.precision);
    }

    @Override
    public void explain(Deduction d, Explanation e) {
    }

    @Override
    public void explain(VariableState what, Explanation to) {
    }

    @Override
    public void explain(VariableState what, int val, Explanation to) {
    }
}
