/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.Group;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/02/2025
 */
public class RoundRobinGroup<V extends Variable> implements VariableSelector<V> {
    private final IStateInt lastG; // index of the last non-instantiated variable
    private final IStateInt lastIdx; // index of the last non-instantiated variable
    private final ArrayList<Group<?>> groups;
    private final java.util.Random rnd;

    public RoundRobinGroup(Model model, long seed) {
        this.groups = new ArrayList<>();
        groups.addAll(model.getGroups());
        this.lastG = model.getEnvironment().makeInt(0);
        this.lastIdx = model.getEnvironment().makeInt(0);
        this.rnd = new java.util.Random(seed);
    }

    @Override
    public V getVariable(V[] variables) {
        if(lastG.get() == 0 && lastIdx.get() == 0){
            groups.add(groups.remove(0));
//            Collections.shuffle(groups, rnd);
        }
        for (int gI = lastG.get(); gI < groups.size(); gI++) {
            Group<?> g = groups.get(gI);
            Variable[] variables1 = g.getVariables();
            for (int idx = lastIdx.get(); idx < variables1.length; idx++) {
                if (!variables1[idx].isInstantiated()) {
                    lastIdx.set(idx);
                    //noinspection unchecked
                    return (V) variables1[idx];
                }
            }
            lastIdx.set(0);
            lastG.add(1);
        }
        return null;
    }
}
