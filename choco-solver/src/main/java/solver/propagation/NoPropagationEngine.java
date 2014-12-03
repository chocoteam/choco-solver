/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 08/02/13
 * Time: 20:30
 */

package org.chocosolver.solver.propagation;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;

public enum NoPropagationEngine implements IPropagationEngine {

    SINGLETON {
        //***********************************************************************************
        // METHODS
        //***********************************************************************************

        private final ContradictionException e = new ContradictionException();

        @Override
        public boolean isInitialized() {
            return false;
        }

        @Override
        public void propagate() throws ContradictionException {
            throw new UnsupportedOperationException("no propagation engine has been defined");
        }

        @Override
        public void flush() {
        }

        @Override
        public void fails(ICause cause, Variable variable, String message) throws ContradictionException {
            /*throw new UnsupportedOperationException("A failure occurred before a propagation engine has been defined." +
                    "This probably means that one variable domain has been wiped out (i.e. the problem has no solution)" +
                  "before starting resolution.");*/
            throw e.set(cause, variable, message);
        }

        @Override
        public ContradictionException getContradictionException() {
            return e;
            /*throw new UnsupportedOperationException("A failure occurred before a propagation engine has been defined." +
                    "This probably means that one variable domain has been wiped out (i.e. the problem has no solution)" +
                  "before starting resolution.");*/
        }

        @Override
        public void clear() {
        }

        @Override
        public void onVariableUpdate(Variable variable, IEventType type, ICause cause) throws ContradictionException {
        }

        @Override
        public void delayedPropagation(Propagator propagator, PropagatorEventType type) throws ContradictionException {

        }

        @Override
        public void onPropagatorExecution(Propagator propagator) {
        }

        @Override
        public void desactivatePropagator(Propagator propagator) {
        }

        @Override
        public void dynamicAddition(Constraint c, boolean permanent) {
        }

        @Override
        public void dynamicDeletion(Constraint c) {
        }
    }
}
