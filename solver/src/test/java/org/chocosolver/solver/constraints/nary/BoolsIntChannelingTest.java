/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Providers;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.strategy.FullyRandom;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 * @author Charles Prud'homme
 */
public class BoolsIntChannelingTest {


    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testNominal(boolean bounded, SettingsBuilder viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = model.boolVarArray(5);
        IntVar intVar = makeVariable(model, 0, 4, bounded);
        model.boolsIntChanneling(boolVars, intVar, 0).post();

        checkSolutions(model, boolVars, intVar);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testTwoTrue(boolean bounded, SettingsBuilder viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = new BoolVar[] {
                model.boolVar(true),
                model.boolVar(true),
                model.boolVar(),
                model.boolVar()
        };
        IntVar intVar = makeVariable(model, 0, 3, bounded);
        model.boolsIntChanneling(boolVars, intVar, 0).post();

        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testAllFalse(boolean bounded, SettingsBuilder viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = new BoolVar[5];
        for (int i = 0; i < boolVars.length; i++) {
            boolVars[i] = model.boolVar(false);
        }
        IntVar intVar = makeVariable(model, 0, 4, bounded);
        model.boolsIntChanneling(boolVars, intVar, 0).post();

        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testInstantiatedIndex(boolean bounded, SettingsBuilder viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = model.boolVarArray(5);
        IntVar variable = makeVariable(model, 2, 2, bounded);
        model.boolsIntChanneling(boolVars, variable, 0).post();

        int nbSol = checkSolutions(model, boolVars, variable);
        assertEquals(nbSol, 1);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testOutOfBoundsOK(boolean bounded, SettingsBuilder viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = model.boolVarArray(3);
        IntVar intVar = makeVariable(model, 0, 3, bounded);
        model.boolsIntChanneling(boolVars, intVar, 0).post();
        checkSolutions(model, boolVars, intVar);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testOutOfBoundsKO(boolean bounded, SettingsBuilder viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = new BoolVar[] {
                model.boolVar(false),
                model.boolVar(false),
                model.boolVar(false)
        };
        // 3 is deleted from the domain
        IntVar intVar = makeVariable(model, 0, 3, bounded);
        model.boolsIntChanneling(boolVars, intVar, 0).post();
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testDifferentDomains(boolean bounded, SettingsBuilder viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = model.boolVarArray(5);
        IntVar var = makeVariable(model, 5, 10, bounded);
        model.boolsIntChanneling(boolVars, var, 0).post();
        // no matching between indexes and domain
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testEmptyArray(boolean bounded, SettingsBuilder viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = model.boolVarArray(0);
        IntVar intVar = makeVariable(model, 0, 100, bounded);
        model.boolsIntChanneling(boolVars, intVar, 0).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    // Filtering checks: on both domain types, bVars fixed to 0 tighten the bounds of the integer variable (BC),
    // interior values are removed only on enumerated domains (AC).

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "trueOrFalse")
    public void testInitialPropagationLowerBound(boolean bounded) throws ContradictionException {
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("b", 6);
        IntVar x = model.intVar("x", 0, 5, bounded);
        bs[0].instantiateTo(0, Cause.Null);
        bs[1].instantiateTo(0, Cause.Null);
        bs[3].instantiateTo(0, Cause.Null);
        model.boolsIntChanneling(bs, x, 0).post();
        model.getSolver().propagate();
        assertEquals(x.getLB(), 2);
        assertEquals(x.getUB(), 5);
        assertEquals(x.contains(3), bounded);
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "trueOrFalse")
    public void testInitialPropagationUpperBound(boolean bounded) throws ContradictionException {
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("b", 6);
        IntVar x = model.intVar("x", 0, 5, bounded);
        bs[5].instantiateTo(0, Cause.Null);
        bs[4].instantiateTo(0, Cause.Null);
        bs[2].instantiateTo(0, Cause.Null);
        model.boolsIntChanneling(bs, x, 0).post();
        model.getSolver().propagate();
        assertEquals(x.getLB(), 0);
        assertEquals(x.getUB(), 3);
        assertEquals(x.contains(2), bounded);
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "trueOrFalse")
    public void testInitialPropagationLowerBoundInsideArray(boolean bounded) throws ContradictionException {
        // the domain of x does not start at the first index: bVars fixed to 0 are not at the start of the array
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("b", 5);
        IntVar x = model.intVar("x", 2, 4, bounded);
        bs[2].instantiateTo(0, Cause.Null);
        bs[3].instantiateTo(0, Cause.Null);
        model.boolsIntChanneling(bs, x, 0).post();
        model.getSolver().propagate();
        assertTrue(x.isInstantiatedTo(4));
        assertTrue(bs[4].isInstantiatedTo(1));
        assertTrue(bs[0].isInstantiatedTo(0));
        assertTrue(bs[1].isInstantiatedTo(0));
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "trueOrFalse")
    public void testInitialPropagationUpperBoundInsideArray(boolean bounded) throws ContradictionException {
        // the domain of x does not end at the last index: bVars fixed to 0 are not at the end of the array
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("b", 5);
        IntVar x = model.intVar("x", 0, 2, bounded);
        bs[1].instantiateTo(0, Cause.Null);
        bs[2].instantiateTo(0, Cause.Null);
        model.boolsIntChanneling(bs, x, 0).post();
        model.getSolver().propagate();
        assertTrue(x.isInstantiatedTo(0));
        assertTrue(bs[0].isInstantiatedTo(1));
        assertTrue(bs[3].isInstantiatedTo(0));
        assertTrue(bs[4].isInstantiatedTo(0));
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "trueOrFalse")
    public void testIncrementalCascadeFromLowerBound(boolean bounded) throws ContradictionException {
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("b", 6);
        IntVar x = model.intVar("x", 0, 5, bounded);
        model.boolsIntChanneling(bs, x, 0).post();
        model.getSolver().propagate();

        bs[2].instantiateTo(0, Cause.Null);
        model.getSolver().propagate();
        assertEquals(x.getLB(), 0);
        assertEquals(x.contains(2), bounded);

        bs[1].instantiateTo(0, Cause.Null);
        model.getSolver().propagate();
        assertEquals(x.getLB(), 0);
        assertEquals(x.contains(1), bounded);

        // removing the lower bound must cascade over the values already known to be unsupported
        bs[0].instantiateTo(0, Cause.Null);
        model.getSolver().propagate();
        assertEquals(x.getLB(), 3);
        assertEquals(x.getUB(), 5);
        assertEquals(x.getDomainSize(), 3);
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "trueOrFalse")
    public void testIncrementalCascadeFromUpperBound(boolean bounded) throws ContradictionException {
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("b", 6);
        IntVar x = model.intVar("x", 0, 5, bounded);
        model.boolsIntChanneling(bs, x, 0).post();
        model.getSolver().propagate();

        bs[3].instantiateTo(0, Cause.Null);
        model.getSolver().propagate();
        assertEquals(x.getUB(), 5);
        assertEquals(x.contains(3), bounded);

        bs[4].instantiateTo(0, Cause.Null);
        model.getSolver().propagate();
        assertEquals(x.getUB(), 5);
        assertEquals(x.contains(4), bounded);

        // removing the upper bound must cascade over the values already known to be unsupported
        bs[5].instantiateTo(0, Cause.Null);
        model.getSolver().propagate();
        assertEquals(x.getLB(), 0);
        assertEquals(x.getUB(), 2);
        assertEquals(x.getDomainSize(), 3);
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "trueOrFalse")
    public void testIncrementalIntVarModifications(boolean bounded) throws ContradictionException {
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("b", 6);
        IntVar x = model.intVar("x", 0, 5, bounded);
        model.boolsIntChanneling(bs, x, 0).post();
        model.getSolver().propagate();

        // interior value removal: only visible on enumerated domains
        x.removeValue(3, Cause.Null);
        model.getSolver().propagate();
        assertEquals(bs[3].isInstantiatedTo(0), !bounded);
        assertFalse(bs[3].isInstantiatedTo(1));

        x.updateLowerBound(2, Cause.Null);
        model.getSolver().propagate();
        assertTrue(bs[0].isInstantiatedTo(0));
        assertTrue(bs[1].isInstantiatedTo(0));
        assertFalse(bs[2].isInstantiated());

        x.updateUpperBound(3, Cause.Null);
        model.getSolver().propagate();
        assertTrue(bs[4].isInstantiatedTo(0));
        assertTrue(bs[5].isInstantiatedTo(0));
        if (bounded) {
            assertFalse(bs[2].isInstantiated());
            assertFalse(bs[3].isInstantiated());
        } else {
            assertTrue(x.isInstantiatedTo(2));
            assertTrue(bs[2].isInstantiatedTo(1));
        }

        x.removeValue(3, Cause.Null);
        model.getSolver().propagate();
        assertTrue(x.isInstantiatedTo(2));
        assertTrue(bs[2].isInstantiatedTo(1));
        assertTrue(bs[3].isInstantiatedTo(0));
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "trueOrFalse")
    public void testIncrementalBoolToOne(boolean bounded) throws ContradictionException {
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("b", 6);
        IntVar x = model.intVar("x", 0, 5, bounded);
        model.boolsIntChanneling(bs, x, 0).post();
        model.getSolver().propagate();

        bs[3].instantiateTo(1, Cause.Null);
        model.getSolver().propagate();
        assertTrue(x.isInstantiatedTo(3));
        for (int i = 0; i < bs.length; i++) {
            assertTrue(bs[i].isInstantiatedTo(i == 3 ? 1 : 0), "b[" + i + "]");
        }
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "trueOrFalse")
    public void testOffset(boolean bounded) throws ContradictionException {
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("b", 4);
        IntVar x = model.intVar("x", -3, 10, bounded);
        model.boolsIntChanneling(bs, x, 1).post();
        model.getSolver().propagate();
        assertEquals(x.getLB(), 1);
        assertEquals(x.getUB(), 4);

        bs[0].instantiateTo(0, Cause.Null);
        model.getSolver().propagate();
        assertEquals(x.getLB(), 2);

        bs[3].instantiateTo(0, Cause.Null);
        model.getSolver().propagate();
        assertEquals(x.getUB(), 3);

        bs[2].instantiateTo(0, Cause.Null);
        model.getSolver().propagate();
        assertTrue(x.isInstantiatedTo(2));
        assertTrue(bs[1].isInstantiatedTo(1));
    }

    /**
     * Random dives (initial and incremental propagation): at each fixpoint,
     * the integer variable must be AC on enumerated domains and BC on bounded domains.
     */
    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "random")
    @Providers.Arguments(values = {"200"})
    public void testFixpointConsistency(int seed) {
        Random rnd = new Random(seed);
        for (boolean bounded : new boolean[]{true, false}) {
            int n = 2 + rnd.nextInt(6);
            int offset = rnd.nextInt(5) - 2;
            int lb = offset - 1 + rnd.nextInt(3);
            int ub = lb + 2 + rnd.nextInt(n + 1);
            Model model = new Model();
            BoolVar[] bs = model.boolVarArray("b", n);
            IntVar x = model.intVar("x", lb, ub, bounded);
            model.boolsIntChanneling(bs, x, offset).post();
            String ctx = "seed=" + seed + ", bounded=" + bounded + ", offset=" + offset;
            try {
                for (int i = 0; i < n; i++) {
                    if (rnd.nextInt(3) == 0) {
                        bs[i].instantiateTo(0, Cause.Null);
                    }
                }
                model.getSolver().propagate();
                checkConsistency(bs, x, offset, bounded, ctx + ", initial");
                for (int step = 0; step < 2 * n; step++) {
                    String action = applyRandomReduction(rnd, bs, x);
                    model.getSolver().propagate();
                    checkConsistency(bs, x, offset, bounded, ctx + ", step " + step + " (" + action + ")");
                }
            } catch (ContradictionException ignored) {
                // a failure is a valid outcome, soundness is checked by testSolutionsVsDecomposition
            }
        }
    }

    @DataProvider
    public static Object[][] seedsDomainsAndLcg() {
        Object[][] seeds = IntStream.range(0, 10).mapToObj(i -> new Object[]{i}).toArray(Object[][]::new);
        return Providers.merge(seeds, Providers.trueOrFalse(), Providers.trueOrFalse());
    }

    /**
     * Counts solutions with the propagator (with or without LCG) and with a reference decomposition.
     */
    @Test(groups = "1s", timeOut = 60000, dataProvider = "seedsDomainsAndLcg")
    public void testSolutionsVsDecomposition(int seed, boolean bounded, boolean lcg) {
        Random rnd = new Random(seed);
        int n = 3 + rnd.nextInt(4);
        int offset = rnd.nextInt(3) - 1;
        int lb = offset - 1 + rnd.nextInt(2);
        int ub = offset + n - rnd.nextInt(2);
        boolean[] fixedToZero = new boolean[n];
        for (int i = 0; i < n; i++) {
            fixedToZero[i] = rnd.nextInt(3) == 0;
        }
        long expected = countSolutions(seed, n, offset, lb, ub, bounded, false, fixedToZero, false);
        long actual = countSolutions(seed, n, offset, lb, ub, bounded, lcg, fixedToZero, true);
        assertEquals(actual, expected, "seed=" + seed + ", fixedToZero=" + Arrays.toString(fixedToZero));
    }

    private static long countSolutions(int seed, int n, int offset, int lb, int ub, boolean bounded, boolean lcg,
                                       boolean[] fixedToZero, boolean channeling) {
        Model model = new Model(SettingsBuilder.init().setLCG(lcg));
        BoolVar[] bs = new BoolVar[n];
        for (int i = 0; i < n; i++) {
            bs[i] = fixedToZero[i] ? model.boolVar(false) : model.boolVar("b" + i);
        }
        IntVar x = model.intVar("x", lb, ub, bounded);
        if (channeling) {
            model.boolsIntChanneling(bs, x, offset).post();
        } else {
            model.member(x, offset, n - 1 + offset).post();
            for (int i = 0; i < n; i++) {
                model.arithm(x, "=", i + offset).reifyWith(bs[i]);
            }
        }
        model.getSolver().setSearch(new FullyRandom(ArrayUtils.concat(bs, x), seed));
        long nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
        }
        return nbSol;
    }

    private static String applyRandomReduction(Random rnd, BoolVar[] bs, IntVar x) throws ContradictionException {
        int i = rnd.nextInt(bs.length);
        switch (rnd.nextInt(6)) {
            case 0:
            case 1:
            case 2:
                if (!bs[i].isInstantiated()) {
                    bs[i].instantiateTo(0, Cause.Null);
                    return "b[" + i + "]=0";
                }
                return "none";
            case 3:
                if (!bs[i].isInstantiated()) {
                    bs[i].instantiateTo(1, Cause.Null);
                    return "b[" + i + "]=1";
                }
                return "none";
            case 4:
                if (!x.isInstantiated()) {
                    if (rnd.nextBoolean()) {
                        x.updateLowerBound(x.getLB() + 1, Cause.Null);
                        return "x>" + (x.getLB() - 1);
                    } else {
                        x.updateUpperBound(x.getUB() - 1, Cause.Null);
                        return "x<" + (x.getUB() + 1);
                    }
                }
                return "none";
            default:
                if (!x.isInstantiated()) {
                    int v = x.getLB() + rnd.nextInt(x.getUB() - x.getLB() + 1);
                    x.removeValue(v, Cause.Null);
                    return "x!=" + v;
                }
                return "none";
        }
    }

    private static void checkConsistency(BoolVar[] bs, IntVar x, int offset, boolean bounded, String ctx) {
        String msg = ctx + ", x=" + x + ", b=" + Arrays.toString(bs);
        int n = bs.length;
        assertTrue(x.getLB() >= offset && x.getUB() <= n - 1 + offset, msg);
        for (int i = 0; i < n; i++) {
            if (!x.contains(i + offset)) {
                assertTrue(bs[i].isInstantiatedTo(0), "b[" + i + "] should be 0: " + msg);
            }
            if (bs[i].isInstantiatedTo(1)) {
                assertTrue(x.isInstantiatedTo(i + offset), "x should be instantiated: " + msg);
            }
        }
        if (x.isInstantiated()) {
            assertTrue(bs[x.getValue() - offset].isInstantiatedTo(1), "b should be 1: " + msg);
        }
        if (bounded) {
            assertFalse(bs[x.getLB() - offset].isInstantiatedTo(0), "LB is not supported: " + msg);
            assertFalse(bs[x.getUB() - offset].isInstantiatedTo(0), "UB is not supported: " + msg);
        } else {
            for (int v = x.getLB(); v <= x.getUB(); v = x.nextValue(v)) {
                assertFalse(bs[v - offset].isInstantiatedTo(0), v + " is not supported: " + msg);
            }
        }
    }

    private IntVar makeVariable(Model model, int lb, int ub, boolean bounded) {
        IntVar var = model.intVar(lb, ub, bounded);
        if(model.getSettings().enableViews()) {
            IntVar first = model.offset(var, 1);
            return model.offset(first, -1);
        } else {
            return var;
        }
    }

    private int checkSolutions(Model model, BoolVar[] boolVars, IntVar intVar) {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            for (int i = 0; i < boolVars.length; i++) {
                if(boolVars[i].getValue() == 1) {
                    assertEquals(i, intVar.getValue());
                }
            }
            assertEquals(boolVars[intVar.getValue()].getValue(), 1);
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }


}
