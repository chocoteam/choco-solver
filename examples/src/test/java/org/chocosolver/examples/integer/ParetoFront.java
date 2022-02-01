/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * @author Jean-Guillaume Fages
 * @since 21/03/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Trivial multi-objective optimization computing pareto solutions
 *
 * @author Jimmy Liang, Jean-Guillaume Fages
 */
public class ParetoFront {

	@Test(groups = "1s", timeOut = 60000)
	public void testPareto(){
		// simple model
		Model model = new Model();
		IntVar a = model.intVar("a", 0, 2, false);
		IntVar b = model.intVar("b", 0, 2, false);
		IntVar c = model.intVar("c", 0, 2, false);
		model.arithm(a, "+", b, "=", c).post();

		// retrieve the pareto front
		List<Solution> paretoFront = model.getSolver().findParetoFront(new IntVar[]{a,b},true );
		System.out.println("The pareto front has "+paretoFront.size()+" solutions : ");
		Assert.assertEquals(3, paretoFront.size());
		for(Solution s:paretoFront){
			System.out.println("a = "+s.getIntVal(a)+" and b = "+s.getIntVal(b));
			Assert.assertEquals(2, s.getIntVal(c));
		}
	}
}
