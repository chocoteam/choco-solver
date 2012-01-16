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

package solver.variables.fast;

import choco.kernel.common.util.iterators.DisposableRangeBoundIterator;
import choco.kernel.common.util.iterators.DisposableRangeIterator;
import choco.kernel.common.util.iterators.DisposableValueBoundIterator;
import choco.kernel.common.util.iterators.DisposableValueIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateDouble;
import choco.kernel.memory.IStateInt;
import com.sun.istack.internal.NotNull;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.OffsetIStateBitset;
import solver.explanations.VariableState;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.*;
import solver.variables.delta.Delta;
import solver.variables.delta.IntDelta;
import solver.variables.delta.NoDelta;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public final class IntervalDoubleVarImpl extends AbstractVariable<DoubleVar> implements DoubleVar {

    private static final long serialVersionUID = 1L;

    private final IStateDouble LB, UB;

    NoDelta delta = NoDelta.singleton;

    //////////////////////////////////////////////////////////////////////////////////////

    public IntervalDoubleVarImpl(String name, double min, double max, Solver solver) {
        super(name, solver);
        IEnvironment env = solver.getEnvironment();
        this.LB = env.makeFloat(min);
        this.UB = env.makeFloat(max);
        this.makeList(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean instantiateTo(double value, ICause cause, boolean informCause) throws ContradictionException {
//        solver.getExplainer().instantiateTo(this, value, cause);
        if (this.instantiated()) {
            if (value != this.getValue()) {
                this.contradiction(cause, EventType.INSTANTIATE, MSG_INST);
            }
            return false;
        } else if (contains(value)) {
            EventType e = EventType.INSTANTIATE;
            this.LB.set(value);
            this.UB.set(value);
            this.notifyMonitors(e, cause);
            return true;
        } else {
            this.contradiction(cause, EventType.INSTANTIATE, MSG_UNKNOWN);
            return false;
        }
    }

    public boolean updateLowerBound(double value, ICause cause, boolean informCause) throws ContradictionException {
//        ICause antipromo = cause;
        double old = this.getLB();
        if (old < value) {
            if (this.getUB() < value) {
//                solver.getExplainer().updateLowerBound(this, old, value, antipromo);
                this.contradiction(cause, EventType.INCLOW, MSG_LOW);
            } else {
                EventType e = EventType.INCLOW;
                LB.set(value);
                if (instantiated()) {
                    e = EventType.INSTANTIATE;
                    if (cause.reactOnPromotion()) {
                        cause = Cause.Null;
                    }
                }
                this.notifyMonitors(e, cause);
//                solver.getExplainer().updateLowerBound(this, old, value, antipromo);
                return true;
            }
        }
        return false;
    }

    public boolean updateUpperBound(double value, ICause cause, boolean informCause) throws ContradictionException {
//        ICause antipromo = cause;
        double old = this.getUB();
        if (old > value) {
            if (this.getLB() > value) {
//                solver.getExplainer().updateUpperBound(this, old, value, antipromo);
                this.contradiction(cause, EventType.DECUPP, MSG_UPP);
            } else {
                EventType e = EventType.DECUPP;
                UB.set(value);
                if (instantiated()) {
                    e = EventType.INSTANTIATE;
                    if (cause.reactOnPromotion()) {
                        cause = Cause.Null;
                    }
                }
                this.notifyMonitors(e, cause);
//                solver.getExplainer().updateUpperBound(this, old, value, antipromo);
                return true;
            }
        }
        return false;
    }

    public boolean instantiated() {
        return LB.get()==UB.get();
    }

	@Override
	public Explanation explain(VariableState what) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Explanation explain(VariableState what, int val) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
    public boolean instantiatedTo(double value) {
        return instantiated() && contains(value);
    }

    public boolean contains(double aValue) {
        return ((aValue >= LB.get()) && (aValue <= UB.get()));
    }

    public double getValue() {
        assert instantiated() : name + " not instantiated";
        return getLB();
    }

    public double getLB() {
        return this.LB.get();
    }

    public double getUB() {
        return this.UB.get();
    }

    public double getDomainSize() {
        return UB.get()-LB.get();
    }

    @Override
    public NoDelta getDelta() {
        return delta;
    }

    public String toString() {
        return String.format("%s = [%d,%d]", name, getLB(), getUB());
    }

    ////////////////////////////////////////////////////////////////
    ///// methode liees au fait qu'une variable est observable /////
    ////////////////////////////////////////////////////////////////

    @Override
    public void attach(Propagator propagator, int idxInProp) {
        super.attach(propagator, idxInProp);
    }

    public void notifyMonitors(EventType event, @NotNull ICause cause) throws ContradictionException {
        if ((modificationEvents & event.mask) != 0) {
            records.forEach(afterModification.set(this, event, cause));
        }
        notifyViews(event, cause);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//    public Explanation explain(VariableState what) {
//        Explanation expl = new Explanation(null, null);
//        OffsetIStateBitset invdom = solver.getExplainer().getRemovedValues(this);
//        DisposableValueIterator it = invdom.getValueIterator();
//        while (it.hasNext()) {
//            int val = it.next();
//            if ((what == VariableState.LB && val < this.getLB())
//                    || (what == VariableState.UB && val > this.getUB())
//                    || (what == VariableState.DOM)) {
////                System.out.println("solver.explainer.explain(this,"+ val +") = " + solver.explainer.explain(this, val));
//                expl.add(solver.getExplainer().explain(this, val));
//            }
//        }
//        return expl;
//    }
//
//    @Override
//    public Explanation explain(VariableState what, int val) {
//        Explanation expl = new Explanation();
//        expl.add(solver.getExplainer().explain(this, val));
//        return expl;
//    }

    @Override
    public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
        records.forEach(onContradiction.set(this, event, cause));
        solver.getEngine().fails(cause, this, message);
    }

    @Override
    public int getType() {
        return Variable.REAL;
    }
}
