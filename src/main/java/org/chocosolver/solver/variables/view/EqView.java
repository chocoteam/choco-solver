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
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;

/**
 * A specific view for equality on bool var
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/07/12
 */
public class EqView extends IntView {

    /**
     * Create an equality view of <i>var<i/> 
     * @param var an integer variable
     */
    public EqView(IntVar var) {
        super("eq(" + var.getName() + ")", var);
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        return var.monitorDelta(propagator);
    }

    @Override
    boolean doInstantiateVar(int value) throws ContradictionException {
        return var.instantiateTo(value, this);
    }

    @Override
    boolean doUpdateLowerBoundOfVar(int value) throws ContradictionException {
        return var.updateLowerBound(value, this);
    }

    @Override
    boolean doUpdateUpperBoundOfVar(int value) throws ContradictionException {
        return var.updateUpperBound(value, this);
    }

    @Override
    boolean doRemoveValueFromVar(int value) throws ContradictionException {
        return var.removeValue(value, this);
    }

    @Override
    boolean doRemoveIntervalFromVar(int from, int to) throws ContradictionException {
        return var.removeInterval(from, to, this);
    }

    @Override
    public boolean contains(int value) {
        return var.contains(value);
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        return var.isInstantiatedTo(value);
    }

    @Override
    public int getValue() {
        return var.getValue();
    }

    @Override
    public int getLB() {
        return var.getLB();
    }

    @Override
    public int getUB() {
        return var.getUB();
    }

    @Override
    public int nextValue(int v) {
        return var.nextValue(v);
    }

    @Override
    public int nextValueOut(int v) {
        return var.nextValueOut(v);
    }

    @Override
    public int previousValue(int v) {
        return var.previousValue(v);
    }

    @Override
    public int previousValueOut(int v) {
        return var.previousValueOut(v);
    }

    @Override
    public String toString() {
        return "eq(" + this.var.toString() + ") = [" + getLB() + "," + getUB() + "]";
    }

    @Override
    public IntVar duplicate() {
        return model.intEqView(this.var);
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        return var.getValueIterator(bottomUp);
    }

    @Override
    public DisposableRangeIterator getRangeIterator(boolean bottomUp) {
        return var.getRangeIterator(bottomUp);
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = false;
        assert var == this.var;
        IntEventType ievt = (IntEventType) evt;
        switch (ievt) {
            case REMOVE:
                newrules |= ruleStore.addRemovalRule(this, value);
                break;
            case DECUPP:
                newrules |= ruleStore.addUpperBoundRule(this);
                break;
            case INCLOW:
                newrules |= ruleStore.addLowerBoundRule(this);
                break;
            case INSTANTIATE:
                newrules |= ruleStore.addFullDomainRule(this);
                break;
        }
        return newrules;
    }
}
