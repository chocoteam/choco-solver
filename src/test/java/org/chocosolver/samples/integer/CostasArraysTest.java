/**
 * Copyright (c) 2016, chocoteam
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of samples nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples.integer;

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
		Assert.assertEquals(3914, ca.getModel().getSolver().getNodeCount());
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
	public void testSols(){
		int[] size = new int[]{5,6,7,8};
		int[] nbSols = new int[]{20,58,100,222};
		for(int i=0;i<size.length;i++) {
			CostasArrays ca = new CostasArrays();
			ca.readArgs("-o", size[i] + "");
			ca.buildModel();
			while (ca.getModel().getSolver().solve()) ;
			Assert.assertEquals(nbSols[i], ca.getModel().getSolver().getSolutionCount());
		}
	}
}
