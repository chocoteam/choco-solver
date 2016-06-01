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

package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.sort.ArraySort;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Propagator for the Knapsack constraint
 * based on Dantzig-Wolfe relaxation
 *
 * @author Jean-Guillaume Fages
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
        super(ArrayUtils.append(itemOccurence, new IntVar[]{capacity, power}), PropagatorPriority.LINEAR, false);
        this.weigth = weight;
        this.energy = energy;
        this.n = itemOccurence.length;
        this.capacity = vars[n];
        this.power = vars[n + 1];
        this.order = new int[n];
        this.ratio = new double[n];
        for (int i = 0; i < n; i++) {
            ratio[i] = weight[i] == 0?Double.MAX_VALUE : ((double) (energy[i]) / (double) (weight[i]));
        }
        this.order = ArrayUtils.array(0,n-1);
        ArraySort sorter = new ArraySort(n,false,true);
        sorter.sort(order, n, (i1, i2) -> {return Double.compare(ratio[i2],ratio[i1]);});
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
        int remainingCapacity = capacity.getUB();
		int maxPower = 0;
        for (int i = 0; i < n; i++) {
            remainingCapacity -= weigth[i] * vars[i].getLB();
            maxPower += energy[i] * vars[i].getLB();
        }
        power.updateLowerBound(maxPower, this);
        if (remainingCapacity < 0) {
            fails();
        } else {
            int idx;
            for (int i = 0; i < n; i++) {
                assert remainingCapacity >= 0;
                idx = order[i];
                if (vars[idx].getUB() - vars[idx].getLB() > 0) {
					int delta = weigth[idx] * (vars[idx].getUB() - vars[idx].getLB());
                    if (delta <= remainingCapacity) {
                        maxPower += energy[idx] * (vars[idx].getUB() - vars[idx].getLB());
                        remainingCapacity -= delta;
                        if (remainingCapacity == 0) {
                            power.updateUpperBound(maxPower, this);
                            return;
                        }
                    } else {
                        int deltaPow = (int) Math.ceil((double)remainingCapacity * ratio[idx]);
                        power.updateUpperBound(maxPower + deltaPow, this);
                        return;
                    }
                }
            }
        }
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
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        assert var == this.power;
        if (IntEventType.isInclow(evt.getMask())) {
            for (int i = 0; i < n; i++) {
                newrules |= ruleStore.addLowerBoundRule(vars[i]);
            }
        } else if (IntEventType.isDecupp(evt.getMask())) {
            for (int i = 0; i < n; i++) {
                newrules |= ruleStore.addBoundsRule(vars[i]);
            }
            newrules |= ruleStore.addUpperBoundRule(vars[n]);
        } else {
            newrules |= super.why(ruleStore, var, evt, value);
        }
        return newrules;
    }

}
