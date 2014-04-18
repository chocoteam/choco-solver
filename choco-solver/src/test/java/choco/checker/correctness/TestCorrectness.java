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

package choco.checker.correctness;

import choco.checker.Modeler;
import org.testng.annotations.Test;
import solver.search.loop.SearchLoops;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
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
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 8) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelTimes, 3, -n / 2, 2 * n, seed, null);
            }

        }
    }

    @Test(groups = "1m")
    public void testABSOLUTE() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelAbsolute, 2, -n / 2, 2 * n, seed, null);
            }

        }
    }

    @Test(groups = "1m")
    public void testEQ() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelEqAC, 2, -n / 2, 2 * n, seed, null);
            }

        }
    }

    @Test(groups = "1m")
    public void testNEQ() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 8) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelNeqAC, 2, -n / 2, 2 * n, seed, null);
            }

        }
    }

    // ALLDIFFERENT
    @Test(groups = "1m")
    public void testALLDIFFERENTAC() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelAllDiffAC, n, -n / 2, 2 * n, seed, null);
            }

        }
    }

    @Test(groups = "1m")
    public void testALLDIFFERENTBC() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelAllDiffBC, n, -n / 2, 2 * n, seed, null);
            }

        }
    }

    @Test(groups = "1m")
    public void testGCC() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelGCC, n, 0, n, seed, true);
            }
        }
    }

    @Test(groups = "1m")
    public void testGCC2() {
        for (int n = 2; n < 33; n *= 2) {
            for (int i = 0; i < 20; i++) {
                long seed = System.currentTimeMillis();
                CorrectnessChecker.checkCorrectness(Modeler.modelGCC_alldiff, n, -n / 2, 2 * n, seed, false);
            }
        }
    }

    // INVERSE
    @Test(groups = "1m")
    public void testINVERSECHANNELING_AC() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 4) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelInverseChannelingAC, n, -n / 2, 2 * n, seed, null);
            }

        }
    }

    @Test(groups = "1m")
    public void testINVERSECHANNELING_Bounds() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 4) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelInverseChannelingBounds, n, -n / 2, 2 * n, seed, null);
            }

        }
    }

    // COUNT
    @Test(groups = "1m")
    public void testCOUNTBCEQ() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelCountBC, n, -n / 2, 2 * n, seed, new int[]{0, 1});
            }

        }
    }

    @Test(groups = "1m")
    public void testCOUNTACEQ() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelCountAC, n, -n / 2, 2 * n, seed, new int[]{0, 1});
            }

        }
    }

    @Test(groups = "1m")
    public void testCOUNTBCLEQ() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelCountBC, n, -n / 2, 2 * n, seed, new int[]{2, 1});
            }

        }
    }

    @Test(groups = "1m")
    public void testCOUNTACLEQ() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelCountAC, n, -n / 2, 2 * n, seed, new int[]{2, 1});
            }

        }
    }

    @Test(groups = "1m")
    public void testCOUNTBCGEQ() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelCountBC, n, -n / 2, 2 * n, seed, new int[]{1, 1});
            }

        }
    }

    @Test(groups = "1m")
    public void testCOUNTACGEQ() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelCountAC, n, -n / 2, 2 * n, seed, new int[]{1, 1});
            }

        }
    }

    // LEX
    @Test(groups = "1m")
    public void testLEX1() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelLexAC, n, -n, 2 * n, seed, true);
            }

        }
    }

    @Test(groups = "1m")
    public void testLEX2() {
        for (int i = 0; i < 50; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelLexAC, n, -n, 2 * n, seed, false);
            }

        }
    }

    // LEX CHAIN
    @Test(groups = "1m")
    public void testLEXCH1() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 3; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelLexChainAC, n, -n, 2 * n, seed, true);
            }

        }
    }

    @Test(groups = "1m")
    public void testLEXCH2() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 3; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelLexChainAC, n, -n, 2 * n, seed, false);
            }

        }
    }

    // ELEMENT
    @Test(groups = "1m")
    public void testELEMENTBC() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelNthBC, 2, -n / 2, 2 * n, seed, null);
            }

        }
    }

    // AMONG
    @Test(groups = "1m")
    public void testAMONGAC() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelAmongAC, n, -n / 2, 2 * n, seed, new int[]{0, 1});
            }

        }
    }

    @Test(groups = "1m")
    public void testAMONGBC() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelAmongAC, n, -n / 2, 2 * n, seed, new int[]{2, 1});
            }

        }
    }

    @Test(groups = "5m")
    public void testNVALUES() {
        String[][] filters = new String[][]{
                {"at_most_BC"},
                {"at_least_AC"},
                {"at_most_greedy"},
                {"at_most_BC", "at_least_AC"},
                {"at_least_AC", "at_most_greedy"},
                {"at_most_BC", "at_most_greedy"},
                {"at_most_BC", "at_least_AC", "at_most_greedy"},
        };
        for (int i = 0; i < 20; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < 33; n *= 2) {
                for (String[] f : filters)
                    CorrectnessChecker.checkCorrectness(Modeler.modelNValues, n, -n / 2, 2 * n, seed, f);
            }

        }
    }

    @Test(groups = "1m")
    public void testTree() {
        for (int n = 2; n < 25; n += 5) {
            for (int i = 0; i < 25; i++) {
                long seed = System.currentTimeMillis();
                CorrectnessChecker.checkCorrectness(Modeler.modelTree, n, -n / 2, 2 * n, seed, true);
                CorrectnessChecker.checkCorrectness(Modeler.modelTree, n, -n / 2, 2 * n, seed, false);
            }
        }
    }

    @Test(groups = "1m")
    public void testCircuit() {
        for (int n = 2; n < 25; n += 5) {
            for (int i = 0; i < 50; i++) {
                long seed = System.currentTimeMillis();
                CorrectnessChecker.checkCorrectness(Modeler.modelCircuit, n, 0, n, seed, true);
            }
        }
    }

    @Test(groups = "1m")
    public void testPath() {
        for (int n = 3; n < 25; n += 5) {
            for (int i = 0; i < 50; i++) {
                long seed = System.currentTimeMillis();
                CorrectnessChecker.checkCorrectness(Modeler.modelPath, n, 0, n, seed, true);
            }
        }
    }

    @Test(groups = "1m")
    public void testSubcircuit() {
        for (int n = 2; n < 25; n += 5) {
            for (int i = 0; i < 50; i++) {
                long seed = System.currentTimeMillis();
                CorrectnessChecker.checkCorrectness(Modeler.modelSubcircuit, n, 0, n - 1, seed, true);
            }
        }
    }

    @Test(groups = "1m")
    public void testDiffn() {
        for (int n = 2; n < 25; n += 5) {
            for (int i = 0; i < 30; i++) {
                long seed = System.currentTimeMillis();
                CorrectnessChecker.checkCorrectness(Modeler.modelDiffn, 4 * n, 1, n * 2, seed, true);
            }
        }
    }

    @Test(groups = "1m")
    public void testCumulative() {
        int nBugSweep = 32;
        long seedBugSweep = 1368003588936l;
        CorrectnessChecker.checkCorrectness(Modeler.modelCumulative, 4 * nBugSweep + 1, 1, nBugSweep, seedBugSweep, true);
        for (int n = 2; n < 25; n += 5) {
            for (int i = 0; i < 30; i++) {
                long seed = System.currentTimeMillis();
                System.out.println("n = " + n);
                System.out.println("seed = " + seed);
                CorrectnessChecker.checkCorrectness(Modeler.modelCumulative, 4 * n + 1, 1, n, seed, true);
            }
        }
    }

    @Test(groups = "1m")
    public void testSORT1() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelSortBC, n, -n, 2 * n, seed, true);
            }

        }
    }

    @Test(groups = "1m")
    public void testSORT2() {
        for (int i = 0; i < 20; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelSortBC, n, -n, 2 * n, seed, false);
            }

        }
    }
}
