/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.min_max;

import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.stream.IntStream;

/**
 * A propagator for ARGMAX constraint.
 * <p>
 * Based on <a href="https://research.monash.edu/en/publications/the-argmax-constraint">
 * The Argmax Constraint</a>.
 * </p>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/06/2021
 */
public class PropArgmax extends Propagator<IntVar> {
    private static final boolean INCREMENTAL = false; // incrementality seems buggy .. see test: testMats1
    // nb elements in 'x'
    private final int n;
    //offset
    private final int o;
    private final IStateInt ubi;
    private final IStateInt lbi;
    private final IStateBitSet Ir;
    private final IIntDeltaMonitor delta;
    private final IntProcedure proc;

    public PropArgmax(IntVar z, int offset, IntVar[] x) {
        super(ArrayUtils.append(x, new IntVar[]{z}), PropagatorPriority.LINEAR, INCREMENTAL);
        this.n = x.length;
        this.o = offset;
        this.ubi = z.getModel().getEnvironment().makeInt(-1);
        this.lbi = z.getModel().getEnvironment().makeInt(-1);
        if (INCREMENTAL) {
            this.delta = z.monitorDelta(this);
            this.Ir = z.getModel().getEnvironment().makeBitSet(n);
            this.proc = j -> {
                int jj = j - o;
                int lbi_ = lbi.get();
                int ubi_ = ubi.get();
                Ir.set(jj, vars[jj].getUB() > vars[lbi_].getLB() - (jj <= lbi_ ? 1 : 0));
                if (jj == ubi_) {
                    filterUb();
                } else {
                    // Eq (1)
                    int ub = vars[ubi_].getUB();
                    vars[jj].updateUpperBound(ub - (jj < ubi_ ? 1 : 0), PropArgmax.this);
                }
            };
        }else{
            this.delta = null;
            this.Ir = null;
            this.proc = null;
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx < n) {
            return IntEventType.boundAndInst();
        } else {
            return IntEventType.all();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        vars[n].updateBounds(o, n + o - 1, this);
        int ubi_ = argmaxub(IntVar::getUB);
        ubi.set(ubi_);
        int ub = vars[ubi_].getUB();
        for (int j = 0; j < n; j++) {
            if (!vars[n].contains(j + o)) {
                vars[j].updateUpperBound(ub - (j < ubi_ ? 1 : 0), PropArgmax.this);
            }
        }
        int lbi_ = argmaxlb(IntVar::getLB);
        lbi.set(lbi_);
        for (int j = vars[n].nextValueOut(-1 + o); j < n + o; j = vars[n].nextValueOut(j)) {
            int jj = j - o;
            if(INCREMENTAL)Ir.set(jj, vars[jj].getUB() > vars[lbi_].getLB() - (jj <= lbi_ ? 1 : 0));
        }
        filterZ();
        if (vars[n].isInstantiated()) {
            filterLb(vars[n].getValue() - o);
        }
        if(INCREMENTAL)delta.startMonitoring();
    }

    @Override
    public void propagate(int j, int mask) throws ContradictionException {
        assert INCREMENTAL;
        if (j == n) { // on z
            delta.forEachRemVal(proc);
            if (vars[n].isInstantiated()) {
                filterLb(vars[n].getValue() - o);
            }
        } else {
            if (IntEventType.isDecupp(mask) || IntEventType.isInstantiate(mask)) {
                if (j == ubi.get()) {
                    filterUb();
                }
                if (vars[j].getUB() <= vars[lbi.get()].getLB() - (j <= lbi.get() ? 1 : 0)) {
                    vars[n].removeValue(j + o, this);
                }
            }
            if (IntEventType.isInclow(mask) || IntEventType.isInstantiate(mask)) {
                if (!(vars[j].getLB() <= vars[lbi.get()].getLB() - (j <= lbi.get() ? 1 : 0))) {
                    lbi.set(j);
                    if (vars[n].isInstantiated()) {
                        filterLb(vars[n].getValue() - o);
                    } else {
                        filterZ();
                    }
                }
            }
        }
    }

    private int argmaxlb(Bound bnd) {
        int lbi = 0;
        int _lb, lb = bnd.bound(vars[lbi]);
        for (int i = 1; i < n; i++) {
            _lb = bnd.bound(vars[i]);
            if (_lb > lb) {
                lb = _lb;
                lbi = i;
            }
        }
        return lbi;
    }

    private int argmaxub(Bound bnd) {
        int ubi = vars[n].getLB();
        int _ub, ub = bnd.bound(vars[ubi - o]);
        for (int i = vars[n].nextValue(ubi); i <= vars[n].getUB(); i = vars[n].nextValue(i)) {
            _ub = bnd.bound(vars[i - o]);
            if (_ub > ub) {
                ub = _ub;
                ubi = i;
            }
        }
        return ubi - o;
    }

    private void filterUb() throws ContradictionException {
        assert INCREMENTAL;
        int ubi_ = argmaxub(IntVar::getUB);
        ubi.set(ubi_);
        int ub = vars[ubi_].getUB();
        for (int jj = Ir.nextSetBit(0); jj > -1; jj = Ir.nextSetBit(jj + 1)) {
            vars[jj].updateUpperBound(ub - (jj < ubi_ ? 1 : 0), this);
        }
    }

    private void filterZ() throws ContradictionException {
        int lbi_ = lbi.get();
        int lb = vars[lbi_].getLB();
        for (int j = vars[n].getLB(); j <= vars[n].getUB(); j = vars[n].nextValue(j)) {
            int jj = j - o;
            if (vars[jj].getUB() <= lb - (jj <= lbi_ ? 1 : 0)) {
                vars[n].removeValue(j, this);
                if(INCREMENTAL)Ir.clear(jj);
            } else {
                if(INCREMENTAL)Ir.set(jj);
            }
        }
    }

    private void filterLb(int j) throws ContradictionException {
        int lbi_ = lbi.get();
        vars[j].updateLowerBound(vars[lbi_].getLB() + (lbi_ < j ? 1 : 0), this);
        lbi.set(j);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int max = IntStream.range(0, n).map(i -> vars[i].getValue()).max().getAsInt();
            int frst = IntStream.range(0, n).filter(i -> vars[i].getValue() == max).findFirst().getAsInt();
            return ESat.eval(
                    vars[n].getValue() == frst + o
                            && vars[frst].getValue() == max
            );
        }
        return ESat.UNDEFINED;
    }

    @FunctionalInterface
    private interface Bound {
        int bound(IntVar var);
    }
}
