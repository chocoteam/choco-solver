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
package org.chocosolver.samples;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

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
                roofing, painting, windows, facade, garden;
        masonry = solver.intVar("masonry", 0, horizon, true);
        carpentry = solver.intVar("carpentry", 0, horizon, false);
        plumbing = solver.intVar("plumbing", 0, horizon, false);
        ceiling = solver.intVar("ceiling", 0, horizon, false);
        roofing = solver.intVar("roofing", 0, horizon, false);
        painting = solver.intVar("painting", 0, horizon, false);
        windows = solver.intVar("windows", 0, horizon, false);
        facade = solver.intVar("facade", 0, horizon, false);
        garden = solver.intVar("garden", 0, horizon, false);
        objective = solver.intVar("moving", 0, horizon - 1, false);

        solver.post(precedence(masonry, 7, carpentry));
        solver.post(precedence(masonry, 7, plumbing));
        solver.post(precedence(masonry, 7, ceiling));
        solver.post(precedence(carpentry, 3, roofing));
        solver.post(precedence(ceiling, 3, roofing));
        solver.post(precedence(roofing, 1, windows));
        solver.post(precedence(windows, 1, painting));
        solver.post(precedence(roofing, 1, facade));
        solver.post(precedence(plumbing, 8, facade));
        solver.post(precedence(roofing, 1, garden));
        solver.post(precedence(plumbing, 8, garden));
        solver.post(precedence(facade, 2, objective));
        solver.post(precedence(garden, 1, objective));
        solver.post(precedence(painting, 2, objective));

        solver.set(IntStrategyFactory.minDom_LB(new IntVar[]{masonry, carpentry, plumbing, ceiling,
                roofing, painting, windows, facade, garden, objective}));
        return solver;

    }

    /**
     * x + d < y
     */
    private static Constraint precedence(IntVar x, int duration, IntVar y) {
        return IntConstraintFactory.arithm(x.getSolver().intOffsetView(x, duration), "<", y);
    }

    @Test(groups="1s", timeOut=60000)
    public void testAll() {
        Solver sol;
        sol = modeler();
        sol.set(new Settings() {
            @Override
            public boolean plugExplanationIn() {
                return true;
            }
        });
        PropagationEngineFactory.values()[0].make(sol);
        sol.findOptimalSolution(ResolutionPolicy.MINIMIZE, objective);
        long nbsol = sol.getMeasures().getSolutionCount();
        long node = sol.getMeasures().getNodeCount();
        for (int t = 1; t < PropagationEngineFactory.values().length; t++) {
            sol = modeler();
            PropagationEngineFactory.values()[t].make(sol);
            sol.findOptimalSolution(ResolutionPolicy.MINIMIZE, objective);
            Assert.assertEquals(sol.getMeasures().getSolutionCount(), nbsol);
            Assert.assertEquals(sol.getMeasures().getNodeCount(), node);
        }
    }
}
