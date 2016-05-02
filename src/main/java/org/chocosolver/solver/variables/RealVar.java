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
package org.chocosolver.solver.variables;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;

/**
 * An interface to declare variable for continuous constraints (solved using IBEX).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/07/12
 */
public interface RealVar extends Variable {

    /**
     * Retrieves the lower bound of the variable
     *
     * @return the lower bound
     */
    double getLB();

    /**
     * Retrieves the upper bound of the variable
     *
     * @return the upper bound
     */
    double getUB();

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
    boolean updateLowerBound(double value, ICause cause) throws ContradictionException;

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
     * @throws org.chocosolver.solver.exception.ContradictionException
     *          if the domain become empty due to this action
     */
    boolean updateUpperBound(double value, ICause cause) throws ContradictionException;

    /**
     * Updates the bounds of the domain of <code>this</code> to <code>value</code>.
     * The instruction comes from <code>propagator</code>.
     * <ul>
     * <li>If the interval defined by [<code>lowerbound</code>,<code>upperbound</code>] includes the domain of this, nothing is done and the return value is <code>false</code>,</li>
     * <li>if updating the domain leads to a dead-end (domain wipe-out),
     * a <code>ContradictionException</code> is thrown,</li>
     * <li>otherwise, if updating the domain be done safely,
     * the event type is created (the original event can be promoted) and observers are notified
     * and the return value is <code>true</code></li>
     * </ul>
     *
     * @param lowerbound new lower bound (included)
     * @param upperbound new upper bound (included)
     * @param cause      update releaser
     * @return true if the upper bound has been updated, false otherwise
     * @throws org.chocosolver.solver.exception.ContradictionException
     *          if the domain become empty due to this action
     */
    boolean updateBounds(double lowerbound, double upperbound, ICause cause) throws ContradictionException;

    double getPrecision();
}
