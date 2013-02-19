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

package solver.constraints;


import common.ESat;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IPriority;
import solver.variables.Variable;

import java.io.Serializable;
import java.util.Arrays;

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
 * @see solver.propagation.IPropagationEngine
 * @since 0.01
 */
public class Constraint<V extends Variable, P extends Propagator<V>> implements Serializable, IPriority {

    private static final long serialVersionUID = 1L;

    public static final String VAR_DEFAULT = "var_default";
    public static final String VAL_DEFAULT = "val_default";
    public static final String METRIC_DEFAULT = "met_default";

    protected final Solver solver;

    public V[] vars;
    public P[] propagators;

    protected int staticPropagationPriority;

    public Constraint(V[] vars, Solver solver) {
        this.vars = vars.clone();
        this.solver = solver;
    }

    public Constraint(Solver solver) {
        this.solver = solver;
        this.vars = (V[]) new Variable[0];
    }

    public V[] getVariables() {
        return vars;
    }


    public Solver getSolver() {
        return solver;
    }

    /**
     * Test if this <code>Constraint</code> object is satisfied, regarding the value of component <code>Variable</code> objects.
     *
     * @return <code>ESat.UNDEFINED</code> if at least one variable is not instantiated,
     *         <code>ESat.TRUE</code> if this <code>this</code> is satisfied, <code>ESat.FALSE</code> otherwise.
     */
    public ESat isSatisfied() {
        return isEntailed();
    }


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
        int sat = 0;
        for (int i = 0; i < propagators.length; i++) {
            if (!propagators[i].isStateLess()) { // we only count constraints with active propagator
                ESat entail = propagators[i].isEntailed();
                //System.out.println(propagators[i]+" => "+entail);
                if (entail.equals(ESat.FALSE)) {
                    return entail;
                } else if (entail.equals(ESat.TRUE)) {
                    sat++;
                }
            } else {
                sat++;
            }
        }
        if (sat == propagators.length) {
            return ESat.TRUE;
        }
        // No need to check if FALSE, must have been returned before
        else {
            return ESat.UNDEFINED;
        }
    }

    /**
     * Define the list of <code>Propagator</code> objects of this <code>Constraint</code> object.
     *
     * @param propagators list of <code>Propagator</code> objects.
     */
    public final void setPropagators(P... propagators) {
        this.propagators = propagators;
        for (int i = 0; i < propagators.length; i++) {
            propagators[i].defineIn(this);
        }
    }

    /**
     * Add new <code>Propagator</code> objects of this <code>Constraint</code> object.
     *
     * @param mPropagators list of <code>Propagator</code> objects to add.
     */
    @SuppressWarnings({"unchecked"})
    public final void addPropagators(P... mPropagators) {
        if (propagators == null) {
            setPropagators(mPropagators);
        } else {
            // add the new propagators at the end of the current array
            P[] tmp = this.propagators;
            this.propagators = (P[]) new Propagator[tmp.length + mPropagators.length];
            System.arraycopy(tmp, 0, propagators, 0, tmp.length);
            System.arraycopy(mPropagators, 0, propagators, tmp.length, mPropagators.length);
            for (int i = tmp.length; i < tmp.length + mPropagators.length; i++) {
                propagators[i].defineIn(this);
            }
        }
    }

    /**
     * Link propagators with variables.
     */
    public void declare() {
        for (int p = 0; p < propagators.length; p++) {
            staticPropagationPriority = Math.max(staticPropagationPriority, propagators[p].getPriority().priority);
        }
        for (int v = 0; v < vars.length; v++) {
            vars[v].declareIn(this);
        }
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

    /**
     * Throws a contradiction exception based on <variable, message>
     *
     * @param cause    ICause object causes the exception
     * @param variable involved variable
     * @param message  detailed message
     * @throws ContradictionException expected behavior
     */
    protected void contradiction(ICause cause,  Variable variable, String message) throws ContradictionException {
        solver.getEngine().fails(cause, variable, message);
    }

    public String toString() {
        return "Cstr(" + Arrays.toString(propagators) + ")";
    }
}
