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

import static org.chocosolver.solver.constraints.checker.DomainBuilder.buildFullDomains;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import gnu.trove.list.array.TIntArrayList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.alldifferent.PropAllDiffBC;
import org.chocosolver.solver.constraints.nary.alldifferent.PropAllDiffInst;
import org.chocosolver.solver.constraints.nary.alldifferentprec.PropAllDiffPrec;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Arthur Godet
 * @since 16 july 2021
 */
public class AllDiffPrecTest {

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        // Example where GODET_BC and GODET_RC filter range consistent value (3 for vars[0])
        int[][] predecessors = new int[][]{ new int[]{}, new int[]{0}, new int[]{1}, new int[]{0} };
        int[][] successors = new int[][]{ new int[]{1,3}, new int[]{2}, new int[]{}, new int[]{} };
        int[][] domains = new int[][]{ new int[]{1,3}, new int[]{2,5}, new int[]{3,4,6}, new int[]{2,5} };

        String[] filters = new String[]{"BESSIERE", "GREEDY", "GREEDY_RC", "GODET_BC", "GODET_RC"};
        for(int i = 0; i < filters.length; i++) {
            Model model = new Model();
            IntVar[] vars = new IntVar[domains.length];
            for(int k = 0; k < domains.length; k++) {
                vars[k] = model.intVar("vars["+k+"]", domains[k]);
            }
            model.allDiffPrec(vars, predecessors, successors, filters[i]).post();
            try {
                model.getSolver().propagate();
                for(int k = 0; k < domains.length; k++) {
                    for(int j : domains[k]) {
                        if(k == 0 && j == 3 && (filters[i].equals("GODET_BC") || filters[i].equals("GODET_RC"))) {
                            Assert.assertFalse(vars[k].contains(j));
                        } else {
                            Assert.assertTrue(vars[k].contains(j));
                        }
                    }
                }
            } catch(ContradictionException ex) {
                fail();
            }
        }
    }

    public static long SEED = System.currentTimeMillis();
    public static final Random RND = new Random(SEED);
    private static final TIntArrayList list = new TIntArrayList();

    @DataProvider(name = "data-provider")
    public Object[][] dataProvider() {
        int maxSize = 20;
        Object[][] res = new Object[100][3];
        for(int k = 0; k < res.length; k++) {
            RND.setSeed(SEED + k);
            int n = RND.nextInt(maxSize);
            while(n <= 1) {
                n = RND.nextInt(maxSize);
            }
            int prob = k % 2 == 0 ? 33 : 50;
            int[][] successors = new int[n][];
            for(int i = 0; i < n; i++) {
                list.clear();
                for(int j = i + 1; j < n; j++) {
                    if(RND.nextInt(100) <= prob) {
                        list.add(j);
                    }
                }
                list.sort();
                successors[i] = list.toArray();
            }
            int[][] predecessors = new int[n][];
            for(int i = 0; i < n; i++) {
                list.clear();
                int finalI = i;
                for(int j = 0; j < n; j++) {
                    if(Arrays.stream(successors[j]).anyMatch(l -> l == finalI)) {
                        list.add(j);
                    }
                }
                list.sort();
                predecessors[i] = list.toArray();
            }
            int maxDomain = n + RND.nextInt(n);
            int[][] domains = new int[n][];
            for(int i = 0; i < n; i++) {
                list.clear();
                for(int v = 0; v < maxDomain; v++) {
                    if(RND.nextInt(100) <= 80) {
                        list.add(v);
                    }
                }
                list.sort();
                domains[i] = list.toArray();
                if(list.size() == 0) {
                    i--;
                }
            }
            res[k] = new Object[]{PropAllDiffPrec.buildAncestors(predecessors, successors), PropAllDiffPrec.buildDescendants(predecessors, successors), domains};
        }
        return res;
    }

    public final static String[] ALL_CONSISTENCIES = new String[]{
        "DECOMPOSITION", "BESSIERE", "GREEDY", "GODET", "GREEDY_RC", "GODET_RC"
    };
    public final static int[][] INCLUDES = new int[][]{
        new int[]{1,0}, // BESSIERE includes in DECOMPOSITION
        new int[]{2,1}, // GREEDY includes in BESSIERE
        new int[]{1,2}, // BESSIERE includes in GREEDY
        new int[]{3,2}, // GREEDY includes in GODET
        new int[]{4,2}, // GREEDY_RC includes in GREEDY
        new int[]{5,3}, // GODET_RC includes in GODET
        new int[]{5,4}, // GODET_RC includes in GREEDY_RC
    };

    public static Model createModel(int[][] ancestors, int[][] descendants, int[][] domains, String consistency) {
        Model model = new Model();
        IntVar[] vars = new IntVar[domains.length];
        for(int k = 0; k < vars.length; k++) {
            vars[k] = model.intVar("vars["+k+"]", domains[k]);
        }
        boolean[][] precedence = PropAllDiffPrec.buildPrecedence(ancestors, descendants, true);
        if(consistency.equals("DECOMPOSITION")) {
            model.allDifferent(vars, "BC").post();
            for(int i = 0; i < precedence.length; i++) {
                for(int j = 0; j < precedence.length; j++) {
                    if(i != j && precedence[i][j]) {
                        model.arithm(vars[i], "<", vars[j]).post();
                    }
                }
            }
        } else {
            model.allDiffPrec(vars, precedence, consistency).post();
            model.post(new Constraint(ConstraintsName.ALLDIFFERENT, new PropAllDiffInst(vars)));
        }
        return model;
    }

    private static boolean allConsistent(boolean[] array) {
        for(int k = 0; k < INCLUDES.length; k++) {
            int i = INCLUDES[k][0];
            int j = INCLUDES[k][1];
            if(!array[i] && array[j]) { // models[i] should not have solution when included into models[j]
                return false;
            }
        }
        return true;
    }

    private static boolean include(Model model1, Model model2) {
        IntVar[] vars1 = new IntVar[model1.getVars().length];
        IntVar[] vars2 = new IntVar[model2.getVars().length];
        for(int i = 0; i < vars1.length; i++) {
            vars1[i] = (IntVar) model1.getVars()[i];
            vars2[i] = (IntVar) model2.getVars()[i];
        }
        for(int i = 0; i < vars1.length; i++) {
            for(int v = vars1[i].getLB(); v <= vars1[i].getUB(); v = vars1[i].nextValue(v)) {
                if(!vars2[i].contains(v)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Test(groups="10s", dataProvider = "data-provider")
    public void compareAllDiffPrecImplementationsFiltering(Object[] data) throws IOException {
        int[][] ancestors = (int[][]) data[0];
        int[][] descendants = (int[][]) data[1];
        int[][] domains = (int[][]) data[2];
        Model[] models = new Model[ALL_CONSISTENCIES.length];
        for(int i = 0; i < models.length; i++) {
            models[i] = createModel(ancestors, descendants, domains, ALL_CONSISTENCIES[i]);
        }
        boolean[] noSolution = new boolean[models.length];
        for(int i = 0; i < models.length; i++) {
            try {
                models[i].getSolver().propagate();
            } catch(ContradictionException exception) {
                noSolution[i] = true;
            }
        }
        Assert.assertTrue(allConsistent(noSolution));
        if(!noSolution[0]) { // compare domains only if there is not a fail
            for(int k = 0; k < INCLUDES.length; k++) {
                int i = INCLUDES[k][0];
                int j = INCLUDES[k][1];
                if(!noSolution[i] && !noSolution[j]) { // compare domains only when no fail has occurred for both models
                    Assert.assertTrue(include(models[i], models[j]));
                }
            }
        }
    }

}