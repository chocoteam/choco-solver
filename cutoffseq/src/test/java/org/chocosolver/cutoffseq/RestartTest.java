/**
 * This file is part of cutoffseq, https://github.com/chocoteam/cutoffseq
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.cutoffseq;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/05/11
 */
public class RestartTest {

	private final static long[] LUBY_2 = {1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1, 2, 4, 8, 1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1, 2, 4, 8, 16};

	private final static long[] GEOMETRIC_1_1_3 = {1, 2, 2, 3, 3, 4, 5, 7, 9, 11, 14, 18, 24, 31, 40};

	private final static long[] GEOMETRIC_4_1_3 = {4, 6, 7, 9, 12, 15, 20, 26, 33, 43, 56, 72, 94, 122, 158, 205, 267, 347, 450, 585, 761, 989, 1285, 1671, 2172, 2823, 3670, 4771, 6202, 8062, 10480};

	private final static long[] GEOMETRIC_2_2 = {
		1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072,
		262144, 524288,
		1048576, 2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728, 268435456,
		536870912, 1073741824,
		2147483648L, 4294967296L, 8589934592L, 17179869184L, 34359738368L, 68719476736L,
		137438953472L,
		274877906944L, 549755813888L,
		1099511627776L, 2199023255552L, 4398046511104L, 8796093022208L, 17592186044416L,
		35184372088832L,
		70368744177664L,
		140737488355328L,
		281474976710656L, 562949953421312L, 1125899906842624L
	};
	
	@Test(timeOut=60000)
	public void testLubyRestarts() {
		testCutoffs(new LubyCutoffStrategy(1), LUBY_2, 1);
		testCutoffs(new LubyCutoffStrategy(4), LUBY_2, 4);
		testCutoffs(new LubyCutoffStrategy(Integer.MAX_VALUE), LUBY_2, Integer.MAX_VALUE);
	}

	@Test(timeOut=60000)
	public void testGeomRestarts() {
		testCutoffs(new GeometricalCutoffStrategy(1, 1.3), GEOMETRIC_1_1_3, 1);
		testCutoffs(new GeometricalCutoffStrategy(4, 1.3), GEOMETRIC_4_1_3, 1);
		testCutoffs(new GeometricalCutoffStrategy(1, 2), GEOMETRIC_2_2, 1);
	}

	private static void testCutoffs(ICutoffStrategy strat, long[] expected, long scale) {
		for (long l : expected) {
			Assert.assertEquals(strat.getNextCutoff(), scale * l);
		}
	}
}
