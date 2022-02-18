/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;


import org.chocosolver.memory.structure.IOperation;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Identity;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Priority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.IntCircularQueue;
import org.chocosolver.util.objects.queues.CircularQueue;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.util.Arrays;
import java.util.function.Consumer;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static org.chocosolver.solver.constraints.PropagatorPriority.LINEAR;
import static org.chocosolver.solver.variables.events.IEventType.ALL_EVENTS;
import static org.chocosolver.solver.variables.events.PropagatorEventType.CUSTOM_PROPAGATION;
import static org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSetUtils.unionOf;


/**
 * A <code>Propagator</code> class defines methods to react on a <code>Variable</code> objects modifications.
 * It is observed by <code>Constraint</code> objects and can notify them when a <code>Variable</code> event occurs.
 * <br/>
 * Propagator methods are assumed to be idempotent, ie :
 * Let f be a propagator method, such that f : D -> D' include D, where D the union of variable domains involved in f.
 * Then, f(D)=f(D').
 * <p>
 * <br/>
 * A <code>Propagator</code> declares a filtering algorithm to apply to the <code>Variables</code> objects
 * in scope in order to reduce their <code>Domain</code> objects.
 * That's why the <code>propagate</code> method should be adapted to the expected filtering algorithm.
 * This method is called through <code>Constraint</code> observers when an event occurs on a scoped <code>Variable</code>
 * object. <code>propagate</code> method can throw a <code>ContradictionException</code>
 * when this <code>Propagator</code> object detects a contradiction, within its filtering algorithm, like domain wipe out,
 * out of domain value instantiation or other incoherence.
 * <br/>
 * Furthermore, a <code>Propagator</code> object can be <i>entailed</i> : considering the current state of its <code>Variable</code>
 * objects, the internal filtering algorithm becomes useless (for example: NEQ propagator and a couple of <code>Variable</code>
 * objects with disjoint domains). In other words, whatever are the future events occurring on <code>Variable</code> objects,
 * new calls to <code>propagate</code> method would be useless.
 * <br/>
 * <code>this</code> can be deactivated using the <code>setPassive</code>method.
 * It automatically informs <code>Constraint</code> observers of this new "state".
 * <p>
 * The developer of a propagator must respect some rules to create a efficient propagator:
 * <br/>- internal references to variables must be achieved referencing the <code>this.vars</code> after the call to super,
 * this prevents from wrong references when a variable occurs more than once in the scope (See {@link org.chocosolver.solver.constraints.nary.count.PropCount_AC} for instance).
 * <br/>- //to complete
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 * @version 0.01, june 2010
 * @see org.chocosolver.solver.variables.Variable
 * @see Constraint
 * @since 0.01
 * @param <V> type of variables involved in this propagator
 */
public abstract class Propagator<V extends Variable> implements ICause, Identity, Comparable<Propagator<V>> {

    /**
     * Status of this propagator on creation.
     */
    private static final short NEW = 0;

    /**
     * Status of this propagator when reified.
     */
    private static final short REIFIED = 1;

    /**
     * Status of the propagator when activated (ie, after initial propagation).
     */
    protected static final short ACTIVE = 2;

    /**
     * Status of the propagator when entailed.
     */
    private static final short PASSIVE = 3;

    /**
     * Ignore propagation during execution.
     */
    private boolean enabled = true;

    /**
     * For debugging purpose only, set to true to use default explanation schema, false to fail
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean DEFAULT_EXPL = true;
    /**
     * Set to true to output the name of the constraint that use the default explanation schema
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean OUTPUT_DEFAULT_EXPL = false;


    /**
     * Unique ID of this propagator.
     */
    private final int ID;

    /**
     * Current status of this propagator.
     * In: {@link #NEW}, {@link #REIFIED}, {@link #ACTIVE} and {@link #PASSIVE}.
     */
    protected short state = NEW;

    /**
     * Backtrackable operations to maintain the status on backtrack.
     */
    protected IOperation[] operations;

    /**
     * On propagator passivation, should this propagator be swapped from active to passive in its
     * variables' propagators list.
     */
    private final boolean swapOnPassivate;

