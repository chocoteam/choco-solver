/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
 package org.chocosolver.examples.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import java.util.Arrays;

public class BugSetVar {
    public static void main(String[] args) {
        Model model = new Model();
        Solver solver = model.getSolver();

        int[] ub = new int[]{4,7,9,11};
        int max_ub = Arrays.stream(ub).max().getAsInt();
        BoolVar[] in = new BoolVar[max_ub+1];
        for (int i = 0; i < max_ub+1; i++) {
            in[i] = model.boolVar("in" + i);
            in[i].not();
        }
        SetVar set = model.setVar("var", new int[]{}, ub);
        model.setBoolsChanneling(in, set).post();
        IntVar card = model.intVar("card", 1, 4);
        set.setCard(card);

        solver.plugMonitor((IMonitorSolution) () -> {
            BoolVar[] diff = new BoolVar[card.getValue()];
            int j = 0;
            for (int i = 0; i < max_ub+1; i++)
                if (in[i].getValue() == 1)
                    diff[j++] = in[i].not();
            model.or(diff).post();
        });

        solver.setSearch(Search.inputOrderLBSearch(card), Search.inputOrderUBSearch(in));
        while (solver.solve())
            System.out.println("Set: " + set.getValue());
    }
}
