/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 01/02/2016
 * Time: 14:28
 */
public class ESatTest {

	@Test(groups="1s", timeOut=1000)
	public void testESat(){
		Assert.assertNotEquals(ESat.TRUE,ESat.FALSE);
		Assert.assertNotEquals(ESat.UNDEFINED,ESat.FALSE);
		Assert.assertNotEquals(ESat.UNDEFINED,ESat.TRUE);

		Assert.assertEquals(ESat.not(ESat.TRUE),ESat.FALSE);
		Assert.assertEquals(ESat.not(ESat.FALSE),ESat.TRUE);
		Assert.assertEquals(ESat.not(ESat.UNDEFINED),ESat.UNDEFINED);

		Assert.assertEquals(ESat.eval(false),ESat.FALSE);
		Assert.assertEquals(ESat.eval(true),ESat.TRUE);
	}
}