    /**
     * When a propagator is removed while being passivate with swap operation,
     * this variable ensures that no side-effects occurs on backtrack
     */
    private boolean alive = true;

    /**
     * Priority of this propagator.
     * Mix between arity and compexity.
     */
    protected final Priority priority;

    /**
     * Set to <tt>true</tt> to indidates that this propagator reacts to fine event.
     * If set to <tt>false</tt>, the method {@link #propagate(int, int)} will never be called.
     */
    protected final boolean reactToFineEvt;

    /**
     * Encapsuling constraint.
     */
    protected Constraint constraint;

    /**
     * Reference to the model declaring this propagator.
     */
    protected final Model model;

    /**
     * List of variables this propagators deal with.
     * A variable can occur more than once, but it is considered then as n distinct variables.
     */
    protected V[] vars;

    /**
     * Index of this propagator within each variable's propagators.
     */
    private int[] vindices;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FOR PROPAGATION PURPOSE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * True if this is scheduled for propagation
     */
    private boolean scheduled;
    /**
     * This set of events (modified variables) to propagate next time
     */
    private IntCircularQueue eventsets;
    /**
     * This set of events' mask to propagate next time
     */
    private int[] eventmasks;
    /**
     * Position of this in the propgation engine
     */
    private int position = -1;

    /**
     * A bi-int-consumer
     */
    private interface IntIntConsumer{
        void accept(int a, int b);
    }

    /**
     * Default action to do on fine event : nothing
     */
    private IntIntConsumer fineevt = (i, m) -> {};

    /**
     * Denotes the reifying variable when this propagator is reified, null otherwise.
     */
    private BoolVar reifVar;

    /**
     * Creates a new propagator to filter the domains of vars.
     * <p>
     * <br/>
     * To limit memory consumption, the array of variables is <b>referenced directly</b> (no clone).
     * This is the responsibility of the propagator's developer to take care of that point.
     *
     * @param vars            variables of the propagator. Their modification will trigger
     *                        filtering
     * @param priority        priority of this propagator (lowest priority propagators are called
     *                        first)
     * @param reactToFineEvt  indicates whether or not this propagator must be informed of every
     *                        variable modification, i.e. if it should be incremental or not
     * @param swapOnPassivate indicates if, on propagator passivation, the propagator should be
     *                        ignored in its variables' propagators list.
     */
    protected Propagator(V[] vars, Priority priority, boolean reactToFineEvt, boolean swapOnPassivate) {
        assert vars != null && vars.length > 0 && vars[0] != null : "wrong variable set in propagator constructor";
        this.model = vars[0].getModel();
        this.reactToFineEvt = reactToFineEvt;
        this.priority = priority;
        // To avoid too much memory consumption, the array of variables is referenced directly, no clone anymore.
        // This is the responsibility of the propagator's developer to take care of that point.
        if (model.getSettings().cloneVariableArrayInPropagator()) {
            this.vars = vars.clone();
        } else {
            this.vars = vars;
        }
        this.vindices = new int[vars.length];
        Arrays.fill(vindices, -1);
        ID = model.nextId();
        this.swapOnPassivate = model.getSettings().swapOnPassivate() & swapOnPassivate;
        if (this.swapOnPassivate) {
            operations = new IOperation[3 + vars.length];
            for (int i = 0; i < vars.length; i++) {
                int i0 = i;
                operations[3 + i] = () -> {
                    if (alive) {
                        doSwap(i0);
                    }
                };
            }
        } else {
            operations = new IOperation[3];
        }

        operations[0] = () -> state = NEW;
        operations[1] = () -> state = REIFIED;
        operations[2] = () -> state = ACTIVE;

        // for propagation purpose
        eventmasks = new int[vars.length];
        if (reactToFineEvent()) {
            eventsets = new IntCircularQueue(vars.length);
            eventmasks = new int[vars.length];
            fineevt = (i, m) -> {
                if (eventmasks[i] == 0) {
                    eventsets.addLast(i);
                }
                eventmasks[i] |= m;
            };
        }
    }

