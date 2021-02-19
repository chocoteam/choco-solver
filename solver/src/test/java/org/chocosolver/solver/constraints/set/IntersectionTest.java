/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.set;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.loop.monitors.IMonitorDownBranch;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.SetDomainMin;
import org.chocosolver.solver.search.strategy.strategy.SetStrategy;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.annotations.Test;

import java.util.Random;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class IntersectionTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(3, new int[]{}, new int[]{0, 1, 2, 3, 4, 5});
        SetVar intersect = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6});
        model.intersection(setVars, intersect).post();

        assertEquals(229376, checkSolutions(model, setVars, intersect));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNominalBoundConsistent() {
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(3, new int[]{}, new int[]{0, 1, 2, 3, 4, 5});
        SetVar intersect = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6});
        model.intersection(setVars, intersect, true).post();

        assertEquals(229376, checkSolutions(model, setVars, intersect));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testVarsToIntersect() {
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3, 4, 5, 6});
        SetVar intersect = model.setVar(new int[]{1, 2, 3, 4});
        model.intersection(setVars, intersect).post();

        assertEquals(961, checkSolutions(model, setVars, intersect));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testVarsToIntersectBoundConsistent() {
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3, 4, 5, 6});
        SetVar intersect = model.setVar(new int[]{1, 2, 3, 4});
        model.intersection(setVars, intersect, true).post();

        assertEquals(961, checkSolutions(model, setVars, intersect));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testIntersectToVars() {
        Model model = new Model();
        SetVar[] setVars = new SetVar[3];
        setVars[0] = model.setVar(new int[]{0, 1, 2});
        setVars[1] = model.setVar(new int[]{1, 2, 3});
        setVars[2] = model.setVar(new int[]{2, 4, 5});
        SetVar intersect = model.setVar(new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        model.intersection(setVars, intersect).post();

        assertEquals(1, checkSolutions(model, setVars, intersect));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testIntersectToVarsBoundConsistent() {
        Model model = new Model();
        SetVar[] setVars = new SetVar[3];
        setVars[0] = model.setVar(new int[]{0, 1, 2});
        setVars[1] = model.setVar(new int[]{1, 2, 3});
        setVars[2] = model.setVar(new int[]{2, 4, 5});
        SetVar intersect = model.setVar(new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        model.intersection(setVars, intersect, true).post();

        assertEquals(1, checkSolutions(model, setVars, intersect));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testDifferentDomains() {
        Model model = new Model();
        SetVar[] setVars = new SetVar[3];
        setVars[0] = model.setVar(new int[]{}, new int[]{1, 2, 4, 5});
        setVars[1] = model.setVar(new int[]{}, new int[]{3, 5});
        setVars[2] = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5});
        SetVar intersect = model.setVar(new int[]{1, 2, 3, 4, 5});
        model.intersection(setVars, intersect).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    private final Random rand = new Random();

    private TIntSet randIntSet() {
        int size = rand.nextInt(6);
        TIntSet set = new TIntHashSet(size);
        for (int i = 0; i < size; i++) {
            set.add(rand.nextInt(5));
        }
        return set;
    }

    private SetVar randSet(String name, Model model) {
        TIntSet lb = randIntSet();
        TIntSet ub = randIntSet();
        ub.addAll(lb);
        return model.setVar(name, lb.toArray(), ub.toArray());
    }

    private SetStrategy randomSearch(SetVar... vars) {
        return Search.setVarSearch(
                new org.chocosolver.solver.search.strategy.selectors.variables.Random<>(rand.nextLong()),
                new SetDomainMin(), rand.nextBoolean(), vars);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testFuzzIdempotent() {
        for (int repeat = 0; repeat < 100; repeat++) {
            Model model = new Model();
            SetVar[] sets = new SetVar[rand.nextInt(5) + 1];
            for (int i = 0; i < sets.length; i++) {
                sets[i] = randSet("set#" + i, model);
            }
            SetVar intersection = randSet("interection", model);
            model.intersection(sets, intersection).post();
            model.getSolver().setSearch(randomSearch(ArrayUtils.append(sets, new SetVar[]{intersection})));
            checkSolutions(model, sets, intersection);
        }
    }

    @Test(groups = "1s", timeOut=60000)
    public void testFuzzIdempotentAndBoundConsistent() {
        for (int repeat = 0; repeat < 100; repeat++) {
            Model model = new Model();
            SetVar[] sets = new SetVar[rand.nextInt(5) + 1];
            for (int i = 0; i < sets.length; i++) {
                sets[i] = randSet("set#" + i, model);
            }
            SetVar intersection = randSet("intersection", model);
            model.intersection(sets, intersection, true).post();
            model.getSolver().plugMonitor(new BoundConsistent(model.getSolver()));
            model.getSolver().setSearch(randomSearch(ArrayUtils.append(sets, new SetVar[]{intersection})));
            checkSolutions(model, sets, intersection);
        }
    }

    private int checkSolutions(Model model, SetVar[] setVars, SetVar intersect) {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            ISet computed = SetFactory.makeBipartiteSet(0);
            for (SetVar setVar : setVars) {
                for (Integer value : setVar.getValue()) {
                    computed.add(value);
                }
            }
            for (Integer inIntersect : intersect.getValue()) {
                assertTrue(computed.contains(inIntersect));
            }
        }
        return nbSol;
    }

    // Throws an error when bound consistency is violated.
    public class BoundConsistent implements IMonitorDownBranch, IMonitorContradiction {

        private final Solver solver;
        private final StringBuilder lastDecision = new StringBuilder();

        public BoundConsistent(Solver solver) {
            this.solver = solver;
        }

        @Override
        public void beforeDownBranch(boolean left) {
            Variable[] vars = solver.getModel().getVars();
            // Clear the string.
            lastDecision.setLength(0);
            lastDecision.append(left ? "Left" : "Right").append(" branch: ").append(solver.getDecisionPath().getLastDecision()).append('\n');
            lastDecision.append("Variables: ");
            for (Variable var : vars) {
                lastDecision.append(var).append(' ');
            }
        }

        @Override
        public void onContradiction(ContradictionException cex) {
            if (lastDecision.length() > 0) {
                // Encountered a contradiction after a decision, hence not
                // bound consistent since that decision should have been pruned.
                throw new Error("Not bound consistent: " + "\n" + lastDecision + "\n" + cex, cex);
            }
        }
    }
}
