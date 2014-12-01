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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 21/06/12
 * Time: 19:07
 */

package org.chocosolver.solver.constraints.nary;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.BitSet;

/**
 * !redundant propagator!
 */
public class PropKnapsack extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int[] weigth, energy, order;
    private double[] ratio;
    private int n;
    private IntVar capacity, power;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropKnapsack(IntVar[] itemOccurence, IntVar capacity, IntVar power,
                        int[] weight, int[] energy) {
        super(ArrayUtils.append(itemOccurence, new IntVar[]{capacity, power}), PropagatorPriority.LINEAR, true);
        this.weigth = weight;
        this.energy = energy;
        this.n = itemOccurence.length;
        this.capacity = vars[n];
        this.power = vars[n + 1];
        this.order = new int[n];
        this.ratio = new double[n];
        for (int i = 0; i < n; i++) {
            ratio[i] = (double) (energy[i]) / (double) (weight[i]);
        }// sort (could be improved)
        BitSet in = new BitSet(n);
        double best = -1;
        int index = 0;
        for (int i = 0; i < n; i++) {
            int item = -1;
            for (int o = in.nextClearBit(0); o < n; o = in.nextClearBit(o + 1)) {
                if (item == -1 || ratio[o] > best) {
                    best = ratio[o];
                    item = o;
                }
            }
            in.set(item);
            if (item == -1) {
                throw new UnsupportedOperationException();
            } else {
                order[index++] = item;
            }
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        double camax = capacity.getUB();
        double pomin = 0;
        for (int i = 0; i < n; i++) {
            camax -= weigth[i] * vars[i].getLB();
            pomin += energy[i] * vars[i].getLB();
        }
        power.updateLowerBound((int) pomin, aCause);
        if (camax == 0) {
            power.updateUpperBound((int) pomin, aCause);
        } else if (camax < 0) {
            contradiction(capacity, "");
        } else {
            int idx;
            for (int i = 0; i < n; i++) {
                if (camax < 0) {
                    throw new UnsupportedOperationException(camax + " < 0");
                }
                idx = order[i];
                int delta = weigth[idx] * (vars[idx].getUB() - vars[idx].getLB());
                if (delta > 0) {
                    if (delta <= camax) {
                        pomin += energy[idx] * (vars[idx].getUB() - vars[idx].getLB());
                        camax -= delta;
                        if (camax == 0) {
                            power.updateUpperBound((int) pomin, aCause);
                            return;
                        }
                    } else {
                        pomin += Math.ceil(camax * ratio[idx]);
                        power.updateUpperBound((int) pomin, aCause);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        double camax = capacity.getUB();
        double pomin = 0;
        for (int i = 0; i < n; i++) {
            camax -= weigth[i] * vars[i].getLB();
            pomin += energy[i] * vars[i].getLB();
        }
        if (camax < 0 || pomin > power.getUB()) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            if (pomin == power.getValue()) {
                return ESat.TRUE;
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.vars.length - 2;
            IntVar[] aVars = new IntVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                aVars[i] = (IntVar) identitymap.get(this.vars[i]);
            }
            this.vars[size].duplicate(solver, identitymap);
            IntVar C = (IntVar) identitymap.get(this.vars[size]);
            this.vars[size + 1].duplicate(solver, identitymap);
            IntVar E = (IntVar) identitymap.get(this.vars[size + 1]);
            identitymap.put(this, new PropKnapsack(aVars, C, E, this.weigth, this.energy));
        }
    }
}
