/**
 *  Copyright (c) 2010, Ecole des Mines de Nantes
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

package solver.constraints;

import choco.kernel.ESat;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.propagation.IPriority;
import solver.variables.Variable;

import java.io.Serializable;

/**
 * A class can extend <code>Constraint</code> interface and specifies a list of <code>Variable</code> objects
 * and a list of <code>Propagator</code> objects.<br/>
 * It observes <code>Variable</code> objects and are observed by <code>IPropagationEngine</code> objects, and can
 * send instructions to some <code>Propagator</code> objects.
 * <br/>
 * At least a <code>Constraint</code> is a checker, and should defined the <code>isSatisfied</code> method
 * to state wether a tuple of values is a solution or not.
 * <br/>
 * But, most commonly, a <code>Constraint</code> is based on one or more <code>Propagator</code> object.
 * A <code>Constraint</code> listens to modifications occuring the <code>Variable</code> objects it is observing
 * and notify the <code>Propagator</code> objects, through the <code>filterOnEvent</code> method
 * and <code>propagate</code> method.
 * <br/>
 * When an event (value removal or instantiation) occurs on one of the <code>Variable</code> objects, this notifies
 * its <code>IPropagationEngine</code>, to let them drive the consequencies.
 * <br/>
 * A <code>Constraint</code> also defines a <code>getPropagationConditions</code> method which indicates on what type of event
 * it can react. Default mask is <code>INSTINT_MASK</code> and <code>REMVAL_MASK</code>.
 * <br/>
 * A <code>Constraint</code> can be <i>passive</i>, when each of its <code>Propagator</code> object is <i>passive</i>.
 * A newly passive <code>Propagator</code> object should inform the <code>Constraint</code>
 * using <code>updateActivity</code>.
 * Within the internal structure, passive <code>Propagator</code> objects are moved and won't be considered
 * in following notifications. This mechanism is backtrackable.
 * <br/>
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @see solver.variables.Variable
 * @see solver.constraints.propagators.Propagator
 * @see solver.Observer
 * @see solver.Observable
 * @see solver.propagation.engines.IPropagationEngine
 * @since 0.01
 */
public abstract class Constraint<V extends Variable, P extends Propagator<V>> implements Serializable, IPriority {

    private static final long serialVersionUID = 1L;

    public static PropagatorPriority _DEFAULT_THRESHOLD = PropagatorPriority.TERNARY;

    protected final Solver solver;

    public V[] vars;
    public P[] propagators;
    protected final IStateInt lastPropagatorActive;

    protected int propagationConditions;
    protected int staticPropagationPriority;

    protected final PropagatorPriority storeThreshold; // exclusive

    protected boolean slowPropagation = true;

    protected transient boolean initialize = false;

    public Constraint(V[] vars, Solver solver, PropagatorPriority storeThreshold) {
        this.vars = vars.clone();
        this.solver = solver;
        this.storeThreshold = storeThreshold;
        this.lastPropagatorActive = solver.getEnvironment().makeInt();
        initialize = false;
    }


    /**
     * Return the specific mask indicating the event on which this <code>Constraint</code> object can react.<br/>
     * <i>Checks are made applying bitwise AND between the mask and the event.</i>
     *
     * @return int composed of <code>REMOVE</code> and/or <code>INSTANTIATE</code>
     *         and/or <code>DECUPP</code> and/or <code>INCLOW</code>
     */
    public final int getPropagationConditions() {
        return propagationConditions;
    }

    /**
     * Set the specific mask indicating the event on which this <code>Constraint</code> object can react.<br/>
     * <i>Checks are made applying bitwise AND between the mask and the event.</i>
     *
     * @param propagationConditions todo
     */
    protected final void setPropagationConditions(int propagationConditions) {
        this.propagationConditions = propagationConditions;
    }

    public V[] getVariables() {
        return vars;
    }

