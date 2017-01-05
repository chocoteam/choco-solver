/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSet;


/**
 * Interface for integer variables. Provides every required services.
 * The domain is explicitly represented but is not (and should not be) accessible from outside.
 * <br/>
 * <p>
 * CPRU r544: remove default implementation
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public interface IntVar extends ICause, Variable, Iterable<Integer>, ArExpression {

    /**
     * Provide a minimum value for integer variable lower bound.
     * Do not prevent from underflow, but may avoid it, somehow.
     */
    int MIN_INT_BOUND = Integer.MIN_VALUE / 100;

    /**
     * Provide a minimum value for integer variable lower bound.
     * Do not prevent from overflow, but may avoid it, somehow.
     */
    int MAX_INT_BOUND = Integer.MAX_VALUE / 100;

    /**
     * Removes <code>value</code>from the domain of <code>this</code>. The instruction comes from <code>propagator</code>.
     * <ul>
     * <li>If <code>value</code> is out of the domain, nothing is done and the return value is <code>false</code>,</li>
     * <li>if removing <code>value</code> leads to a dead-end (domain wipe-out),
     * a <code>ContradictionException</code> is thrown,</li>
     * <li>otherwise, if removing <code>value</code> from the domain can be done safely,
     * the event type is created (the original event can be promoted) and observers are notified
     * and the return value is <code>true</code></li>
     * </ul>
     *
     * @param value value to remove from the domain (int)
     * @param cause removal releaser
     * @return true if the value has been removed, false otherwise
     * @throws ContradictionException if the domain become empty due to this action
     */
    boolean removeValue(int value, ICause cause) throws ContradictionException;

    /**
     * Removes the value in <code>values</code>from the domain of <code>this</code>. The instruction comes from <code>propagator</code>.
     * <ul>
     * <li>If all values are out of the domain, nothing is done and the return value is <code>false</code>,</li>
     * <li>if removing a value leads to a dead-end (domain wipe-out),
     * a <code>ContradictionException</code> is thrown,</li>
     * <li>otherwise, if removing the <code>values</code> from the domain can be done safely,
     * the event type is created (the original event can be promoted) and observers are notified
     * and the return value is <code>true</code></li>
     * </ul>
     *
     * @param values set of ordered values to remove
     * @param cause  removal release
     * @return true if at least a value has been removed, false otherwise
     * @throws ContradictionException if the domain become empty due to this action
     */
    boolean removeValues(IntIterableSet values, ICause cause) throws ContradictionException;

    /**
     * Removes all values from the domain of <code>this</code> except those in <code>values</code>. The instruction comes from <code>propagator</code>.
     * <ul>
     * <li>If all values are out of the domain,
     * a <code>ContradictionException</code> is thrown,</li>
     * <li>if the domain is a subset of values,
     * nothing is done and the return value is <code>false</code>,</li>
     * <li>otherwise, if removing all values but <code>values</code> from the domain can be done safely,
     * the event type is created (the original event can be promoted) and observers are notified
     * and the return value is <code>true</code></li>
     * </ul>
     *
     * @param values set of ordered values to keep in the domain
     * @param cause  removal release
     * @return true if a at least a value has been removed, false otherwise
     * @throws ContradictionException if the domain become empty due to this action
     */
    boolean removeAllValuesBut(IntIterableSet values, ICause cause) throws ContradictionException;

    /**
     * Removes values between [<code>from, to</code>] from the domain of <code>this</code>. The instruction comes from <code>propagator</code>.
     * <ul>
     * <li>If union between values and the current domain is empty, nothing is done and the return value is <code>false</code>,</li>
     * <li>if removing a <code>value</code> leads to a dead-end (domain wipe-out),
     * a <code>ContradictionException</code> is thrown,</li>
     * <li>otherwise, if removing at least a <code>value</code> from the domain can be done safely,
     * the event type is created (the original event can be promoted) and observers are notified
     * and the return value is <code>true</code></li>
     * </ul>
     *
     * @param from  lower bound of the interval to remove (int)
     * @param to    upper bound of the interval to remove(int)
     * @param cause removal releaser
     * @return true if the value has been removed, false otherwise
     * @throws ContradictionException if the domain become empty due to this action
     */
    boolean removeInterval(int from, int to, ICause cause) throws ContradictionException;

    /**
     * Instantiates the domain of <code>this</code> to <code>value</code>. The instruction comes from <code>propagator</code>.
     * <ul>
     * <li>If the domain of <code>this</code> is already instantiated to <code>value</code>,
     * nothing is done and the return value is <code>false</code>,</li>
     * <li>If the domain of <code>this</code> is already instantiated to another value,
     * then a <code>ContradictionException</code> is thrown,</li>
     * <li>Otherwise, the domain of <code>this</code> is restricted to <code>value</code> and the observers are notified
     * and the return value is <code>true</code>.</li>
     * </ul>
     *
     * @param value instantiation value (int)
     * @param cause instantiation releaser
     * @return true if the instantiation is done, false otherwise
     * @throws ContradictionException if the domain become empty due to this action
     */
    boolean instantiateTo(int value, ICause cause) throws ContradictionException;

    /**
     * Updates the lower bound of the domain of <code>this</code> to <code>value</code>.
     * The instruction comes from <code>propagator</code>.
     * <ul>
     * <li>If <code>value</code> is smaller than the lower bound of the domain, nothing is done and the return value is <code>false</code>,</li>
     * <li>if updating the lower bound to <code>value</code> leads to a dead-end (domain wipe-out),
     * a <code>ContradictionException</code> is thrown,</li>
     * <li>otherwise, if updating the lower bound to <code>value</code> can be done safely,
     * the event type is created (the original event can be promoted) and observers are notified
     * and the return value is <code>true</code></li>
     * </ul>
     *
     * @param value new lower bound (included)
     * @param cause updating releaser
     * @return true if the lower bound has been updated, false otherwise
     * @throws ContradictionException if the domain become empty due to this action
     */
    boolean updateLowerBound(int value, ICause cause) throws ContradictionException;

    /**
     * Updates the upper bound of the domain of <code>this</code> to <code>value</code>.
     * The instruction comes from <code>propagator</code>.
     * <ul>
     * <li>If <code>value</code> is greater than the upper bound of the domain, nothing is done and the return value is <code>false</code>,</li>
     * <li>if updating the upper bound to <code>value</code> leads to a dead-end (domain wipe-out),
     * a <code>ContradictionException</code> is thrown,</li>
     * <li>otherwise, if updating the upper bound to <code>value</code> can be done safely,
     * the event type is created (the original event can be promoted) and observers are notified
     * and the return value is <code>true</code></li>
     * </ul>
     *
     * @param value new upper bound (included)
     * @param cause update releaser
     * @return true if the upper bound has been updated, false otherwise
     * @throws ContradictionException if the domain become empty due to this action
     */
    boolean updateUpperBound(int value, ICause cause) throws ContradictionException;


    /**
     * Updates the lower bound and the upper bound of the domain of <code>this</code> to, resp. <code>lb</code> and <code>ub</code>.
     * The instruction comes from <code>propagator</code>.
     * <p>
     * <ul>
     * <li>If <code>lb</code> is smaller than the lower bound of the domain
     * and <code>ub</code> is greater than the upper bound of the domain,
     * <p>
     * nothing is done and the return value is <code>false</code>,</li>
     * <li>if updating the lower bound to <code>lb</code>, or updating the upper bound to <code>ub</code> leads to a dead-end (domain wipe-out),
     * or if <code>lb</code> is strictly greater than <code>ub</code>,
     * a <code>ContradictionException</code> is thrown,</li>
     * <li>otherwise, if updating the lower bound to <code>lb</code> and/or the upper bound to <code>ub</code>
     * can be done safely can be done safely,
     * the event type is created (the original event can be promoted) and observers are notified
     * and the return value is <code>true</code></li>
     * </ul>
     *
     * @param lb    new lower bound (included)
     * @param ub    new upper bound (included)
     * @param cause update releaser
     * @return true if the upper bound has been updated, false otherwise
     * @throws ContradictionException if the domain become empty due to this action
     */
    boolean updateBounds(int lb, int ub, ICause cause) throws ContradictionException;

    /**
     * Checks if a value <code>v</code> belongs to the domain of <code>this</code>
     *
     * @param value int
     * @return <code>true</code> if the value belongs to the domain of <code>this</code>, <code>false</code> otherwise.
     */
    boolean contains(int value);

    /**
     * Checks wether <code>this</code> is instantiated to <code>val</code>
     *
     * @param value int
     * @return true if <code>this</code> is instantiated to <code>val</code>, false otherwise
     */
    boolean isInstantiatedTo(int value);

    /**
     * Retrieves the current value of the variable if instantiated, otherwier the lower bound.
     *
     * @return the current value (or lower bound if not yet instantiated).
     */
    int getValue();

    /**
     * Retrieves the lower bound of the variable
     *
     * @return the lower bound
     */
    int getLB();

    /**
     * Retrieves the upper bound of the variable
     *
     * @return the upper bound
     */
    int getUB();

    /**
     * Returns the size of this domain, that is the number of elements in this domain.
     *
     * @return size of the domain
     */
    int getDomainSize();


    /**
     * Returns the range of this domain, that is, the difference between the upper bound and the lower bound.
     * @return the range of this domain
     */
    int getRange();

    /**
     * Returns the first value just after v in <code>this</code> which is <b>in</b> the domain.
     * If no such value exists, returns Integer.MAX_VALUE;
     * <p>
     * To iterate over the values in a <code>IntVar</code>,
     * use the following loop:
     * <p>
     * <pre>
     * int ub = iv.getUB();
     * for (int i = iv.getLB(); i <= ub; i = iv.nextValue(i)) {
     *     // operate on value i here
     * }</pre>
     *
     * @param v the value to start checking (exclusive)
     * @return the next value in the domain
     */
    int nextValue(int v);

    /**
     * Returns the first value just after v in <code>this</code> which is <b>out of</b> the domain.
     * If <i>v</i> is less than or equal to {@link #getLB()-2}, returns <i>v + 1</i>,
     * if <i>v</i> is greater than or equal to {@link #getUB()}, returns <i>v + 1</i>.
     *
     * @param v the value to start checking (exclusive)
     * @return the next value out of the domain
     */
    int nextValueOut(int v);

    /**
     * Returns the previous value just before v in <code>this</code>.
     * If no such value exists, returns Integer.MIN_VALUE;
     * <p>
     * To iterate over the values in a <code>IntVar</code>,
     * use the following loop:
     * <p>
     * <pre>
     * int lb = iv.getLB();
     * for (int i = iv.getUB(); i >= lb; i = iv.previousValue(i)) {
     *     // operate on value i here
     * }</pre>
     *
     * @param v the value to start checking (exclusive)
     * @return the previous value in the domain
     */
    int previousValue(int v);

    /**
     * Returns the first value just before v in <code>this</code> which is <b>out of</b> the domain.
     * If <i>v</i> is greater than or equal to {@link #getUB()+2}, returns <i>v - 1</i>,
     * if <i>v</i> is less than or equal to {@link #getLB()}, returns <i>v - 1</i>.
     *
     * @param v the value to start checking (exclusive)
     * @return the previous value out of the domain
     */
    int previousValueOut(int v);

    /**
     * Retrieves an iterator over values of <code>this</code>.
     * <p>
     * The values can be iterated in a bottom-up way or top-down way.
     * <p>
     * To bottom-up iterate over the values in a <code>IntVar</code>,
     * use the following loop:
     * <p>
     * <pre>
     * DisposableValueIterator vit = var.getValueIterator(true);
     * while(vit.hasNext()){
     *     int v = vit.next();
     *     // operate on value v here
     * }
     * vit.dispose();</pre>
     *
     *
     * To top-down iterate over the values in a <code>IntVar</code>,
     * use the following loop:
     *
     * <pre>
     * DisposableValueIterator vit = var.getValueIterator(false);
     * while(vit.hasPrevious()){
     *     int v = vit.previous();
     *     // operate on value v here
     * }
     * vit.dispose();</pre>
     *
     * <b>Using both previous and next can lead to unexpected behaviour.</b>
     *
     * @param bottomUp way to iterate over values. <code>true</code> means from lower bound to upper bound,
     *                 <code>false</code> means from upper bound to lower bound.
     * @return a disposable iterator over values of <code>this</code>.
     */
    DisposableValueIterator getValueIterator(boolean bottomUp);

    /**
     * Retrieves an iterator over ranges (or intervals) of <code>this</code>.
     * <p>
     * The ranges can be iterated in a bottom-up way or top-down way.
     * <p>
     * To bottom-up iterate over the values in a <code>IntVar</code>,
     * use the following loop:
     * <p>
     * <pre>
     * DisposableRangeIterator rit = var.getRangeIterator(true);
     * while (rit.hasNext()) {
     *     int from = rit.min();
     *     int to = rit.max();
     *     // operate on range [from,to] here
     *     rit.next();
     * }
     * rit.dispose();</pre>
     *
     * To top-down iterate over the values in a <code>IntVar</code>,
     * use the following loop:
     *
     * <pre>
     * DisposableRangeIterator rit = var.getRangeIterator(false);
     * while (rit.hasPrevious()) {
     *     int from = rit.min();
     *     int to = rit.max();
     *     // operate on range [from,to] here
     *     rit.previous();
     * }
     * rit.dispose();</pre>
     *
     * <b>Using both previous and next can lead to unexpected behaviour.</b>
     *
     * @param bottomUp way to iterate over ranges. <code>true</code> means from lower bound to upper bound,
     *                 <code>false</code> means from upper bound to lower bound.
     * @return a disposable iterator over ranges of <code>this</code>.
     */
    DisposableRangeIterator getRangeIterator(boolean bottomUp);

    /**
     * Indicates wether (or not) <code>this</code> has an enumerated domain (represented in extension)
     * or not (only bounds)
     *
     * @return <code>true</code> if the domain is enumerated, <code>false</code> otherwise.
     */
    boolean hasEnumeratedDomain();

    /**
     * Allow to monitor removed values of <code>this</code>.
     *
     * @param propagator the cause that requires to monitor delta
     * @return a delta monitor
     */
    IIntDeltaMonitor monitorDelta(ICause propagator);


    /**
     * @return true iff the variable has a binary domain
     */
    boolean isBool();

    @Override
    default boolean why(RuleStore ruleStore, IntVar modifiedVar, IEventType evt, int value) {
        boolean newrules = false;
        boolean observed = modifiedVar == this;
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


    /**
     * @param evt   original event
     * @return transforms the original event wrt this IntVar
     */
    default IEventType transformEvent(IEventType evt){
        return evt;
    }


    /**
     * @param value original value
     * @return transforms the original value wrt this IntVar
     */
    default int transformValue(int value){
        return value;
    }

    /**
     * @param value original value
     * @return reverses the original value wrt this IntVar
     */
    default int reverseValue(int value){
        return value;
    }


    @Override
    default IntVar intVar(){
        return this;
    }

    @Override
    default boolean isExpressionLeaf() {
        return true;
    }
}