    /**
     * Creates a new propagator to filter the domains of vars.
     * <p>
     * <br/>
     * To limit memory consumption, the array of variables is <b>referenced directly</b> (no clone).
     * This is the responsibility of the propagator's developer to take care of that point.
     *
     * @param vars           variables of the propagator. Their modification will trigger filtering
     * @param priority       priority of this propagator (lowest priority propagators are called
     *                       first)
     * @param reactToFineEvt indicates whether or not this propagator must be informed of every
     *                       variable modification, i.e. if it should be incremental or not
     */
    protected Propagator(V[] vars, Priority priority, boolean reactToFineEvt) {
        this(vars, priority, reactToFineEvt, false);
    }

    /**
     * Creates a non-incremental propagator which does not react to fine events but simply calls a
     * coarse propagation any time a variable in vars has changed.
     * This propagator has a regular (linear) priority.
     *
     * @param vars variables of the propagator. Their modification will trigger filtering
     */
    @SafeVarargs
    protected Propagator(V... vars) {
        this(vars, LINEAR, false);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    /**
     * Enlarges the variable scope of this propagator
     * Should not be called by the user.
     *
     * @param nvars variables to be added to this propagator
     */
    @SafeVarargs
    protected final void addVariable(V... nvars) {
        assert !swapOnPassivate:"Cannot add variable to a propagator that allows being swapped on passivate";
        V[] tmp = vars;
        vars = copyOf(vars, vars.length + nvars.length);
        arraycopy(nvars, 0, vars, tmp.length, nvars.length);
        int[] itmp = this.vindices;
        vindices = new int[vars.length];
        arraycopy(itmp, 0, vindices, 0, itmp.length);
        for (int v = tmp.length; v < vars.length; v++) {
            vindices[v] = vars[v].link(this, v);
        }
        if(reactToFineEvt) {
            itmp = this.eventmasks;
            eventmasks = new int[vars.length];
            arraycopy(itmp, 0, eventmasks, 0, itmp.length);
        }
        if (model.getSolver().getEngine().isInitialized()) {
            model.getSolver().getEngine().updateInvolvedVariables(this);
        }
    }

    /**
     * Creates links between this propagator and its variables.
     * The propagator will then be referenced in each of its variables.
     */
    public final void linkVariables() {
        for (int v = 0; v < vars.length; v++) {
            if (!vars[v].isAConstant()) {
                vindices[v] = vars[v].link(this, v);
            }
        }
    }

    /**
     * Destroy links between this propagator and its variables.
     */
    public final void unlinkVariables() {
        for (int v = 0; v < vars.length; v++) {
            if (!vars[v].isAConstant()) {
                vars[v].unlink(this, v);
                vindices[v] = -1;
                alive = false;
            }
        }
    }

    /**
     * Informs this propagator the (unique) constraint it filters.
     * The constraint reference will be overwritten in case of reification.
     * Should not be called by the user.
     *
     * @param c the constraint containing this propagator
     * @throws SolverException if the propagator is declared in more than one constraint
     */
    void defineIn(Constraint c) throws SolverException {
        if ((constraint != null && constraint.getStatus() != Constraint.Status.FREE)
                || (c.getStatus() != Constraint.Status.FREE)) {
            throw new SolverException("This propagator is already defined in a constraint. " +
                    "This happens when a constraint is reified and posted.");
        }
        this.constraint = c;
    }

    /**
     * Returns the specific mask indicating the <b>variable events</b> on which this <code>Propagator</code> object can react.<br/>
     * A mask is a bitwise OR operations over {@link IEventType} this can react on.
     *
     * For example, consider a propagator that can deduce filtering based on the lower bound of the integer variable X.
     * Then, for this variable, the mask should be equal to :
     * <pre>
     *     int mask = IntEventType.INCLOW.getMask() | IntEventType.INSTANTIATE.getMask();
     * </pre>
     * or, in a more convenient way:
     * <pre>
     *     int mask = IntEvtType.combine(IntEventType.INCLOW,IntEventType.INSTANTIATE);
     * </pre>
     *
     * That indicates the following behavior:
     * <ol>
     *     <li>if X is instantiated, this propagator will be executed,</li>
     *     <li>if the lower bound of X is modified, this propagator will be executed,</li>
     *     <li>if the lower bound of X is removed, the event is promoted from REMOVE to INCLOW and this propagator will NOT be executed,</li>
     *     <li>otherwise, this propagator will NOT be executed</li>
     * </ol>
     *
     * Some combinations are valid.
     * For example, a propagator which reacts on REMOVE and INSTANTIATE should also declare INCLOW and DECUPP as conditions.
     * Indeed INCLOW (resp. DECUPP), for efficiency purpose, removing the lower bound (resp. upper bound) of an integer variable
     * will automatically be <i>promoted</i> into INCLOW (resp. DECUPP).
     * So, ignoring INCLOW and/or DECUPP in that case may result in a lack of filtering.
     *
     * The same goes with events of other variable types, but most of the time, there are only few combinations.
     *
     * Reacts to any kind of event by default.
     *
     * Alternatively, this method can return {@link IntEventType#VOID} which states
     * that this propagator should not be aware of modifications applied to the variable in position <i>vIdx</i>.
     *
     * @param vIdx index of the variable within the propagator
     * @return an int composed of <code>REMOVE</code> and/or <code>INSTANTIATE</code>
     * and/or <code>DECUPP</code> and/or <code>INCLOW</code>
     */
    public int getPropagationConditions(int vIdx) {
        return ALL_EVENTS;
    }

    /**
     * Call the main filtering algorithm to apply to the <code>Domain</code> of the <code>Variable</code> objects.
     * It considers the current state of this objects to remove some values from domains and/or instantiate some variables.
     * Calling this method is done from 2 (and only 2) steps:
     * <br/>- at the initial propagation step,
     * <br/>- when involved in a reified constraint.
     * <br/>
     * It should initialized the internal data structure and apply filtering algorithm from scratch.
     *
     * @param evtmask type of propagation event <code>this</code> must consider.
     * @throws org.chocosolver.solver.exception.ContradictionException when a contradiction occurs, like domain wipe out or other incoherencies.
     */
    public abstract void propagate(int evtmask) throws ContradictionException;

    /**
     * Incremental filtering algorithm defined within the <code>Propagator</code>, called whenever the variable
     * of index idxVarInProp has changed. This method calls a CUSTOM_PROPAGATION (coarse-grained) by default.
     * <p>
     * This method should be overridden if the argument <code>reactToFineEvt</code> is set to <code>true</code> in the constructor.
     * Otherwise, it executes <code>propagate(PropagatorEventType.CUSTOM_PROPAGATION.getStrengthenedMask());</code>
     *
     * @param idxVarInProp index of the variable <code>var</code> in <code>this</code>
     * @param mask         type of event
     * @throws org.chocosolver.solver.exception.ContradictionException if a contradiction occurs
     */
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (reactToFineEvt) {
            throw new SolverException(this + " has been declared to ignore which variable is modified.\n" +
                    "To change the configuration, consider:\n" +
                    "- to set 'reactToFineEvt' to false or,\n" +
                    "- to override the following method:\n" +
                    "\t'public void propagate(int idxVarInProp, int mask) throws ContradictionException'." +
                    "The latter enables incrementality but also to delay calls to complex filtering algorithm (see the method 'forcePropagate(EventType evt)'.");
        }
        propagate(CUSTOM_PROPAGATION.getMask());
    }

