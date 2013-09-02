/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.nary.tree;

import memory.IStateInt;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetFactory;
import util.objects.setDataStructures.SetType;
import util.tools.ArrayUtils;

/**
 * Incremental propagator which restricts the number of loops:
 * |{succs[i]=i+offSet}| = nbLoops
 *
 * @author Jean-Guillaume Fages
 */
public class PropKLoops extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // number of nodes
    private int n;
    // offset (usually 0 but 1 with MiniZinc)
    private int offSet;
    // uninstantiated variables that can be loops
    private ISet possibleLoops;
    private IStateInt nbMinLoops;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Incremental propagator which restricts the number of loops:
     * |{succs[i]=i+offSet}| = nbLoops
     *
     * @param succs
     * @param nbLoops
     * @param offSet
     */
    public PropKLoops(IntVar[] succs, IntVar nbLoops, int offSet) {
        super(ArrayUtils.append(succs, new IntVar[]{nbLoops}), PropagatorPriority.UNARY, true);
        this.n = succs.length;
        this.offSet = offSet;
        this.possibleLoops = SetFactory.makeStoredSet(SetType.SWAP_ARRAY, n, environment);
        this.nbMinLoops = environment.makeInt();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            possibleLoops.clear();
            nbMinLoops.set(0);
            for (int i = 0; i < n; i++) {
                if (vars[i].contains(i + offSet)) {
                    if (vars[i].instantiated()) {
                        nbMinLoops.add(1);
                    } else {
                        possibleLoops.add(i);
                    }
                }
            }
        }
        int nbMin = nbMinLoops.get();
        int nbMax = nbMin + possibleLoops.getSize();
        vars[n].updateLowerBound(nbMin, aCause);
        vars[n].updateUpperBound(nbMax, aCause);
        if (vars[n].instantiated() && nbMin != nbMax) {
            if (vars[n].getValue() == nbMax) {
                for (int i = possibleLoops.getFirstElement(); i >= 0; i = possibleLoops.getNextElement()) {
                    vars[i].instantiateTo(i + offSet, aCause);
                    nbMinLoops.add(1);
                }
                possibleLoops.clear();
            } else if (vars[n].getValue() == nbMin) {
                for (int i = possibleLoops.getFirstElement(); i >= 0; i = possibleLoops.getNextElement()) {
                    vars[i].removeValue(i + offSet, aCause);
                }
                possibleLoops.clear();
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < n) {
            if (possibleLoops.contain(idxVarInProp)) {
                if (vars[idxVarInProp].contains(idxVarInProp + offSet)) {
                    if (vars[idxVarInProp].instantiated()) {
                        nbMinLoops.add(1);
                        possibleLoops.remove(idxVarInProp);
                        vars[n].updateLowerBound(nbMinLoops.get(), aCause);
                        forcePropagate(EventType.FULL_PROPAGATION);
                    }
                } else {
                    possibleLoops.remove(idxVarInProp);
                    vars[n].updateUpperBound(possibleLoops.getSize() + nbMinLoops.get(), aCause);
                    forcePropagate(EventType.FULL_PROPAGATION);
                }
            }
        } else {
            forcePropagate(EventType.FULL_PROPAGATION);
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INT_ALL_MASK();
    }

    @Override
    public ESat isEntailed() {
        int nbMax = 0;
        int nbMin = 0;
        for (int i = 0; i < n; i++) {
            if (vars[i].contains(i + offSet)) {
                nbMax++;
                if (vars[i].instantiated()) {
                    nbMin++;
                }
            }
        }
        if (vars[n].getLB() > nbMax || vars[n].getUB() < nbMin) {
            return ESat.FALSE;
        }
        if (nbMin == nbMax && vars[n].instantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
