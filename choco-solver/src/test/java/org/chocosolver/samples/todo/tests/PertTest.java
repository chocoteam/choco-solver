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
package org.chocosolver.samples.todo.tests;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.ResolutionPolicy.MINIMIZE;
import static org.chocosolver.solver.propagation.PropagationEngineFactory.values;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.minDomLBSearch;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class PertTest {

    int horizon = 29;
    IntVar objective;

    protected Model modeler() {
        Model model = new Model();

        IntVar masonry, carpentry, plumbing, ceiling,
                roofing, painting, windows, facade, garden;
        masonry = model.intVar("masonry", 0, horizon, true);
        carpentry = model.intVar("carpentry", 0, horizon, false);
        plumbing = model.intVar("plumbing", 0, horizon, false);
        ceiling = model.intVar("ceiling", 0, horizon, false);
        roofing = model.intVar("roofing", 0, horizon, false);
        painting = model.intVar("painting", 0, horizon, false);
        windows = model.intVar("windows", 0, horizon, false);
        facade = model.intVar("facade", 0, horizon, false);
        garden = model.intVar("garden", 0, horizon, false);
        objective = model.intVar("moving", 0, horizon - 1, false);

        precedence(masonry, 7, carpentry).post();
        precedence(masonry, 7, plumbing).post();
        precedence(masonry, 7, ceiling).post();
        precedence(carpentry, 3, roofing).post();
        precedence(ceiling, 3, roofing).post();
        precedence(roofing, 1, windows).post();
        precedence(windows, 1, painting).post();
        precedence(roofing, 1, facade).post();
        precedence(plumbing, 8, facade).post();
        precedence(roofing, 1, garden).post();
        precedence(plumbing, 8, garden).post();
        precedence(facade, 2, objective).post();
        precedence(garden, 1, objective).post();
        precedence(painting, 2, objective).post();

        model.getSolver().set(minDomLBSearch(new IntVar[]{masonry, carpentry, plumbing, ceiling,
                roofing, painting, windows, facade, garden, objective}));
        return model;

    }

    /**
     * x + d < y
     */
    private static Constraint precedence(IntVar x, int duration, IntVar y) {
        return x.getModel().arithm(x.getModel().intOffsetView(x, duration), "<", y);
    }

    @Test(groups="1s", timeOut=60000)
    public void testAll() {
        Model sol;
        sol = modeler();
        sol.set(new Settings() {
            @Override
            public boolean plugExplanationIn() {
                return true;
            }
        });
        values()[0].make(sol);
        sol.setObjective(MINIMIZE, objective);
        while(sol.solve());
        long nbsol = sol.getSolver().getMeasures().getSolutionCount();
        long node = sol.getSolver().getMeasures().getNodeCount();
        for (int t = 1; t < values().length; t++) {
            sol = modeler();
            values()[t].make(sol);
            sol.setObjective(MINIMIZE, objective);
            while(sol.solve());
            assertEquals(sol.getSolver().getMeasures().getSolutionCount(), nbsol);
            assertEquals(sol.getSolver().getMeasures().getNodeCount(), node);
        }
    }
}
