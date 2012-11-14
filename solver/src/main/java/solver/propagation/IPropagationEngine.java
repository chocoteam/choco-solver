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
package solver.propagation;

import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.Variable;

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
     * Initializes <code>this</code>
     *
     * @param solver the solver
     */
    void init(Solver solver);

    /**
     * Reach a fixpoint
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
     * @throws ContradictionException
     */
    void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException;

    void onPropagatorExecution(Propagator propagator);

    /**
     * Set the propagator as inactivated within the propagation engine
     *
     * @param propagator propagator to desactivate
     */
    void desactivatePropagator(Propagator propagator);
}
