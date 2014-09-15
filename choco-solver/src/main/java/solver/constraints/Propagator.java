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

package solver.constraints;


import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.TIntHashSet;
import memory.structure.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Identity;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.Variable;
import solver.variables.events.IEventType;
import solver.variables.events.PropagatorEventType;
import util.ESat;

import java.io.Serializable;
import java.util.Arrays;


/**
 * A <code>Propagator</code> class defines methods to react on a <code>Variable</code> objects modifications.
 * It is observed by <code>Constraint</code> objects and can notify them when a <code>Variable</code> event occurs.
 * <br/>
 * Propagator methods are assumed to be idempotent, ie :
 * Let f be a propagator method, such that f : D -> D' include D, where D the union of variable domains involved in f.
 * Then, f(D)=f(D').
 * <p/>
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
 * <p/>
 * The developer of a propagator must respect some rules to create a efficient propagator:
 * <br/>- internal references to variables must be achieved referencing the <code>this.vars</code> after the call to super,
 * this prevents from wrong references when a variable occurs more than once in the scope (See {@link solver.constraints.nary.count.PropCount_AC} for instance).
 * <br/>- //to complete
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 * @version 0.01, june 2010
 * @see solver.variables.Variable
 * @see solver.constraints.Constraint
 * @since 0.01
 */
