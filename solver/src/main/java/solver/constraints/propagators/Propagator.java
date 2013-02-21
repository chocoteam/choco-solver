/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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

package solver.constraints.propagators;


import common.ESat;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import memory.IEnvironment;
import memory.structure.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.Configuration;
import solver.ICause;
import solver.Identity;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.propagation.IPropagationEngine;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.VariableFactory;

import java.io.Serializable;


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
 * this prevents from wrong references when a variable occurs more than once in the scope (See {@link solver.constraints.propagators.nary.globalcardinality.PropFastCount} for instance).
 * <br/>- //to complete
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @see solver.variables.Variable
 * @see solver.constraints.Constraint
 * @since 0.01
 */
public abstract class Propagator<V extends Variable> implements Serializable, ICause, Identity, Comparable<Propagator> {

    private static final long serialVersionUID = 2L;
    protected final static Logger LOGGER = LoggerFactory.getLogger(Propagator.class);
    private final int ID; // unique id of this

    // List of <code>variable</code> objects -- a variable can occur more than once, but it could not have the same index
    protected V[] vars;
    // index of this within the list of propagator of the i^th variable
    private int[] vindices;
    // the i^th event mask stores modification events on the i^th variable, since the last propagation
    protected int[] eventmasks;


    protected static final short NEW = 0, REIFIED = 1, ACTIVE = 2, PASSIVE = 3;
    private Operation[] operations;

    /**
     * Backtrackable boolean indicating wether <code>this</code> is active
     */
    private short state; // 0 : new -- 1 : active -- 2 : passive
    private int nbPendingEvt = 0; // counter of enqued records -- usable as trigger for complex algorithm
    public long fineERcalls, coarseERcalls;  // statistics of calls to filter
    protected int fails;

    /**
     * Reference to the <code>Solver</code>'s <code>IEnvironment</code>,
     * to deal with internal backtrackable structure.
     */
    public IEnvironment environment;
    /**
     * Declaring constraint
     */
    protected Constraint<V, Propagator<V>> constraint;
    protected final PropagatorPriority priority;
    protected final boolean reactOnPromotion;
    protected final Solver solver;
    private TIntSet set = new TIntHashSet();

    // cause of variable modifications. The default value is 'this"
    protected Propagator aCause;

    // 2012-06-13 <cp>: multiple occurrences of variables in a propagator is strongly inadvisable
    private <V extends Variable> void checkVariable(V[] vars) {
        set.clear();
        for (int i = 0; i < vars.length; i++) {
            Variable v = vars[i];
            if ((v.getTypeAndKind() & Variable.CSTE) == 0) {
                if (set.contains(v.getId())) {
                    switch (Configuration.MUL_OCC_VAR_PROP) {
                        case disabled:
                            throw new UnsupportedOperationException(v.toString() + " occurs more than one time in this propagator.\n" +
                                    "See configurations.property to change this policy.");
                        case warn:
                            LOGGER.warn(v.toString() + " occurs more than one time in this propagator.");
                            break;
                        case view:
                            if ((v.getTypeAndKind() & Variable.INT) != 0) {
                                vars[i] = (V) VariableFactory.eq((IntVar) v);
                            } else {
                                throw new UnsupportedOperationException(v.toString() + " occurs more than one time in this propagator. " +
                                        "However, this type of variable cannot be declared in a view.");
                            }
                            break;
                        case duplicate:
                            if ((v.getTypeAndKind() & Variable.INT) != 0) {
                                Solver solver = v.getSolver();
                                vars[i] = (V) v.duplicate();
                                solver.post(IntConstraintFactory.arithm((IntVar) v, "=", (IntVar) vars[i]));
                            } else {
                                throw new UnsupportedOperationException(v.toString() + " occurs more than one time in this propagator. " +
                                        "However, this type of variable does not allow to post an EQ constraint over it.");
                            }
                            break;
                        case silent:
                        default:
                    }

                }
                set.add(vars[i].getId());
            }
        }
    }


