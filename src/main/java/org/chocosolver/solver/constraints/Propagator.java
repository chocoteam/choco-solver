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
package org.chocosolver.solver.constraints;


import org.chocosolver.memory.structure.IOperation;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Identity;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.propagation.NoPropagationEngine;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static org.chocosolver.solver.constraints.PropagatorPriority.LINEAR;
import static org.chocosolver.solver.variables.events.IEventType.ALL_EVENTS;
import static org.chocosolver.solver.variables.events.PropagatorEventType.CUSTOM_PROPAGATION;


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
public abstract class Propagator<V extends Variable> implements ICause, Identity, Comparable<Propagator> {

    /**
     * Status of this propagator on creation.
     */
    protected static final short NEW = 0;

    /**
     * Status of this propagagator when reified.
     */
    protected static final short REIFIED = 1;

    /**
     * Status of the propagator when activated (ie, after initial propagation).
     */
    protected static final short ACTIVE = 2;

    /**
     * Status of the propagator when entailed.
     */
    protected static final short PASSIVE = 3;

    /**
     * Unique ID of this propagator.
     */
    private final int ID;

    /**
     * Current status of this propagator.
     * In: {@link #NEW}, {@link #REIFIED}, {@link #ACTIVE} and {@link #PASSIVE}.
     */
    private short state;

    /**
     * Backtrackable operations to maintain the status on backtrack.
     */
    private IOperation[] operations;

    /**
     * Priority of this propagator.
     * Mix between arity and compexity.
     */
    protected final PropagatorPriority priority;

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

    /**
     * Creates a new propagator to filter the domains of vars.
     * <p>
     * <br/>
     * To limit memory consumption, the array of variables is <b>referenced directly</b> (no clone).
     * This is the responsibility of the propagator's developer to take care of that point.
     *
     * @param vars           variables of the propagator. Their modification will trigger filtering
     * @param priority       priority of this propagator (lowest priority propagators are called first)
     * @param reactToFineEvt indicates whether or not this propagator must be informed of every variable
     *                       modification, i.e. if it should be incremental or not
     */
    protected Propagator(V[] vars, PropagatorPriority priority, boolean reactToFineEvt) {
        assert vars != null && vars.length > 0 && vars[0] != null : "wrong variable set in propagator constructor";
        this.model = vars[0].getModel();
        this.reactToFineEvt = reactToFineEvt;
        this.state = NEW;
        this.priority = priority;
        // To avoid too much memory consumption, the array of variables is referenced directly, no clone anymore.
        // This is the responsibility of the propagator's developer to take care of that point.
        if (model.getSettings().cloneVariableArrayInPropagator()) {
            this.vars = vars.clone();
        } else {
            this.vars = vars;
        }
        this.vindices = new int[vars.length];
        ID = model.nextId();
        operations = new IOperation[]{
                new IOperation() {
                    @Override
                    public void undo() {
                        state = NEW;
                    }
                },
                new IOperation() {
                    @Override
                    public void undo() {
                        state = REIFIED;
                    }
                },
                new IOperation() {
                    @Override
                    public void undo() {
                        state = ACTIVE;
                    }
                }
        };
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
        V[] tmp = vars;
        vars = copyOf(vars, vars.length + nvars.length);
        arraycopy(nvars, 0, vars, tmp.length, nvars.length);
        int[] itmp = this.vindices;
        vindices = new int[vars.length];
        arraycopy(itmp, 0, vindices, 0, itmp.length);
        for (int v = tmp.length; v < vars.length; v++) {
            vindices[v] = vars[v].link(this, v);
        }
        if (model.getSolver().getEngine() != NoPropagationEngine.SINGLETON && model.getSolver().getEngine().isInitialized()) {
            model.getSolver().getEngine().updateInvolvedVariables(this);
        }
    }

    /**
     * Creates links between this propagator and its variables.
     * The propagator will then be referenced in each of its variables.
     */
    public final void linkVariables() {
        for (int v = 0; v < vars.length; v++) {
            vindices[v] = vars[v].link(this, v);
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
    public void defineIn(Constraint c) throws SolverException{
        if((constraint != null && constraint.getStatus() != Constraint.Status.FREE)
            || (c.getStatus() != Constraint.Status.FREE)){
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
    public void setActive() throws SolverException{
        if(isStateLess()) {
            state = ACTIVE;
            model.getEnvironment().save(operations[NEW]);
        }else{
            throw new SolverException("Try to activate a propagator already active, passive or reified.\n" +
                this + " of "+ this.getConstraint());
        }
    }

    /**
     * informs that this reified propagator must hold. Should not be called by the user.
     * @throws SolverException if the propagator cannot be activated due to its current state
     */
    public void setReifiedTrue() throws SolverException{
        if(isReifiedAndSilent()) {
            state = ACTIVE;
            model.getEnvironment().save(operations[REIFIED]);
        }else{
            throw new SolverException("Reification process tries to force activation of a propagator already active or passive.\n" +
                    this + " of "+ this.getConstraint());
        }
    }

    /**
     * informs that this reified propagator may not hold. Should not be called by the user.
     * @throws SolverException if the propagator cannot be reified due to its current state
     */
    public void setReifiedSilent() throws SolverException{
        if (isStateLess() || isReifiedAndSilent()) {
            state = REIFIED;
        } else {
            throw new SolverException("Reification process try to reify a propagator already active or posted.\n" +
                    this + " of "+ this.getConstraint());
        }
    }

    /**
     * informs that this propagator is now passive : it holds but no further filtering can occur,
     * so it is useless to propagate it. Should not be called by the user.
     * @throws SolverException if the propagator cannot be set passive due to its current state
     */
    @SuppressWarnings({"unchecked"})
    public void setPassive() throws SolverException{
        // Note: calling isCompletelyInstantiated() to avoid next steps may lead to error when
        // dealing with reification and dynamic addition.
        if(isActive()){
            state = PASSIVE;
            model.getEnvironment().save(operations[ACTIVE]);
            //TODO: update var mask back
            model.getSolver().getEngine().desactivatePropagator(this);
        }else{
            throw new SolverException("Try to passivate a propagator already passive or reified.\n"+
                    this + " of "+ this.getConstraint());
        }
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
            return priority.priority;
        } else return arity;
    }

    /**
     * Throws a contradiction exception
     *
     * @throws org.chocosolver.solver.exception.ContradictionException expected behavior
     */
    public void fails() throws ContradictionException {
        model.getSolver().getEngine().fails(this, null, null);
    }

    @Override
    public int compareTo(Propagator o) {
        return this.ID - o.ID;
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
        return o instanceof Propagator && ((Propagator) o).ID == ID;
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
    public final PropagatorPriority getPriority() {
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
     * @return true iff this propagator is active (it should filter)
     */
    public boolean isActive() {
        return state == ACTIVE;
    }

    /**
     * @return true iff this propagator is passive. This happens when it is entailed : the propagator still hold
     * but no more filtering can occur
     */
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
        if(vars.length>=3)st.append(vars[i++].getName()).append(", ");
        if(vars.length>=2)st.append(vars[i++].getName()).append(", ");
        if(vars.length>=1)st.append(vars[i++].getName());
        if (i < vars.length) {
            if (vars.length > 4) {
                st.append(", ...");
            }
            st.append(", ").append(vars[vars.length - 1].getName());
        }
        st.append(')');

        return st.toString();
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean nrules = ruleStore.addPropagatorActivationRule(this);
        for (int i = 0; i < vars.length; i++) {
            if (vars[i] != var) nrules |= ruleStore.addFullDomainRule((IntVar) vars[i]);
        }
        return nrules;
    }
}
