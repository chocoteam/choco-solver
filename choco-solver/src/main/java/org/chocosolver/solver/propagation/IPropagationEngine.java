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
package org.chocosolver.solver.propagation;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;

import java.io.Serializable;

/**
 * An interface for propagation engines, it defines every required services.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/12/11
 */
public interface IPropagationEngine extends Serializable {


    /**
     * Build up internal structure, if not yet done, in order to allow propagation.
     * If new constraints are added after having initializing the engine, dynamic addition is used.
     * A call to clear erase the internal structure, and allow new initialisation.
     */
    default void initialize(){}

    /**
     * Is the engine initialized?
     * Important for dynamic addition of constraints
     *
     * @return true if the engine has been initialized
     */
    default boolean isInitialized() {
        return false;
    }

    /**
     * Launch the proapagation, ie, active propagators if necessary, then reach a fix point
     *
     * @throws ContradictionException if a contradiction occurrs
     */
    default void propagate() throws ContradictionException {
    }

    /**
     * Flush <code>this</code>, ie. remove every pending events
     */
    default void flush() {
    }

    default void fails(ICause cause, Variable variable, String message) throws ContradictionException {
    }

    default ContradictionException getContradictionException() {
        throw new UnsupportedOperationException("no propagation engine has been defined");
    }

    default void clear() {
    }

    //********************************//
    //      SERVICES FOR UPDATING     //
    //********************************//

    /**
     * Take into account the modification of a variable
     *
     * @param variable modified variable
     * @param type     type of modification event
     * @throws ContradictionException
     */
    default void onVariableUpdate(Variable variable, IEventType type, ICause cause) throws ContradictionException {
    }

    default void delayedPropagation(Propagator propagator, PropagatorEventType type) throws ContradictionException {
    }

    default void onPropagatorExecution(Propagator propagator) {
    }

    /**
     * Set the propagator as inactivated within the propagation engine
     *
     * @param propagator propagator to desactivate
     */
    default void desactivatePropagator(Propagator propagator) {
    }

    /**
     * Add a constraint to the propagation engine
     *
     * @param permanent does the constraint is permanently added
     * @param ps        propagators to add
     */
    default void dynamicAddition(boolean permanent, Propagator... ps) {
    }

    /**
     * Update the scope of variable of a propagator (addition or deletion are allowed -- p.vars are scanned)
     *
     * @param p a propagator
     */
    default void updateInvolvedVariables(Propagator p) {
    }


    /**
     * State that the propagator needs to be propagated (coarse event) on backtrack.
     * For dynamic propagator only, such as PropSat or PropNogoods.
     *
     * @param p a propagator
     */
    default void propagateOnBacktrack(Propagator p) {
    }

    /**
     * Delete the list of propagators in input from the engine
     *
     * @param ps a list of propagators
     */
    default void dynamicDeletion(Propagator... ps) {
    }
}
