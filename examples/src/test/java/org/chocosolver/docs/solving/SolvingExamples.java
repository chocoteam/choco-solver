/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.docs.solving;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.objective.IObjectiveManager;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Examples shown in the user documentation, pages "Solving / Launching the resolution process"
 * (docs/content/docs/Solving/Solving.md) and "Solving / Large Neighborhood Search" (docs/content/docs/Solving/LNS.md).
 * The regions between {@code tag::name[]} and {@code end::name[]} are included in the pages.
 *
 * @author Charles Prud'homme
 */
public class SolvingExamples {

    private static IntVar model(Model model) {
        IntVar[] x = model.intVarArray("x", 5, 0, 9);
        model.allDifferent(x).post();
        IntVar obj = model.intVar("obj", 0, 45);
        model.sum(x, "=", obj).post();
        return obj;
    }

    @Test(groups = "1s")
    public void solve() {
        Model model = new Model();
        model(model);
        Solver solver = model.getSolver();
        solver.limitTime("10s");
        // tag::solve[]
        if (solver.solve()) {
            // do something, e.g. print out variable values
        } else if (solver.isStopCriterionMet()) {
            System.out.println("The solver could not find a solution "
                    + "nor prove that none exists in the given limits");
        } else {
            System.out.println("The solver has proved the problem has no solution");
        }
        // end::solve[]
        Assert.assertEquals(solver.getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void cut() {
        Model model = new Model();
        IntVar obj = model(model);
        model.setObjective(Model.MAXIMIZE, obj);
        Solver solver = model.getSolver();
        // tag::cut[]
        IObjectiveManager<IntVar> oman = solver.getObjectiveManager();
        // when maximizing, the next solution must be at least 10 better than the best one so far
        oman.setCutComputer(n -> n.intValue() + 10);
        // end::cut[]
        int previous = -1;
        while (solver.solve()) {
            Assert.assertTrue(previous == -1 || obj.getValue() >= previous + 10);
            previous = obj.getValue();
        }
    }

    @Test(groups = "1s")
    public void walking() {
        Model model = new Model();
        IntVar objective = model(model);
        model.setObjective(Model.MAXIMIZE, objective);
        Solver solver = model.getSolver();
        // tag::walking[]
        // accept solutions as good as the best one so far (equivalent solutions)
        solver.getObjectiveManager().setWalkingDynamicCut();
        // end::walking[]
        solver.limitSolution(50);
        while (solver.solve()) ;
        Assert.assertEquals(solver.getSolutionCount(), 50);
    }
}
