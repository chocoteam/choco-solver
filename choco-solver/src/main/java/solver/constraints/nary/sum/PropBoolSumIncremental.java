/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 18/06/12
 * Time: 18:32
 */

package solver.constraints.nary.sum;

import gnu.trove.map.hash.THashMap;
import memory.IEnvironment;
import memory.IStateInt;
import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.events.IntEventType;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * Constraint that state that the sum of boolean variables vars is equal to the integer variable sum
 * Works incrementally in O(1) per instantiation event
 */
public class PropBoolSumIncremental extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    IntVar sum;
    int n;
    IStateInt min, max;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Constraint that state that the sum of boolean variables vars is equal to the integer variable sum
     * Works in O(1) per instantiation event
     *
     * @param variables
     * @param sum
     */
    public PropBoolSumIncremental(BoolVar[] variables, IntVar sum) {
        super(ArrayUtils.append(variables, new IntVar[]{sum}), PropagatorPriority.UNARY, true);
        n = variables.length;
        this.sum = vars[n];
        IEnvironment environment = solver.getEnvironment();
        min = environment.makeInt();
        max = environment.makeInt();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int lb = 0;
        int ub = 0;
        for (int i = 0; i < n; i++) {
            lb += vars[i].getLB();
            ub += vars[i].getUB();
        }
        min.set(lb);
        max.set(ub);
        filter();
    }

    private void filter() throws ContradictionException {
        int lb = min.get();
        int ub = max.get();
        sum.updateLowerBound(lb, aCause);
        sum.updateUpperBound(ub, aCause);
        if (lb != ub && sum.isInstantiated()) {
            if (sum.getValue() == lb) {
                for (int i = 0; i < n; i++) {
                    if (!vars[i].isInstantiated()) {
                        vars[i].instantiateTo(0, aCause);
                    }
                }
            }
            if (sum.getValue() == ub) {
                for (int i = 0; i < n; i++) {
                    if (!vars[i].isInstantiated()) {
                        vars[i].instantiateTo(1, aCause);
                    }
                }
            }
            // remarque : ne pas mettre de setPassive() car:
            // 1) C'est inutile : tout est instancie
            // 2) Il faudrait verifier que tous les evenements ont bien ete depiles
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < n) {
            if (vars[idxVarInProp].getValue() == 1) {
                min.add(1);
            } else {
                max.add(-1);
            }
        }
        filter();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == n)
            return IntEventType.boundAndInst();
        return IntEventType.instantiation();
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
        sb.append("PropBoolSumIncremental(");
        for (int i = 0; i < vars.length - 2; i++) {
            sb.append(vars[i] + "+");
        }
        sb.append(vars[vars.length - 2] + ")");
        sb.append(" = " + vars[vars.length - 1]);
        return sb.toString();
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.vars.length - 1;
            BoolVar[] aVars = new BoolVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                aVars[i] = (BoolVar) identitymap.get(this.vars[i]);
            }
            this.vars[size].duplicate(solver, identitymap);
            IntVar S = (IntVar) identitymap.get(this.vars[size]);
            identitymap.put(this, new PropBoolSumIncremental(aVars, S));
        }
    }
}