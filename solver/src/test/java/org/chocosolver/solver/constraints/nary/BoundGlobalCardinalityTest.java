/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.constraints.nary.globalcardinality.GlobalCardinality.reformulate;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/11
 */
public class BoundGlobalCardinalityTest {

    @Test(groups="1s", timeOut=60000)
    public void test0() throws ContradictionException {
        Model model = new Model();

        IntVar[] vars = model.intVarArray("vars", 6, 0, 3, true);
        IntVar[] card = model.intVarArray("card", 4, 0, 6, true);

        int[] values = new int[4];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }
        model.globalCardinality(vars, values, card, false).post();

        vars[0].instantiateTo(0, Null);
        vars[1].instantiateTo(1, Null);
        vars[2].instantiateTo(3, Null);
        vars[3].instantiateTo(2, Null);
        vars[4].instantiateTo(0, Null);
        vars[5].instantiateTo(0, Null);

        model.getSolver().setSearch(inputOrderLBSearch(append(vars, card)));
        while (model.getSolver().solve()) ;
        assertTrue(model.getSolver().getSolutionCount() > 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void testRandom() {
        Random random = new Random();
//        int seed= 108;{
        for (int seed = 0; seed < 200; seed++) {
//            System.out.println(seed);
            random.setSeed(seed);
            int n = 1 + random.nextInt(6);
            int m = 1 + random.nextInt(4);
            //solver 1
            Model model = new Model(Settings.init().setCheckDeclaredConstraints(false));
            int[] values = new int[m];
            for (int i = 0; i < values.length; i++) {
                values[i] = i;
            }
            {
                IntVar[] vars = model.intVarArray("vars", n, 0, m - 1, true);
                IntVar[] cards = model.intVarArray("cards", m, 0, n, true);
                model.globalCardinality(vars, values, cards, false).post();
//              solver.set(StrategyFactory.random(ArrayUtils.append(vars, cards), solver.getEnvironment(), seed));
                model.getSolver().setSearch(inputOrderLBSearch(append(vars, cards)));
            }
            // reformulation
            Model ref = new Model(Settings.init().setCheckDeclaredConstraints(false));
            {
                IntVar[] vars = ref.intVarArray("vars", n, 0, m - 1, true);
                IntVar[] cards = ref.intVarArray("cards", m, 0, n, true);
                reformulate(vars, cards, ref).post();
                ref.getSolver().setSearch(inputOrderLBSearch(append(vars, cards)));
            }
//            SearchMonitorFactory.log(solver, false, true);
            while (model.getSolver().solve()) ;
            while (ref.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), ref.getSolver().getSolutionCount());

        }
    }

    @Test(groups="10s", timeOut=60000)
    public void testRandom2() {
        Random random = new Random();
        for (int seed = 0; seed < 200; seed++) {
//            System.out.println(seed);
            random.setSeed(seed);
            int n = 1 + random.nextInt(6);
            int m = 1 + random.nextInt(4);
            //solver 1
            Model model = new Model(Settings.init().setCheckDeclaredConstraints(false));
            int[] values = new int[m];
            for (int i = 0; i < values.length; i++) {
                values[i] = i;
            }
            {
                IntVar[] vars = model.intVarArray("vars", n, 0, m - 1, true);
                IntVar[] cards = model.intVarArray("cards", m, 0, n, true);
                model.globalCardinality(vars, values, cards, false).post();
//                solver.set(StrategyFactory.random(ArrayUtils.append(vars, cards), solver.getEnvironment(), seed));
                model.getSolver().setSearch(inputOrderLBSearch(vars));
            }
            // reformulation
            Model ref = new Model(Settings.init().setCheckDeclaredConstraints(false));
            {
                IntVar[] cards = ref.intVarArray("cards", m, 0, n, true);
                IntVar[] vars = ref.intVarArray("vars", n, 0, m - 1, true);
                reformulate(vars, cards, ref).post();
                ref.getSolver().setSearch(inputOrderLBSearch(vars));
            }
//            SearchMonitorFactory.log(solver, false, true);
            while (model.getSolver().solve()) ;
            while (ref.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), ref.getSolver().getSolutionCount());

        }
    }
}
