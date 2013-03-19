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
 * Retrieves the maximum element of the set
 *
 * @author Jean-Guillaume Fages
 */
public class PropMaxElement extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar max;
    private SetVar set;
    private int offSet;
    private int[] weights;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Retrieves the maximum element of the set
     * MAX{i | i in setVar} = max
     *
     * @param setVar
     * @param max
     */
    public PropMaxElement(SetVar setVar, IntVar max) {
        this(setVar, null, 0, max);
    }

    /**
     * Retrieves the maximum element induced by set
     * MAX{weight[i-offset] | i in setVar} = max
     *
     * @param setVar
     * @param weights
     * @param offset
     * @param max
     */
    public PropMaxElement(SetVar setVar, int[] weights, int offset, IntVar max) {
        super(new Variable[]{setVar, max}, PropagatorPriority.BINARY);
        this.max = (IntVar) vars[1];
        this.set = (SetVar) vars[0];
        this.weights = weights;
        this.offSet = offset;
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
        for (int j=set.getKernelFirst(); j!=SetVar.END; j=set.getKernelNext()) {
            max.updateLowerBound(get(j), aCause);
        }
        int maxVal = get(set.getEnvelopeFirst());
        int ub = max.getUB();
        for (int j=set.getEnvelopeFirst(); j!=SetVar.END; j=set.getEnvelopeNext()) {
            int k = get(j);
            if (k > ub) {
                set.removeFromEnvelope(j, aCause);
            } else {
                if (maxVal < k) {
                    maxVal = k;
                }
            }
        }
        max.updateUpperBound(maxVal, aCause);
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        propagate(0);
    }

    @Override
    public ESat isEntailed() {
        int lb = max.getLB();
        int ub = max.getUB();
        for (int j=set.getKernelFirst(); j!=SetVar.END; j=set.getKernelNext()) {
            if (get(j) > ub) {
                return ESat.FALSE;
            }
        }
        int maxVal = get(set.getEnvelopeFirst());
        for (int j=set.getEnvelopeFirst(); j!=SetVar.END; j=set.getEnvelopeNext()) {
            if (maxVal < get(j)) {
                maxVal = get(j);
            }
        }
        if (maxVal < lb) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    private int get(int j) {
        return (weights == null) ? j : weights[j - offSet];
    }
}
