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

package solver.constraints.set;

import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;
import util.ESat;

/**
 * Retrieves the minimum element of the set
 * the set must not be empty
 *
 * @author Jean-Guillaume Fages
 */
public class PropMinElement extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar min;
    private SetVar set;
    private int offSet;
    private int[] weights;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Retrieves the minimum element of the set
     * MIN(i | i in setVar) = min
     *
     * @param setVar
     * @param min
     */
    public PropMinElement(SetVar setVar, IntVar min) {
        this(setVar, null, 0, min);
    }

    /**
     * Retrieves the minimum element induced by setVar
     * MIN{weights[i-offSet] | i in setVar} = min
     *
     * @param setVar
     * @param weights
     * @param offSet
     * @param min
     */
    public PropMinElement(SetVar setVar, int[] weights, int offSet, IntVar min) {
        super(new Variable[]{setVar, min}, PropagatorPriority.BINARY, true);
        this.min = (IntVar) vars[1];
        this.set = (SetVar) vars[0];
        this.weights = weights;
        this.offSet = offSet;
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
            min.updateUpperBound(get(j), aCause);
        }
        int minVal = Integer.MAX_VALUE;
        int lb = min.getLB();
        for (int j=set.getEnvelopeFirst(); j!=SetVar.END; j=set.getEnvelopeNext()) {
            int k = get(j);
            if (k < lb) {
                set.removeFromEnvelope(j, aCause);
            } else {
                if (minVal > k) {
                    minVal = k;
                }
            }
        }
        min.updateLowerBound(minVal, aCause);
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        propagate(0);
    }

    @Override
    public ESat isEntailed() {
		if(set.getEnvelopeSize()==0){
			return ESat.FALSE;
		}
        int lb = min.getLB();
        int ub = min.getUB();
        for (int j=set.getKernelFirst(); j!=SetVar.END; j=set.getKernelNext()) {
            if (get(j) < lb) {
                return ESat.FALSE;
            }
        }
        int minVal = get(set.getEnvelopeFirst());
        for (int j=set.getEnvelopeFirst(); j!=SetVar.END; j=set.getEnvelopeNext()) {
            if (minVal > get(j)) {
                minVal = get(j);
            }
        }
        if (minVal > ub) {
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
