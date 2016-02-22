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
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * A Set Variable is defined by a domain which is a set interval [LB,UB], where:
 * LB is the set of integers that must belong to every single solution.
 * UB is the set of integers that may belong to at least one solution.
 * <p/>
 * One must notice that in this context, a VALUE of the variable is a set of integers.
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 15 nov. 2012 Major update 2016
 */
public interface SetVar extends Variable {

	/**
	 * Get SetVar Lower Bound : the set of integers that must belong to every solution (i.e. a subset of all solutions)
	 * To iterate over this set, use the following loop:
	 *
	 * ISet lbSet = getLB();
	 * for(int i : lbSet){
	 *  	...
	 * }
	 *
	 * This object is read-only. Use variable methods <code>force</code> to update the domain
	 *
	 * @return the lower bound of this SetVar.
	 */
	ISet getLB();


	/**
	 * Get SetVar Upper Bound : the set of integers that may belong to a solution (i.e. a superset of all solutions)
	 * To iterate over this set, use the following loop:
	 *
	 * ISet ubSet = getUB();
	 * for(int i : ubSet){
	 *  	...
	 * }
	 *
	 * This object is read-only. Use variable methods <code>remove</code> to update the domain
	 *
	 * @return the lower bound of this SetVar.
	 */
	ISet getUB();

    /**
     * Adds element to the lower bound, i.e. every solution must include <code>element</code>
     *
     * @param element value to add
     * @param cause cause of value addition
     * @return true iff element has been added to the lower bound
     * @throws ContradictionException
     */
    boolean force(int element, ICause cause) throws ContradictionException;

    /**
     * Removes element from the upper bound, i.e. the set variable cannot contain <code>element</code> anymore
     *
     * @param element value to remove
     * @param cause cause of value removal
     * @return true iff element has been removed from the upper bound
     * @throws ContradictionException
     */
    boolean remove(int element, ICause cause) throws ContradictionException;

    /**
     * Enforces the set variable to contain exactly the set of integers given in parameter
     *
     * @param value a set of integers
     * @param cause cause of instantiation
     * @return true iff a domain modification occurred
     * @throws ContradictionException
     */
    boolean instantiateTo(int[] value, ICause cause) throws ContradictionException;

    /**
     * Retrieves the current value of the variable if instantiated, otherwise the lower bound (kernel).
     *
     * @return the current value (or kernel if not yet instantiated).
     */
    int[] getValue();

    /**
     * Allow propagator to monitor element removal/enforcing of this
     *
     * @param propagator observer
     * @return a new SetDeltaMonitor
     */
	ISetDeltaMonitor monitorDelta(ICause propagator);
}
