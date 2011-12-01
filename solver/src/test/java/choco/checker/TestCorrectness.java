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

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import solver.search.loop.SearchLoops;


/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/02/11
 */
public class TestCorrectness {
    private int slType; // search loop type default value

    public TestCorrectness() {
        this.slType = 0;
    }

    public TestCorrectness(int slType) {
        this.slType = slType;
    }

    @BeforeTest(alwaysRun = true)
    private void beforeTest() {
        SearchLoops._DEFAULT = slType;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Test(groups = "1m")
    public void testTIMES() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 8) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelTimes, 3, -n / 2, 2 * n, seed, null);
        }
    }

    @Test(groups = "1m")
    public void testABSOLUTE() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 8) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelAbsolute, 2, -n / 2, 2 * n, seed, null);
        }
    }

    @Test(groups = "1m")
    public void testALLDIFFERENTBC() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 8) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelAllDiffBC, n, -n / 2, 2 * n, seed, null);
        }
    }

    @Test(groups = "1m")
    public void testEQ() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 8) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelEqAC, 2, -n / 2, 2 * n, seed, null);
        }
    }

    @Test(groups = "1m")
    public void testNEQ() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 8) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelNeqAC, 2, -n / 2, 2 * n, seed, null);
        }
    }

    @Test(groups = "1m")
    public void testALLDIFFERENTAC() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 7) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelAllDiffAC, n, -n / 2, 2 * n, seed, null);
        }
    }

    /**
     * TODO: Ce test pose probl�me: la contrainte inverse_channeling calcule automatiquement les x_off et y_off!
     */
    @Test(groups = "1m")
    public void testINVERSECHANNELING() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 8) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelInverseChannelingAC, n, -n / 2, 2 * n, seed, null);
        }
    }

    @Test(groups = "1m")
    public void testCOUNTBCEQ() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 7) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelCountBC, n, -n / 2, 2 * n, seed, new int[]{0, 1});
        }
    }

    @Test(groups = "1m")
    public void testCOUNTACEQ() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 7) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelCountAC, n, -n / 2, 2 * n, seed, new int[]{0, 1});
        }
    }

    @Test(groups = "1m")
    public void testCOUNTBCLEQ() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 7) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelCountBC, n, -n / 2, 2 * n, seed, new int[]{2, 1});
        }
    }

    @Test(groups = "1m")
    public void testCOUNTACLEQ() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 7) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelCountAC, n, -n / 2, 2 * n, seed, new int[]{2, 1});
        }
    }

    @Test(groups = "1m")
    public void testCOUNTBCGEQ() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 7) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelCountBC, n, -n / 2, 2 * n, seed, new int[]{1, 1});
        }
    }

    @Test(groups = "1m")
    public void testCOUNTACGEQ() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 7) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelCountAC, n, -n / 2, 2 * n, seed, new int[]{1, 1});
        }
    }

    @Test(groups = "1m")
    public void testLEX1() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 7) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelLexAC, n, -n, 2*n, seed, true);
        }
    }

    @Test(groups = "1m")
    public void testLEX2() {
        long seed = System.currentTimeMillis();
        for (int n = 2; n < (1 << 7) + 1; n *= 2) {
            CorrectnessChecker.checkCorrectness(Modeler.modelLexAC, n, -n, 2*n, seed, false);
        }
    }
}
