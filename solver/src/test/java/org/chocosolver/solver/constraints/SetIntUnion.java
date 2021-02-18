/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * @author Jean-Guillaume Fages
 * @since 08/10/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.annotations.Test;

import static java.lang.System.out;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.testng.Assert.assertEquals;

public class SetIntUnion {

	@Test(groups="1s", timeOut=60000)
	public void test1() {
        Model s = new Model();
        IntVar[] x = s.intVarArray("ints", 4, 0, 5, false);
        SetVar values = s.setVar("values", new int[]{0, 1, 4});
        s.union(x, values).post();


        s.getSolver().setSearch(inputOrderLBSearch(x));
        while (s.getSolver().solve()) ;
    }

	@Test(groups="1s", timeOut=60000)
	public void test2() {
        Model s = new Model();
        IntVar[] x = new IntVar[]{
                s.intVar(0)
                , s.intVar(2)
                , s.intVar(5)
                , s.intVar(0)
                , s.intVar(2)
        };
        SetVar values = s.setVar("values", new int[]{0, 1, 4});
        s.union(x, values).post();


        s.getSolver().setSearch(inputOrderLBSearch(x));
        while (s.getSolver().solve()) ;
        assertEquals(s.getSolver().getSolutionCount(), 0);
    }

	@Test(groups="1s", timeOut=60000)
	public void test3() {
        Model s = new Model();
        IntVar[] x = new IntVar[]{
                s.intVar(0)
                , s.intVar(2)
                , s.intVar(5)
                , s.intVar(0)
                , s.intVar(2)
        };
        SetVar values = s.setVar("values", new int[]{}, new int[]{-1, 0, 1, 2, 3, 4, 5, 6});
        s.union(x, values).post();


        s.getSolver().setSearch(inputOrderLBSearch(x));
        while (s.getSolver().solve()) ;
        out.println(values);
        assertEquals(s.getSolver().getSolutionCount(), 1);
    }
}
