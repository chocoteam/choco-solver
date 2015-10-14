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
import org.chocosolver.solver.Identity;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.delta.IDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.IView;
import org.chocosolver.util.iterators.EvtScheduler;

import java.io.Serializable;

/**
 *
 * To developers: any constructor of variable must pass in parameter
 * the back-end ISolver, that is, in decreasing order:
 * - the solver portfolio,
 * - the solver (or portfolio workers but fes).
 * Created by IntelliJ IDEA.
 * User: xlorca, Charles Prud'homme
 */
public interface Variable extends Identity, Serializable, Comparable<Variable> {

    // **** DEFINE THE TYPE OF A VARIABLE **** //
    // MUST BE A COMBINATION OF TYPE AND KIND
    // TYPE (exclusive)
    int VAR = 1;
    int CSTE = 1 << 1;
    int VIEW = 1 << 2;
    int TYPE = (1 << 3) - 1;
    // KIND (exclusive)
    int INT = 1 << 3;
    int BOOL = INT | (1 << 4);
    int SET = 1 << 5;
    int REAL = 1 << 6;
    int KIND = (1 << 10) - 1 - TYPE;

    /**
     * Indicates whether <code>this</code> is instantiated (see implemtations to know what instantiation means).
     *
     * @return <code>true</code> if <code>this</code> is instantiated
     */
    boolean isInstantiated();

    /**
     * Returns the name of <code>this</code>
     *
     * @return a String representing the name of <code>this</code>
     */
    String getName();

    /**
     * Return the array of propagators this
     *
     * @return the array of proapgators of this
     */
    Propagator[] getPropagators();

    /**
     * Return the "idx" th propagator of this
     *
     * @param idx position of the propagator
     * @return a propagator
     */
    Propagator getPropagator(int idx);

    /**
     * Return the number of propagators
     *
     * @return number of propagators of this
     */
    int getNbProps();

    int[] getPIndices();

    int getDindex(int i);

    /**
     * Return the position of the variable in the propagator at position pidx
     *
     * @param pidx index of the propagator within the list of propagators of this
     * @return position of this in the propagator pidx
     */
    int getIndexInPropagator(int pidx);

    /**
     * Build and add a monitor to the monitor list of <code>this</code>.
     * The monitor is inactive at the creation and must be activated (by the engine propagation).
     *
     * @param monitor a variable monitor
     */
    void addMonitor(IVariableMonitor monitor);

    //todo : to complete
    void removeMonitor(IVariableMonitor monitor);

    void subscribeView(IView view);

    /**
     * Return the delta domain of this
     *
     * @return the delta domain of the variable
     */
    IDelta getDelta();

    /**
     * Create a delta, if necessary, in order to observe removed values of a this.
     * If the delta already exists, has no effect.
     */
    void createDelta();

    /**
     * Link the propagator to this
     *
     * @param propagator a newly added propagator
     * @param idxInProp  index of the variable in the propagator
     * @return return the index of the propagator within the variable
     */
    int link(Propagator propagator, int idxInProp);

    /**
     * Analysis propagator event reaction on this, and adapt this
     *
     * @param mask event mask
     */
    void recordMask(int mask);

    /**
     * Remove a propagator from the list of propagator of <code>this</code>.
     * SHOULD BE CONTAINED IN THIS.
     *
     * @param propagator the propagator to remove
     *
     */
    void unlink(Propagator propagator);

    /**
     * If <code>this</code> has changed, then notify all of its observers.<br/>
     * Each observer has its update method.
     *
     * @param event event on this object
     * @param cause object which leads to the modification of this object
     * @throws ContradictionException
     *          if a contradiction occurs during notification
     */
    void notifyPropagators(IEventType event, ICause cause) throws ContradictionException;

    /**
     * Notify views of observed variable modifications
     *
     * @param event the event which occurred on the variable
     * @throws ContradictionException
     */
    void notifyViews(IEventType event, ICause cause) throws ContradictionException;

	/**
	 * Get the views observing this variables
	 * @return views observing this variables
	 */
	IView[] getViews();

    /**
     * Notify monitors of observed variable modifications
     *
     * @param event the event which occurred on the variable
     * @throws ContradictionException
     */
    void notifyMonitors(IEventType event) throws ContradictionException;

    /**
     * Throws a contradiction exception based on <cause, message>
     * @param cause   ICause causing the exception
     * @param message the detailed message  @throws ContradictionException expected behavior
     */
    void contradiction(ICause cause, String message) throws ContradictionException;

    /**
     * Return the associated solver
     *
     * @return a Solver object
     */
    Solver getSolver();

    /**
     * Return a MASK composed of 2 main information: TYPE and KIND.
     * <br/>TYPE is defined in the 3 first bits : VAR ( 1 << 0), CSTE (1 << 1) or VIEW (1 << 2)
     * <br/>KIND is defined on the other bits : INT (1 << 3), BOOL (INT + 1 << 4), GRAPH (1 << 5) or META (1 << 6)
     * <p/>
     * <p/>
     * To get the TYPE of a variable: </br>
     * <pre>
     * int type = var.getTypeAndKind() & Variable.TYPE;
     * </pre>
     * <p/>
     * To get the KIND of a variable: </br>
     * <pre>
     * int kind = var.getTypeAndKind() & Variable.KIND;
     * </pre>
     * <p/>
     * To check a specific type or kind of a variable: </br>
     * <pre>
     *     boolean isVar = (var.getTypeAndKind() & Variable.VAR) !=0;
     *     boolean isInt = (var.getTypeAndKind() & Variable.INT) !=0;
     * </pre>
     *
     * @return an int representing the type and kind of the variable
     */
    int getTypeAndKind();

    /**
     * Duplicate <code>this</code>.
     *
     * @param <V> the copy
     * @return a copy of <code>this</code>
     */
    <V extends Variable> V duplicate();

    /**
     * For scheduling purpose only
     */
    EvtScheduler _schedIter();
}
