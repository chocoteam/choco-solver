/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.real;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;

import java.util.Random;

import static java.lang.String.format;
import static java.lang.System.out;
import static org.chocosolver.solver.search.strategy.Search.domOverWDegSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/07/12
 */
public class SantaClaude extends AbstractProblem {

    int n_gifts = 20;
    int n_kids = 10;
    int max_price = 25;
    int[] gift_price;

    IntVar[] kid_gift;
    IntVar[] kid_price;
    IntVar total_cost;
    RealVar average;


    @Override
    public void buildModel() {
        model = new Model();
        Random rand = new Random(29091981);
        double precision = 1.e-6;

        kid_gift = model.intVarArray("g2k", n_kids, 0, n_gifts, false);
        kid_price = model.intVarArray("p2k", n_kids, 0, max_price, false);
        total_cost = model.intVar("total cost", 0, max_price * n_kids, true);
        average = model.realVar("average", 0, max_price * n_kids, precision);


        gift_price = new int[n_gifts];
        for (int i = 0; i < n_gifts; i++) {
            gift_price[i] = rand.nextInt(max_price) + 1;
        }
        model.allDifferent(kid_gift, "BC").post();
        for (int i = 0; i < n_kids; i++) {
            model.element(kid_price[i], gift_price, kid_gift[i], 0).post();
        }
        model.sum(kid_price, "=", total_cost).post();

        StringBuilder funBuilder = new StringBuilder("(");
        for (int i = 0; i < n_kids; i++) {
            funBuilder.append("+{").append(i).append('}');
        }
        funBuilder.append(")/").append(n_kids).append("=").append('{').append(n_kids).append('}');

        RealVar[] all_vars = append(model.realIntViewArray(kid_price, precision), new RealVar[]{average});
        String function = funBuilder.toString();

        model.realIbexGenericConstraint(function, all_vars).post();
    }

    @Override
    public void configureSearch() {
        Solver r = model.getSolver();
        r.setSearch(domOverWDegSearch(kid_gift));
    }

    @Override
    public void solve() {
        model.getSolver().plugMonitor((IMonitorSolution) () -> {
            out.println("*******************");
            for (int i = 0; i < n_kids; i++) {
                out.println(format("Kids #%d has received the gift #%d at a cost of %d euros",
                        i, kid_gift[i].getValue(), kid_price[i].getValue()));
            }
            out.println(format("Total cost: %d euros", total_cost.getValue()));
            out.println(format("Average: [%.3f,%.3f] euros", average.getLB(), average.getUB()));
        });
        while (model.getSolver().solve()) ;
    }

    public static void main(String[] args) {
        new SantaClaude().execute(args);
    }
}
