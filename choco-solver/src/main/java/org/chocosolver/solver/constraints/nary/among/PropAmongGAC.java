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
package org.chocosolver.solver.constraints.nary.among;

import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.solver.variables.ranges.IntIterableBitSet;
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.procedure.UnarySafeIntProcedure;

import java.util.Arrays;

/**
 * GCCAT:
 * NVAR is the number of variables of the collection VARIABLES that take their value in VALUES.
 * <br/><a href="http://www.emn.fr/x-info/sdemasse/gccat/Camong.html">gccat among</a>
 * <br/>
 * Propagator :
 * C. Bessiere, E. Hebrard, B. Hnich, Z. Kiziltan, T. Walsh,
 * Among, common and disjoint Constraints
 * CP-2005
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/01/12
 */
public class PropAmongGAC extends Propagator<IntVar> {

    private final int[] values;
    private final int nb_vars;
    private final IStateBitSet both;
    private final IStateInt LB;
    private final IStateInt UB;

    private TIntHashSet setValues;

    private IStateInt[] occs;

    protected final IIntDeltaMonitor[] idms;

    protected final RemProc rem_proc;

    protected boolean needFilter;

    protected final IntIterableSet vrms;

    public PropAmongGAC(IntVar[] variables, int[] values) {
        super(variables, PropagatorPriority.LINEAR, true);
        nb_vars = variables.length - 1;
        this.idms = new IIntDeltaMonitor[vars.length];
        for (int i = 0; i < vars.length; i++) {
            idms[i] = vars[i].hasEnumeratedDomain() ? vars[i].monitorDelta(this) : IIntDeltaMonitor.Default.NONE;
        }
        IEnvironment environment = solver.getEnvironment();
        both = environment.makeBitSet(nb_vars);
        LB = environment.makeInt(0);
        UB = environment.makeInt(0);
        this.setValues = new TIntHashSet(values);
        this.values = setValues.toArray();
        Arrays.sort(this.values);
        this.occs = new IStateInt[nb_vars];
        for (int i = 0; i < nb_vars; i++) {
            occs[i] = environment.makeInt(0);
        }
        rem_proc = new RemProc(this);
        vrms = new IntIterableBitSet();
    }

    @Override
    public int getPropagationConditions(int idx) {
        if (idx == nb_vars) {
            return IntEventType.boundAndInst();
        }
        return IntEventType.all();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            int lb = 0;
            int ub = nb_vars;
            for (int i = 0; i < nb_vars; i++) {
                IntVar var = vars[i];
                int nb = 0;
                for (int j = 0; j < values.length; j++) {
                    nb += (var.contains(values[j]) ? 1 : 0);
                }
                occs[i].set(nb);
                if (nb == var.getDomainSize()) {
                    lb++;
                } else if (nb == 0) {
                    ub--;
                } else if (nb > 0) {
                    both.set(i, true);
                }
            }
            LB.set(lb);
            UB.set(ub);
            for (int i = 0; i < idms.length; i++) {
                idms[i].unfreeze();
            }
        }
        filter();
    }

    protected void filter() throws ContradictionException {
        int lb = LB.get();
        int ub = UB.get();
        vars[nb_vars].updateLowerBound(lb, this);
        vars[nb_vars].updateUpperBound(ub, this);

        int min = Math.max(vars[nb_vars].getLB(), lb);
        int max = Math.min(vars[nb_vars].getUB(), ub);

        if (max < min) fails();

        if (lb == min && lb == max) {
            removeOnlyValues();
        }

        if (ub == min && ub == max) {
            removeButValues();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        filter();
    }

    /**
     * Remove from {@code v} every values contained in {@code values}.
     *
     * @throws ContradictionException if contradiction occurs.
     */
    private void removeOnlyValues() throws ContradictionException {
        for (int i = both.nextSetBit(0); i >= 0; i = both.nextSetBit(i + 1)) {
            IntVar v = vars[i];
            if (v.hasEnumeratedDomain()) {
                for (int value : values) {
                    if (v.removeValue(value, this)) {
                        occs[i].add(-1);
                    }
                }
            } else {
                int lb = v.getLB();
                int ub = v.getUB();
                int k1 = 0;
                int k2 = values.length - 1;
                // values is sorted
                // so first, find the first value inside dom(v)
                while (k1 < k2 && values[k1] < lb) {
                    k1++;
                }
                // and bottom-up shaving
                while (k1 <= k2 && v.removeValue(values[k1], this)) {
                    occs[i].add(-1);
                    k1++;
                }
                // then find the last value inside dom(v)
                while (k2 > k1 && values[k2] > ub) {
                    k2--;
                }
                // and top bottom shaving
                while (k2 >= k1 && v.removeValue(values[k2], this)) {
                    occs[i].add(-1);
                    k2--;
                }
            }
        }
    }

    /**
     * Remove from {@code v} each value but {@code values}.
     *
     * @throws ContradictionException if contradiction occurs.
     */
    private void removeButValues() throws ContradictionException {
        for (int i = both.nextSetBit(0); i >= 0; i = both.nextSetBit(i + 1)) {
            IntVar v = vars[i];
            DisposableValueIterator it = v.getValueIterator(true);
            vrms.clear();
            vrms.setOffset(v.getLB());
            while (it.hasNext()) {
                int value = it.next();
                if (!setValues.contains(value)) {
                    vrms.add(value);
                }
            }
            v.removeValues(vrms, this);
            it.dispose();
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int nb = 0;
            for (int i = 0; i < nb_vars; i++) {
                if (setValues.contains(vars[i].getValue())) {
                    nb++;
                }
            }
            return ESat.eval(vars[nb_vars].getValue() == nb);
        }
        return ESat.UNDEFINED;
    }

    protected static class RemProc implements UnarySafeIntProcedure<Integer> {

        final PropAmongGAC p;
        int varIdx;

        public RemProc(PropAmongGAC p) {
            this.p = p;
        }

        @Override
        public UnarySafeIntProcedure set(Integer integer) {
            varIdx = integer;
            return this;
        }

        @Override
        public void execute(int val) {
            if (p.both.get(varIdx)) {
                if (p.setValues.contains(val)) {
                    p.occs[varIdx].add(-1);
                }
                IntVar var = p.vars[varIdx];
                int nb = p.occs[varIdx].get();
                if (nb == var.getDomainSize()) {  //Can only be instantiated to a value in the group
                    p.LB.add(1);
                    p.both.set(varIdx, false);
//                    p.filter();
                    p.needFilter = true;
                } else if (nb == 0) { //No value in the group
                    p.UB.add(-1);
                    p.both.set(varIdx, false);
//                    p.filter();
                    p.needFilter = true;
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AMONG(");
        sb.append("[");
        for (int i = 0; i < nb_vars; i++) {
            if (i > 0) sb.append(",");
            sb.append(vars[i].toString());
        }
        sb.append("],{");
        sb.append(Arrays.toString(values));
        sb.append("},");
        sb.append(vars[nb_vars].toString()).append(")");
        return sb.toString();
    }

}
