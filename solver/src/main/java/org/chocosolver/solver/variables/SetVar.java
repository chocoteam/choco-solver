/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
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
	 * get the constrained cardinality variable of this set.
	 * <ul>
	 * 	<li>if no variable already has this role, creates a new variable, store and constraint it.</li>
	 * 	<li>if a variable already has this role, return it.</li>
	 * </ul>
	 * @return a variable constrained to this set cardinality. Successive calls should return the same value
	 */
	IntVar getCard();
	
	/**
	 * @return true if a variable is constrained to this set's cardinality, false otherwise
	 */
	boolean hasCard();
        
	/**
	 * ensure a variable is equal to the cardinality of this set.
	 * <ul>
	 * 	<li>If not call has been already performed to this set cardinality, post a constraint on the number of variables</li>
	 *  <li>if this set already has a variable constrained to the cardinality, post an equality</li>
	 * </ul>
	 * @param card a variable of the same model.
	 */
	void setCard(IntVar card);

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
    default ISet getValue(){
		assert isInstantiated() : getName() + " not instantiated";
		return getLB();
	}

    /**
     * Allow propagator to monitor element removal/enforcing of this
     *
     * @param propagator observer
     * @return a new SetDeltaMonitor
     */
	ISetDeltaMonitor monitorDelta(ICause propagator);
}
