/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
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

    public GlobalCardinality(IntVar[] vars, int[] values, IntVar[] cards) {
    	super(ConstraintsName.GCC, createProp(vars, values, cards));
    }

	private static Propagator createProp(IntVar[] vars, int[] values, IntVar[] cards) {
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
		return new PropFastGCC(vars, values, map, cards);
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
