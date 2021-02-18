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
 * @since 19/09/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.tree.PropAntiArborescences;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.testng.Assert.assertEquals;

public class TreeTest {

	@Test(groups="10s", timeOut=60000)
	public void test1() {
		Model s1 = model(true);
		Model s2 = model(false);
		while (s1.getSolver().solve()) ;
		while (s2.getSolver().solve()) ;
		assertEquals(s1.getSolver().getSolutionCount(), s2.getSolver().getSolutionCount());
		assertEquals(s1.getSolver().getNodeCount(), s2.getSolver().getNodeCount());
	}

	private Model model(boolean defaultCstr) {
		Model model = new Model();
		IntVar[] VS = model.intVarArray("VS", 6, -1, 6, false);
		IntVar NT = model.intVar("NT", 2, 3, false);
		if (defaultCstr) {
			model.tree(VS, NT, 0).post();
		} else {
			new Constraint("tree",
					new PropAntiArborescences(VS, 0, false),
					new PropKLoops(VS, 0, NT)
			).post();
		}
		model.getSolver().setSearch(randomSearch(VS, 0));
		return model;
	}
}
