/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl.lazyness;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.impl.IntVarLazyLit;

/**
 * Will use a weak chaining:
 * when a bound is modified, the channeling is done only with the previous value.
 * It provides smaller reasons, which are faster to compute but weaker in terms of explanation generation.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/09/2024
 */
public class WeakBound implements ILazyBound {

    // to store the mapping between value and SAT variable
    final TIntIntHashMap val2svar = new TIntIntHashMap(16, 1.5f, -1, -1);
    // store the current chain of lower bound SAT variables
    final TIntArrayList lbs;
    // store the current chain of upper bound SAT variables
    final TIntArrayList ubs;
    // pointer to the current position in the chain of lower bound SAT variables
    final IStateInt li;
    // pointer to the current position in the chain of upper bound SAT variables
    final IStateInt hi;

    public WeakBound(Model model) {
        lbs = new TIntArrayList();
        lbs.add(0);
        li = model.getEnvironment().makeInt(0);
        ubs = new TIntArrayList();
        ubs.add(1);
        hi = model.getEnvironment().makeInt(0);
    }

    @Override
    public int currentMinVar() {
        return lbs.get(li.get());
    }

    @Override
    public int currentMaxVar() {
        return ubs.get(hi.get());
    }

    @Override
    public int getSATVar(int value, IntVarLazyLit cvar, MiniSat sat) {
        int svar = val2svar.get(value);
        if (svar != -1) {
            return svar;
        } else {
            int var = sat.newVariable(new MiniSat.ChannelInfo(cvar, 1, 1, value));
            val2svar.put(value, var);
            return var;
        }
    }

    @Override
    public void channelMin(int value, MiniSat sat, Reason r) {
        int i = li.add(1);
        if (i == lbs.size()) {
            lbs.add(-1);
        }
        lbs.setQuick(i, val2svar.get(value));
    }

    @Override
    public void channelMax(int value, MiniSat sat, Reason r) {
        int i = hi.add(1);
        if (i == ubs.size()) {
            ubs.add(-1);
        }
        ubs.setQuick(i, val2svar.get(value));
    }
}