    /**
     * Schedules a coarse propagation to filter all variables at once.
     * <p>
     * Add the coarse event recorder into the engine
     *
     * @param evt event type
     * @throws ContradictionException if the propagation encounters inconsistency.
     */
    public final void forcePropagate(PropagatorEventType evt) throws ContradictionException {
        model.getSolver().getEngine().delayedPropagation(this, evt);
    }

    /**
     * informs that this propagator is now active. Should not be called by the user.
     * @throws SolverException if the propagator cannot be activated due to its current state
     */
    public void setActive() throws SolverException {
        if (isStateLess()) {
            state = ACTIVE;
            model.getEnvironment().save(operations[NEW]);
        } else {
            throw new SolverException("Try to activate a propagator already active, passive or reified.\n" +
                    this + " of " + this.getConstraint());
        }
    }

    protected void setActive0() {
        state = ACTIVE;
    }

    /**
     * informs that this reified propagator must hold. Should not be called by the user.
     * @throws SolverException if the propagator cannot be activated due to its current state
     */
    public void setReifiedTrue() throws SolverException {
        if (isReifiedAndSilent()) {
            state = ACTIVE;
            model.getEnvironment().save(operations[REIFIED]);
        } else {
            throw new SolverException("Reification process tries to force activation of a propagator already active or passive.\n" +
                    this + " of " + this.getConstraint());
        }
    }

