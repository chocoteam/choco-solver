/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco.checker;

import org.testng.annotations.Test;
import solver.search.loop.SearchLoops;


/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/02/11
 */
public class TestCorrectness {
	private SearchLoops slType; // search loop type default value

	public TestCorrectness() {
		this.slType = SearchLoops.BINARY;
	}

	public TestCorrectness(SearchLoops sl) {
		this.slType = slType;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test(groups = "1m")
	public void testTIMES() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 8) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelTimes, 3, -n / 2, 2 * n, seed, null);
			}
			System.gc();
		}
	}

	@Test(groups = "1m")
	public void testABSOLUTE() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelAbsolute, 2, -n / 2, 2 * n, seed, null);
			}
			System.gc();
		}
	}

	@Test(groups = "1m")
	public void testEQ() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelEqAC, 2, -n / 2, 2 * n, seed, null);
			}
			System.gc();
		}
	}

	@Test(groups = "1m")
	public void testNEQ() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 8) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelNeqAC, 2, -n / 2, 2 * n, seed, null);
			}
			System.gc();
		}
	}

	// ALLDIFFERENT
	@Test(groups = "1m")
	public void testALLDIFFERENTAC() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelAllDiffAC, n, -n / 2, 2 * n, seed, null);
			}
			System.gc();
		}
	}
	@Test(groups = "1m")
	public void testALLDIFFERENTBC() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelAllDiffBC, n, -n / 2, 2 * n, seed, null);
			}
			System.gc();
		}
	}

	// INVERSE
	@Test(groups = "1m")
	public void testINVERSECHANNELING_AC() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelInverseChannelingAC, n, -n / 2, 2 * n, seed, null);
			}
			System.gc();
		}
	}
	@Test(groups = "1m")
	public void testINVERSECHANNELING_Bounds() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelInverseChannelingBounds, n, -n / 2, 2 * n, seed, null);
			}
			System.gc();
		}
	}

	// COUNT
	@Test(groups = "1m")
	public void testCOUNTBCEQ() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelCountBC, n, -n / 2, 2 * n, seed, new int[]{0, 1});
			}
			System.gc();
		}
	}
	@Test(groups = "1m")
	public void testCOUNTACEQ() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelCountAC, n, -n / 2, 2 * n, seed, new int[]{0, 1});
			}
			System.gc();
		}
	}
	@Test(groups = "1m")
	public void testCOUNTBCLEQ() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelCountBC, n, -n / 2, 2 * n, seed, new int[]{2, 1});
			}
			System.gc();
		}
	}
	@Test(groups = "1m")
	public void testCOUNTACLEQ() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelCountAC, n, -n / 2, 2 * n, seed, new int[]{2, 1});
			}
			System.gc();
		}
	}

	@Test(groups = "1m")
	public void testCOUNTBCGEQ() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelCountBC, n, -n / 2, 2 * n, seed, new int[]{1, 1});
			}
			System.gc();
		}
	}
	@Test(groups = "1m")
	public void testCOUNTACGEQ() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelCountAC, n, -n / 2, 2 * n, seed, new int[]{1, 1});
			}
			System.gc();
		}
	}

	// LEX
	@Test(groups = "1m")
	public void testLEX1() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelLexAC, n, -n, 2*n, seed, true);
			}
			System.gc();
		}
	}
	@Test(groups = "1m")
	public void testLEX2() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelLexAC, n, -n, 2*n, seed, false);
			}
			System.gc();
		}
	}

	// ELEMENT
	@Test(groups = "1m")
	public void testELEMENTBC() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelNthBC, 2, -n / 2, 2 * n, seed, null);
			}
			System.gc();
		}
	}

	// AMONG
	@Test(groups = "1m")
	public void testAMONGAC() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelAmongAC, n, -n / 2, 2 * n, seed, new int[]{0, 1});
			}
			System.gc();
		}
	}
	@Test(groups = "1m")
	public void testAMONGBC() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < (1 << 6) + 1; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelAmongAC, n, -n / 2, 2 * n, seed, new int[]{2, 1});
			}
			System.gc();
		}
	}

	// NValue
	@Test(groups = "1m")
	public void testNVALUES() {
		for(int i=0;i<20;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < 33; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelNValues, n, -n / 2, 2 * n, seed, new int[]{2, 1});
			}
			System.gc();
		}
	}
	@Test(groups = "1m")
	public void testNVALUES_AtMostBC() {
		for(int i=0;i<20;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < 33; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelNValues_AtMostBC, n, -n / 2, 2 * n, seed, new int[]{2, 1});
			}
			System.gc();
		}
	}
	@Test(groups = "1m")
	public void testNVALUES_AtLeastAC() {
		for(int i=0;i<10;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < 33; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelNValues_AtLeastAC, n, -n / 2, 2 * n, seed, new int[]{2, 1});
			}
			System.gc();
		}
	}
	@Test(groups = "1m")
	public void testNVALUES_AtMostGreedy() {
		for(int i=0;i<20;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < 33; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelNValues_AtMostGreedy, n, -n / 2, 2 * n, seed, new int[]{2, 1});
			}
			System.gc();
		}
	}
	@Test(groups = "1m")
	public void testNVALUES_simple() {
		for(int i=0;i<20;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < 33; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelNValues_simple, n, -n / 2, 2 * n, seed, new int[]{2, 1});
			}
			System.gc();
		}
	}

	// GCC
	@Test(groups = "1m")
	public void testGCC_AD_CARDS() {
		for(int i=0;i<20;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < 33; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelGCC_alldiff_Cards, n, -n / 2, 2 * n, seed, new int[]{2, 1});
			}
			System.gc();
		}
	}
	@Test(groups = "1m")
	public void testGCC_AD_FAST() {
		for(int i=0;i<20;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < 33; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelGCC_alldiff_Fast, n, -n / 2, 2 * n, seed, new int[]{2, 1});
			}
			System.gc();
		}
	}
	@Test(groups = "1m")
	public void testGCC_AD_LOWUP() {
		for(int i=0;i<20;i++){
			long seed = System.currentTimeMillis();
			for (int n = 2; n < 33; n *= 2) {
				CorrectnessChecker.checkCorrectness(Modeler.modelGCC_alldiff_LowUp, n, -n / 2, 2 * n, seed, new int[]{2, 1});
			}
			System.gc();
		}
	}
}
