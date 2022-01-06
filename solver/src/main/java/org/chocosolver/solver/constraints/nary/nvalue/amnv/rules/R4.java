/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue.amnv.rules;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.alldifferent.algo.AlgoAllDiffBC;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.mis.F;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;

import java.util.BitSet;

/**
 * R4 filtering rule (AllDifferent propagation)
 *
 * @author Jean-Guillaume Fages
 * @since 01/01/2014
 */
public class R4 implements R {

    private AlgoAllDiffBC filter;

    public void filter(IntVar[] vars, UndirectedGraph graph, F heur, Propagator aCause) throws ContradictionException {
        int n = vars.length - 1;
        BitSet mis = heur.getMIS();
        if (mis.cardinality() == vars[n].getUB()) {
            IntVar[] vs = new IntVar[mis.cardinality()];
            int idx = 0;
            for (int x = mis.nextSetBit(0); x >= 0; x = mis.nextSetBit(x + 1)) {
                vs[idx++] = vars[x];
            }
            if (filter == null) filter = new AlgoAllDiffBC(aCause);
            filter.reset(vs);
            filter.filter();
        }
    }
}