    /**
     * informs that this reified propagator may not hold. Should not be called by the user.
     * @param boolVar the reifying variable
     * @throws SolverException if the propagator cannot be reified due to its current state
     */
    public void setReifiedSilent(BoolVar boolVar) throws SolverException {
        if (isStateLess() || isReifiedAndSilent()) {
            state = REIFIED;
            this.reifVar = boolVar;
        } else {
            throw new SolverException("Reification process try to reify a propagator already active or posted.\n" +
                    this + " of " + this.getConstraint());
        }
    }

    /**
     * informs that this propagator is now passive : it holds but no further filtering can occur,
     * so it is useless to propagate it. Should not be called by the user.
     * @throws SolverException if the propagator cannot be set passive due to its current state
     */
    public void setPassive() throws SolverException {
        // Note: calling isCompletelyInstantiated() to avoid next steps may lead to error when
        // dealing with reification and dynamic addition.
        if (isActive()) {
            state = PASSIVE;
            model.getEnvironment().save(operations[ACTIVE]);
            //TODO: update var mask back
            model.getSolver().getEngine().desactivatePropagator(this);
            if (swapOnPassivate) {
                for (int i = 0; i < vars.length; i++) {
                    if (!vars[i].isInstantiated()) {
                        vindices[i] = vars[i].swapOnPassivate(this, i);
                        assert vars[i].getPropagator(vindices[i]) == this;
                        model.getEnvironment().save(operations[3 + i]);
                    }
                }
            }
        } else {
            throw new SolverException("Try to passivate a propagator already passive or reified.\n" +
                    this + " of " + this.getConstraint());
        }
    }

    private void doSwap(int i0){
        vindices[i0] = vars[i0].swapOnActivate(this, i0);
    }

    /**
     * Call this method when either the propagator has to be awake on backtrack.
     * This is helpful when:
     * <ul>
     *     <li>the scope of this propagator has changed on failures or solutions (eg. learning clauses)</li>
     *     <li>this propagator's internal structure has changed (eg. this acts as a cut)</li>
     * </ul>
     */
    protected void forcePropagationOnBacktrack() {
        if (isPassive()) { // force activation on backtrack, because something can have changed on our back
            state = ACTIVE;
        }
        model.getSolver().getEngine().propagateOnBacktrack(this);
    }

    /**
     * Check wether <code>this</code> is entailed according to the current state of its internal structure.
     * At least, should check the satisfaction of <code>this</code> (when all is instantiated).
     *
     * @return ESat.TRUE if entailed, ESat.FALSE if not entailed, ESat.UNDEFINED if unknown
     */
    public abstract ESat isEntailed();

    /**
     * @return true iff all this propagator's variables are instantiated
     */
    public boolean isCompletelyInstantiated() {
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].isInstantiated()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the number of uninstantiated variables
     */
    public int arity() {
        int arity = 0;
        for (int i = 0; i < vars.length; i++) {
            arity += vars[i].isInstantiated() ? 0 : 1;
        }
        return arity;
    }

    /**
     * Return the dynamic priority of this propagator.
     * It excludes from the arity variables instantiated.
     * But may be time consuming.
     * @return a more accurate priority excluding instantiated variables.
     */
    @SuppressWarnings("unused")
    public int dynPriority() {
        int arity = 0;
        for (int i = 0; i < vars.length && arity <= 3; i++) {
            arity += vars[i].isInstantiated() ? 0 : 1;
        }
        if (arity > 3) {
            return priority.getValue();
        } else return arity;
    }

    /**
     * Throws a contradiction exception
     *
     * @throws org.chocosolver.solver.exception.ContradictionException expected behavior
     */
    public void fails() throws ContradictionException {
        model.getSolver().throwsException(this, null, null);
    }

