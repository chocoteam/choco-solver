/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.function.IntFunction;

import static org.chocosolver.solver.search.strategy.Search.domOverWDegSearch;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;

/**
 * Small models solved with a linear relaxation, for each available backend.
 *
 * @author Charles Prud'homme
 */
public class HybridizationTest {


    /**
     * @return the relaxation backends to test, each one as a name and a factory
     */
    @DataProvider
    public Object[][] algo() {
        return new Object[][]{
                {"OJALGO", (IntFunction<ILinearRelaxation>) ILinearRelaxation.Solver.OJALGO::create},
                {"OJALGO_EBM", (IntFunction<ILinearRelaxation>) ILinearRelaxation.Solver.OJALGO_EBM::create},
        };
    }

    /**
     * @return a model whose linear relaxation is solved with the given backend
     */
    static Model model(IntFunction<ILinearRelaxation> factory) {
        Model model = new Model();
        new Constraint("LinearRelaxation", new PropSimplex(model, factory)).post();
        return model;
    }

    @Test(groups = "1s", dataProvider = "algo")
    public void testHybridization1(String name, IntFunction<ILinearRelaxation> algo) {
        //         double[] c = {50, 70};
        //        double[][] A = {
        //                {40, 30},
        //                {20, 30}
        //        };
        //        double[] b = {360, 480};
        Model model = model(algo);
        IntVar x = model.intVar("x", 0, 100);
        IntVar y = model.intVar("y", 0, 100);
        IntVar o = model.intVar("obj", 0, 2000);

        model.scalar(new IntVar[]{x, y}, new int[]{4, 3}, "<=", 36).post();
        model.scalar(new IntVar[]{x, y}, new int[]{2, 3}, "<=", 48).post();
        model.scalar(new IntVar[]{x, y}, new int[]{5, 7}, "=", o).post();
        model.setObjective(true, o);

        Solver solver = model.getSolver();
        solver.setSearch(inputOrderLBSearch(x, y));
//        solver.showDecisions(0);
//        solver.showSolutions(x, y);
        int bestValue = 0;
        while (solver.solve()) {
            bestValue = o.getValue();
        }
        Assert.assertEquals(bestValue, 84);

    }

    @Test(groups = "1s", dataProvider = "algo", timeOut = 10000)
    public void testHybridization2(String name, IntFunction<ILinearRelaxation> algo) {
        Model model = model(algo);
        int[] itemSize = new int[]{48, 30, 19, 36, 36, 27, 42, 42, 36, 24, 30};
        int nbItems = itemSize.length;
        int nbBins = 6;
        int binCapa = 100;
        IntVar[] itemBin = model.intVarArray(nbItems, 0, nbBins - 1);
        IntVar[] binLoad = model.intVarArray(nbBins, 0, binCapa);
        IntVar minLoad = model.intVar("minLoad", 0, binCapa);
        model.min(minLoad, binLoad).post();
        model.binPacking(itemBin, itemSize, binLoad, 0).post();
        model.setObjective(true, minLoad);

        Solver solver = model.getSolver();
        solver.setSearch(domOverWDegSearch(itemBin));
        int bestValue = 0;
        while (solver.solve()) {
            bestValue = minLoad.getValue();
        }
        Assert.assertEquals(bestValue, 48);
    }

    @Test(groups = "1s", dataProvider = "algo", timeOut = 10000)
    public void testHybridization3(String name, IntFunction<ILinearRelaxation> algo) {
        int n = 8;
        Model model = model(algo);
        IntVar[] vars = new IntVar[n];
        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];

        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("Q_" + i, 1, n, false);
            diag1[i] = model.offset(vars[i], i);
            diag2[i] = model.offset(vars[i], -i);
        }

        model.allDifferent(vars).post();
        model.allDifferent(diag1).post();
        model.allDifferent(diag2).post();

        Solver solver = model.getSolver();
        solver.setSearch(domOverWDegSearch(vars));
        //solver.showSolutions(vars);
        while(solver.solve());
        Assert.assertEquals(solver.getSolutionCount(), 92);
    }
}
