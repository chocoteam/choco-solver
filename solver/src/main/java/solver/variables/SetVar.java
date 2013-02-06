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

package solver.variables;

import memory.setDataStructures.ISet;
import solver.ICause;
import solver.exception.ContradictionException;
import solver.variables.delta.SetDelta;
import solver.variables.delta.monitor.SetDeltaMonitor;

/**
 * A Set Variable is defined by a domain which is a set interval [S_low,S_up]
 * S_low is the set of elements that must belong to every single solution. It is called the kernel.
 * S_up is the set of elements that may belong to at least one solution. It is called the envelope.
 * <p/>
 * One must notice that in this context, a VALUE of the variable is a set of elements
 * (which are integers here).
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 15 nov. 2012
 */
public interface SetVar extends Variable<SetDelta> {

    /**
     * Gets the set of elements that belong to every solution
     *
     * @return the kernel of the set variable
     */
    public ISet getKernel();

    /**
     * Gets the set of elements that may belong to a solution
     *
     * @return the envelope of the set variable
     */
    public ISet getEnvelope();

    /**
     * Adds element to the kernel, i.e. enforces that the set variable
     * will contain element in every solution
     *
     * @param element
     * @param cause
     * @return true iff value was not already in the kernel
     * @throws ContradictionException
     */
    boolean addToKernel(int element, ICause cause) throws ContradictionException;

    /**
     * Removes element from the envelop, i.e. the set variable cannot contain element anymore
     *
     * @param element
     * @param cause
     * @return true iff value was present in the envelope
     * @throws ContradictionException
     */
    boolean removeFromEnvelope(int element, ICause cause) throws ContradictionException;

    /**
     * Enforces the set variable to contain exactly the set of integers given in parameter
     *
     * @param value a set of integers
     * @param cause
     * @return true iff a domain modification occurred
     * @throws ContradictionException
     */
    boolean instantiateTo(int[] value, ICause cause) throws ContradictionException;

    /**
     * Checks if an element <code>v</code> belongs to the domain of <code>this</code>
     *
     * @param v the element to check
     * @return <code>true</code> if the element belongs to the domain of <code>this</code>, <code>false</code> otherwise.
     */
    boolean contains(int v);

    /**
     * Retrieves the current value of the variable if instantiated, otherwier the lower bound.
     *
     * @return the current value (or lower bound if not yet instantiated).
     */
    int[] getValue();

    /**
     * Allow propagator to monitor element removal/enforcing of this
     *
     * @param propagator
     * @return a new SetDeltaMonitor
     */
    public SetDeltaMonitor monitorDelta(ICause propagator);
}
