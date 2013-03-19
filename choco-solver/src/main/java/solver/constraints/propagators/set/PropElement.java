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
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetFactory;
import util.tools.ArrayUtils;

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

    private ISet constructiveDisjunction;
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
     * @param index
     * @param array
     * @param offSet
     * @param set
     */
    public PropElement(IntVar index, SetVar[] array, int offSet, SetVar set) {
        super(ArrayUtils.append(array, new Variable[]{set, index}), PropagatorPriority.LINEAR);
        this.index = (IntVar) vars[vars.length - 1];
        this.set = (SetVar) vars[vars.length - 2];
        this.array = new SetVar[array.length];
        for (int i = 0; i < array.length; i++) {
            this.array[i] = (SetVar) vars[i];
        }
        this.offSet = offSet;
        constructiveDisjunction = SetFactory.makeLinkedList(false);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx <= array.length) {
            return EventType.ADD_TO_KER.mask + EventType.REMOVE_FROM_ENVELOPE.mask;
        } else {
            return EventType.INT_ALL_MASK();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        forcePropagate(EventType.FULL_PROPAGATION);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        index.updateLowerBound(offSet, aCause);
        index.updateUpperBound(array.length - 1 + offSet, aCause);
        if (index.instantiated()) {
            // filter set and array
            setEq(set, array[index.getValue() - offSet]);
            setEq(array[index.getValue() - offSet], set);
        } else {
            // filter index
            int ub = index.getUB();
            boolean noEmptyKer = true;
            for (int i = index.getLB(); i <= ub; i = index.nextValue(i)) {
                if (disjoint(set, array[i - offSet]) || disjoint(array[i - offSet], set)) {// array[i] != set
                    index.removeValue(i, aCause);
                } else {
                    if (array[i - offSet].getKernelSize() == 0) {
                        noEmptyKer = false;
                    }
                }
            }
            ub = index.getUB();
            // filter set (constructive disjunction)
            if (noEmptyKer) {// from ker
                constructiveDisjunction.clear();
                ISet tmpSet = array[index.getLB() - offSet].getKernel();
                for (int j = tmpSet.getFirstElement(); j >= 0; j = tmpSet.getNextElement()) {
                    constructiveDisjunction.add(j);
                }
                tmpSet = set.getKernel();
                for (int j = tmpSet.getFirstElement(); j >= 0; j = tmpSet.getNextElement()) {
                    constructiveDisjunction.remove(j);
                }
                tmpSet = constructiveDisjunction;
                for (int j = tmpSet.getFirstElement(); j >= 0; j = tmpSet.getNextElement()) {
                    for (int i = index.nextValue(index.getLB()); i <= ub; i = index.nextValue(i)) {
                        if (!array[i - offSet].getKernel().contain(j)) {
                            tmpSet.remove(j);
                            break;
                        }
                    }
                }
                for (int j = tmpSet.getFirstElement(); j >= 0; j = tmpSet.getNextElement()) {
                    set.addToKernel(j, aCause);
                }
            }
            if (!set.instantiated()) {// from env
                for (int j=set.getEnvelopeFirstElement(); j!=SetVar.END; j=set.getEnvelopeNextElement()) {
                    boolean valueExists = false;
                    for (int i = index.getLB(); i <= ub; i = index.nextValue(i)) {
                        if (array[i - offSet].envelopeContains(j)) {
                            valueExists = true;
                            break;
                        }
                    }
                    if (!valueExists) {
                        set.removeFromEnvelope(j, aCause);
                    }
                }
            }
        }
    }

    private void setEq(SetVar s1, SetVar s2) throws ContradictionException {
        for (int j=s2.getKernelFirstElement(); j!=SetVar.END; j=s2.getKernelNextElement()) {
            s1.addToKernel(j, aCause);
        }
        for (int j=s1.getEnvelopeFirstElement(); j!=SetVar.END; j=s1.getEnvelopeNextElement()) {
            if (!s2.envelopeContains(j)) {
                s1.removeFromEnvelope(j, aCause);
            }
        }
    }

    private boolean disjoint(SetVar s1, SetVar s2) {
        for (int j=s2.getKernelFirstElement(); j!=SetVar.END; j=s2.getKernelNextElement()) {
            if (!s1.envelopeContains(j)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ESat isEntailed() {
        if (index.instantiated()) {
            if (disjoint(set, array[index.getValue() - offSet]) || disjoint(array[index.getValue() - offSet], set)) {
                return ESat.FALSE;
            } else {
                if (set.instantiated() && array[index.getValue() - offSet].instantiated()) {
                    return ESat.TRUE;
                } else {
                    return ESat.UNDEFINED;
                }
            }
        }
        return ESat.UNDEFINED;
    }
}
