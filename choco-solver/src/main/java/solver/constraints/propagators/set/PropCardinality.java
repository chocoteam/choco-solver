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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
 */

package solver.constraints.propagators.set;

import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;
import util.ESat;

/**
 * A propagator ensuring that |set| = card
 *
 * @author Jean-Guillaume Fages
 */
public class PropCardinality extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar card;
    private SetVar set;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator ensuring that |setVar| = cardinality
     *
     * @param setVar
     * @param cardinality
     */
    public PropCardinality(SetVar setVar, IntVar cardinality) {
        super(new Variable[]{setVar, cardinality}, PropagatorPriority.BINARY);
        this.set = (SetVar) vars[0];
        this.card = (IntVar) vars[1];
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) return EventType.ADD_TO_KER.mask + EventType.REMOVE_FROM_ENVELOPE.mask;
        else return EventType.INSTANTIATE.mask + EventType.DECUPP.mask + EventType.INCLOW.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int k = set.getKernelSize();
        card.updateLowerBound(k, aCause);
        int e = set.getEnvelopeSize();
        card.updateUpperBound(e, aCause);
        if (card.instantiated()) {
            int c = card.getValue();
            ISet env = set.getEnvelope();
            if (c == k) {
			// TODO CHECK BIZARE D'AVOIR KER = ENVELOPE
                ISet ker = set.getEnvelope();
                for (int j=set.getEnvelopeFirstElement(); j!=SetVar.END; j=set.getEnvelopeNextElement()) {
                    if (!ker.contain(j)) {
                        set.removeFromEnvelope(j, aCause);
                    }
                }
            } else if (c == e) {
                for (int j=set.getEnvelopeFirstElement(); j!=SetVar.END; j=set.getEnvelopeNextElement()) {
                    set.addToKernel(j, aCause);
                }
            }
        }
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        propagate(0);
    }

    @Override
    public ESat isEntailed() {
        int k = set.getKernel().getSize();
        int e = set.getEnvelope().getSize();
        if (k > card.getUB() || e < card.getLB()) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
