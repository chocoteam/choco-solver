/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.binPacking.PropBinPacking;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Jean-Guillaume Fages
 * @author Arthur Godet
 * @since 18/09/18
 */
public class BinPackingTest {

	@Test(groups="1s", timeOut=60000)
	public void testFixedLoadBackPropag() {
		Model model = new Model();
		int[] itemSize = new int[]{2,3,1};
		IntVar[] items = model.intVarArray("items",3,-1,1);
		IntVar[] loads = model.intVarArray("loads",2,3,3);
		int offset = -1;
		model.post(new Constraint("BinPacking", new PropBinPacking(items, loads, itemSize, offset)));
		model.getSolver().showSolutions();
		model.getSolver().setSearch(Search.inputOrderLBSearch(items));
		boolean sol = model.getSolver().solve();
		assertTrue(sol);
		assertEquals(0, model.getSolver().getFailCount());
		assertEquals(2, model.getSolver().getNodeCount());
	}

	@Test(groups="1s", timeOut=60000)
	public void test2() {
		Model model = new Model();
		int[] itemSize = new int[]{78, 69, 52, 30, 28, 23, 19, 17, 15, 3};
		int n = itemSize.length;
		int binSize = 100;
		int offset = 0;
		IntVar[] items = model.intVarArray("items", n, offset, n-(1-offset));
        IntVar[] loads = model.intVarArray("loads", n, 0, binSize);
        IntVar z = model.intVar("z", 1, n);
        model.nValues(items, z).post();
        model.setObjective(false, z);
        model.post(new Constraint("BinPacking", new PropBinPacking(items, loads, itemSize, offset)));
        Solution s = model.getSolver().findOptimalSolution(z, false, null);
    	assertEquals(4, s.getIntVal(z));
	}
	
	@Test(groups="1s", timeOut=60000)
	public void test3() {
		Model model = new Model();
		int[] itemSize = new int[]{4193, 3632, 1334, 1026, 901, 750, 437, 153, 99, 6};
		int n = itemSize.length;
		int binSize = 4193;
		int offset = 0;
		IntVar[] items = model.intVarArray("items", n, offset, n-(1-offset));
        IntVar[] loads = model.intVarArray("loads", n, 0, binSize);
        IntVar z = model.intVar("z", 1, n);
        model.nValues(items, z).post();
        model.setObjective(false, z);
        model.post(new Constraint("BinPacking", new PropBinPacking(items, loads, itemSize, offset)));
        model.getSolver().findAllOptimalSolutions(z, false, null);
    	assertEquals(1440, model.getSolver().getSolutionCount());
	}

	@Test(groups="1s", timeOut=60000)
	public void testNoSum() {
		ArrayList<Integer> set = new ArrayList<>();
		for(int i = 0; i<9; i++) {
			set.add(i);
		}
		int[] weights = new int[] {10, 10, 10, 9, 9, 9, 9, 2, 1};
		int alpha = 34, beta = 35;
		int[] ns = PropBinPacking.noSum(set, weights, alpha, beta);
		assertEquals(2, ns.length);
		assertEquals(33, ns[0]);
		assertEquals(36, ns[1]);
		// we could also check that k==3 and k2==2
	}
}
