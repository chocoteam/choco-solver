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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
 */

package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;

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
     * @param sets array of set variables
     */
    public PropAllDiff(SetVar[] sets) {
        super(sets, PropagatorPriority.LINEAR, true);
        n = sets.length;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            if (vars[i].isInstantiated()) {
                propagate(i, 0);
            }
        }
    }

    @Override
    public void propagate(int idx, int mask) throws ContradictionException {
        if (vars[idx].isInstantiated()) {
            int s = vars[idx].getUB().getSize();
            for (int i = 0; i < n; i++) {
                if (i != idx) {
                    int sei = vars[i].getUB().getSize();
                    int ski = vars[i].getLB().getSize();
                    if (ski >= s - 1 && sei <= s + 1) {
                        int nbSameInKer = 0;
                        int diff = -1;
                        for (int j : vars[idx].getLB())
                            if (vars[i].getLB().contain(j)) {
                                nbSameInKer++;
                            } else {
                                diff = j;
                            }
                        if (nbSameInKer == s) {
                            if (sei == s) { // check diff
                                fails(); // TODO: could be more precise, for explanation purpose
                            } else if (sei == s + 1 && ski < sei) { // force other (if same elements in ker)
                                for (int j: vars[i].getUB())
                                    vars[i].force(j, this);
                            }
                        } else if (sei == s && nbSameInKer == s - 1) { // remove other (if same elements in ker)
                            if (vars[i].getUB().contain(diff)) {
                                vars[i].remove(diff, this);
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
            if (!vars[i].isInstantiated()) {
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
        if (vars[i].getUB().getSize() < vars[i2].getLB().getSize()) return false;
        if (vars[i2].getUB().getSize() < vars[i].getLB().getSize()) return false;
        if (vars[i].isInstantiated() && vars[i2].isInstantiated()) {
            for (int j : vars[i].getLB()) {
                if (!vars[i2].getUB().contain(j)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
