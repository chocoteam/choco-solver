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

package solver.constraints.propagators.set;

import common.ESat;
import common.util.objects.setDataStructures.ISet;
import common.util.objects.setDataStructures.SetFactory;
import common.util.objects.setDataStructures.SetType;
import common.util.tools.ArrayUtils;
import memory.IStateInt;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;

/**
 * Restricts the number of empty sets
 * |{s in sets such that |s|=0}| = nbEmpty
 *
 * @author Jean-Guillaume Fages
 */
public class PropNbEmpty extends Propagator<Variable> {

    private SetVar[] sets;
    private IntVar nbEmpty;
    private int n;
    private ISet canBeEmpty, isEmpty;
    private IStateInt nbAlreadyEmpty, nbMaybeEmpty;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Restricts the number of empty sets
     * |{s in sets such that |s|=0}| = nbEmpty
     *
     * @param sets
     * @param nbEmpty
     */
    public PropNbEmpty(SetVar[] sets, IntVar nbEmpty) {
        super(ArrayUtils.append(sets, new Variable[]{nbEmpty}), PropagatorPriority.UNARY);
        this.n = sets.length;
        this.sets = new SetVar[sets.length];
        for (int i = 0; i < sets.length; i++) {
            this.sets[i] = (SetVar) vars[i];
        }
        this.nbEmpty = (IntVar) vars[n];
        this.canBeEmpty = SetFactory.makeStoredSet(SetType.SWAP_ARRAY, n, environment);
        this.isEmpty = SetFactory.makeStoredSet(SetType.SWAP_ARRAY, n, environment);
        this.nbAlreadyEmpty = environment.makeInt();
        this.nbMaybeEmpty = environment.makeInt();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx < n) {
            if (isEmpty.contain(vIdx)) {
                throw new UnsupportedOperationException("The variable's domain is already empty, how can it be modified?");
            }
            if (canBeEmpty.contain(vIdx)) {
                return EventType.REMOVE_FROM_ENVELOPE.mask + EventType.ADD_TO_KER.mask;
            } else {
                return EventType.VOID.mask;
            }
        } else {
            return EventType.INSTANTIATE.mask;
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            int nbMin = 0;
            int nbMax = 0;
            canBeEmpty.clear();
            isEmpty.clear();
            for (int i = 0; i < n; i++) {
                if (sets[i].getKernel().getSize() == 0) {
                    nbMax++;
                    if (sets[i].getEnvelope().getSize() == 0) {
                        nbMin++;
                        isEmpty.add(i);
                    } else {
                        canBeEmpty.add(i);
                    }
                }
            }
            nbAlreadyEmpty.set(nbMin);
            nbMaybeEmpty.set(nbMax - nbMin);
        }
        filter();
    }

    @Override
    public void propagate(int v, int mask) throws ContradictionException {
        if (v < n) {
            if (!canBeEmpty.contain(v)) {
                throw new UnsupportedOperationException();
            }
            if (sets[v].getKernel().getSize() > 0) {
                canBeEmpty.remove(v);
                nbMaybeEmpty.add(-1);
            } else {
                if (sets[v].getEnvelope().getSize() == 0) {
                    isEmpty.add(v);
                    canBeEmpty.remove(v);
                    nbMaybeEmpty.add(-1);
                    nbAlreadyEmpty.add(1);
                }
            }
        }
        filter();
    }

    public void filter() throws ContradictionException {
        int nbMin = nbAlreadyEmpty.get();
        int nbMax = nbMin + nbMaybeEmpty.get();
        nbEmpty.updateLowerBound(nbMin, aCause);
        nbEmpty.updateUpperBound(nbMax, aCause);
        ///////////////////////////////////////
        if (nbEmpty.instantiated() && nbMin < nbMax) {
            if (nbEmpty.getValue() == nbMax) {
                for (int i = canBeEmpty.getFirstElement(); i >= 0; i = canBeEmpty.getNextElement()) {
                    ISet s = sets[i].getEnvelope();
                    for (int j = s.getFirstElement(); j >= 0; j = s.getNextElement()) {
                        sets[i].removeFromEnvelope(j, aCause);
                    }
                    canBeEmpty.remove(i);
                    isEmpty.add(i);
                }
                setPassive();
            }
            if (nbEmpty.getValue() == nbMin) {
                boolean allFixed = true;
                for (int i = canBeEmpty.getFirstElement(); i >= 0; i = canBeEmpty.getNextElement()) {
                    ISet s = sets[i].getEnvelope();
                    ISet k = sets[i].getKernel();
                    if (s.getSize() == k.getSize() + 1) {
                        for (int j = s.getFirstElement(); j >= 0; j = s.getNextElement()) {
                            sets[i].addToKernel(j, aCause);
                        }
                        canBeEmpty.remove(i);
                    } else {
                        allFixed = false;
                    }
                }
                if (allFixed) {
                    setPassive();
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int nbMin = 0;
        int nbMax = 0;
        for (int i = 0; i < n; i++) {
            if (sets[i].getKernel().getSize() == 0) {
                nbMax++;
                if (sets[i].getEnvelope().getSize() == 0) {
                    nbMin++;
                }
            }
        }
        if (nbEmpty.getLB() > nbMax || nbEmpty.getUB() < nbMin) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
