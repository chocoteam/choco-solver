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

package solver.constraints.propagators;

import choco.kernel.ESat;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.IStateInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.propagation.engines.IPropagationEngine;
import solver.requests.IRequest;
import solver.requests.InitializeRequest;
import solver.requests.PropRequest;
import solver.variables.IntVar;
import solver.variables.Variable;

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
 * out of domain value instantiation or other incoherencies.
 * <br/>
 * Furthermore, a <code>Propagator</code> object can be <i>entailed</i> : considering the current state of its <code>Variable</code>
 * objects, the internal filtering algorithm becomes useless (for example: NEQ propagator and a couple of <code>Variable</code>
 * objects with disjoint domains). In other words, whatever are the future events occurring on <code>Variable</code> objects,
 * new calls to <code>propagate</code> method would be useless.
 * <br/>
 * <code>this</code> can be desactivate using the <code>setPassive</code>method.
 * It automatically informs <code>Constraint</code> observers of this new "state".
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @see solver.variables.Variable
 * @see solver.constraints.Constraint
 * @since 0.01
 */
public abstract class Propagator<V extends Variable> implements Serializable, ICause {

    private static final long serialVersionUID = 2L;

    protected final static Logger LOGGER = LoggerFactory.getLogger(Propagator.class);

    /**
     * List of <code>variable</code> objects
     */
    protected V[] vars;

    /**
     * List of requests of <code>this</code>
     */
    protected IRequest<V>[] requests;

    protected InitializeRequest initializeRequest;

    /**
     * Reference to the <code>Solver</code>'s <code>IEnvironment</code>,
     * to deal with internal backtrackable structure.
     */
    public IEnvironment environment;

    /**
     * Backtrackable boolean indicating wether <code>this</code> is active
     */
    protected IStateBool isActive;

    protected int nbRequestEnqued = 0; // counter of enqued requests -- usable as trigger for complex algorithm

    public long filterCall;  // statistics of calls to filter

    protected final IStateInt arity; // arity of this -- number of uninstantiated variables

    protected int fails;

    /**
     * Declaring constraint
     */
    protected Constraint constraint;

    protected final PropagatorPriority priority;

    protected final boolean reactOnPromotion;

    protected final IPropagationEngine engine;

    @SuppressWarnings({"unchecked"})
    protected Propagator(V[] vars, Solver solver, Constraint<V, Propagator<V>> constraint, PropagatorPriority priority, boolean reactOnPromotion) {
        this.vars = vars;
        this.environment = solver.getEnvironment();
        this.engine = solver.getEngine();
        this.isActive = environment.makeBool(true);
        this.constraint = constraint;
        this.priority = priority;
        this.reactOnPromotion = reactOnPromotion;
        this.initializeRequest = new InitializeRequest(this, -1);
        int nbNi = 0;
        for (int v = 0; v < vars.length; v++) {
            if (!vars[v].instantiated()) {
                nbNi++;
            }
        }
        arity = environment.makeInt(nbNi);
        linkToVariables();
        fails = 0;
    }

    /**
     * Return the specific mask indicating the event on which this <code>Propagator</code> object can react.<br/>
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
     * @throws ContradictionException when a contradiction occurs, like domain wipe out or other incoherencies.
     */
    public abstract void propagate() throws ContradictionException;

    /**
     * Call filtering algorihtm defined within the <code>Propagator</code> objects.
     *
     * @param request      request to propagate
     * @param idxVarInProp index of the variable <code>var</code> in <code>this</code>
     * @param mask         type of event
     * @throws solver.exception.ContradictionException
     *          if a contradiction occurs
     */
    public abstract void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException;

    @SuppressWarnings({"unchecked"})
    public void setPassive() {
        assert isActive() : "the propagator is already passive, it cannot set passive more than once in one filtering call";
        isActive.set(false);
        this.constraint.updateActivity(this);
        // then notify the linked variables
        for (int i = 0; i < requests.length; i++) {
            requests[i].desactivate();
        }
    }

    public boolean isActive() {
        return isActive.get();
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

    /**
     * Returns the number of variables involved in <code>this</code>.
     *
     * @return number of variables
     */
    public final int getNbVars() {
        return vars.length;
    }

    public int nbRequests() {
        return requests.length;
    }

    public IRequest getRequest(int i) {
        if (i >= 0 && i < requests.length) {
            return requests[i];
        } else if (i == -1) {
            return initializeRequest;
        }
        throw new IndexOutOfBoundsException();
    }

    @SuppressWarnings({"unchecked"})
    protected void linkToVariables() {
        requests = new IRequest[vars.length];
        for (int i = 0; i < vars.length; i++) {
            vars[i].addPropagator(this, i);
            requests[i] = new PropRequest<V, Propagator<V>>(this, vars[i], i);
            vars[i].addRequest(requests[i]);
        }
    }

    /**
     * BEWARE: this method should not be removed!!
     * It is called by reflection within ReifiedConstraint
     */
    @SuppressWarnings({"UnusedDeclaration", "unchecked"})
    protected void unlinkVariables() {
        for (int v = 0; v < requests.length; v++) {
            IRequest request = requests[v];
            request.getVariable().deleteRequest(request);
            requests[v] = null;
        }
        requests = new IRequest[0];
    }

    /**
     * Returns the constraint including this propagator
     *
     * @return Constraint
     */
    @Override
    public final Constraint getConstraint() {
        return constraint;
    }

    public final PropagatorPriority getPriority() {
        return priority;
    }

    public final boolean reactOnPromotion() {
        return reactOnPromotion;
    }

    /**
     * returns a explanation for the decision mentionned in parameters
     *
     * @param d : a <code>Deduction</code> to explain
     * @return a set of constraints and past decisions
     */

    @Override
    public Explanation explain(IntVar var, Deduction d) {
        Explanation expl = new Explanation(null, null);
        // the current deduction is due to the current domain of the involved variables
        for (Variable v : this.vars) {
            expl.add(v.explain());
        }
        // and the application of the current propagator
        expl.add(this);
        return expl;
    }

    protected boolean isCompletelyInstantiated() {
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].instantiated()) {
                return false;
            }
        }
        return true;
    }

    protected int getNbRequestEnqued() {
        return nbRequestEnqued;
    }

    public void incNbRequestEnqued() {
        assert (nbRequestEnqued >= 0) : "number of enqued requests is < 0";
        nbRequestEnqued++;
    }

    public void decNbRequestEnqued() {
        assert (nbRequestEnqued > 0) : "number of enqued requests is < 0";
        nbRequestEnqued--;
    }

    /**
     * Returns the number of uninstanciated variables
     *
     * @return number of uninstanciated variables
     */
    public int arity() {
        return arity.get();
    }

    public void decArity() {
        assert (arity.get() >= 0) : "arity < 0";
        arity.add(-1);
    }

    public void incFail() {
        fails++;
    }

    public long getFails() {
        return fails;
    }

    /**
     * Throws a contradiction exception based on <variable, message>
     *
     * @param variable involved variable
     * @param message  detailed message
     * @throws ContradictionException expected behavior
     */
    protected void contradiction(Variable variable, String message) throws ContradictionException {
        engine.fails(this, variable, message);
    }
}
