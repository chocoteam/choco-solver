/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;

/**
 * Tests for path and subpath
 * @author Jean-Guillaume FAGES
 */
public class PathTest {

    @Test(groups="1s", timeOut=60000)
    public static void test1() {
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", 10, 0, 20, true);
        model.path(x,model.intVar(0),model.intVar(1)).post();
        model.getSolver().solve();
        assertEquals(1, model.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public static void test2() {
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", 10, 0, 10, false);
        model.path(x,model.intVar(0),model.intVar(1)).post();
        model.getSolver().solve();
        assertEquals(1, model.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public static void test2wrong() {
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", 10, 0, 9, false);
        model.path(x,model.intVar(0),model.intVar(1)).post();
        model.getSolver().solve();
        assertEquals(0, model.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public static void test3() {
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", 5, 0, 4, true);
        IntVar[] y = model.intVarArray("y", 5, 5, 9, true);
        IntVar[] vars = append(x, y);
        model.path(vars,model.intVar(0),model.intVar(1)).post();
        model.getSolver().solve();
        assertEquals(0, model.getSolver().getSolutionCount());
    }

	@Test(groups="1s", timeOut=60000)
	public static void test4() {
		Model model = new Model();
		IntVar[] x = new IntVar[]{
				model.intVar(4),
				model.intVar(2),
				model.intVar(0),
				model.intVar(1),
		};
		IntVar start = model.intVar(-3,10);
		IntVar end = model.intVar(-3,10);
		model.path(x,start,end).post();
		model.getSolver().solve();
		assertEquals(1, model.getSolver().getSolutionCount());
		assertEquals(3, start.getValue());
		assertEquals(0, end.getValue());
	}

	@Test(groups="1s", timeOut=60000)
	public static void test5() {
		Model model = new Model();
		IntVar[] x = new IntVar[]{
				model.intVar(1),
				model.intVar(2),
				model.intVar(3),
				model.intVar(4),
		};
		model.path(x,model.intVar(0),model.intVar(2)).post();
		model.getSolver().solve();
		assertEquals(0, model.getSolver().getSolutionCount());
	}

	@Test(groups="1s", timeOut=60000)
	public static void test4sub() {
		Model model = new Model();
		IntVar[] x = new IntVar[]{
				model.intVar(4),
				model.intVar(1),
				model.intVar(0),
				model.intVar(2),
		};
		IntVar start = model.intVar(-3,10);
		IntVar end = model.intVar(-3,10);
		IntVar size = model.intVar(-3,10);
		model.subPath(x,start,end,0,size).post();
		model.getSolver().solve();
		assertEquals(1, model.getSolver().getSolutionCount());
		assertEquals(3, size.getValue());
		assertEquals(3, start.getValue());
		assertEquals(0, end.getValue());
	}

	@Test(groups="1s", timeOut=60000)
	public static void test4sub2() {
		Model model = new Model();
		IntVar[] x = new IntVar[]{
				model.intVar(4),
				model.intVar(-3,6),
				model.intVar(0),
				model.intVar(2),
		};
		IntVar start = model.intVar(-3,10);
		IntVar end = model.intVar(-3,10);
		IntVar size = model.intVar(3);
		model.subPath(x,start,end,0,size).post();
		model.getSolver().solve();
		assertEquals(1, model.getSolver().getSolutionCount());
		assertEquals(3, size.getValue());
		assertEquals(3, start.getValue());
		assertEquals(1,x[1].getValue());
		assertEquals(0, end.getValue());
	}
}
