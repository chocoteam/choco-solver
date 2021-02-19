/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.monitors;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.nary.sat.PropNogoods;
import org.chocosolver.sat.SatSolver;
import org.chocosolver.solver.variables.IntVar;

/**
 * Avoid exploring same solutions (useful with restart on solution)
 * Beware :
 * - Must be plugged as a monitor
 * - Only works for integer variables
 * <p>
 * This can be used to remove similar/symmetric solutions
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since 20/06/13
 */
public class NogoodFromSolutions implements IMonitorSolution {

    private final PropNogoods png;
    private final IntVar[] decisionVars;
    private final TIntList ps;

    /**
     * Avoid exploring same solutions (useful with restart on solution)
     * Beware :
     * - Must be posted as a constraint AND plugged as a monitor as well
     * - Cannot be reified
     * - Only works for integer variables
     * <p>
     * This can be used to remove similar/symmetric solutions
     *
     * @param vars all decision variables which define a solution (can be a subset of variables)
     */
    public NogoodFromSolutions(IntVar[] vars) {
        decisionVars = vars;
        png = vars[0].getModel().getNogoodStore().getPropNogoods();
        ps = new TIntArrayList();
    }

    @Override
    public void onSolution() {
        int n = decisionVars.length;
        ps.clear();
        for (int i = 0; i < n; i++) {
            ps.add(SatSolver.negated(png.Literal(decisionVars[i], decisionVars[i].getValue(), true)));
        }
        png.addLearnt(ps.toArray());
    }

}
