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

package solver.recorders.conditions;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import solver.constraints.propagators.Propagator;
import solver.recorders.IEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * A simple condition based on number of instantiated variables.
 * Recorders are scheduled on variable instantiation.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/03/11
 */
public class IsInstantiated<R extends IEventRecorder> extends AbstractCondition<R> {

    final IStateInt nbVarInstantiated;
    final IntVar variable;

    public IsInstantiated(IEnvironment environment, IntVar variable) {
        super(environment);
        nbVarInstantiated = environment.makeInt();
        this.variable = variable;
    }

    @Override
    boolean isValid() {
        return variable.instantiated();
    }

    @Override
    boolean alwaysValid() {
        return true;
    }

    @Override
    void update(R recorder, Propagator propagator, EventType event) {
    }
}
