/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

/**
 *
 * <p>
 * Project: choco.
 * @author Charles Prud'homme
 * @since 25/02/2016.
 */
public class LocalConstructiveDisjunction extends Constraint {
    /**
     * Make a new constraint defined as a set of given propagators
     *
     * @param constraints set of constraints in disjunction
     */
    public LocalConstructiveDisjunction(Constraint... constraints) {
        super(ConstraintsName.LOCALCONSTRUCTIVEDISJUNCTION, createProps(constraints));
    }

    @SuppressWarnings("unchecked")
    private static Propagator[] createProps(Constraint... constraints) {
        Propagator<IntVar>[][] propagators = new Propagator[constraints.length][];
        TIntObjectHashMap<IntVar> map1 = new TIntObjectHashMap<>();
        for (int i = 0; i < constraints.length; i++) {
            propagators[i] = constraints[i].getPropagators().clone();
            for (int j = 0; j < propagators[i].length; j++) {
                Propagator<IntVar> prop = propagators[i][j];
                prop.setReifiedSilent(null);
                for (int k = 0; k < prop.getNbVars(); k++) {
                    map1.put(prop.getVar(k).getId(), prop.getVar(k));
                }
            }
        }
        int[] keys = map1.keys();
        Arrays.sort(keys);
        IntVar[] allvars = new IntVar[keys.length];
        int k = 0;
        for (int i = 0; i < keys.length; i++) {
            allvars[k++] = map1.get(keys[i]);
        }
        IntVar[] vars = Arrays.copyOf(allvars, k);
        assert vars.length > 0;
        return ArrayUtils.append(new Propagator[]{new PropLocalConDis(vars, propagators)},
                ArrayUtils.flatten(propagators));
    }

    @Override
    public ESat isSatisfied() {
        return propagators[0].isEntailed();
    }
}