    @Override
    public int compareTo(Propagator o) {
        return this.ID - o.ID;
    }

    /**
     * @return the boolean variable that reifies this propagator, null otherwise.
     */
    public BoolVar reifiedWith() {
        return reifVar;
    }

    /**
     * @return <i>true</i> if this is reified.
     * Call {@link #reifiedWith()} to get the reifying variable.
     */
    public boolean isReified(){
        return reifVar != null;
    }

    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    @Override
    public int getId() {
        return ID;
    }

    /**
     * @return the model this propagator is defined in
     */
    public Model getModel() {
        return model;
    }

    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Propagator<?> && ((Propagator<?>) o).ID == ID;
    }

    /**
     * Returns the element at the specified position in this internal list of <code>V</code> objects.
     *
     * @param i index of the element
     * @return a <code>V</code> object
     */
    public final V getVar(int i) {
        return vars[i];
    }

    /**
     * @return the variable set this propagator holds on.
     * Note that variable multiple occurrence may have lead to variable duplications
     * (i.e. the creation of new variable)
     */
    public final V[] getVars() {
        return vars;
    }

    /**
     * @return the index of the propagator within its variables
     */
    @SuppressWarnings("unused")
    public int[] getVIndices() {
        return vindices;
    }

    /**
     * @return the index of the propagator within its idx^th variable
     */
    @SuppressWarnings("unused")
    public int getVIndice(int idx) {
        return vindices[idx];
    }

    /**
     * Changes the index of a variable in this propagator.
     * This method should not be called by the user.
     *
     * @param idx old index
     * @param val new index
     */
    @SuppressWarnings("unused")
    public void setVIndices(int idx, int val) {
        vindices[idx] = val;
    }

    /**
     * @return the number of variables involved in <code>this</code>.
     */
    public final int getNbVars() {
        return vars.length;
    }

    /**
     * @return the constraint including this propagator
     */
    public final Constraint getConstraint() {
        return constraint;
    }

    /**
     * @return the priority of this propagator (may influence the order in which propagators are called)
     */
    public final Priority getPriority() {
        return priority;
    }

    /**
     * @return true iff this propagator is stateless: its initial propagation has not been performed yet
     */
    public boolean isStateLess() {
        return state == NEW;
    }

    /**
     * @return true iff this propagator is reified and it is not established yet whether it should hold or not
     */
    public boolean isReifiedAndSilent() {
        return state == REIFIED;
    }

    /**
     * The propagator is active if the state is ACTIVE and the constraint related to it is enabled.
     * The constraint is disabled to allow faster execution of algorithms like {@link org.chocosolver.solver.QuickXPlain}.
     *
     * @return true iff this propagator is active (it should filter)
     */
    public boolean isActive() {
        return state == ACTIVE && enabled;
    }

    /**
     * @return true iff this propagator is passive. This happens when it is entailed : the propagator still hold
     * but no more filtering can occur
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isPassive() {
        return state == PASSIVE;
    }

    /**
     * @return true iff the propagator reacts to fine event, that is,
     * it needs to know which variable has been modified and the modification that happened.
     */
    public final boolean reactToFineEvent() {
        return reactToFineEvt;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append(getClass().getSimpleName()).append("(");
        int i = 0;
        if (vars.length >= 3) st.append(vars[i++].getName()).append(", ");
        if (vars.length >= 2) st.append(vars[i++].getName()).append(", ");
        if (vars.length >= 1) st.append(vars[i++].getName());
        if (i < vars.length) {
            if (vars.length > 4) {
                st.append(", ...");
            }
            st.append(", ").append(vars[vars.length - 1].getName());
        }
        st.append(')');

        return st.toString();
    }

    /**
     * @implSpec
     * Based on the scope of this propagator, domains of variables are extracted as they
     * were just before propagation that leads to node <i>p</i>.
     * <p>
     *     Consider that v_1 has been modified by propagation of this.
     *     Before the propagation, the domains were like:
     * <pre>
     *         (v1 &isin; D1 &and; v2 &isin; D2 &and; .... &and; vn &isin; D_n)
     *     </pre>
     * Then this propagates v1 &isin; D1', then:
     * <pre>
     *         (v1 &isin; D1 &and; v2 &isin; D2 &and; .... &and; vn &isin; D_n) &rarr; v1 &isin; D1'
     *     </pre>
     * Converting to DNF:
     * <pre>
     *         (v1 &isin; (U \ D1) &cup; D'1  &or; v2 &isin; (U \ D2) &or; .... &or; vn &isin; (U \ Dn))
     *     </pre>
     * </p>
     */
    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        if (DEFAULT_EXPL) {
            if(OUTPUT_DEFAULT_EXPL)model.getSolver().log().bold().printf("-- default explain for %s \n",this.getClass().getSimpleName());
            defaultExplain(this, p, explanation);
        } else {
            ICause.super.explain(p, explanation);
        }
    }

    public static void defaultExplain(Propagator<?> prop, int p, ExplanationForSignedClause explanation) {
        IntVar pivot = p > -1 ? explanation.readVar(p) : null;
        IntIterableRangeSet dom;
        IntVar var;
        boolean found = false;
        for (int i = 0; i < prop.vars.length; i++) {
            var = (IntVar) prop.vars[i];
            if (var == pivot) {
                if (!found) {
                    dom = explanation.complement(var);
                    // when a variable appears more than once AND is pivot : should be treated only once
                    unionOf(dom, explanation.readDom(p));
                    found = true;
                    var.intersectLit(dom, explanation);
                }
            }else{
                var.unionLit(explanation.complement(var), explanation);
            }
        }
        assert found || p == -1 : pivot + " not declared in scope of " + prop;
    }

    @Override
    public void forEachIntVar(Consumer<IntVar> action) {
        for (int i = 0; i < vars.length; i++) {
            action.accept((IntVar) vars[i]);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FOR PROPAGATION PURPOSE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return the position of this in the propagation engine
     */
    public int getPosition(){
        return position;
    }

    /**
     * Set the position of this in the propagation engine or -1 if removed.
     * @param p position of this in the propagation engine or -1 if removed.
     */
    public void setPosition(int p){
        this.position = p;
    }

    /**
     * Set this as unscheduled
     */
    public final void unschedule(){
        scheduled = false;
    }

    private void schedule(){
        scheduled = true;
    }

    /**
     * @return true if scheduled for propagation
     */
    public final boolean isScheduled() {
        return scheduled;
    }


    /**
     * Apply scheduling instruction
     * @param queues array of queues in which this can be scheduled
     * @return propagator priority
     */
    public int doSchedule(CircularQueue<Propagator<?>>[] queues){
        int prio = priority.getValue();
        if(!scheduled) {
            queues[prio].addLast(this);
            schedule();
        }
        return prio;
    }

    public void doScheduleEvent(int pindice, int mask){
        fineevt.accept(pindice, mask);
    }

    /**
     * Apply fine event propagation of this.
     * It iterates over pending modified variables and run propagation on each of them.
     * @throws ContradictionException if a contradiction occurred.
     */
    public void doFinePropagation() throws ContradictionException {
        while (eventsets.size() > 0) {
            int v = eventsets.pollFirst();
            assert isActive() : "propagator is not active:" + this;
            // clear event
            int mask = eventmasks[v];
            eventmasks[v] = 0;
            // run propagation on the specific event
            propagate(v, mask);
        }
    }

    /**
     * Flush pending events
     */
    public void doFlush(){
        if (reactToFineEvent()) {
            while (eventsets.size() > 0) {
                int v = eventsets.pollLast();
                eventmasks[v] = 0;
            }
        }
        unschedule();
    }

    /**
     * Disable a propagator from being propagated during search and from feasibility
     * check ({@link org.chocosolver.solver.Solver#isSatisfied()}). A propagator
     * shouldn't swap between enabled/disabled during solver execution (branching,
     * filtering, etc...) because there is not control of the side effects it can
     * cause (e.g.: when at node n, if a propagator becomes disabled, it doesn't
     * undo filtering it has done at n-1).
     * See {@link Constraint#setEnabled(boolean)}
     *
     * @param enabled is this propagator enabled?
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
