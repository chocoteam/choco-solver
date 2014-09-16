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

package solver.variables;

import solver.ICause;
import solver.exception.ContradictionException;
import solver.variables.delta.ISetDeltaMonitor;

/**
 * A Set Variable is defined by a domain which is a set interval [kernel,envelope]
 * The kernel is the set of elements that must belong to every single solution.
 * The envelope is the set of elements that may belong to at least one solution.
 * <p/>
 * One must notice that in this context, a VALUE of the variable is a set of integers.
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 15 nov. 2012
 */
public interface SetVar extends Variable {

	/**
	 * Constant used for enumerating elements in the envelope or the kernel of a SetVar.
	 * This value indicates that the iteration is over:
	 *
	 * <code>for(int e=getKernelFirst(); e!=SetVar.END; e=getKernelNext()){
	 *     // something
	 * }</code>
	 */
	public final static int END = Integer.MIN_VALUE;

	/**
	 * Get the first element currently in the kernel domain of <code>this</code>.
	 * Returns <code>END</code> if the set is empty.
	 * Note that elements are not sorted.
	 * To iterate over elements that are present in the kernel, do the following loop:
	 * <code>for(int e=getKernelFirst(); e!=SetVar.END; e=getKernelNext()){
	 *     // something
	 * }</code>
	 *
	 * @return the first element in the kernel or <code>END</code> if it is empty.
	 */
	public int getKernelFirst();

	/**
	 * Get the next element in the kernel domain of <code>this</code>.
	 * Returns <code>END</code> once all elements have been visited.
	 * To iterate over elements that are present in the kernel, do the following loop:
	 * <code>for(int e=getKernelFirst(); e!=SetVar.END; e=getKernelNext()){
	 *     // something
	 * }</code>
	 *
	 * @return the next element in the kernel, if any, or <code>END</code> otherwise.
	 */
	public int getKernelNext();

	/**
	 * Get the number of elements in the kernel domain of <code>this</code>.
	 *
	 * @return the number of elements currently present in the kernel.
	 */
	public int getKernelSize();

	/**
	 * Test whether element is present or not in the kernel
	 * @param element
	 * @return true iff element is present in the kernel
	 */
	public boolean kernelContains(int element);

	/**
	 * Get the first element currently in the envelope domain of <code>this</code>.
	 * Returns <code>END</code> if the set is empty.
	 * Note that elements are not sorted.
	 * To iterate over elements that are present in the envelope, do the following loop:
	 * <code>for(int e=getEnvelopeFirst(); e!=SetVar.END; e=getEnvelopeNext()){
	 *     // something
	 * }</code>
	 *
	 *
	 * @return the first element in the envelope or <code>END</code> if it is empty.
	 */
	public int getEnvelopeFirst();

	/**
	 * Get the next element in the envelope domain of <code>this</code>.
	 * Returns <code>END</code> once all elements have been visited.
	 * To iterate over elements that are present in the envelope, do the following loop:
	 * <code>for(int e=getEnvelopeFirst(); e!=SetVar.END; e=getEnvelopeNext()){
	 *     // something
	 * }</code>
	 *
	 *
	 * @return the next element in the envelope, if any, or <code>END</code> otherwise.
	 */
	public int getEnvelopeNext();

	/**
	 * Get the number of elements in the envelope domain of <code>this</code>.
	 *
	 * @return the number of elements currently present in the envelope.
	 */
	public int getEnvelopeSize();

	/**
	 * Test whether element is present or not in the envelope
	 * @param element
	 * @return true iff element is present in the envelope
	 */
	public boolean envelopeContains(int element);

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
     * Retrieves the current value of the variable if instantiated, otherwise the lower bound (kernel).
     *
     * @return the current value (or kernel if not yet instantiated).
     */
    int[] getValues();

    /**
     * Allow propagator to monitor element removal/enforcing of this
     *
     * @param propagator
     * @return a new SetDeltaMonitor
     */
    public ISetDeltaMonitor monitorDelta(ICause propagator);
}
