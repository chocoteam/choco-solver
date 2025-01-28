/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.element;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.chocosolver.solver.variables.IntVar.LR_EQ;
import static org.chocosolver.solver.variables.IntVar.LR_NE;

/**
 * Fast Element constraint
 *
 * @author Jean-Guillaume Fages
 * @author Charles Prud'homme
 * @since 05/2013
 */
@Explained
public class PropElementV2 extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final IntVar y;
    private final int ylb;
    private final IntVar x;
    private final int xlb;
    private final IntVar[] a;

    private final IStateBitSet[] supports;


    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropElementV2(IntVar value, IntVar[] values, IntVar index, int offset) {
        super(ArrayUtils.append(new IntVar[]{value, index.getModel().intView(1, index, -offset)}, values), PropagatorPriority.LINEAR, false);
        this.y = vars[0];
        this.x = vars[1];
        this.a = values;
        this.ylb = y.getLB();
        this.xlb = x.getLB();
        supports = new IStateBitSet[y.getUB() - ylb + 1];
        TIntList temp = new TIntArrayList();
        for (int v = ylb; v <= y.getUB(); v++) {
            temp.clear();
            if (y.contains(v)) {
                for (int i = x.getLB(); i <= x.getUB(); i = x.nextValue(i)) {
                    if (i >= 0 && i < a.length && a[i].contains(v)) {
                        temp.add(i - xlb);
                    }
                }
            }
            supports[v - ylb] = model.getEnvironment().makeBitSet(x.getRange());
            for (int i = 0; i < temp.size(); i++) {
                supports[v - ylb].set(temp.get(i));
            }
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        x.updateLowerBound(0, this, Reason.undef());
        x.updateUpperBound(a.length - 1, this, Reason.undef());
        // propagate holes in y
        for (int v = y.getLB(); v <= y.getUB(); v = y.nextValue(v)) {
            IStateBitSet s = supports[v - ylb];
            int ns = s.cardinality();
            int f = 0;
            if (ns > 0) {  // fast??
                for (int k = s.nextSetBit(0); k >= 0; k = s.nextSetBit(k + 1)) {
                    int i = k + xlb;
                    if (x.contains(i)) {
                        if (!a[i].contains(v)) {
                            s.clear(k);
                            f++;
                            boolean support = false;
                            for (int w = a[i].getLB(); !support && w <= a[i].getUB(); w = a[i].nextValue(w)) {
                                support = y.contains(w);
                            }
                            if (!support) {
                                // i has no support, remove from x
                                Reason r = Reason.undef();
                                if (lcg()) {
                                    r = Propagator.reason(null, y, a[i]); // TODO could be more precise
                                }
                                x.removeValue(i, this, r);
                            }
                        }
                    } else {
                        s.clear(k);
                        f++;
                    }
                }
            }
            if (f == ns) {
                // v has no support, remove from y
                Reason r = Reason.undef();
                if (lcg() && y.contains(v)) {
                    int[] ps = new int[x.getUB() + 4 - x.getLB()];
                    ps[1] = x.getMinLit();
                    ps[2] = x.getMaxLit();
                    int lb = x.getLB();
                    for (int i = lb, m = 3; i <= x.getUB(); i++, m++) {
                        if (x.contains(i)) {
                            ps[m] = MiniSat.neg(a[i].getLit(v, LR_NE));
                        } else {
                            ps[m] = MiniSat.neg(x.getLit(i, LR_NE));
                        }
                    }
                    r = Reason.r(ps);
                }
                y.removeValue(v, this, r);
            }
        }
        if (x.isInstantiated()) {
            // propagate holes in a_i
            int v = x.getValue();
            IntVar av = a[v];
            if (y.getLB() < av.getLB()) {
                y.updateLowerBound(av.getLB(), this,
                        lcg() ? Reason.r(av.getMinLit(), x.getValLit()) : Reason.undef());
            }
            if(av.getLB() < y.getLB()) {
                av.updateLowerBound(y.getLB(), this,
                        lcg() ? Reason.r(y.getMinLit(), x.getValLit()) : Reason.undef());
            }
            if(y.getUB() > av.getUB()) {
                y.updateUpperBound(av.getUB(), this,
                        lcg() ? Reason.r(av.getMaxLit(), x.getValLit()) : Reason.undef());
            }
            if(av.getUB() > y.getUB()){
                av.updateUpperBound(y.getUB(), this,
                        lcg() ? Reason.r(y.getMaxLit(), x.getValLit()) : Reason.undef());
            }
            for (int i = av.getLB(); i <= av.getUB(); i = av.nextValue(i)) {
                if (!y.contains(i)) {
                    av.removeValue(i, this,
                            lcg() ? Reason.r(MiniSat.neg(y.getLit(i, LR_NE)), MiniSat.neg(x.getLit(v, LR_EQ)))
                                    : Reason.undef());
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int lb = x.getLB();
        int ub = x.getUB();
        int min = MAX_VALUE / 2;
        int max = MIN_VALUE / 2;
        int val = y.getLB();
        boolean exists = false;
        for (int i = lb; i <= ub; i = x.nextValue(i)) {
            if (i >= 0 && i < a.length) {
                min = min(min, a[i].getLB());
                max = max(max, a[i].getUB());
                exists |= a[i].contains(val);
            }
        }
        if (min > y.getUB() || max < y.getLB()) {
            return ESat.FALSE;
        }
        if (y.isInstantiated() && !exists) {
            return ESat.FALSE;
        }
        if (y.isInstantiated() && min == max) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
