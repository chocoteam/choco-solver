/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.*;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.strategy.FullyRandom;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 08/06/11
 */
public class CountTest {

    protected static Model modelit(int n) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("var", n, 0, n - 1, true);
        for (int i = 0; i < n; i++) {
            model.count(i, vars, vars[i]).post();
        }
        model.sum(vars, "=", n).post(); // cstr redundant 1
        int[] coeff2 = new int[n - 1];
        IntVar[] vs2 = new IntVar[n - 1];
        for (int i = 1; i < n; i++) {
            coeff2[i - 1] = i;
            vs2[i - 1] = vars[i];
        }
        model.scalar(vs2, coeff2, "=", n).post(); // cstr redundant 1
        return model;
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testMS4() {
        Model model = modelit(4);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMS8() {
        Model model = modelit(8);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testRandomProblems() {
        for (int bigseed = 1; bigseed < 11; bigseed++) {
            long nbsol, nbsol2;
            //nb solutions of the gac constraint
            long realNbSol = randomOcc(-1, bigseed, true, 1, true);
            //nb solutions of occurrence + enum
            nbsol = randomOcc(realNbSol, bigseed, true, 3, false);
            //b solutions of occurrences + bound
            nbsol2 = randomOcc(realNbSol, bigseed, false, 3, false);
            Assert.assertEquals(nbsol, nbsol2);
            Assert.assertEquals(nbsol2, realNbSol);
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2() {
        int n = 2;
        for (int i = 0; i < 200; i++) {
            Model model = new Model();
            IntVar[] vars = model.intVarArray("o", n, 0, n, true);
            int value = 1;
            IntVar occ = model.count("oc", value, vars);
            IntVar[] allvars = append(vars, new IntVar[]{occ});

            Solver r = model.getSolver();
            r.setSearch(randomSearch(allvars, i));

//        solver.post(getTableForOccurence(solver, vars, occ, value, n));
//            SearchMonitorFactory.log(solver, true, true);
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 9);
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2VE() {
        int n = 2;
        for (int i = 0; i < 20; i++) {
            Model model = new Model();
            IntVar[] vars = model.intVarArray("o", n, 0, n, true);
            IntVar value = model.intVar(new int[]{-5, 1, 3});
            IntVar occ = model.count("oc", value, vars);
            IntVar[] allvars = append(vars, new IntVar[]{occ});
            model.arithm(value, "=", 1).post();
            Solver r = model.getSolver();
            r.setSearch(randomSearch(allvars, i));
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 9);
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2VB() {
        int n = 2;
        for (int i = 0; i < 20; i++) {
            Model model = new Model();
            IntVar[] vars = model.intVarArray("o", n, 0, n, true);
            IntVar value = model.intVar(-5, 5);
            IntVar occ = model.count("oc", value, vars);
            IntVar[] allvars = append(vars, new IntVar[]{occ});
            model.arithm(value, "=", 1).post();
            Solver r = model.getSolver();
            r.setSearch(randomSearch(allvars, i));
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 9);
        }
    }

    public long randomOcc(long nbsol, int seed, boolean enumvar, int nbtest, boolean gac) {
        for (int interseed = 0; interseed < nbtest; interseed++) {
            int nbOcc = 2;
            int nbVar = 9;
            int sizeDom = 4;
            int sizeOccurence = 4;

            Model model = new Model(SettingsBuilder.init().getPropagationEnginType((byte) 0b00));
            IntVar[] vars;
            if (enumvar) {
                vars = model.intVarArray("e", nbVar, 0, sizeDom, false);
            } else {
                vars = model.intVarArray("e", nbVar, 0, sizeDom, true);
            }

            List<IntVar> lvs = new LinkedList<>();
            lvs.addAll(asList(vars));

            Random rand = new Random(seed);
            for (int i = 0; i < nbOcc; i++) {
                IntVar[] vs = new IntVar[sizeOccurence];
                for (int j = 0; j < sizeOccurence; j++) {
                    IntVar iv = lvs.get(rand.nextInt(lvs.size()));
                    lvs.remove(iv);
                    vs[j] = iv;
                }
                IntVar ivc = lvs.get(rand.nextInt(lvs.size()));
                int val = rand.nextInt(sizeDom);
                if (gac) {
                    getTableForOccurence(vs, ivc, val, sizeDom).post();
                } else {
                    model.count(val, vs, ivc).post();
                }
            }
            model.scalar(new IntVar[]{vars[0], vars[3], vars[6]}, new int[]{1, 1, -1}, "=", 0).post();

            //s.setValIntSelector(new RandomIntValSelector(interseed));
            //s.setVarIntSelector(new RandomIntVarSelector(s, interseed + 10));
//            if (!gac) {
//                SearchMonitorFactory.log(solver, true, true);
//            }

            Solver r = model.getSolver();
            r.setSearch(randomSearch(vars, seed));
            while (model.getSolver().solve()) ;
            if (nbsol == -1) {
                nbsol = r.getMeasures().getSolutionCount();
            } else {
                assertEquals(r.getMeasures().getSolutionCount(), nbsol);
            }

        }
        return nbsol;
    }

    /**
     * generate a table to encode an occurrence constraint.
     *
     * @param vs  array of variables
     * @param occ occurence variable
     * @param val value
     * @param ub  upper bound
     * @return Constraint
     */
    public Constraint getTableForOccurence(IntVar[] vs, IntVar occ, int val, int ub) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("e", vs.length + 1, 0, ub, false);

        Tuples tuples = new Tuples(true);
        model.getSolver().setSearch(inputOrderLBSearch(vars));
        model.getSolver().solve();
        do {
            int[] tuple = new int[vars.length];
            for (int i = 0; i < tuple.length; i++) {
                tuple[i] = vars[i].getValue();
            }
            int checkocc = 0;
            for (int i = 0; i < (tuple.length - 1); i++) {
                if (tuple[i] == val) checkocc++;
            }
            if (checkocc == tuple[tuple.length - 1]) {
                tuples.add(tuple);
            }
        } while (model.getSolver().solve() == TRUE);

        IntVar[] newvs = new IntVar[vs.length + 1];
        arraycopy(vs, 0, newvs, 0, vs.length);
        newvs[vs.length] = occ;

        return model.table(newvs, tuples);
    }

    /**
     * generate a table to encode an occurrence constraint.
     *
     * @param vs  array of variables
     * @param occ occurence variable
     * @param val value
     * @return Constraint
     */
    public Constraint getDecomposition(Model model, IntVar[] vs, IntVar occ, int val) {
        BoolVar[] bs = model.boolVarArray("b", vs.length);
        IntVar vval = model.intVar(val);
        for (int i = 0; i < vs.length; i++) {
            model.ifThenElse(bs[i], model.arithm(vs[i], "=", vval), model.arithm(vs[i], "!=", vval));
        }
        return model.sum(bs, "=", occ);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCountIntVarVsDecomposition() {
        for (int seed = 0; seed < 20; seed++) {
            int n = 5;
            int dom = 4;
            // Reference: decomposition via ifThenElse + sum
            Model mRef = new Model();
            IntVar[] varsRef = mRef.intVarArray("v", n, 0, dom, false);
            IntVar valueRef = mRef.intVar("val", 0, dom, true);
            IntVar limitRef = mRef.intVar("lim", 0, n, false);
            getDecomposition(mRef, varsRef, limitRef, valueRef).post();
            IntVar[] allRef = append(varsRef, new IntVar[]{valueRef, limitRef});
            mRef.getSolver().setSearch(new FullyRandom(allRef, seed));
            long nbSolRef = 0;
            while (mRef.getSolver().solve()) nbSolRef++;

            // count(IntVar value, IntVar[] vars, IntVar limit)
            Model mCount = new Model();
            IntVar[] varsCount = mCount.intVarArray("v", n, 0, dom, false);
            IntVar valueCount = mCount.intVar("val", 0, dom, true);
            IntVar limitCount = mCount.intVar("lim", 0, n, false);
            mCount.count(valueCount, varsCount, limitCount).post();
            IntVar[] allCount = append(varsCount, new IntVar[]{valueCount, limitCount});
            mCount.getSolver().setSearch(new FullyRandom(allCount, seed));
            long nbSolCount = 0;
            while (mCount.getSolver().solve()) nbSolCount++;

            assertEquals(nbSolCount, nbSolRef, "seed=" + seed);
        }
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "random")
    @Providers.Arguments(values={"1","20"})
    public void testCountBoundedValueVsDecomposition(int seed) {
        int[][] holes = {
                {0, 2, 4}, {1, 3, 5}, {0, 1, 4, 5}, {2, 3}, {0, 3, 5}
        };
        int n = holes.length;
        // Reference: decomposition via ifThenElse + sum
        Model mRef = new Model();
        IntVar[] varsRef = new IntVar[n];
        for (int i = 0; i < n; i++) {
            varsRef[i] = mRef.intVar("v" + i, holes[i]);
        }
        IntVar valueRef = mRef.intVar("val", 0, 5, true); // bounded
        IntVar limitRef = mRef.intVar("lim", 0, n, false);
        getDecomposition(mRef, varsRef, limitRef, valueRef).post();
        IntVar[] allRef = append(varsRef, new IntVar[]{valueRef, limitRef});
        mRef.getSolver().setSearch(new FullyRandom(allRef, seed));
        long nbSolRef = 0;
        while (mRef.getSolver().solve()) nbSolRef++;

        // count(IntVar value, IntVar[] vars, IntVar limit) with bounded value
        Model mCount = new Model();
        IntVar[] varsCount = new IntVar[n];
        for (int i = 0; i < n; i++) {
            varsCount[i] = mCount.intVar("v" + i, holes[i]);
        }
        IntVar valueCount = mCount.intVar("val", 0, 5, true); // bounded
        IntVar limitCount = mCount.intVar("lim", 0, n, false);
        mCount.count(valueCount, varsCount, limitCount).post();
        IntVar[] allCount = append(varsCount, new IntVar[]{valueCount, limitCount});
        mCount.getSolver().setSearch(new FullyRandom(allCount, seed));
        long nbSolCount = 0;
        while (mCount.getSolver().solve()) nbSolCount++;

        assertEquals(nbSolCount, nbSolRef, "seed=" + seed);
    }

    private Constraint getDecomposition(Model model, IntVar[] vs, IntVar occ, IntVar value) {
        BoolVar[] bs = model.boolVarArray("b", vs.length);
        for (int i = 0; i < vs.length; i++) {
            model.ifThenElse(bs[i], model.arithm(vs[i], "=", value), model.arithm(vs[i], "!=", value));
        }
        return model.sum(bs, "=", occ);
    }
    @DataProvider(name = "testFilteringToZeroProvider")
    public static Object[][] testFilteringToZeroProvider() {
        return new Object[][]{{true}, {false}};
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "testFilteringToZeroProvider")
    public void testFilteringToZero(Boolean boundedDomain) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("vars", 10, 0, 10, boundedDomain);
        IntVar count = model.intVar("count", 0, 10);
        model.count(4, vars, count).post();

        try {
            model.getSolver().propagate();
            for (int i = 0; i < vars.length; i++) {
                Assert.assertEquals(11, vars[i].getDomainSize());
            }
            Assert.assertEquals(11, count.getDomainSize());
        } catch (ContradictionException ex) {
            fail();
        }

        try {
            for (int i = 1; i < vars.length; i++) {
                vars[i].updateLowerBound(5, Cause.Null);
            }
            model.getSolver().propagate();
            for (int i = 0; i < vars.length; i++) {
                if (i == 0) {
                    Assert.assertEquals(11, vars[i].getDomainSize());
                } else {
                    Assert.assertEquals(6, vars[i].getDomainSize());
                }
            }
            Assert.assertEquals(0, count.getLB());
            Assert.assertEquals(1, count.getUB());
        } catch (ContradictionException ex) {
            fail();
        }

        try {
            count.updateUpperBound(0, Cause.Null);
            model.getSolver().propagate();
            for (int i = 0; i < vars.length; i++) {
                if (i == 0) {
                    if (vars[i].hasEnumeratedDomain()) {
                        Assert.assertEquals(10, vars[i].getDomainSize());
                        Assert.assertFalse(vars[i].contains(4));
                    } else {
                        Assert.assertEquals(11, vars[i].getDomainSize());
                        Assert.assertTrue(vars[i].contains(4));
                    }
                } else {
                    Assert.assertEquals(6, vars[i].getDomainSize());
                    Assert.assertFalse(vars[i].contains(4));
                }
            }
            Assert.assertTrue(count.isInstantiatedTo(0));
        } catch (ContradictionException ex) {
            fail();
        }

        try {
            // To test (in debug mode), that we do not enter in PropCount_AC.filter()
            for (int j = 1; j < 4; j++) {
                vars[0].updateLowerBound(j, Cause.Null);
                model.getSolver().propagate();
                for (int i = 0; i < vars.length; i++) {
                    if (i == 0) {
                        if (vars[i].hasEnumeratedDomain()) {
                            Assert.assertEquals(10 - j, vars[i].getDomainSize());
                            Assert.assertFalse(vars[i].contains(4));
                        } else {
                            Assert.assertEquals(11 - j, vars[i].getDomainSize());
                            Assert.assertTrue(vars[i].contains(4));
                        }
                    } else {
                        Assert.assertEquals(6, vars[i].getDomainSize());
                        Assert.assertFalse(vars[i].contains(4));
                    }
                }
                Assert.assertTrue(count.isInstantiatedTo(0));
            }
        } catch (ContradictionException ex) {
            fail();
        }

        try {
            vars[0].updateLowerBound(4, Cause.Null);
            model.getSolver().propagate();
            for (int i = 0; i < vars.length; i++) {
                Assert.assertEquals(6, vars[i].getDomainSize());
                Assert.assertFalse(vars[i].contains(4));
            }
            Assert.assertTrue(count.isInstantiatedTo(0));
        } catch (ContradictionException ex) {
            fail();
        }
    }

    // Filtering checks of count(IntVar value, IntVar[] vars, IntVar limit): every value of an enumerated `value`
    // must be supported (AC), only the bounds of a bounded `value` are guaranteed to be supported (BC).

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "trueOrFalse")
    public void testCountVarUpperBoundCascade(boolean bounded) throws ContradictionException {
        Model model = new Model();
        IntVar[] vars = {
                model.intVar("x0", new int[]{0, 1}),
                model.intVar("x1", new int[]{0, 1}),
                model.intVar("x2", new int[]{0, 1, 2, 3})
        };
        IntVar value = model.intVar("val", 0, 3, bounded);
        IntVar limit = model.intVar("lim", 0, 3, false);
        model.count(value, vars, limit).post();
        model.getSolver().propagate();
        assertEquals(value.getLB(), 0);
        assertEquals(value.getUB(), 3);

        // 2 and 3 can be taken by x2 only: both become unsupported
        limit.updateLowerBound(2, Cause.Null);
        model.getSolver().propagate();
        assertEquals(value.getLB(), 0);
        assertEquals(value.getUB(), 1);
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "trueOrFalse")
    public void testCountVarValueFixedByBounds(boolean bounded) throws ContradictionException {
        Model model = new Model();
        IntVar[] vars = {
                model.intVar("x0", new int[]{0, 1}),
                model.intVar("x1", new int[]{0, 1, 3}),
                model.intVar("x2", new int[]{1, 2, 3})
        };
        IntVar value = model.intVar("val", 0, 3, bounded);
        IntVar limit = model.intVar("lim", 3);
        model.count(value, vars, limit).post();
        model.getSolver().propagate();
        // only 1 can be taken by 3 variables
        assertTrue(value.isInstantiatedTo(1));
        for (IntVar x : vars) {
            assertTrue(x.isInstantiatedTo(1), x.getName());
        }
    }

    /**
     * Random dives (initial and incremental propagation): at each fixpoint, checks the consistency reached on
     * `value`, `limit` and `vars`.
     */
    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "random")
    @Providers.Arguments(values = {"300"})
    public void testCountVarFixpointConsistency(int seed) {
        Random rnd = new Random(seed);
        for (boolean bounded : new boolean[]{true, false}) {
            Model model = new Model();
            int n = 2 + rnd.nextInt(4);
            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = model.intVar("x" + i, 0, 1 + rnd.nextInt(4), rnd.nextBoolean());
            }
            IntVar value = model.intVar("val", -1 + rnd.nextInt(2), 2 + rnd.nextInt(4), bounded);
            IntVar limit = model.intVar("lim", rnd.nextInt(2), 1 + rnd.nextInt(n), rnd.nextBoolean());
            model.count(value, vars, limit).post();
            String ctx = "seed=" + seed + ", bounded=" + bounded;
            try {
                model.getSolver().propagate();
                checkCountVarConsistency(vars, value, limit, bounded, ctx + ", initial");
                for (int step = 0; step < 3 * n; step++) {
                    String action = applyRandomReduction(rnd, append(vars, new IntVar[]{value, limit}));
                    model.getSolver().propagate();
                    checkCountVarConsistency(vars, value, limit, bounded, ctx + ", step " + step + " (" + action + ")");
                }
            } catch (ContradictionException ignored) {
                // a failure is a valid outcome, soundness is checked by testCountVarSolutionsVsDecomposition
            }
        }
    }

    /**
     * {seed, bounded value, LCG}.
     * With LCG, only enumerated domains are checked: count is then decomposed with reifXrelYC,
     * whose reification of x = y produces invalid solutions when x or y has a bounded domain.
     */
    @DataProvider
    public static Object[][] seedsDomainsAndLcg() {
        List<Object[]> data = new ArrayList<>();
        for (int seed = 0; seed < 15; seed++) {
            data.add(new Object[]{seed, true, false});
            data.add(new Object[]{seed, false, false});
            data.add(new Object[]{seed, false, true});
        }
        return data.toArray(new Object[0][]);
    }

    /**
     * Counts solutions with the global constraint (with or without LCG) and with a reference decomposition.
     */
    @Test(groups = "1s", timeOut = 60000, dataProvider = "seedsDomainsAndLcg")
    public void testCountVarSolutionsVsDecomposition(int seed, boolean bounded, boolean lcg) {
        long expected = countVarSolutions(seed, bounded, false, false, lcg);
        long actual = countVarSolutions(seed, bounded, lcg, true, lcg);
        assertEquals(actual, expected, "seed=" + seed);
    }

    @Test(groups = "1s", timeOut = 60000, dataProviderClass = Providers.class, dataProvider = "trueOrFalse")
    public void testCountVarReification(boolean bounded) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("x", 3, 0, 2, false);
        IntVar value = model.intVar("val", -1, 3, bounded);
        IntVar limit = model.intVar("lim", 0, 3, false);
        BoolVar b = model.count(value, vars, limit).reify();
        Set<String> seen = new HashSet<>();
        while (model.getSolver().solve()) {
            int nb = 0;
            StringBuilder key = new StringBuilder();
            for (IntVar x : vars) {
                key.append(x.getValue()).append(',');
                if (x.getValue() == value.getValue()) {
                    nb++;
                }
            }
            key.append(value.getValue()).append(',').append(limit.getValue());
            if (seen.add(key + "," + b.getValue())) {
                assertEquals(b.getValue() == 1, nb == limit.getValue(), key.toString());
            }
        }
        // exactly one b per assignment of (vars, value, limit)
        assertEquals(seen.size(), 3 * 3 * 3 * 5 * 4);
    }

