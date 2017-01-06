/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.propagation;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;



/**
 * An interface for propagation engines, it defines every required services.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/12/11
 */
public interface IPropagationEngine  {

    enum Trace {;

        public static void printFirstPropagation(Propagator p) {
            p.getModel().getSolver().getOut().printf("[A] %s\n", p);
        }

        public static void printPropagation(Variable v, Propagator p) {
            if (v == null) {
                p.getModel().getSolver().getOut().printf("[P] %s\n", p);
            } else {
                p.getModel().getSolver().getOut().printf("[P] %s on %s\n", v, p);
            }
        }

        public static void printModification(Variable v, IEventType e, ICause c) {
            v.getModel().getSolver().getOut().printf("\t[M] %s %s b/c %s\n", v, e, c );
        }


        public static void printFineSchedule(Propagator p) {
            p.getModel().getSolver().getOut().printf("\t\t[FS] %s\n", p);
        }

        public static void printCoarseSchedule(Propagator p) {
            p.getModel().getSolver().getOut().printf("\t\t[CS] %s\n", p);
        }
    }

    /**
     * Build up internal structure, if not yet done, in order to allow propagation.
     * If new constraints are added after having initializing the engine, dynamic addition is used.
     * A call to clear erase the internal structure, and allow new initialisation.
     * @throws SolverException if a constraint is declared more than once in this propagation engine
     */
    default void initialize() throws SolverException{}

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

    /**
     * Throw a contradiction exception
     * @param cause origin of the failure
     * @param variable can be null
     * @param message can be null
     * @throws ContradictionException failure forced
     */
    default void fails(ICause cause, Variable variable, String message) throws ContradictionException {
    }

    /**
     * @return the (unique) contradiction attached to this propagation engine
     */
    default ContradictionException getContradictionException() {
        throw new UnsupportedOperationException("no propagation engine has been defined");
    }

    /**
     * Clear internal structures
     */
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
     * @param cause origin of the modification
     */
    default void onVariableUpdate(Variable variable, IEventType type, ICause cause) {
    }

    /**
     * Exeucte a delayed propagator
     * @param propagator propagator to execute
     * @param type type of event to execute
     * @throws ContradictionException if a failure is encountered
     */
    default void delayedPropagation(Propagator propagator, PropagatorEventType type) throws ContradictionException {
    }

    /**
     * Action to do when a propagator is executed
     * @param propagator propagator to execute
     */
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
     * * @throws SolverException if a constraint is declared more than once in this propagation engine
     */
    default void dynamicAddition(boolean permanent, Propagator... ps) throws SolverException{
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
