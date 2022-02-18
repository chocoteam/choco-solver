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
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 18:25
 */

package org.chocosolver.examples.set;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Small problem to illustrate how to use set variables
 * finds a partition a universe so that the sum of elements in universe
 * (restricted to the arbitrary interval [12,19]) is minimal
 *
 * @author Jean-Guillaume Fages
 */
public class SetPartitionTest {

	@Test(groups = "10s", timeOut = 60000)
	public void test(){
		SetPartition sp = new SetPartition();
		sp.buildModel();
		sp.solve();
		Assert.assertEquals(2,sp.getModel().getSolver().getSolutionCount());
		Assert.assertEquals(13,sp.getModel().getSolver().getObjectiveManager().getBestSolutionValue().intValue());
	}
}
