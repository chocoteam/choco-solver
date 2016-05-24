/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.nValue;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Propagator for the atMostNValues constraint
 * The number of distinct values in the set of variables vars is at most equal to nValues
 * No level of consistency but better than BC in general (for enumerated domains with holes)
 *
 * @author Jean-Guillaume Fages
 */
public class PropAtLeastNValues extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int[] concernedValues;
    private int n;
    private int[] mate;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for the NValues constraint
     * The number of distinct values among concerned values in the set of variables vars is exactly equal to nValues
     * No level of consistency for the filtering
     *
     * @param variables       array of integer variables
     * @param concernedValues will be sorted!
     * @param nValues         integer variable
     */
    public PropAtLeastNValues(IntVar[] variables, int[] concernedValues, IntVar nValues) {
        super(ArrayUtils.append(variables, new IntVar[]{nValues}), PropagatorPriority.QUADRATIC, false);
        n = variables.length;
        this.concernedValues = concernedValues;
        mate = new int[concernedValues.length];
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        vars[n].updateUpperBound(n, this);
        int count = 0;
        int countMax = 0;
        for (int i = concernedValues.length - 1; i >= 0; i--) {
            boolean possible = false;
            boolean mandatory = false;
            mate[i] = -1;
            int value = concernedValues[i];
            for (int v = 0; v < n; v++) {
                if (vars[v].contains(value)) {
                    possible = true;
                    if (vars[v].isInstantiated()) {
                        mandatory = true;
                        mate[i] = -2;
                        break;
                    }else{
                        if (mate[i] == -1) {
                            mate[i] = v;
                        } else {
                            mate[i] = -2;
                        }
                    }
                }
            }
            if (possible) {
                countMax++;
            }
            if (mandatory) {
                count++;
            }
        }
        // filtering cardinality variable
        vars[n].updateUpperBound(countMax, this);
        // filtering decision variables
        boolean again = false;
        if (count < countMax && countMax == vars[n].getLB()) {
            for (int i = concernedValues.length - 1; i >= 0; i--) {
                if (mate[i] >= 0) {
                    if(vars[mate[i]].instantiateTo(concernedValues[i], this)){
                        again = true;
                    }
                }
            }
        }
        if (count >= vars[n].getUB()) {
            setPassive();
        }else if(again){
            propagate(0);// fix point is required as not all possible values add a mate
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public ESat isEntailed() {
        int countMin = 0;
        int countMax = 0;
        for (int i = concernedValues.length - 1; i >= 0; i--) {
            boolean possible = false;
            boolean mandatory = false;
            for (int v = 0; v < n; v++) {
                if (vars[v].contains(concernedValues[i])) {
                    possible = true;
                    if (vars[v].isInstantiated()) {
                        mandatory = true;
                        break;
                    }
                }
            }
            if (possible) {
                countMax++;
            }
            if (mandatory) {
                countMin++;
            }
        }
        if (countMin >= vars[n].getUB()) {
            return ESat.TRUE;
        }
        if (countMax < vars[n].getLB()) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

}
