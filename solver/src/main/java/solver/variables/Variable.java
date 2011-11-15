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

import choco.kernel.common.util.objects.IList;
import com.sun.istack.internal.NotNull;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.requests.IRequest;
import solver.variables.delta.IDelta;
import solver.variables.view.IView;

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

    /**
     * Build and add a request to the request list of <code>this</code>.
     * The request is inactive at the creation and must be activated (by the engine propagation).
     * @param request
     */
    void addRequest(IRequest request);

    /**
     * Activate a request
     * @param request
     */
    void activate(IRequest request);

    /**
     * Desactivate a request, the request is reactivate upon backtracking.
     * @param request
     */
    void desactivate(IRequest request);

    //todo : to complete
    void deleteRequest(IRequest request);

    IList getRequests();

    int nbRequests();

    void subscribeView(IView view);

    /**
     * Returns the number of constraints involving <code>this</code>
     * TODO: MostConstrained: count requests instead of constraints
     *
     * @return the number of constraints of <code>this</code>
     */
    int nbConstraints();


    /**
     * returns an explanation of the current state of the Variable
     *
     * @param what specifies what we are interested in
     * @return
     */

    Explanation explain(VariableState what);
    Explanation explain(VariableState what, int val);

    D getDelta();

    /**
     * Regarding the propagator in parameter, update the propagation conditions of the variable
     * @param propagator a newly added propagator
     * @param idxInProp index of the variable in the propagator
     */
    void updatePropagationConditions(Propagator propagator, int idxInProp);

    /**
     * Deletes an observers from the set of observers for this object.
     * @param observer the observer to delete
     */
    void deletePropagator(Propagator observer);

    /**
     * attach a propagator to the variable
     * @param propagator a propagator
     * @param idxInProp index of the variable within the propagator
     */
    void attachPropagator(Propagator propagator, int idxInProp);

    /**
     * If <code>this</code> has changed, then notify all of its observers.<br/>
     * Each observer has its update method.
     *
     * @param event event on this object
     * @param cause object which leads to the modification of this object
     * @throws solver.exception.ContradictionException if a contradiction occurs during notification
     */
    void notifyMonitors(EventType event, @NotNull ICause cause) throws ContradictionException;


    void notifyViews(EventType event, @NotNull ICause cause) throws ContradictionException;

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
     *
     * @param cause ICause causing the exception
     * @param event
     *@param message the detailed message  @throws ContradictionException expected behavior
     */
    void contradiction(@NotNull ICause cause, EventType event, String message) throws ContradictionException;

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
