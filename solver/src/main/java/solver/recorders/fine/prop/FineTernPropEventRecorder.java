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
package solver.recorders.fine.prop;

import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.variables.Variable;

/**
 * A fine event recorder prop-oriented dedicated to ternary propagators
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/06/12
 */
public final class FineTernPropEventRecorder<V extends Variable> extends FinePropEventRecorder<V> {

    public FineTernPropEventRecorder(V[] variables, Propagator<V> vPropagator, int[] idxVinPs, Solver solver, IPropagationEngine engine) {
        super(variables, vPropagator, idxVinPs, solver, engine);
    }

    @Override
    public boolean execute() throws ContradictionException {
        if (DEBUG_PROPAG) LoggerFactory.getLogger("solver").info("* {}", this.toString());
        _execute(0);
        _execute(1);
        _execute(2);
        return true;
    }


    @Override
    public void flush() {
        this.evtmasks[0] = 0;
        this.evtmasks[1] = 0;
        this.evtmasks[2] = 0;
    }

    @Override
    public void virtuallyExecuted(Propagator propagator) {
        assert this.propagators[PINDEX] == propagator : "wrong propagator";
        this.evtmasks[0] = 0;
        this.evtmasks[1] = 0;
        this.evtmasks[2] = 0;
        if (enqueued) {
            scheduler.remove(this);
        }
    }

    @Override
    public void desactivate(Propagator<V> element) {
        super.desactivate(element);
        this.evtmasks[0] = 0;
        this.evtmasks[1] = 0;
        this.evtmasks[2] = 0;
    }
}