    private long countVarSolutions(int seed, boolean bounded, boolean lcg, boolean global, boolean enumeratedOnly) {
        Random rnd = new Random(seed);
        Model model = new Model(SettingsBuilder.init().setLCG(lcg));
        int n = 3 + rnd.nextInt(2);
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x" + i, rnd.nextInt(2), 1 + rnd.nextInt(3), rnd.nextBoolean() && !enumeratedOnly);
        }
        IntVar value = model.intVar("val", -1, 3 + rnd.nextInt(2), bounded && !enumeratedOnly);
        IntVar limit = model.intVar("lim", rnd.nextInt(2), 1 + rnd.nextInt(n), rnd.nextBoolean());
        if (global) {
            model.count(value, vars, limit).post();
        } else {
            getDecomposition(model, vars, limit, value).post();
        }
        IntVar[] all = append(vars, new IntVar[]{value, limit});
        if (lcg) {
            // bounded domains only provide bound literals in LCG (IntVarLazyLit): branch with bound splits
            model.getSolver().setSearch(Search.intVarSearch(
                    new org.chocosolver.solver.search.strategy.selectors.variables.Random<>(seed),
                    new IntDomainMin(), DecisionOperatorFactory.makeIntSplit(), all));
        } else {
            model.getSolver().setSearch(new FullyRandom(all, seed));
        }
        long nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
        }
        return nbSol;
    }

    private static String applyRandomReduction(Random rnd, IntVar[] all) throws ContradictionException {
        IntVar x = all[rnd.nextInt(all.length)];
        if (x.isInstantiated()) {
            return "none";
        }
        // a value of the domain
        int v = x.nextValue(x.getLB() - 1 + rnd.nextInt(x.getUB() - x.getLB() + 1));
        switch (rnd.nextInt(4)) {
            case 0:
                x.updateLowerBound(v, Cause.Null);
                return x.getName() + ">=" + v;
            case 1:
                x.updateUpperBound(v, Cause.Null);
                return x.getName() + "<=" + v;
            case 2:
                x.removeValue(v, Cause.Null);
                return x.getName() + "!=" + v;
            default:
                x.instantiateTo(v, Cause.Null);
                return x.getName() + "=" + v;
        }
    }

    private static int nbContaining(IntVar[] vars, int v) {
        return (int) Arrays.stream(vars).filter(x -> x.contains(v)).count();
    }

    private static int nbInstantiatedTo(IntVar[] vars, int v) {
        return (int) Arrays.stream(vars).filter(x -> x.isInstantiatedTo(v)).count();
    }

    private static void checkCountVarConsistency(IntVar[] vars, IntVar value, IntVar limit, boolean bounded, String ctx) {
        String msg = ctx + ", val=" + value + ", lim=" + limit + ", vars=" + Arrays.toString(vars);
        int minCard = Integer.MAX_VALUE;
        int maxCard = Integer.MIN_VALUE;
        for (int v = value.getLB(); v <= value.getUB(); v = value.nextValue(v)) {
            int min = nbInstantiatedTo(vars, v);
            int max = nbContaining(vars, v);
            if (limit.getLB() <= max && min <= limit.getUB()) {
                minCard = Math.min(minCard, min);
                maxCard = Math.max(maxCard, max);
            } else if (!bounded || v == value.getLB() || v == value.getUB()) {
                fail(v + " is not supported: " + msg);
            }
        }
        assertTrue(minCard <= limit.getLB() && limit.getUB() <= maxCard, "limit is not filtered: " + msg);
        if (value.isInstantiated() && limit.isInstantiated()) {
            int v = value.getValue();
            int nb = limit.getValue();
            if (nb == nbContaining(vars, v)) {
                for (IntVar x : vars) {
                    if (x.contains(v)) {
                        assertTrue(x.isInstantiatedTo(v), x.getName() + " should be " + v + ": " + msg);
                    }
                }
            } else if (nb == nbInstantiatedTo(vars, v)) {
                for (IntVar x : vars) {
                    // interior values of bounded domains cannot be removed
                    if (!x.isInstantiated() && (x.hasEnumeratedDomain() || x.getLB() == v || x.getUB() == v)) {
                        assertFalse(x.contains(v), x.getName() + " should not contain " + v + ": " + msg);
                    }
                }
            }
        }
    }
}
