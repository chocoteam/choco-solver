/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Propagator for element constraint over sets
 * states that
 * array[index-offSet] = set
 *
 * @author Jean-Guillaume Fages
 */
public class PropElement extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private TIntArrayList constructiveDisjunction;
    private IntVar index;
    private SetVar set;
    private SetVar[] array;
    private int offSet;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for element constraint over sets
     * states that array[index-offSet] = set
     *
     * @param index integer variable
     * @param array array of set variables
     * @param offSet int
     * @param set set variable
     */
    public PropElement(IntVar index, SetVar[] array, int offSet, SetVar set) {
        super(ArrayUtils.append(array, new Variable[]{set, index}), PropagatorPriority.LINEAR, false);
        this.index = (IntVar) vars[vars.length - 1];
        this.set = (SetVar) vars[vars.length - 2];
        this.array = new SetVar[array.length];
        for (int i = 0; i < array.length; i++) {
            this.array[i] = (SetVar) vars[i];
        }
        this.offSet = offSet;
        constructiveDisjunction = new TIntArrayList();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        index.updateBounds(offSet, array.length - 1 + offSet, this);
        if (index.isInstantiated()) {
            // filter set and array
            setEq(set, array[index.getValue() - offSet]);
            setEq(array[index.getValue() - offSet], set);
        } else {
            // filter index
            int ub = index.getUB();
            boolean noEmptyKer = true;
            for (int i = index.getLB(); i <= ub; i = index.nextValue(i)) {
                if (disjoint(set, array[i - offSet]) || disjoint(array[i - offSet], set)) {// array[i] != set
                    index.removeValue(i, this);
                } else {
                    if (array[i - offSet].getLB().getSize() == 0) {
                        noEmptyKer = false;
                    }
                }
            }
            ub = index.getUB();
            // filter set (constructive disjunction)
            if (noEmptyKer) {// from ker
                constructiveDisjunction.clear();
                SetVar v = array[index.getLB() - offSet];
                for (int j : v.getLB()) {
                    if (!set.getLB().contain(j)) {
                        constructiveDisjunction.add(j);
                    }
                }
                for (int cd = constructiveDisjunction.size() - 1; cd >= 0; cd--) {
                    int j = constructiveDisjunction.get(cd);
                    for (int i = index.nextValue(index.getLB()); i <= ub; i = index.nextValue(i)) {
                        if (!array[i - offSet].getLB().contain(j)) {
                            constructiveDisjunction.remove(j);
                            break;
                        }
                    }
                }
                for (int cd = constructiveDisjunction.size() - 1; cd >= 0; cd--) {
                    int j = constructiveDisjunction.get(cd);
                    set.force(j, this);
                }
            }
            if (!set.isInstantiated()) {// from env
                for (int j : set.getUB()) {
                    boolean valueExists = false;
                    for (int i = index.getLB(); i <= ub; i = index.nextValue(i)) {
                        if (array[i - offSet].getUB().contain(j)) {
                            valueExists = true;
                            break;
                        }
                    }
                    if (!valueExists) {
                        set.remove(j, this);
                    }
                }
            }
        }
    }

    private void setEq(SetVar s1, SetVar s2) throws ContradictionException {
        for (int j : s2.getLB()) {
            s1.force(j, this);
        }
        for (int j : s1.getUB()) {
            if (!s2.getUB().contain(j)) {
                s1.remove(j, this);
            }
        }
    }

    private boolean disjoint(SetVar s1, SetVar s2) {
        for (int j : s2.getLB()) {
            if (!s1.getUB().contain(j)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ESat isEntailed() {
        if (index.isInstantiated()) {
            if (disjoint(set, array[index.getValue() - offSet]) || disjoint(array[index.getValue() - offSet], set)) {
                return ESat.FALSE;
            } else {
                if (set.isInstantiated() && array[index.getValue() - offSet].isInstantiated()) {
                    return ESat.TRUE;
                } else {
                    return ESat.UNDEFINED;
                }
            }
        }
        return ESat.UNDEFINED;
    }

}
