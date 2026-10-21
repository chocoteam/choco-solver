/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Tests of the custom constraints ({@code Model.custom}) built with parameters.
 *
 * @author Charles Prud'homme
 */
public class CustomConstraintTest {

    /**
     * A custom propagator with parameters: each variable is at most its bound.
     */
    static final class PropUpperBounds extends Propagator<IntVar> {
        final int[] bounds;

        PropUpperBounds(IntVar[] vars, int[] bounds) {
            super(vars);
            this.bounds = bounds;
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            for (int i = 0; i < vars.length; i++) {
                vars[i].updateUpperBound(bounds[i], this);
            }
        }

        @Override
        public ESat isEntailed() {
            boolean all = true;
            for (int i = 0; i < vars.length; i++) {
                if (vars[i].getLB() > bounds[i]) {
                    return ESat.FALSE;
                }
                all &= vars[i].getUB() <= bounds[i];
            }
            return all ? ESat.TRUE : ESat.UNDEFINED;
        }
    }

    /**
     * Several parameters, in a record.
     */
    record Bounds(int[] ub, int shift) {
    }

    /**
     * A record which holds a variable: not supported, variables must be passed in {@code vars}.
     */
    record WithVar(IntVar var) {
    }

    private static PropUpperBounds of(IntVar[] vs, Bounds b) {
        return new PropUpperBounds(vs, Arrays.stream(b.ub()).map(u -> u + b.shift()).toArray());
    }

    private static Model build(Model m) {
        IntVar[] x = m.intVarArray("x", 3, 0, 9);
        m.custom("int", x, 7, (vs, k) -> new PropUpperBounds(vs, new int[]{k, k, k})).post();
        m.custom("array", x, new int[]{6, 5, 8}, PropUpperBounds::new).post();
        m.custom("record", x, new Bounds(new int[]{3, 7, 7}, 1), CustomConstraintTest::of).post();
        // data holding variables: mapped to those of each copy
        m.custom("vars", new IntVar[]{x[0]}, new IntVar[]{x[1], x[2]},
                (vs, ws) -> new PropUpperBounds(new IntVar[]{vs[0], ws[0], ws[1]}, new int[]{9, 9, 9})).post();
        m.sum(x, "=", 14).post();
        return m;
    }

    private static int[] bounds(Model m, String name) {
        Constraint c = Arrays.stream(m.getCstrs()).filter(k -> k.getName().equals(name)).findFirst().orElseThrow();
        return ((PropUpperBounds) c.getPropagator(0)).bounds;
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testParametersAreReplayed() {
        String expected = SpecTestUtils.structure(build(new Model("m")));
        String stats = SpecTestUtils.solve(build(new Model("m")), 100_000);
        Model original = build(Model.record("m"));
        ModelSpec spec = original.getRecorder().snapshot();
        Model a = spec.instantiate().model();
        Model b = spec.instantiate().model();
        for (Model m : new Model[]{a, b}) {
            Assert.assertEquals(SpecTestUtils.structure(m), expected);
            Assert.assertEquals(bounds(m, "int"), new int[]{7, 7, 7});
            Assert.assertEquals(bounds(m, "array"), new int[]{6, 5, 8});
            Assert.assertEquals(bounds(m, "record"), new int[]{4, 8, 8});
        }
        // mutable data are not shared
        Assert.assertNotSame(bounds(a, "array"), bounds(original, "array"));
        Assert.assertNotSame(bounds(a, "array"), bounds(b, "array"));
        Assert.assertEquals(SpecTestUtils.solve(a, 100_000), stats);
        Assert.assertEquals(SpecTestUtils.solve(original, 100_000), stats);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRecordOfVariablesIsReported() {
        Model m = Model.record("m");
        IntVar[] x = m.intVarArray("x", 2, 0, 3);
        m.custom("withVar", x, new WithVar(x[0]), (vs, w) -> new PropUpperBounds(vs, new int[]{3, 3})).post();
        Assert.assertThrows(SpecException.class, m::duplicate);
    }
}
