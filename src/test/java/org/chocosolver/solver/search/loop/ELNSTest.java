/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.lns.neighbors.ExplainingCut;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.loop.lns.neighbors.RandomNeighborhood;
import org.chocosolver.solver.search.loop.lns.neighbors.SequenceNeighborhood;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.limits.ICounter.Impl.None;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/06/13
 */
public class ELNSTest {

    private void small(long seed, int n) {
        Model model = new Model();
        final IntVar[] vars = model.intVarArray("var", 6, 0, 4, true);
        final IntVar obj = model.intVar("obj", 0, 6, true);

        model.sum(vars, "=", obj).post();
        model.arithm(vars[0], "+", vars[1], "<", 2).post();
        model.arithm(vars[4], "+", vars[5], ">", 3).post();

        model.getSolver().setCBJLearning(false, false);

        Solver r = model.getSolver();
        INeighbor neighbor = null;
        switch (n){
            case 0:
                neighbor = new RandomNeighborhood(vars, 200, 123456L);
                break;
            case 1:
                neighbor = new ExplainingCut(model, 200, 123456L);
                break;
            case 2:
                neighbor = new SequenceNeighborhood(
                        new ExplainingCut(model, 200, 123456L),
                        new RandomNeighborhood(vars, 200, 123456L)
                );
                break;
        }

        r.setLNS(neighbor, None);
        r.setSearch(randomSearch(vars, seed));
        r.limitFail(500);
        r.showSolutions();
        model.setObjective(Model.MINIMIZE, obj);
        while(model.getSolver().solve());
    }


    @Test(groups="1s", timeOut=60000)
    public void test1() {
        for(int i = 0; i < 3; i++) {
            System.out.printf("case: %d\n", i);
            small(8, i);
        }
    }


}
