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
 * Date: 18/06/12
 * Time: 18:32
 */

package org.chocosolver.solver.constraints.nary.sum;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Constraint that state that the sum of boolean variables vars is equal to the integer variable sum
 */
public class PropBoolSumCoarse extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    IntVar sum;
    int n;
    int min, max;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Constraint that state that the sum of boolean variables vars is equal to the integer variable sum
     *
     * @param variables the boolean variables to sum
     * @param sum       the resulting integer variable
     */
    public PropBoolSumCoarse(BoolVar[] variables, IntVar sum) {
        super(ArrayUtils.append(variables, new IntVar[]{sum}), PropagatorPriority.UNARY, false);
        n = variables.length;
        this.sum = vars[n];
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        min = 0;
        max = 0;
        for (int i = 0; i < n; i++) {
            min += vars[i].getLB();
            max += vars[i].getUB();
        }
        filter();
    }

    private void filter() throws ContradictionException {
        sum.updateLowerBound(min, aCause);
        sum.updateUpperBound(max, aCause);
        if (min != max && sum.isInstantiated()) {
            if (sum.getValue() == min) {
                for (int i = 0; i < n; i++) {
                    if (!vars[i].isInstantiated()) {
                        vars[i].instantiateTo(0, aCause);
                    }
                }
            }
            if (sum.getValue() == max) {
                for (int i = 0; i < n; i++) {
                    if (!vars[i].isInstantiated()) {
                        vars[i].instantiateTo(1, aCause);
                    }
                }
            }
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public ESat isEntailed() {
        int lb = 0;
        int ub = 0;
        for (int i = 0; i < n; i++) {
            lb += vars[i].getLB();
            ub += vars[i].getUB();
        }
        if (lb > sum.getUB() || ub < sum.getLB()) {
            return ESat.FALSE;
        }
        if (lb == ub && sum.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PropBoolSumCoarse(");
        for (int i = 0; i < vars.length - 2; i++) {
            sb.append(vars[i]).append("+");
        }
        sb.append(vars[vars.length - 2]).append(")");
        sb.append(" = ").append(vars[vars.length - 1]);
        return sb.toString();
    }


    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        for (int i = 0; i < vars.length -1; i++) {
            if (vars[i] != var) {
                newrules |= ruleStore.addFullDomainRule(vars[i]);
            }
        }
        if(var != sum) {
            newrules |= ruleStore.addBoundsRule(sum);
        }
        return newrules;
    }
}
