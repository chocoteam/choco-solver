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

import com.sun.istack.internal.NotNull;
import solver.ICause;
import solver.Identity;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.delta.IDelta;
import solver.variables.delta.IDeltaMonitor;
import solver.variables.view.IView;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: xlorca
 */
public interface Variable<D extends IDelta, DM extends IDeltaMonitor<D>,W extends IView> extends Identity, Serializable {

    // **** DEFINE THE TYPE OF A VARIABLE **** //
    // MUST BE A COMBINATION OF TYPE AND KIND
    // TYPE (exclusive)
    public final int VAR = 1;
    public final int CSTE = 1 << 1;
    public final int VIEW = 1 << 2;
    public final int TYPE = (1 << 3) - 1;
    // KIND (exclusive)
    public final int INT = 1 << 3;
    public final int BOOL = INT + 1 << 4;
    public final int GRAPH = 1 << 5;
    public final int SET = 1 << 6;
    public final int REAL = 1 << 7;
    public final int TASK = 1 << 8;
    public final int META = 1 << 9;
    public final int KIND = (1 << 9) - 1 - TYPE;

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
     * Returns the array of constraints <code>this</code> appears in.
     *
     * @return array of constraints
     */
    Constraint[] getConstraints();

    /**
     * Link a constraint within a variable
     *
     * @param constraint a constraint
     */
    void declareIn(Constraint constraint);

    /**
     * Return the arrau of propagators this
     *
     * @return
     */
    Propagator[] getPropagators();

    int[] getPIndices();

    W[] getViews();

    /**
     * Build and add a monitor to the monitor list of <code>this</code>.
     * The monitor is inactive at the creation and must be activated (by the engine propagation).
     *
     * @param monitor a variable monitor
     */
    void addMonitor(IVariableMonitor monitor);

    //todo : to complete
    void removeMonitor(IVariableMonitor monitor);

    void subscribeView(W view);

    /**
     * Returns the number of constraints involving <code>this</code>
     * TODO: MostConstrained: count monitors instead of constraints
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

    /**
     * Return the delta domain of this
     * @return
     */
    D getDelta();

    /**
     * Create a delta, if necessary, in order to observe removed values of a this.
     * If the delta already exists, has no effect.
     */
    void createDelta();

    /**
     * Allow to monitor removed values of <code>this</code>.
     * @param propagator the cause that requires to monitor delta
     * @return a delta monitor
     */
    DM monitorDelta(ICause propagator);

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
     * @param mask
     */
    void recordMask(int mask);

    /**
     * Remove a propagator from the list of propagator of <code>this</code>.
     * SHOULD BE CONTAINED IN THIS.
     *
     * @param propagator the propagator to remove
     */
    void unlink(Propagator propagator);

    /**
     * If <code>this</code> has changed, then notify all of its observers.<br/>
     * Each observer has its update method.
     *
     * @param event event on this object
     * @param cause object which leads to the modification of this object
     * @throws solver.exception.ContradictionException
     *          if a contradiction occurs during notification
     */
    void notifyPropagators(EventType event, @NotNull ICause cause) throws ContradictionException;

    void notifyViews(EventType event, @NotNull ICause cause) throws ContradictionException;

    void notifyMonitors(EventType event, @NotNull ICause cause) throws ContradictionException;

    /**
     * Throws a contradiction exception based on <cause, message>
     *
     * @param cause   ICause causing the exception
     * @param event
     * @param message the detailed message  @throws ContradictionException expected behavior
     */
    void contradiction(@NotNull ICause cause, EventType event, String message) throws ContradictionException;

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
}
