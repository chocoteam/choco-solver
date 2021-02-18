/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.limits.NodeCounter;
import org.chocosolver.solver.search.loop.lns.neighbors.IntNeighbor;
import org.chocosolver.solver.search.loop.lns.neighbors.RandomNeighborhood;
import org.chocosolver.solver.search.loop.move.Move;
import org.chocosolver.solver.search.loop.move.MoveLNS;
import org.testng.annotations.Test;

import static org.chocosolver.util.ProblemMaker.makeNQueenWithBinaryConstraints;
import static org.testng.Assert.assertEquals;

/**
 * These tests are true in the current solver configuration
 * They may not be satisfied if that configuration changes.
 *
 * Those tests are here to warn of any unexpected change
 * <br/>
 *
 * @author Jean-Guillaume Fages
 */
public class SuspiciousTest {

    @Test(groups="1s", timeOut=60000)
    public void testBacktrack() {
        Model s = makeNQueenWithBinaryConstraints(12);
        s.getSolver().limitBacktrack(50);
        while (s.getSolver().solve()) ;
        long bc = s.getSolver().getBackTrackCount();
        assertEquals(bc, 57);
    }

    @Test(groups="1s", timeOut=60000)
    public void testGregy4() {
        Model model = makeNQueenWithBinaryConstraints(12);
        NodeCounter nodeCounter = new NodeCounter(model, 100);
        IntNeighbor rnd = new RandomNeighborhood(model.retrieveIntVars(true), 30, 0);
        Move currentMove = model.getSolver().getMove();
        model.getSolver().setMove(new MoveLNS(currentMove, rnd, new FailCounter(model, 100)) {
            @Override
            public boolean extend(Solver solver) {
                if (nodeCounter.isMet()) {
                    return super.extend(solver);
                }
                return currentMove.extend(solver);
            }

            @Override
            public boolean repair(Solver solver) {
                if (nodeCounter.isMet()) {
                    return super.repair(solver);
                } else if (this.solutions > 0
                        // the second condition is only here for intiale calls, when solutions is not already up to date
                        || solver.getSolutionCount() > 0) {
                    // the detection of a new solution can only be met here
                    if (solutions < solver.getSolutionCount()) {
                        assert solutions == solver.getSolutionCount() - 1;
                        solutions++;
                        neighbor.recordSolution();
                    }
                }
                return currentMove.repair(solver);
            }
        });
        model.getSolver().limitNode(200);
        while (model.getSolver().solve()) ;
        long sc = model.getSolver().getSolutionCount();
        assertEquals(sc, 47);
    }
}
