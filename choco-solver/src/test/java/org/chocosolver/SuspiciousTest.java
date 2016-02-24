/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.limits.NodeCounter;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
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
        while (s.solve()) ;
        long bc = s.getSolver().getMeasures().getBackTrackCount();
        assertEquals(bc, 52);
    }

    @Test(groups="1s", timeOut=60000)
    public void testGregy4() {
        Model model = makeNQueenWithBinaryConstraints(12);
        NodeCounter nodeCounter = new NodeCounter(model, 100);
        INeighbor rnd = new RandomNeighborhood(model.retrieveIntVars(true), 30, 0);
        Move currentMove = model.getSolver().getMove();
        model.getSolver().set(new MoveLNS(currentMove, rnd, new FailCounter(model, 100)) {
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
                        || solver.getMeasures().getSolutionCount() > 0) {
                    // the detection of a new solution can only be met here
                    if (solutions < solver.getMeasures().getSolutionCount()) {
                        assert solutions == solver.getMeasures().getSolutionCount() - 1;
                        solutions++;
                        neighbor.recordSolution();
                    }
                }
                return currentMove.repair(solver);
            }
        });
        model.getSolver().limitNode(200);
        while (model.solve()) ;
        long sc = model.getSolver().getMeasures().getSolutionCount();
        assertEquals(sc, 54);
    }
}