    /**
     * Test if this <code>Constraint</code> object is active, i.e. at least one propagator is active.
     *
     * @return <code>true</code> if this <code>Constraint</code> object is active, <code>false</code> otherwise.
     */
    public final boolean isActive() {
        return (lastPropagatorActive.get() == 0);
    }

    /**
     * Test if this <code>Constraint</code> object is satisfied, regarding the value of component <code>Variable</code> objects.
     *
     * @return <code>ESat.UNDEFINED</code> if at least one variable is not instantiated,
     *         <code>ESat.TRUE</code> if this <code>this</code> is satisfied, <code>ESat.FALSE</code> otherwise.
     */
    public abstract ESat isSatisfied();


    /**
     * Evaluates the current entailment of <code>this</code>.
     * There are 3 possible states:
     * <ul>
     * <li>regarding the current states of the variables, <code>this</code> is always satisfied,</li>
     * <li>regarding the current states of the variables, <code>this</code> is always violated,</li>
     * <li>regarding the current states of the variables, nothing can be deduced</li>
     * </ul>
     * This is mandatory for the reification.
     *
     * @return the satisfaction of the constraint
     */
    public ESat isEntailed() {
        int last = lastPropagatorActive.get();
        int sat = 0;
        for (int i = 0; i < last; i++) {
            ESat entail = propagators[i].isEntailed();
            if (entail.equals(ESat.FALSE)) {
                return entail;
            } else if (entail.equals(ESat.TRUE)) {
                sat++;
            }
        }
        if (sat == last) {
            return ESat.TRUE;
        }
        // No need to check if FALSE, must have been returned before
        else {
            return ESat.UNDEFINED;
        }
    }

    /**
     * Move the new entailed propagator from its position to the position of the first entailed propagators
     * (right side of the lastPropagatorActive)
     * BEWARE: do not preserve order of the propagators
     *
     * @param prop newly entailed propagator
     */
    public void updateActivity(P prop) {
        int last = lastPropagatorActive.get();
        if (propagators.length > 1) {
            // get the index of the propagator within the list of propagators
            int i = 0;
            for (; i < last; i++) {
                if (prop == propagators[i]) {
                    break;
                }
            }
            // move propagators[i] at the right side of lastPropagatorActive
            P tmp = propagators[--last];
            propagators[last] = prop;
            propagators[i] = tmp;
        }
        lastPropagatorActive.add(-1);
    }

    /**
     * Define the list of <code>Propagator</code> objects of this <code>Constraint</code> object.
     *
     * @param propagators list of <code>Propagator</code> objects.
     */
    public final void setPropagators(P... propagators) {
        this.propagators = propagators;
        this.lastPropagatorActive.set(propagators.length);
        for (int p = 0; p < propagators.length; p++) {
            Propagator prop = propagators[p];
            propagationConditions |= prop.getPropagationConditions();
            staticPropagationPriority = Math.max(staticPropagationPriority, prop.getPriority().priority);
        }

        slowPropagation = (staticPropagationPriority < storeThreshold.priority);
    }

    /**
     * Initial propagation of the constraint
     * @throws ContradictionException
     */
    public void filter() throws ContradictionException {
        int last = lastPropagatorActive.get();
        Propagator prop;
        for (int p = 0; p < last; p++) {
            prop = propagators[p];
            ESat entailed = prop.isEntailed();
            switch (entailed) {
                case FALSE:
                    ContradictionException.throwIt(prop, null, "Entailed false");
                    break;
                case TRUE:
                    prop.setPassive();
                    p--;
                    last--;
                    break;
                case UNDEFINED:
                    prop.propagate();
                    if (!prop.isActive()) {
                        p--;
                        last--;
                    }
                    break;

            }
        }
        initialize = true;
    }

    /**
     * Returns the priority of the constraint.
     * Should be between 0 and 6. (0 is very slow, 6 very fast).
     *
     * @return the priority of the constraint
     */
    public int getPriority() {
        return staticPropagationPriority;
    }
}
