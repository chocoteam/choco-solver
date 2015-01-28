/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package org.chocosolver.solver.variables;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;

import java.io.Serializable;

/**
 * Observer of events generated during resolution.
 * Defined initially for explanation engine.
 * Created by cprudhom on 09/12/14.
 * Project: choco.
 */
public interface EventObserver extends Serializable {

    /**
     * Explain the activation of a propagator involved in a reified constraint
     *
     * @param var        the reified variable
     * @param propagator the propagator to awake.
     */
    default void activePropagator(BoolVar var, Propagator propagator) {
    }

    /**
     * Explain the removal of the <code>val</code> from <code>var</code>, due to <code>cause</code>.
     *
     * @param var   an integer variable
     * @param val   a value
     * @param cause a cause
     */
    default void removeValue(IntVar var, int val, ICause cause) {
    }

    /**
     * Explain the removal of [<code>old</code>,<code>value</code>[ from <code>var</code>, due to <code>cause</code>.
     * <p/>
     * Prerequisite: <code>value</code> should belong to <code>var</code>
     *
     * @param intVar an integer variable
     * @param old    the previous lower bound
     * @param value  the current lower bound
     * @param cause  the cause
     */
    default void updateLowerBound(IntVar intVar, int old, int value, ICause cause) {
    }

    /**
     * Explain the removal of ]<code>value</code>,<code>old</code>] from <code>var</code>, due to <code>cause</code>.
     * <p/>
     * Prerequisite: <code>value</code> should belong to <code>var</code>
     *
     * @param var   an integer variable
     * @param old   the previous upper bound
     * @param value the current upper bound
     * @param cause the cause
     */
    default void updateUpperBound(IntVar var, int old, int value, ICause cause) {
    }

    /**
     * Explain the assignment to <code>val</code> of <code>var</code> due to <code>cause</code>.
     *
     * @param var   an integer variable
     * @param val   the assignment value
     * @param cause the cause
     * @param oldLB the previous LB
     * @param oldUB the previous UB
     */
    default void instantiateTo(IntVar var, int val, ICause cause, int oldLB, int oldUB) {
    }

}