    protected Propagator(V[] vars, PropagatorPriority priority, boolean reactOnPromotion) {
        this(vars[0].getSolver(), vars, priority, reactOnPromotion);
    }

    @SuppressWarnings({"unchecked"})
    protected Propagator(Solver solver, V[] vars, PropagatorPriority priority, boolean reactOnPromotion) {
        checkVariable(vars);
        this.vars = vars.clone();
        this.solver = solver;
        this.vindices = new int[vars.length];
        this.eventmasks = new int[vars.length];
        this.environment = solver.getEnvironment();
        this.state = NEW;
        this.priority = priority;
        this.reactOnPromotion = reactOnPromotion;
        this.aCause = this;
        for (int v = 0; v < vars.length; v++) {
            vindices[v] = vars[v].link(this, v);
            /*if (!vars[v].instantiated()) {
                nbNi++;
            }*/
        }
        fails = 0;
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

    protected Propagator(V[] vars, PropagatorPriority priority) {
        this(vars, priority, true);
    }

    @Override
    public int getId() {
        return ID;
    }

    public Solver getSolver() {
        return solver;
    }

    public void defineIn(Constraint c) {
        this.constraint = c;
    }

    /**
     * Update the mask of the vidx^th variable, and return true if the variable was not modify before.
     *
     * @param vidx    index of the modified variable
     * @param evtType type of event occurring on the variable
     * @return true if this the first modification recording since last propagation
     */
    public final boolean updateMask(int vidx, EventType evtType) {
        boolean needSched = eventmasks[vidx] == 0;
        eventmasks[vidx] |= evtType.strengthened_mask;
        return needSched;
    }

    public final int getMask(int vidx) {
        return eventmasks[vidx];
    }

    public final void clearMask(int vidx) {
        eventmasks[vidx] = 0;
    }

    /**
     * Return the specific mask indicating the <b>variable events</b> on which this <code>Propagator</code> object can react.<br/>
     * <i>Checks are made applying bitwise AND between the mask and the event.</i>
     *
     * @param vIdx index of the variable within the propagator
     * @return int composed of <code>REMOVE</code> and/or <code>INSTANTIATE</code>
     *         and/or <code>DECUPP</code> and/or <code>INCLOW</code>
     */
    public abstract int getPropagationConditions(int vIdx);

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
     * Call filtering algorihtm defined within the <code>Propagator</code> objects.
     *
     * @param idxVarInProp index of the variable <code>var</code> in <code>this</code>
     * @param mask         type of event
     * @throws solver.exception.ContradictionException
     *          if a contradiction occurs
     */
    public abstract void propagate(int idxVarInProp, int mask) throws ContradictionException;

    /**
     * Add the coarse event recorder into the engine
     *
     * @param evt event type
     */
    public final void forcePropagate(EventType evt) throws ContradictionException {
        if (nbPendingEvt == 0) {
            if (Configuration.PRINT_PROPAGATION) {
                IPropagationEngine.Trace.printPropagation(null, this);
            }
            coarseERcalls++;
            propagate(evt.getStrengthenedMask());
        }
    }

    public void setActive() {
        assert isStateLess() : "the propagator is already active, it cannot set active";
        state = ACTIVE;
        environment.save(operations[NEW]);
        // update activity mask of variables
        for (int v = 0; v < vars.length; v++) {
            vars[v].recordMask(getPropagationConditions(v));
        }
    }

    public void setReifiedTrue() {
        assert isReifiedAndSilent() : "the propagator was not in a silent reified state";
        state = ACTIVE;
        environment.save(operations[REIFIED]);
        // update activity mask of variables
        for (int v = 0; v < vars.length; v++) {
            vars[v].recordMask(getPropagationConditions(v));
        }
    }

    public void setReifiedSilent() {
        assert isStateLess() : "the propagator was not stateless";
        state = REIFIED;
    }

    @SuppressWarnings({"unchecked"})
    public void setPassive() {
        assert isActive() : this.toString() + " is already passive, it cannot set passive more than once in one filtering call";
        state = PASSIVE;
        environment.save(operations[ACTIVE]);
        //TODO: update var mask back
        solver.getEngine().desactivatePropagator(this);
    }

    public boolean isStateLess() {
        return state == NEW;
    }

    public boolean isReifiedAndSilent() {
        return state == REIFIED;
    }

    public boolean isActive() {
        return state == ACTIVE;
    }

    public boolean isPassive() {
        return state == PASSIVE;
    }

    /**
     * Check wether <code>this</code> is entailed according to the current state of its internal structure.
     * At least, should check the satisfaction of <code>this</code> (when all is instantiated).
     *
     * @return ESat.TRUE if entailed, ESat.FALSE if not entailed, ESat.UNDEFINED if unknown
     */
    public abstract ESat isEntailed();

    /**
     * Returns the element at the specified position in this internal list of <code>V</code> objects.
     *
     * @param i index of the element
     * @return a <code>V</code> object
     */
    public final V getVar(int i) {
        return vars[i];
    }

    public final V[] getVars() {
        return vars;
    }

    /**
     * index of the propagator within its variables
     *
     * @return
     */
    public int[] getVIndices() {
        return vindices;
    }

    public void setVIndices(int idx, int val) {
        vindices[idx] = val;
    }

    /**
     * Returns the number of variables involved in <code>this</code>.
     *
     * @return number of variables
     */
    public final int getNbVars() {
        return vars.length;
    }

    /**
     * Returns the promomotion policy of <code>this</code>.
     * If <code>this</code> reacts on promotion, it must be informed of the promotion of an event it created.
     * (example: removing the lower bound of a variable is promoted in lower-bound modification)
     *
     * @return <code>true</code> if <code>this</code> must be informed of promotion
     */
    public final boolean reactOnPromotion() {
        return reactOnPromotion;
    }

    /**
     * Returns the constraint including this propagator
     *
     * @return Constraint
     */
    public final Constraint getConstraint() {
        return constraint;
    }

    public final PropagatorPriority getPriority() {
        return priority;
    }


    /**
     * returns a explanation for the decision mentionned in parameters
     *
     * @param d : a <code>Deduction</code> to explain
     * @param e
     * @return a set of constraints and past decisions
     */
    @Override
    public void explain(Deduction d, Explanation e) {
        // the current deduction is due to the current domain of the involved variables
        for (Variable v : this.vars) {
            v.explain(VariableState.DOM, e);
        }
        // and the application of the current propagator
        e.add(this);
    }

    public boolean isCompletelyInstantiated() {
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].instantiated()) {
                return false;
            }
        }
        return true;
    }

    public int getNbPendingEvt() {
        return nbPendingEvt;
    }

    public void incNbPendingEvt() {
        assert (nbPendingEvt >= 0) : "number of enqued records is < 0 " + this;
        nbPendingEvt++;
        //if(LoggerFactory.getLogger("solver").isDebugEnabled())
        //    LoggerFactory.getLogger("solver").debug("[I]{}:{}", nbPendingEvt, this);
    }

    public void decNbPendingEvt() {
        assert (nbPendingEvt > 0) : "number of enqued records is < 0 " + this;
        nbPendingEvt--;
        //if(LoggerFactory.getLogger("solver").isDebugEnabled())
        //    LoggerFactory.getLogger("solver").debug("[D]{}:{}", nbPendingEvt, this);
    }

    public void flushPendingEvt() {
        nbPendingEvt = 0;
        //if(LoggerFactory.getLogger("solver").isDebugEnabled())
        //    LoggerFactory.getLogger("solver").debug("[F]{}:{}", nbPendingEvt, this);
    }

    /**
     * Returns the number of uninstanciated variables
     *
     * @return number of uninstanciated variables
     */
    public int arity() {
        int arity = 0;
        for (int i = 0; i < vars.length; i++) {
            arity += vars[i].instantiated() ? 0 : 1;
        }
        return arity;
    }

    public int dynPriority() {
        int arity = 0;
        for (int i = 0; i < vars.length && arity <= 3; i++) {
            arity += vars[i].instantiated() ? 0 : 1;
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

    @Override
    public int hashCode() {
        return ID;
    }
}
