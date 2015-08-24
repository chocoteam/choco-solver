/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.set;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;

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
     * @param sets    array of set variables
     * @param nbEmpty integer variable
     */
    public PropNbEmpty(SetVar[] sets, IntVar nbEmpty) {
        super(ArrayUtils.append(sets, new Variable[]{nbEmpty}), PropagatorPriority.UNARY, true);
        this.n = sets.length;
        this.sets = new SetVar[sets.length];
        for (int i = 0; i < sets.length; i++) {
            this.sets[i] = (SetVar) vars[i];
        }
        this.nbEmpty = (IntVar) vars[n];
        IEnvironment environment = solver.getEnvironment();
        this.canBeEmpty = SetFactory.makeStoredSet(SetType.BIPARTITESET, n, solver);
        this.isEmpty = SetFactory.makeStoredSet(SetType.BIPARTITESET, n, solver);
        this.nbAlreadyEmpty = environment.makeInt();
        this.nbMaybeEmpty = environment.makeInt();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************


    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx < vars.length - 1) {
            return SetEventType.all();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            int nbMin = 0;
            int nbMax = 0;
            canBeEmpty.clear();
            isEmpty.clear();
            for (int i = 0; i < n; i++) {
                if (sets[i].getKernelSize() == 0) {
                    nbMax++;
                    if (sets[i].getEnvelopeSize() == 0) {
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
            if (canBeEmpty.contain(v)) {
                if (sets[v].getKernelSize() > 0) {
                    canBeEmpty.remove(v);
                    nbMaybeEmpty.add(-1);
                } else {
                    if (sets[v].getEnvelopeSize() == 0) {
                        isEmpty.add(v);
                        canBeEmpty.remove(v);
                        nbMaybeEmpty.add(-1);
                        nbAlreadyEmpty.add(1);
                    }
                }
            }
        }
        filter();
    }

    public void filter() throws ContradictionException {
        int nbMin = nbAlreadyEmpty.get();
        int nbMax = nbMin + nbMaybeEmpty.get();
        nbEmpty.updateLowerBound(nbMin, this);
        nbEmpty.updateUpperBound(nbMax, this);
        ///////////////////////////////////////
        if (nbEmpty.isInstantiated() && nbMin < nbMax) {
            if (nbEmpty.getValue() == nbMax) {
                for (int i = canBeEmpty.getFirstElement(); i >= 0; i = canBeEmpty.getNextElement()) {
                    for (int j = sets[i].getEnvelopeFirst(); j != SetVar.END; j = sets[i].getEnvelopeNext()) {
                        sets[i].removeFromEnvelope(j, this);
                    }
                    canBeEmpty.remove(i);
                    isEmpty.add(i);
                }
                setPassive();
            }
            if (nbEmpty.getValue() == nbMin) {
                boolean allFixed = true;
                for (int i = canBeEmpty.getFirstElement(); i >= 0; i = canBeEmpty.getNextElement()) {
                    if (sets[i].getEnvelopeSize() == 1) {
                        sets[i].addToKernel(sets[i].getEnvelopeFirst(), this);
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
            if (sets[i].getKernelSize() == 0) {
                nbMax++;
                if (sets[i].getEnvelopeSize() == 0) {
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
