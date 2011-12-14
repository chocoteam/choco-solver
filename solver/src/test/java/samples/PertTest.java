/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package samples;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.Sum;
import solver.objective.MaxObjectiveManager;
import solver.propagation.comparators.EngineStrategies;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class PertTest {

    int horizon = 29;
    IntVar objective;

    protected Solver modeler() {
        Solver solver = new Solver();

        IntVar masonry, carpentry, plumbing, ceiling,
                roofing, painting, windows, facade, garden, moving;
        masonry = VariableFactory.bounded("masonry", 0, horizon, solver);
        carpentry = VariableFactory.enumerated("carpentry", 0, horizon, solver);
        plumbing = VariableFactory.enumerated("plumbing", 0, horizon, solver);
        ceiling = VariableFactory.enumerated("ceiling", 0, horizon, solver);
        roofing = VariableFactory.enumerated("roofing", 0, horizon, solver);
        painting = VariableFactory.enumerated("painting", 0, horizon, solver);
        windows = VariableFactory.enumerated("windows", 0, horizon, solver);
        facade = VariableFactory.enumerated("facade", 0, horizon, solver);
        garden = VariableFactory.enumerated("garden", 0, horizon, solver);
        objective = VariableFactory.enumerated("moving", 0, horizon - 1, solver);

        solver.post(precedence(masonry, 7, carpentry, solver));
        solver.post(precedence(masonry, 7, plumbing, solver));
        solver.post(precedence(masonry, 7, ceiling, solver));
        solver.post(precedence(carpentry, 3, roofing, solver));
        solver.post(precedence(ceiling, 3, roofing, solver));
        solver.post(precedence(roofing, 1, windows, solver));
        solver.post(precedence(windows, 1, painting, solver));
        solver.post(precedence(roofing, 1, facade, solver));
        solver.post(precedence(plumbing, 8, facade, solver));
        solver.post(precedence(roofing, 1, garden, solver));
        solver.post(precedence(plumbing, 8, garden, solver));
        solver.post(precedence(facade, 2, objective, solver));
        solver.post(precedence(garden, 1, objective, solver));
        solver.post(precedence(painting, 2, objective, solver));

        solver.set(StrategyFactory.minDomMinVal(new IntVar[]{masonry, carpentry, plumbing, ceiling,
                roofing, painting, windows, facade, garden, objective}, solver.getEnvironment()));

        solver.getSearchLoop().stopAtFirstSolution(false);
        MaxObjectiveManager maom = new MaxObjectiveManager(objective);
        maom.setMeasures(solver.getMeasures());
        solver.getSearchLoop().setObjectivemanager(maom);
        return solver;

    }

    /**
     * x + d < y
     */
    private static Constraint precedence(IntVar x, int duration, IntVar y, Solver solver) {
        return Sum.leq(new IntVar[]{x, y}, new int[]{1, -1}, -duration, solver);
    }

    @Test(groups = "1s")
    public void testAll() {
        Solver sol;
        sol = modeler();
        sol.findAllSolutions();
        long nbsol = sol.getMeasures().getSolutionCount();
        long node = sol.getMeasures().getNodeCount();
        for (int t = 0; t < EngineStrategies.values().length; t++) {
            sol = modeler();
            EngineStrategies.values()[t].defineIn(sol);
            sol.findAllSolutions();
            Assert.assertEquals(sol.getMeasures().getSolutionCount(), nbsol);
            Assert.assertEquals(sol.getMeasures().getNodeCount(), node);
        }
    }

}