public abstract class Propagator<V extends Variable> implements Serializable, ICause, Identity, Comparable<Propagator> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private static final long serialVersionUID = 2L;
    protected final static Logger LOGGER = LoggerFactory.getLogger(Propagator.class);
    protected static final short NEW = 0, REIFIED = 1, ACTIVE = 2, PASSIVE = 3;
    private static ThreadLocal<TIntHashSet> set = new ThreadLocal<TIntHashSet>() {
        @Override
        protected TIntHashSet initialValue() {
            return new TIntHashSet();
        }
    };

    // propagator attributes
    private final int ID; // unique id of this
    private short state;  // 0 : new -- 1 : active -- 2 : passive
    private Operation[] operations; // propagator state operations
    private int nbPendingEvt = 0;   // counter of enqued records -- usable as trigger for complex algorithm
    public long fineERcalls, coarseERcalls;  // statistics of calls to filter
    protected Propagator aCause; // cause of variable modifications. The default value is 'this"
    protected final PropagatorPriority priority;
    protected final boolean reactToFineEvt;
    // references
    protected Constraint constraint; // declaring constraint
    protected final Solver solver;   // solver of this propagator
    // variable related information
    protected V[] vars;// List of <code>variable</code> objects -- a variable can occur more than once, but it could not have the same index
    private int[] vindices;// index of this within the list of propagator of the i^th variable

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates a new propagator to filter the domains of vars.
     *
     * @param vars           variables of the propagator. Their modification will trigger filtering
     * @param priority       priority of this propagator (lowest priority propagators are called first)
     * @param reactToFineEvt indicates whether or not this propagator must be informed of every variable
     *                       modification, i.e. if it should be incremental or not
     */
    protected Propagator(V[] vars, PropagatorPriority priority, boolean reactToFineEvt) {
        assert vars != null && vars.length > 0 && vars[0] != null : "wrong variable set in propagator constructor";
        this.solver = vars[0].getSolver();
        this.reactToFineEvt = reactToFineEvt;
        this.state = NEW;
        this.priority = priority;
        this.aCause = this;
        this.vars = vars.clone();
        this.vindices = new int[vars.length];
        for (int v = 0; v < vars.length; v++) {
            vindices[v] = vars[v].link(this, v);
        }
        ID = solver.nextId();
        operations = new Operation[]{
                new Operation() {
                    @Override
                    public void undo() {
                        state = NEW;
                    }
                },
                new Operation() {
                    @Override
                    public void undo() {
                        state = REIFIED;
                    }
                },
                new Operation() {
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
    protected Propagator(V... vars) {
        this(vars, PropagatorPriority.LINEAR, false);
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
    protected void addVariable(V... nvars) {
        V[] tmp = vars;
        vars = Arrays.copyOf(vars, vars.length + nvars.length);
        System.arraycopy(tmp, 0, vars, 0, tmp.length);
        System.arraycopy(nvars, 0, vars, tmp.length, nvars.length);
        int[] itmp = this.vindices;
        vindices = new int[vars.length];
        System.arraycopy(itmp, 0, vindices, 0, itmp.length);
        for (int v = tmp.length; v < vars.length; v++) {
            vindices[v] = vars[v].link(this, v);
        }
    }

    /**
     * Informs this propagator the (unique) constraint it filters.
     * The constraint reference will be overwritten in case of reification.
     * Should not be called by the user.
     *
     * @param c the constraint containing this propagator
     */
    public void defineIn(Constraint c) {
        this.constraint = c;
    }

    /**
     * Return the specific mask indicating the <b>variable events</b> on which this <code>Propagator</code> object can react.<br/>
     * <i>Checks are made applying bitwise AND between the mask and the event.</i>
     * Reacts to any kind of event by default.
     *
     * @param vIdx index of the variable within the propagator
     * @return int composed of <code>REMOVE</code> and/or <code>INSTANTIATE</code>
     * and/or <code>DECUPP</code> and/or <code>INCLOW</code>
     */
    protected int getPropagationConditions(int vIdx) {
        return IEventType.MAX;
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
     * @throws ContradictionException when a contradiction occurs, like domain wipe out or other incoherencies.
     */
    public abstract void propagate(int evtmask) throws ContradictionException;

    /**
     * Advise a propagator of a modification occurring on one of its variables,
     * and decide if <code>this</code> should be scheduled.
     * At least, this method SHOULD check the propagation condition of the event received.
     * In addition, this method can be used to update internal state of <code>this</code>.
     * This method can returns <code>true</code> even if the propagator is already scheduled.
     *
     * @param idxVarInProp index of the modified variable
     * @param mask         modification event mask
     * @return <code>true</code> if <code>this</code> should be scheduled, <code>false</code> otherwise.
     */
    public boolean advise(int idxVarInProp, int mask) {
        return (mask & getPropagationConditions(idxVarInProp)) != 0;
    }

    /**
     * Incremental filtering algorithm defined within the <code>Propagator</code>, called whenever the variable
     * of index idxVarInProp has changed. This method calls a CUSTOM_PROPAGATION (coarse-grained) by default.
     * <p/>
     * This method should be overridden if the argument <code>reactToFineEvt</code> is set to <code>true</code> in the constructor.
     * Otherwise, it executes <code>propagate(PropagatorEventType.CUSTOM_PROPAGATION.getStrengthenedMask());</code>
     *
     * @param idxVarInProp index of the variable <code>var</code> in <code>this</code>
     * @param mask         type of event
     * @throws solver.exception.ContradictionException if a contradiction occurs
     */
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (reactToFineEvt) {
            throw new SolverException(this + " has been declared to ignore which variable is modified.\n" +
                    "To change the configuration, consider:\n" +
                    "- to set 'reactToFineEvt' to false or,\n" +
                    "- to override the following methode:\n" +
                    "\t'public void propagate(int idxVarInProp, int mask) throws ContradictionException'." +
                    "The latter enables incrementality but also to delay calls to complex filtering algorithm (see the method 'forcePropagate(EventType evt)'.");
        }
        propagate(PropagatorEventType.CUSTOM_PROPAGATION.getStrengthenedMask());
    }

    /**
     * Schedules a coarse propagation to filter all variables at once.
     * <p/>
     * Add the coarse event recorder into the engine
     *
     * @param evt event type
     */
    public final void forcePropagate(PropagatorEventType evt) throws ContradictionException {
        solver.getEngine().delayedPropagation(this, evt);
    }

    /**
     * informs that this propagator is now active. Should not be called by the user.
     */
    public void setActive() {
        assert isStateLess() : "the propagator is already active, it cannot set active";
        state = ACTIVE;
        solver.getEnvironment().save(operations[NEW]);
        // update activity mask of variables
        for (int v = 0; v < vars.length; v++) {
            vars[v].recordMask(getPropagationConditions(v));
        }
    }

    /**
     * informs that this reified propagator must hold. Should not be called by the user.
     */
    public void setReifiedTrue() {
        assert isReifiedAndSilent() : "the propagator was not in a silent reified state";
        state = ACTIVE;
        solver.getEnvironment().save(operations[REIFIED]);
        // update activity mask of variables
        for (int v = 0; v < vars.length; v++) {
            vars[v].recordMask(getPropagationConditions(v));
        }
    }

    /**
     * informs that this reified propagator may not hold. Should not be called by the user.
     */
    public void setReifiedSilent() {
        assert isStateLess() || isReifiedAndSilent() : "the propagator was neither stateless nor reified";
        state = REIFIED;
    }

    /**
     * informs that this propagator is now passive : it holds but no further filtering can occur,
     * so it is useless to propagate it. Should not be called by the user.
     */
    @SuppressWarnings({"unchecked"})
    public void setPassive() {
        if (!isCompletelyInstantiated()) {// useless call to setPassive if all vars are instantiated
            assert isActive() : this.toString() + " is already passive, it cannot set passive more than once in one filtering call";
            state = PASSIVE;
            solver.getEnvironment().save(operations[ACTIVE]);
            //TODO: update var mask back
            solver.getEngine().desactivatePropagator(this);
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
     * returns a explanation for the decision mentioned in parameters
     *
     * @param d : a <code>Deduction</code> to explain
     * @param e : the explanation to feed
     * @return a set of constraints and past decisions
     */
    @Override
    public void explain(Deduction d, Explanation e) {
        e.add(solver.getExplainer().getPropagatorActivation(this));
        // the current deduction is due to the current domain of the involved variables
        for (Variable v : this.vars) {
            v.explain(VariableState.DOM, e);
        }
        // and the application of the current propagator
        e.add(this);
    }

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
     * informs that a new fine event has to be treated. Should not be called by the user.
     */
    public void incNbPendingEvt() {
        assert (nbPendingEvt >= 0) : "number of enqued records is < 0 " + this;
        nbPendingEvt++;
        //if(LoggerFactory.getLogger("solver").isDebugEnabled())
        //    LoggerFactory.getLogger("solver").debug("[I]{}:{}", nbPendingEvt, this);
    }

    /**
     * informs that a fine event has been treated. Should not be called by the user.
     */
    public void decNbPendingEvt() {
        assert (nbPendingEvt > 0) : "number of enqued records is < 0 " + this;
        nbPendingEvt--;
        //if(LoggerFactory.getLogger("solver").isDebugEnabled())
        //    LoggerFactory.getLogger("solver").debug("[D]{}:{}", nbPendingEvt, this);
    }

    /**
     * informs that all fine events have been treated. Should not be called by the user.
     */
    public void flushPendingEvt() {
        nbPendingEvt = 0;
        //if(LoggerFactory.getLogger("solver").isDebugEnabled())
        //    LoggerFactory.getLogger("solver").debug("[F]{}:{}", nbPendingEvt, this);
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
     * Throws a contradiction exception based on <variable, message>
     *
     * @param variable involved variable
     * @param message  detailed message
     * @throws ContradictionException expected behavior
     */
    public void contradiction(Variable variable, String message) throws ContradictionException {
        solver.getEngine().fails(aCause, variable, message);
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
     * @return the solver this propagator is defined in
     */
    public Solver getSolver() {
        return solver;
    }

    @Override
    public int hashCode() {
        return ID;
    }

    /**
     * @return the number of fine events which have not been treated yet
     */
    public int getNbPendingEvt() {
        return nbPendingEvt;
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
        st.append(getClass().getSimpleName() + "(");
        int i = 0;
        for (; i < Math.min(4, vars.length); i++) {
            st.append(vars[i].getName()).append(", ");
        }
        if (i < vars.length - 2) {
            st.append("...,");
        }
        st.append(vars[vars.length - 1].getName()).append(")");
        return st.toString();
    }

    /**
     * Duplicate the current propagator.
     * A restriction is that the resolution process should have not begun yet.
     * That's why state of the propagator may not be duplicated.
     *
     * @param solver      the target solver
     * @param identitymap a map to ensure uniqueness of objects
     */
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap){
        throw new SolverException("The propagator cannot be duplicated: the method is not defined.");
    }
}
