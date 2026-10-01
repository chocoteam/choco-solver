/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;

/**
 * A solution expressed with the identifiers of a {@link ModelSpec}: found by one instance, it can be read by any
 * other instance, or by the recorded model.
 * <p>
 * Only integer (and boolean) variables are stored. Immutable, hence shareable among threads.
 *
 * @author Charles Prud'homme
 */
public final class SpecSolution {

    /**
     * Value of each identifier, meaningful only when stored.
     */
    private final int[] values;
    private final boolean[] stored;

    private SpecSolution(int[] values, boolean[] stored) {
        this.values = values;
        this.stored = stored;
    }

    /**
     * Record the current values of the instantiated integer variables of an instance, typically when a solution
     * is found.
     *
     * @param resolver the resolver of the instance
     * @return the solution
     */
    public static SpecSolution record(Resolver resolver) {
        int n = resolver.capacity();
        int[] values = new int[n];
        boolean[] stored = new boolean[n];
        for (int id = 0; id < n; id++) {
            if (resolver.isBound(id) && resolver.get(id) instanceof IntVar v && v.isInstantiated()) {
                values[id] = v.getValue();
                stored[id] = true;
            }
        }
        return new SpecSolution(values, stored);
    }

    /**
     * @param id an identifier
     * @return <i>true</i> if this solution holds a value for <i>id</i>
     */
    public boolean contains(int id) {
        return id >= 0 && id < stored.length && stored[id];
    }

    /**
     * @param id identifier of an integer variable
     * @return its value in this solution
     * @throws SpecException if this solution holds no value for <i>id</i>
     */
    public int valueOf(int id) {
        if (!contains(id)) {
            throw new SpecException("No value for identifier " + id);
        }
        return values[id];
    }

    /**
     * Express this solution in terms of the variables of a model which follows the spec (an instance, or the
     * recorded model).
     *
     * @param resolver identifies the variables of the target model
     * @return a solution of the target model
     */
    public Solution toSolution(Resolver resolver) {
        Solution sol = new Solution(resolver.model());
        for (int id = 0; id < stored.length; id++) {
            if (stored[id] && resolver.isBound(id) && resolver.get(id) instanceof IntVar v) {
                sol.setIntVal(v, values[id]);
            }
        }
        return sol;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof SpecSolution s
                && Arrays.equals(values, s.values) && Arrays.equals(stored, s.stored));
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(values) + Arrays.hashCode(stored);
    }
}
