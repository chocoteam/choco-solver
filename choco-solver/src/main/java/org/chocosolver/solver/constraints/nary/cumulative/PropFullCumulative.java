/**
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
package org.chocosolver.solver.constraints.nary.cumulative;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

/**
 * Cumulative propagator
 * Performs energy checking and mandatory part based filtering
 * BEWARE : not idempotent, use two propagators to get the fix point
 *
 * @author Jean-Guillaume Fages
 * @since 31/01/13
 */
public class PropFullCumulative extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected final int n;
    protected final IntVar[] s, d, e, h;
    protected final IntVar capa;
    protected CumulFilter[] filters;
    protected ISet allTasks;
    protected final boolean fast;
    protected final int awakeningMask;
    protected final IStateInt lastCapaMax;
    protected final Cumulative.Filter[] _filters;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * protected constructor, should not be called by a user
     */
    protected PropFullCumulative(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa,
                                 boolean reactToFineEvt, boolean fast, Cumulative.Filter... filters) {
        super(ArrayUtils.append(s, d, e, h, new IntVar[]{capa}), PropagatorPriority.QUADRATIC, reactToFineEvt);
        this.n = s.length;
        if (!(n == d.length && n == e.length && n == h.length)) {
            throw new UnsupportedOperationException();
        }
        this.fast = fast;
        this.s = Arrays.copyOfRange(vars, 0, n);
        this.d = Arrays.copyOfRange(vars, n, n * 2);
        this.e = Arrays.copyOfRange(vars, n * 2, n * 3);
        this.h = Arrays.copyOfRange(vars, n * 3, n * 4);
        this.capa = this.vars[4 * n];
        this.filters = new CumulFilter[filters.length];
        _filters = filters;
        for (int f = 0; f < filters.length; f++) {
            this.filters[f] = filters[f].make(n, this);
        }
        // awakes on instantiations only when FAST mode is set to true
        awakeningMask = fast ? IntEventType.instantiation() : IntEventType.boundAndInst();
        lastCapaMax = solver.getEnvironment().makeInt(capa.getUB() + 1);
        allTasks = SetFactory.makeFullSet(n);
    }

    /**
     * Classical cumulative propagator
     *
     * @param s       start 		variables
     * @param d       duration	variables
     * @param e       end			variables
     * @param h       height		variables
     * @param capa    capacity	variable
     * @param fast    optimization parameter: reduces the amount of filtering calls when set to true
     *                (only reacts to instantiation events)
     * @param filters filtering algorithm to use
     */
    public PropFullCumulative(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa,
                              boolean fast, Cumulative.Filter... filters) {
        this(s, d, e, h, capa, false, fast, filters);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int idx) {
        if (idx >= 4 * n) {
            return IntEventType.DECUPP.getMask() + IntEventType.INSTANTIATE.getMask();
        }
        return awakeningMask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            propIni();
        }
        updateMaxCapa();
        filter(allTasks);
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    protected void propIni() throws ContradictionException {
        for (int i = 0; i < n; i++) {
            d[i].updateLowerBound(0, aCause); // should even be 1
            h[i].updateLowerBound(0, aCause);
            s[i].updateLowerBound(e[i].getLB() - d[i].getUB(), aCause);
            s[i].updateUpperBound(e[i].getUB() - d[i].getLB(), aCause);
            e[i].updateUpperBound(s[i].getUB() + d[i].getUB(), aCause);
            e[i].updateLowerBound(s[i].getLB() + d[i].getLB(), aCause);
            d[i].updateUpperBound(e[i].getUB() - s[i].getLB(), aCause);
            d[i].updateLowerBound(e[i].getLB() - s[i].getUB(), aCause);
        }
    }

    protected void updateMaxCapa() throws ContradictionException {
        if (lastCapaMax.get() != capa.getUB()) {
            int capaMax = capa.getUB();
            lastCapaMax.set(capaMax);
            for (int i = 0; i < n; i++) {
                h[i].updateUpperBound(capaMax, aCause);
            }
        }
    }

    public void filter(ISet tasks) throws ContradictionException {
        for (CumulFilter cf : filters) {
            cf.filter(s, d, e, h, capa, tasks);
        }
    }

    @Override
    public ESat isEntailed() {
        int min = s[0].getUB();
        int max = e[0].getLB();
        // check start + duration = end
        for (int i = 0; i < n; i++) {
            min = Math.min(min, s[i].getUB());
            max = Math.max(max, e[i].getLB());
            if (s[i].getLB() + d[i].getLB() > e[i].getUB()
                    || s[i].getUB() + d[i].getUB() < e[i].getLB()) {
                return ESat.FALSE;
            }
        }
        // check capacity
        int maxLoad = 0;
        if (min <= max) {
            int capamax = capa.getUB();
            int[] consoMin = new int[max - min];
            for (int i = 0; i < n; i++) {
                for (int t = s[i].getUB(); t < e[i].getLB(); t++) {
                    consoMin[t - min] += h[i].getLB();
                    if (consoMin[t - min] > capamax) {
                        return ESat.FALSE;
                    }
                    maxLoad = Math.max(maxLoad, consoMin[t - min]);
                }
            }
        }
        // check variables are instantiated
        for (int i = 0; i < vars.length - 1; i++) {
            if (!vars[i].isInstantiated()) {
                return ESat.UNDEFINED;
            }
        }
        assert min <= max;
        // capacity check entailed
        if (maxLoad <= vars[4 * n].getLB()) {
            return ESat.TRUE;
        }
        // capacity not instantiated
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName() + "(");
        for (int i = 0; i < n; i++) {
            sb.append("[").append(vars[i].toString());
            sb.append(",").append(vars[i + n].toString());
            sb.append(",").append(vars[i + 2 * n].toString());
            sb.append(",").append(vars[i + 3 * n].toString()).append("],");
        }
        sb.append(vars[4 * n].toString()).append(")");
        return sb.toString();
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            // IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa,
            // boolean fast, Cumulative.Filter... filters

            IntVar[] sVars = new IntVar[n];
            IntVar[] dVars = new IntVar[n];
            IntVar[] eVars = new IntVar[n];
            IntVar[] hVars = new IntVar[n];
            for (int i = 0; i < this.n; i++) {
                this.s[i].duplicate(solver, identitymap);
                sVars[i] = (IntVar) identitymap.get(this.s[i]);
                this.d[i].duplicate(solver, identitymap);
                dVars[i] = (IntVar) identitymap.get(this.d[i]);
                this.e[i].duplicate(solver, identitymap);
                eVars[i] = (IntVar) identitymap.get(this.e[i]);
                this.h[i].duplicate(solver, identitymap);
                hVars[i] = (IntVar) identitymap.get(this.h[i]);
            }
            this.capa.duplicate(solver, identitymap);
            IntVar cVar = (IntVar) identitymap.get(this.capa);
            identitymap.put(this, new PropFullCumulative(sVars, dVars, eVars, hVars, cVar, fast, _filters.clone()));
        }
    }
}
