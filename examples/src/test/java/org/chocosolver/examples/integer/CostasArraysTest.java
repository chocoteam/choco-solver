/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.parser.SetUpException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Costas Arrays
 * "Given n in N, find an array s = [s_1, ..., s_n], such that
 * <ul>
 * <li>s is a permutation of Z_n = {0,1,...,n-1};</li>
 * <li>the vectors v(i,j) = (j-i)x + (s_j-s_i)y are all different </li>
 * </ul>
 * <br/>
 * An array v satisfying these conditions is called a Costas array of size n;
 * the problem of finding such an array is the Costas Array problem of size n."
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 25/01/11
 */
public class CostasArraysTest {


	@Test(groups = "10s", timeOut = 60000)
	public void test(){
		CostasArrays ca = new CostasArrays();
		ca.execute();
		Assert.assertEquals(1,ca.getModel().getSolver().getSolutionCount());
		Assert.assertEquals(6295, ca.getModel().getSolver().getNodeCount());
	}

	@Test(groups = "10s", timeOut = 60000)
	public void tests(){
		for(int i=5;i<14;i++) {
			CostasArrays ca = new CostasArrays();
			ca.execute("-o", i + "");
			Assert.assertEquals(1,ca.getModel().getSolver().getSolutionCount());
		}
	}

	@Test(groups = "10s", timeOut = 60000)
	public void testSols() throws SetUpException {
		int[] size = new int[]{5,6,7,8};
		int[] nbSols = new int[]{20,58,100,222};
		for(int i=0;i<size.length;i++) {
			CostasArrays ca = new CostasArrays();
			ca.setUp("-o", size[i] + "");
			ca.buildModel();
			while (ca.getModel().getSolver().solve()) ;
			Assert.assertEquals(nbSols[i], ca.getModel().getSolver().getSolutionCount());
		}
	}
}
