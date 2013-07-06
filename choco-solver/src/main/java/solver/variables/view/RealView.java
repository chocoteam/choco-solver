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
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 20/07/12
 */
public class RealView extends AbstractVariable<NoDelta, RealVar>
        implements IView<NoDelta>, RealVar {

    protected final IntVar var;

    protected final double precision;

    public RealView(IntVar var, double precision) {
        super("(real)" + var.getName(), var.getSolver());
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

    @Override
    public String toString() {
        return "(real)" + var.toString();
    }

    ///////////// SERVICES REQUIRED FROM CAUSE ////////////////////////////

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
        if (var.updateLowerBound((int) Math.ceil(value - precision), this)) {
            notifyPropagators(EventType.INCLOW, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(double value, ICause cause) throws ContradictionException {
        if (var.updateUpperBound((int) Math.floor(value + precision), this)) {
            notifyPropagators(EventType.INCLOW, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateBounds(double lowerbound, double upperbound, ICause cause) throws ContradictionException {
        int c = 0;
        c += (var.updateLowerBound((int) Math.ceil(lowerbound - precision), this) ? 1 : 0);
        c += (var.updateUpperBound((int) Math.floor(upperbound + precision), this) ? 2 : 0);
        switch (c) {
            case 3:
                notifyPropagators(EventType.BOUND, cause);
                return true;
            case 2:
                notifyPropagators(EventType.DECUPP, cause);
                return true;
            case 1:
                notifyPropagators(EventType.INCLOW, cause);
                return true;
            default: //cas 0;
                return false;
        }
    }

    @Override
    public double getPrecision() {
        return precision;
    }

    @Override
    public boolean instantiated() {
        return var.instantiated();
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
        return VIEW | REAL;
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
