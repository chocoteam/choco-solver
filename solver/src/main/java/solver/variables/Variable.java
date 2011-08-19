/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.variables;

import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.requests.IRequest;
import solver.requests.list.IRequestList;
import solver.variables.domain.delta.IDelta;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: xlorca
 */
public interface Variable<D extends IDelta> extends Serializable {
	

	public final static int INTEGER = 0;
	public final static int SET = 1;
	public final static int META = 2;
	public final static int GRAPH = 3;
	

    //todo: to complete
    void updateEntailment(IRequest request);

    /**
     * Indicates wether <code>this</code> is instantiated (see implemetations to know what instantiation means).
     *
     * @return <code>true</code> if <code>this</code> is instantiated
     */
    boolean instantiated();

    /**
     * Returns the name of <code>this</code>
     *
     * @return a String reprensenting the name of <code>this</code>
     */
    String getName();

    //todo : to complete

    void addRequest(IRequest request);

    //todo : to complete

    void deleteRequest(IRequest request);

    IRequestList getRequests();

    int nbRequests();

    /**
     * Returns the number of constraints involving <code>this</code>
     * TODO: MostConstrained: count requests instead of constraints
     *
     * @return the number of constraints of <code>this</code>
     */
    int nbConstraints();

    Explanation explain();

    D getDelta();

    /**
     * Adds an observers to the set of observers for this object,
     * provided that it is not the same as some observer already in the set.
     * @param observer an observer to add
     * @param idxInProp index of the variable in the propagator
     */
    public void addPropagator(Propagator observer, int idxInProp);

    /**
     * Deletes an observers from the set of observers for this object.
     * @param observer the observer to delete
     */
    public void deletePropagator(Propagator observer);

    /**
     * If <code>this</code> has changed, then notify all of its observers.<br/>
     * Each observer has its update method.
     *
     *
     * @param e event on this object
     * @param o object which leads to the modification of this object
     * @throws solver.exception.ContradictionException if a contradiction occurs during notification
     */
    public void notifyPropagators(EventType e, ICause o) throws ContradictionException;

    /**
     * The solver attributes a unique ID to the variable (used as hashCode)
     * @param id unique ID
     */
    void setUniqueID(int id);

    /**
     * Returns the ID of the variable
     * @return the ID
     */
    int getUniqueID();

    /**
     * Throws a contradiction exception based on <cause, message>
     * @param cause ICause causing the exception
     * @param message the detailed message
     * @throws ContradictionException expected behavior
     */
    void contradiction(ICause cause, String message) throws ContradictionException;

    /**
     * Return the associated solver
     * @return a Solver object
     */
    Solver getSolver();

	/**
	 * @return an int representing the type of the variable
	 */
	int getType();
}
