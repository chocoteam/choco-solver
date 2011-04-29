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

package solver.requests;

import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.Variable;

/**
 * A propagation request storing events occuring on a variable to inform a propagator.
 * It stores mask event (type of event) and pointers to removed values to propagate (if any).
 * <br/>
 * These paramaters are lazy cleared when necessary: usually before updating the request and before treating events.
 * </br>
 *
 * @author Charles Prud'homme
 * @since 23 sept. 2010
 */
public class InitializeRequest<V extends Variable, P extends Propagator<V>> extends AbstractRequest<V, P> {

    public InitializeRequest(P propagator, int idxInProp) {
        super(propagator, null, idxInProp);
    }

    @Override
    public int fromDelta() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int toDelta() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void filter() throws ContradictionException {
        assert (propagator.isActive());
        propagator.filterCall++;
        // events on that propagator should be removed first
        // to avoid conflict and useless call
        for (int i = 0; i < propagator.nbRequests(); i++) {
            if (propagator.getRequest(i).enqueued()) {
                engine.remove(propagator.getRequest(i));
            }
        }
        propagator.propagate();
    }

    @Override
    public void update(EventType e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "(" + propagator.getConstraint().toString() + ")";
    }
}
