/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;


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
public interface IntVar extends Variable {

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
     * Returns the size of the domain of <code>this</code>
     *
     * @return size of the domain
     */
    int getDomainSize();

    /**
     * Returns the next value just after v in <code>this</code>.
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
     * Returns the previous value just befor v in <code>this</code>.
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
    <DM extends IIntDeltaMonitor> DM monitorDelta(ICause propagator);


    /**
     * @return true iff the variable has a binary domain
     */
    boolean isBool();
}
