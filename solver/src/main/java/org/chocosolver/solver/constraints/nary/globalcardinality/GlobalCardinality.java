/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.globalcardinality;

import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

/**
 * Global Cardinality constraint
 *
 * @author Hadrien Cambazard, Charles Prud'homme, Jean-Guillaume Fages
 * @since 16/06/11
 */
public class GlobalCardinality extends Constraint {

    /**
     * Consistency level enforced by the propagator(s) posted for a {@link GlobalCardinality}
     * constraint.
     */
    public enum Consistency {
        /**
         * Fast filtering (see {@link PropFastGCC}), without any well-defined consistency level
         * guarantee.
         */
        DEFAULT,
        /**
         * Bound-consistency (see {@link PropGcc}), following:
         * C.-G. Quimper, P. van Beek, A. Lopez-Ortiz, A. Golynski, and S.B. Sadjad.
         * "An efficient bounds consistency algorithm for the global cardinality constraint."
         * CP-2003.
         */
        BC,
        /**
         * Arc-consistency (see {@link PropGcc}), following:
         * J.-C. Regin. "Generalized Arc Consistency for Global Cardinality Constraint." AAAI-96.
         */
        AC
    }

    /**
     * System property used to override the default {@link Consistency} level, e.g.
     * {@code -Dchoco.gcc.consistency=AC}. See {@link #defaultConsistency()}.
     */
    public static final String CONSISTENCY_PROPERTY = "choco.gcc.consistency";

    /**
     * Returns the {@link Consistency} level used when none is explicitly specified, e.g. by
     * {@link #GlobalCardinality(IntVar[], int[], IntVar[])} or by
     * {@link org.chocosolver.solver.constraints.IIntConstraintFactory#globalCardinality(IntVar[], int[], IntVar[], boolean)}.
     * <p>
     * Defaults to {@link Consistency#BC}, which matches or beats {@link Consistency#AC} on
     * solution quality while being cheaper to propagate, and both markedly outperform
     * {@link Consistency#DEFAULT} on tightly-constrained instances.
     * <p>
     * Can be overridden via the {@value #CONSISTENCY_PROPERTY} system property (e.g. to
     * benchmark alternative filtering levels without changing calling code).
     *
     * @return the default consistency level
     * @throws IllegalArgumentException if the {@value #CONSISTENCY_PROPERTY} property is set to
     *                                   a value that is not a valid {@link Consistency} name
     */
    public static Consistency defaultConsistency() {
        return Consistency.valueOf(System.getProperty(CONSISTENCY_PROPERTY, Consistency.BC.name()));
    }

    /**
     * Creates a global cardinality constraint using the {@linkplain #defaultConsistency() default
     * consistency level}.
     *
     * @param vars   collection of variables
     * @param values collection of constrained values
     * @param cards  collection of cardinality variables
     */
    public GlobalCardinality(IntVar[] vars, int[] values, IntVar[] cards) {
        this(vars, values, cards, defaultConsistency().name());
    }

    public GlobalCardinality(IntVar[] vars, int[] values, IntVar[] cards, String consistency) {
    	super(ConstraintsName.GCC, createProp(vars, values, cards, Consistency.valueOf(consistency)));
    }

	private static Propagator<IntVar>[] createProp(IntVar[] vars, int[] values, IntVar[] cards,
                                                     Consistency consistency) {
		assert values.length == cards.length;
		TIntIntHashMap map = new TIntIntHashMap();
		int idx = 0;
		for (int v : values) {
			if (!map.containsKey(v)) {
				map.put(v, idx);
				idx++;
			} else {
				throw new UnsupportedOperationException("ERROR: multiple occurrences of value: " + v);
			}
		}
		PropFastGCC fast = new PropFastGCC(vars, values, map, cards);
		switch (consistency) {
			case BC:
			case AC:
				//noinspection unchecked
				return new Propagator[]{fast, new PropGcc(vars, values, cards, consistency)};
			default:
				//noinspection unchecked
				return new Propagator[]{fast};
		}
	}

    public static Constraint reformulate(IntVar[] vars, IntVar[] card, Model model) {
        List<Constraint> cstrs = new ArrayList<>();
        for (int i = 0; i < card.length; i++) {
			BoolVar[] bs = model.boolVarArray("b_" + i, vars.length);
            for (int j = 0; j < vars.length; j++) {
            	model.reifyXeqC(vars[j], i, bs[j]);
            }
            cstrs.add(model.sum(bs, "=", card[i]));
        }
        return Constraint.merge("reformulatedGCC", cstrs.toArray(new Constraint[0]));
    }
}
