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
package org.chocosolver.solver.propagation;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * An interface for propagation engines, it defines every required services.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/12/11
 */
public interface IPropagationEngine extends Serializable {

    final Logger LOGGER = LoggerFactory.getLogger(IPropagationEngine.class);

    public enum Trace {
        ;

        public static void printPropagation(Variable v, Propagator p) {
            LOGGER.debug("[P] {}", "(" + v + "::" + p + ")");
        }

        public static void printModification(Variable v, IEventType e, ICause c) {
            LOGGER.debug("\t[M] {} {} ({})", v, e, c);
        }


        public static void printSchedule(Propagator p) {
            LOGGER.debug("\t\t[S] {}", p);
        }

        public static void printAlreadySchedule(Propagator p) {
            LOGGER.debug("\t\t[s] {}", p);
        }
    }

    /**
     * Is the engine initialized?
     * Important for dynamic addition of constraints
     *
     * @return
     */
    boolean isInitialized();

    /**
     * Launch the proapagation, ie, active propagators if necessary, then reach a fix point
     *
     * @throws ContradictionException if a contradiction occurrs
     */
    void propagate() throws ContradictionException;

    /**
     * Flush <code>this</code>, ie. remove every pending events
     */
    void flush();

    void fails(ICause cause, Variable variable, String message) throws ContradictionException;

    ContradictionException getContradictionException();

    void clear();

    //********************************//
    //      SERVICES FOR UPDATING     //
    //********************************//

    /**
     * Take into account the modification of a variable
     *
     * @param variable modified variable
     * @param type
     * @throws ContradictionException
     */
    void onVariableUpdate(Variable variable, IEventType type, ICause cause) throws ContradictionException;

    void delayedPropagation(Propagator propagator, PropagatorEventType type) throws ContradictionException;

    void onPropagatorExecution(Propagator propagator);

    /**
     * Set the propagator as inactivated within the propagation engine
     *
     * @param propagator propagator to desactivate
     */
    void desactivatePropagator(Propagator propagator);

    void dynamicAddition(Constraint c, boolean permanent);

    void dynamicDeletion(Constraint c);
}
