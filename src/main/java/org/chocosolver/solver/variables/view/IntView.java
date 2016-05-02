/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.variables.view;


import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IDelta;
import org.chocosolver.solver.variables.delta.IntDelta;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.AbstractVariable;
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.chocosolver.util.iterators.*;

import java.util.Iterator;

/**
 * "A view implements the same operations as a variable. A view stores a reference to a variable.
 * Invoking an operation on the view executes the appropriate operation on the view's variable."
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public abstract class IntView extends AbstractVariable implements IView, IntVar {

    /**
     * Observed variable
     */
    protected final IntVar var;

    /**
     * To store removed values
     */
    protected IntDelta delta;

    /**
     * Value iterator
     */
    protected DisposableValueIterator _viterator;

    /**
     * Range iterator
     */
    protected DisposableRangeIterator _riterator;

    /**
     * Value iterator allowing for(int i:this) loops
     */
    private IntVarValueIterator _javaIterator = new IntVarValueIterator(this);

    /**
     * Create a view based on {@code var}
     * @param name name of the view
     * @param var observed variable
     */
    protected IntView(String name, IntVar var) {
        super(name, var.getModel());
        this.var = var;
        this.delta = NoDelta.singleton;
        this.var.subscribeView(this);
    }

    /**
     * Action to execute on {@link #var} when this view requires to instantiate it
     * @param value value before modification of the view
     * @return <tt>true</tt> if {@link #var} has been modified
     * @throws ContradictionException if modification fails
     */
    protected boolean doInstantiateVar(int value) throws ContradictionException{
        throw new UnsupportedOperationException();
    }

    /**
     * Action to execute on {@link #var} when this view requires to update its lower bound
     * @param value value before modification of the view
     * @return <tt>true</tt> if {@link #var} has been modified
     * @throws ContradictionException if modification fails
     */
    protected boolean doUpdateLowerBoundOfVar(int value) throws ContradictionException{
        throw new UnsupportedOperationException();
    }

    /**
     * Action to execute on {@link #var} when this view requires to update its upper bound
     * @param value value before modification of the view
     * @return <tt>true</tt> if {@link #var} has been modified
     * @throws ContradictionException if modification fails
     */
    protected boolean doUpdateUpperBoundOfVar(int value) throws ContradictionException{
        throw new UnsupportedOperationException();
    }

    /**
     * Action to execute on {@link #var} when this view requires to remove a value from it
     * @param value value before modification of the view
     * @return <tt>true</tt> if {@link #var} has been modified
     * @throws ContradictionException if modification fails
     */
    protected boolean doRemoveValueFromVar(int value) throws ContradictionException{
        throw new UnsupportedOperationException();
    }

    /**
     * Action to execute on {@link #var} when this view requires to remove an interval from it
     * @param from first value of the interval before modification of the view
     * @param to last value of the interval before modification of the view
     * @return <tt>true</tt> if {@link #var} has been modified
     * @throws ContradictionException if modification fails
     */
    protected boolean doRemoveIntervalFromVar(int from, int to) throws ContradictionException{
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int inf = getLB();
        int sup = getUB();
        if (inf <= value && value <= sup) {
            IntEventType e = IntEventType.REMOVE;
            model.getSolver().getExplainer().removeValue(this, value, cause);
            if (doRemoveValueFromVar(value)) {
                if (value == inf) {
                    e = IntEventType.INCLOW;
                } else if (value == sup) {
                    e = IntEventType.DECUPP;
                }
                if (this.isInstantiated()) {
                    e = IntEventType.INSTANTIATE;
                }
                this.notifyPropagators(e, cause);
                return true;
            }else{
                model.getSolver().getExplainer().undo();
            }
        }
        return false;
    }

    @Override
    public boolean removeValues(IntIterableSet values, ICause cause) throws ContradictionException {
        assert cause != null;
        int olb = getLB();
        int oub = getUB();
        int nlb = values.nextValue(olb - 1);
        int nub = values.previousValue(oub + 1);
        if (nlb > oub || nub < olb) {
            return false;
        }
        if (nlb == olb) {
            // look for the new lb
            do {
                olb = nextValue(olb);
                nlb = values.nextValue(olb - 1);
            } while (olb < Integer.MAX_VALUE && oub < Integer.MAX_VALUE && nlb == olb);
        }
        if (nub == oub) {
            // look for the new ub
            do {
                oub = previousValue(oub);
                nub = values.previousValue(oub + 1);
            } while (olb > Integer.MIN_VALUE && oub > Integer.MIN_VALUE && nub == oub);
        }
        // the new bounds are now known, delegate to the right method
        boolean hasChanged = updateBounds(olb, oub, cause);
        // now deal with holes
        int value = nlb, to = nub;
        boolean hasRemoved = false;
        while (value <= to) {
            model.getSolver().getExplainer().removeValue(this, value, cause);
            if(doRemoveValueFromVar(value)){
                hasRemoved |= true;
            }else{
                model.getSolver().getExplainer().undo();
            }
            value = values.nextValue(value);
        }
        if (hasRemoved) {
            IntEventType e = IntEventType.REMOVE;
            if (var.isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            this.notifyPropagators(e, cause);
        }
        return hasRemoved || hasChanged;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        assert cause != null;
        if (from <= getLB()) {
            return updateLowerBound(to + 1, cause);
        } else if (getUB() <= to) {
            return updateUpperBound(from - 1, cause);
        } else if(var.hasEnumeratedDomain()){
            for (int v = from; v <= to; v++) {
                if (this.contains(v)) {
                    model.getSolver().getExplainer().removeValue(this, v, cause);
                }
            }
            boolean done = doRemoveIntervalFromVar(from, to);
            if (done) {
                notifyPropagators(IntEventType.REMOVE, cause);
            }// no else needed, since all values were checked
            return done;
        }
        return false;
    }

    @Override
    public boolean removeAllValuesBut(IntIterableSet values, ICause cause) throws ContradictionException {
        int olb = getLB();
        int oub = getUB();
        int nlb = values.nextValue(olb - 1);
        int nub = values.previousValue(oub + 1);
        // the new bounds are now known, delegate to the right method
        boolean hasChanged = updateBounds(nlb, nub, cause);
        // now deal with holes
        int to = previousValue(nub);
        boolean hasRemoved = false;
        int value = nextValue(nlb);
        // iterate over the values in the domain, remove the ones that are not in values
        for (; value <= to; value = nextValue(value)) {
            if (!values.contains(value)) {
                model.getSolver().getExplainer().removeValue(this, value, cause);
                if(doRemoveValueFromVar(value)){
                    hasRemoved |= true;
                }else{
                    model.getSolver().getExplainer().undo();
                }
            }
        }
        if (hasRemoved) {
            IntEventType e = IntEventType.REMOVE;
            if (isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            this.notifyPropagators(e, cause);
        }
        return hasRemoved || hasChanged;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        model.getSolver().getExplainer().instantiateTo(this, value, cause, getLB(), getUB());
        boolean done = doInstantiateVar(value);
        if (done) {
            notifyPropagators(IntEventType.INSTANTIATE, cause);
            return true;
        }else{
            model.getSolver().getExplainer().undo();
        }
        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int old = this.getLB();
        if (old < value) {
            model.getSolver().getExplainer().updateLowerBound(this, value, getLB(), cause);
            IntEventType e = IntEventType.INCLOW;
            boolean done = doUpdateLowerBoundOfVar(value);
            if (isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            if (done) {
                this.notifyPropagators(e, cause);
                return true;
            }else{
                model.getSolver().getExplainer().undo();
            }
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int old = this.getUB();
        if (old > value) {
            model.getSolver().getExplainer().updateUpperBound(this, value, getUB(), cause);
            IntEventType e = IntEventType.DECUPP;
            boolean done = doUpdateUpperBoundOfVar(value);
            if (isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            if (done) {
                this.notifyPropagators(e, cause);
                return true;
            }else{
                model.getSolver().getExplainer().undo();
            }
        }
        return false;
    }

    @Override
    public boolean updateBounds(int lb, int ub, ICause cause) throws ContradictionException {
        assert cause != null;
        if(lb>ub)contradiction(cause,"");
        int olb = this.getLB();
        int oub = this.getUB();
        boolean hasChanged = false;
        if (olb < lb || oub > ub) {
            IntEventType e = null;

            if (olb < lb) {
                model.getSolver().getExplainer().updateLowerBound(this, lb, getLB(), cause);
                e = IntEventType.INCLOW;
                if(doUpdateLowerBoundOfVar(lb)){
                    hasChanged = true;
                }else{
                    model.getSolver().getExplainer().undo();
                }
            }
            if (oub > ub) {
                e = e == null ? IntEventType.DECUPP : IntEventType.BOUND;
                model.getSolver().getExplainer().updateUpperBound(this, ub, getUB(), cause);
                if(doUpdateUpperBoundOfVar(ub)){
                    hasChanged |= true;
                }else{
                    model.getSolver().getExplainer().undo();
                }
            }
            if (isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            if (hasChanged) {
                this.notifyPropagators(e, cause);
            }
        }
        return hasChanged;
    }

    @Override
    public int getTypeAndKind() {
        return Variable.VIEW | Variable.INT;
    }

	@Override
    public IntVar getVariable() {
        return var;
    }

    @Override
    public int getDomainSize() {
        return var.getDomainSize();
    }

    @Override
    public int getRange() {
        return var.getRange();
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return var.hasEnumeratedDomain();
    }

    @Override
    public boolean isInstantiated() {
        return var.isInstantiated();
    }

	@Override
    public IDelta getDelta() {
        return var.getDelta();
    }

    @Override
    public void createDelta() {
        var.createDelta();
    }

    @Override
    public int compareTo(Variable o) {
        return this.getId() - o.getId();
    }

	@Override
    public void notifyMonitors(IEventType event) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event);
        }
    }

    @Override
    public void notifyPropagators(IEventType event, ICause cause) throws ContradictionException {
        super.notifyPropagators(transformEvent(event), this);
    }

    @Override
    public IEventType transformEvent(IEventType evt){
        return evt;
    }

    @Override
    public void contradiction(ICause cause, String message) throws ContradictionException {
        assert cause != null;
        model.getSolver().getEngine().fails(cause, this, message);
    }


    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || _viterator.isNotReusable()) {
            _viterator = new DisposableValueBoundIterator(this);
        }
        if (bottomUp) {
            _viterator.bottomUpInit();
        } else {
            _viterator.topDownInit();
        }
        return _viterator;
    }

	@Override
    public DisposableRangeIterator getRangeIterator(boolean bottomUp) {
        if (_riterator == null || _riterator.isNotReusable()) {
            _riterator = new DisposableRangeBoundIterator(this);
        }
        if (bottomUp) {
            _riterator.bottomUpInit();
        } else {
            _riterator.topDownInit();
        }
        return _riterator;
    }

    @Override
    public Iterator<Integer> iterator() {
        _javaIterator.reset();
        return _javaIterator;
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar modifiedVar, IEventType evt, int value) {
        boolean newrules = false;
        boolean observed = modifiedVar == var;
        IntEventType ievt;
        if(observed){
            value = this.transformValue(value);
            ievt = (IntEventType)this.transformEvent(evt);
        }else {
            value = modifiedVar.reverseValue(value);
            ievt = (IntEventType)modifiedVar.transformEvent(evt);
        }
        switch (ievt) {
            case REMOVE:
                newrules = ruleStore.addRemovalRule(this, value);
                break;
            case DECUPP:
                newrules = ruleStore.addUpperBoundRule(this);
                break;
            case INCLOW:
                newrules = ruleStore.addLowerBoundRule(this);
                break;
            case INSTANTIATE:
                newrules = ruleStore.addFullDomainRule(this);
                break;
        }
        return newrules;
    }
}
