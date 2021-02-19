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

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Identity;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.delta.IDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.IView;
import org.chocosolver.util.iterators.EvtScheduler;

/**
 *
 * To developers: any constructor of variable must pass in parameter
 * the back-end ISolver, that is, in decreasing order:
 * - the model portfolio,
 * - the model (or portfolio workers but fes).
 * Created by IntelliJ IDEA.
 * User: xlorca, Charles Prud'homme
 */
public interface Variable extends Identity, Comparable<Variable> {

    /**
     * Type of variable: variable (unique).
     */
    int VAR = 1;

    /**
     * Type of variable: fixed (unique).
     */
    int CSTE = 1 << 1;

    /**
     * Type of variable: view (unique).
     */
    int VIEW = 1 << 2;

    /**
     * Mask to get the type of a variable.
     */
    int TYPE = (1 << 3) - 1;

    /**
     * Kind of variable: integer (unique).
     */
    int INT = 1 << 3;

    /**
     * Kind of variable: boolean and integer too (unique).
     */
    int BOOL = INT | (1 << 4);

    /**
     * Kind of variable: set.
     */
    int SET = 1 << 5;

    /**
     * Kind of variable: real.
     */
    int REAL = 1 << 6;

    /**
     * Mask to get the kind of a variable.
     */
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
    @SuppressWarnings("unused")
    Propagator getPropagator(int idx);

    /**
     * Return the number of propagators
     *
     * @return number of propagators of this
     */
    int getNbProps();

    /**
     * @return the array of indices of this variable in its propagators.
     */
    int[] getPIndices();

    /**
     * Update the position of the variable in the propagator at position in {@link #getPropagators()}.
     * @param pos position of the propagator
     * @param val position of this variable in the propagator
     */
    void setPIndice(int pos, int val);

    /**
     * This variable's propagators are stored in specific way which ease iteration based on propagation conditions.
     * Any event indicates, through the <i>dependency list</i> which propagators should be executed.
     * Thus, an event indicates a list of <code>i</code>s, passed as parameter, which help returning the right propagators.
     * @param i dependency index
     * @return index of the first propagator associated with that dependency.
     */
    int getDindex(int i);

    /**
     * Return the position of the variable in the propagator at position pidx
     *
     * @param pidx index of the propagator within the list of propagators of this
     * @return position of this in the propagator pidx
     */
    @SuppressWarnings("unused")
    int getIndexInPropagator(int pidx);

    /**
     * Build and add a monitor to the monitor list of <code>this</code>.
     * The monitor is inactive at the creation and must be activated (by the engine propagation).
     *
     * @param monitor a variable monitor
     */
    void addMonitor(IVariableMonitor monitor);

    /**
     * Removes <code>monitor</code> form the list of this variable's monitors.
     * @param monitor the monitor to remove.
     */
    @SuppressWarnings("unused")
    void removeMonitor(IVariableMonitor monitor);

    /**
     * Attaches a view to this variable.
     * @param view a view to add to this variable.
     */
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
     * The propagator will not be informed of any modification of this anymore.
     *
     * @param propagator the propagator to swap
     * @param idxInProp  index of the variable in the propagator
     * @return return the index of the propagator within the variable
     */
    int swapOnPassivate(Propagator propagator, int idxInProp);

    /**
     * The propagator will be informed back of any modification of this.
     *
     * @param propagator the propagator to swap
     * @param idxInProp  index of the variable in the propagator
     * @return return the index of the propagator within the variable
     */
    int swapOnActivate(Propagator propagator, int idxInProp);

    /**
     * Remove a propagator from the list of propagator of <code>this</code>.
     * SHOULD BE CONTAINED IN THIS.
     *
     * @param propagator the propagator to remove
     * @param idxInProp  index of the variable in the propagator
     *
     */
    void unlink(Propagator propagator, int idxInProp);


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
     * @param cause the cause of the notification
     * @throws ContradictionException if the notification detects contradiction.
     */
    void notifyViews(IEventType event, ICause cause) throws ContradictionException;

    /**
     * @return the number of views attached to the variable
     */
    int getNbViews();

	/**
	 * Get the view at position <i>p</i> in this variable views.
     * The array is filled from position 0 to position {@link #getNbViews()} excluded.
     * @param p position of the view to return
	 * @return view observing this variable, at position <i>p</i>
	 */
	IView getView(int p);

    /**
     * Notify monitors of observed variable modifications
     *
     * @param event the event which occurred on the variable
     * @throws ContradictionException if the monitor detects contradiction.
     */
    void notifyMonitors(IEventType event) throws ContradictionException;

    /**
     * Throws a contradiction exception based on <cause, message>
     * @param cause   ICause causing the exception
     * @param message the detailed message  @throws ContradictionException expected behavior
     * @throws ContradictionException the build contradiction.
     */
    void contradiction(ICause cause, String message) throws ContradictionException;

    /**
     * Return the associated model
     *
     * @return a Model object
     */
    Model getModel();

    /**
     * @return the backtracking environment used for this variable
     */
    default IEnvironment getEnvironment(){
        return getModel().getEnvironment();
    }

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
     * @return true iff the variable is a constant (created with a singleton domain)
     */
    boolean isAConstant();

    /**
     * For scheduling purpose only
     * @return the scheduler
     */
    EvtScheduler getEvtScheduler();

    /**
     * @return this cast into an IntVar.
     * @throws ClassCastException if type is not compatible
     */
    IntVar asIntVar();

    /**
     * @return this cast into an BoolVar.
     * @throws ClassCastException if type is not compatible
     */
    BoolVar asBoolVar();

    /**
     * @return this cast into an RealVar.
     * @throws ClassCastException if type is not compatible
     */
    RealVar asRealVar();

    /**
     * @return this cast into an SetVar.
     * @throws ClassCastException if type is not compatible
     */
    SetVar asSetVar();

    /**
     * Temporarily store modification events made on this.
     * This is requiered by the propagation engine.
     * @param mask event's mask
     * @param cause what causes the modification (cannot be null)
     */
    void storeEvents(int mask, ICause cause);

    /**
     * Clear events stored temporarily by {@link #storeEvents(int, ICause)}
     */
    void clearEvents();

    /**
     * @return possibly aggregated mask stored through by {@link #storeEvents(int, ICause)}
     */
    int getMask();

    /**
     * @return cause stored through by {@link #storeEvents(int, ICause)} or {@link org.chocosolver.solver.Cause#Null}
     * if differents causes modified this variable (this may happen when a view refers to this).
     */
    ICause getCause();
}
