/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * @author Dimitri Justeau-Allaire
 */
public class SortedSetIntsChannelTest {

    @Test(groups = "1s", timeOut=60000)
    public void testSimpleEnumeration() {
        Model model = new Model();
        IntVar[] ints = model.intVarArray(5, 0, 5);
        SetVar s = model.setVar(new int[] {}, new int[] {1, 2, 3, 4, 5});
        model.sortedSetIntsChanneling(s, ints, 0, 0).post();
        while (model.getSolver().solve()) {
            int[] values = s.getValue().toArray();
            Arrays.sort(values);
            for (int i = 0; i < ints.length; i++) {
                if (i < values.length) {
                    Assert.assertEquals(ints[i].getValue(), values[i]);
                } else {
                    Assert.assertEquals(ints[i].getValue(), 0);
                }
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), Math.pow(2, 5));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testOneValueCannotBelongToTheSet() {
        Model model = new Model();
        IntVar[] ints = model.intVarArray(5, 0, 4);
        SetVar s = model.setVar(new int[] {}, new int[] {1, 2, 3, 4, 5});
        model.sortedSetIntsChanneling(s, ints, 0, 0).post();
        while (model.getSolver().solve()) {
            int[] values = s.getValue().toArray();
            Arrays.sort(values);
            for (int i = 0; i < ints.length; i++) {
                if (i < values.length) {
                    Assert.assertEquals(ints[i].getValue(), values[i]);
                } else {
                    Assert.assertEquals(ints[i].getValue(), 0);
                }
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), Math.pow(2, 4));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNullValueNotInDomain() {
        Model model = new Model();
        IntVar[] ints = model.intVarArray(5, 1, 5);
        SetVar s = model.setVar(new int[] {}, new int[] {1, 2, 3, 4, 5});
        model.sortedSetIntsChanneling(s, ints, 0, 0).post();
        while (model.getSolver().solve()) {
            int[] values = s.getValue().toArray();
            Arrays.sort(values);
            for (int i = 0; i < ints.length; i++) {
                if (i < values.length) {
                    Assert.assertEquals(ints[i].getValue(), values[i]);
                } else {
                    Assert.assertEquals(ints[i].getValue(), 0);
                }
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testLessIntsThanMaxSetSize() {
        Model model = new Model();
        IntVar[] ints = model.intVarArray(5, 0, 8);
        SetVar s = model.setVar(new int[] {}, new int[] {1, 2, 3, 4, 5, 6, 7, 8});
        model.sortedSetIntsChanneling(s, ints, 0, 0).post();
        while (model.getSolver().solve()) {
            int[] values = s.getValue().toArray();
            Arrays.sort(values);
            for (int i = 0; i < ints.length; i++) {
                if (i < values.length) {
                    Assert.assertEquals(ints[i].getValue(), values[i]);
                } else {
                    Assert.assertEquals(ints[i].getValue(), 0);
                }
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), Math.pow(2, 8));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testMoreIntsThanMaxSetSize() {
        Model model = new Model();
        IntVar[] ints = model.intVarArray(10, 0, 5);
        SetVar s = model.setVar(new int[] {}, new int[] {1, 2, 3, 4, 5});
        model.sortedSetIntsChanneling(s, ints, 0, 0).post();
        while (model.getSolver().solve()) {
            int[] values = s.getValue().toArray();
            Arrays.sort(values);
            for (int i = 0; i < ints.length; i++) {
                if (i < values.length) {
                    Assert.assertEquals(ints[i].getValue(), values[i]);
                } else {
                    Assert.assertEquals(ints[i].getValue(), 0);
                }
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), Math.pow(2, 5));
    }
}
