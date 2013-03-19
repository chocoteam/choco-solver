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
import solver.variables.SetVar;
import util.ESat;

/**
 * Ensures that all sets are different
 *
 * @author Jean-Guillaume Fages
 */
public class PropAllDiff extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Ensures that all sets are different
     *
     * @param sets
     */
    public PropAllDiff(SetVar[] sets) {
        super(sets, PropagatorPriority.LINEAR);
        n = sets.length;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.ADD_TO_KER.mask + EventType.REMOVE_FROM_ENVELOPE.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            if (vars[i].instantiated()) {
                propagate(i, 0);
            }
        }
    }

    @Override
    public void propagate(int idx, int mask) throws ContradictionException {
        if (vars[idx].instantiated()) {
            int s = vars[idx].getEnvelopeSize();
            for (int i = 0; i < n; i++) {
                if (i != idx) {
                    int sei = vars[i].getEnvelopeSize();
                    int ski = vars[i].getKernelSize();
                    if (ski >= s - 1 && sei <= s + 1) {
                        int nbSameInKer = 0;
                        int diff = -1;
                        for (int j=vars[idx].getKernelFirst(); j!=SetVar.END; j=vars[idx].getKernelNext())
                            if (!vars[i].envelopeContains(j)){
                                nbSameInKer++;
							}else{
                                diff = j;
							}
                        if (nbSameInKer == s) {
                            if (sei == s) { // check diff
                                contradiction(vars[i], "");
                            } else if (sei == s + 1 && ski < sei) { // force other (if same elements in ker)
                                for (int j=vars[i].getEnvelopeFirst(); j!=SetVar.END; j=vars[i].getEnvelopeNext())
                                    vars[i].addToKernel(j, aCause);
                            }
                        } else if (sei == s && nbSameInKer == s - 1) { // remove other (if same elements in ker)
                            if (vars[i].envelopeContains(diff)) {
                                vars[i].removeFromEnvelope(diff, aCause);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            if (!vars[i].instantiated()) {
                return ESat.UNDEFINED;
            }
            for (int i2 = i + 1; i2 < n; i2++) {
                if (same(i, i2)) {
                    return ESat.FALSE;
                }
            }
        }
        return ESat.TRUE;
    }

    private boolean same(int i, int i2) {
        if (vars[i].getEnvelopeSize() < vars[i2].getKernelSize()) return false;
        if (vars[i2].getEnvelopeSize() < vars[i].getKernelSize()) return false;
        if (vars[i].instantiated() && vars[i2].instantiated()) {
            for (int j=vars[i].getKernelFirst(); j!=SetVar.END; j=vars[i].getKernelNext()) {
                if (!vars[i2].envelopeContains(j)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
