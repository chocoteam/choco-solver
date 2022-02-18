/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.checker.correctness;

import org.chocosolver.solver.constraints.checker.Modeler;
import org.chocosolver.solver.constraints.extension.TupleValidator;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 15/02/11
 */
public class TestCorrectness {

    // TIMES
    @Test(groups="checker", timeOut=60000)
    public void testTIMES() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 5) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelTimes, 3, -n / 2, 2 * n, seed, null);
            }
        }
    }

    // ABSOLUTE
    @Test(groups="checker", timeOut=60000)
    public void testABSOLUTE() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 5) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelAbsolute, 2, -n / 2, 2 * n, seed, null);
            }
        }
    }

    // ABSOLUTE
    @Test(groups = "checker", timeOut = 60000)
    public void testSQUARE() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 5) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelSquare, 2, -n / 2, 2 * n, seed, null);
            }
        }
    }

    // EQ
    @Test(groups="checker", timeOut=60000)
    public void testEQ() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 5) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelEqAC, 2, -n / 2, 2 * n, seed, null);
            }
        }
    }

    // NEQ
    @Test(groups="checker", timeOut=60000)
    public void testNEQ() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelNeqAC, 2, -n / 2, 2 * n, seed, null);
            }

        }
    }

    // ALLDIFFERENT
    @Test(groups="checker", timeOut=60000)
    public void testALLDIFFERENT() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 5) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelAllDiffAC, n, -n / 2, 2 * n, seed, null);
                CorrectnessChecker.checkCorrectness(Modeler.modelAllDiffBC, n, -n / 2, 2 * n, seed, null);
            }

        }
    }

    // GCC
    @Test(groups="checker", timeOut=60000)
    public void testGCC() {
        for (int i = 0; i < 5; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 5) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelGCC, n, 0, n, seed, true);
                CorrectnessChecker.checkCorrectness(Modeler.modelGCC_alldiff, n, -n / 2, 2 * n, seed, false);
            }
        }
    }

    // INVERSE
    @Test(groups="checker", timeOut=60000)
    public void testINVERSECHANNELING() {
        for (int i = 0; i < 3; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 4) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelInverseChannelingAC, n, -n / 2, 2 * n, seed, null);
                CorrectnessChecker.checkCorrectness(Modeler.modelInverseChannelingBounds, n, -n / 2, 2 * n, seed, null);
            }
        }
    }

    // COUNT
    @Test(groups="checker", timeOut=60000)
    public void testCOUNT() {
        for (int i = 0; i < 3; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 5) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelCountBC, n, -n / 2, 2 * n, seed, new int[]{0, 1});
                CorrectnessChecker.checkCorrectness(Modeler.modelCountBC, n, -n / 2, 2 * n, seed, new int[]{1, 1});
                CorrectnessChecker.checkCorrectness(Modeler.modelCountBC, n, -n / 2, 2 * n, seed, new int[]{2, 1});
                CorrectnessChecker.checkCorrectness(Modeler.modelCountAC, n, -n / 2, 2 * n, seed, new int[]{0, 1});
                CorrectnessChecker.checkCorrectness(Modeler.modelCountAC, n, -n / 2, 2 * n, seed, new int[]{1, 1});
                CorrectnessChecker.checkCorrectness(Modeler.modelCountAC, n, -n / 2, 2 * n, seed, new int[]{2, 1});
            }
        }
    }

    // LEX
    @Test(groups="checker", timeOut=60000)
    public void testLEX() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 5) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelLexAC, n, -n, 2 * n, seed, true);
                CorrectnessChecker.checkCorrectness(Modeler.modelLexAC, n, -n, 2 * n, seed, false);
            }
        }
    }

    // LEX CHAIN
    @Test(groups="checker", timeOut=60000)
    public void testLEXCH() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 3; n < (1 << 5) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelLexChainAC, n, -n, 2 * n, seed, true);
                CorrectnessChecker.checkCorrectness(Modeler.modelLexChainAC, n, -n, 2 * n, seed, false);
            }
        }
    }

    // ELEMENT
    @Test(groups="checker", timeOut=60000)
    public void testELEMENT() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 5) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelNthBC, 2, -n / 2, 2 * n, seed, null);
            }
        }
    }

    // AMONG
    @Test(groups="checker", timeOut=60000)
    public void testAMONG() {
        for (int i = 0; i < 5; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 5) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelAmongAC, n, -n / 2, 2 * n, seed, new int[]{0, 1});
                CorrectnessChecker.checkCorrectness(Modeler.modelAmongAC, n, -n / 2, 2 * n, seed, new int[]{2, 1});
            }
        }
    }

    // NVALUES
    @Test(groups="checker", timeOut=60000)
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
        for (int i = 0; i < 5; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < 13; n += 2) {
                for (String[] f : filters) {
                    CorrectnessChecker.checkCorrectness(Modeler.modelNValues, n, -n / 2, 2 * n, seed, f);
                }
            }
        }
    }

    // TREE
    @Test(groups="checker", timeOut=60000)
    public void testTree() {
        for (int n = 2; n < 25; n += 5) {
            long seed = System.currentTimeMillis();
            CorrectnessChecker.checkCorrectness(Modeler.modelTree, n, -n / 2, 2 * n, seed, true);
            CorrectnessChecker.checkCorrectness(Modeler.modelTree, n, -n / 2, 2 * n, seed, false);
        }
    }

    // CIRCUIT
    @Test(groups="checker", timeOut=60000)
    public void testCircuit() {
        for (int n = 2; n < 25; n += 5) {
            for (int i = 0; i < 10; i++) {
                long seed = System.currentTimeMillis();
                CorrectnessChecker.checkCorrectness(Modeler.modelCircuit, n, 0, n, seed, true);
            }
        }
    }

    // PATH
    @Test(groups="checker", timeOut=60000)
    public void testPath() {
        for (int n = 3; n < 25; n += 5) {
            for (int i = 0; i < 10; i++) {
                long seed = System.currentTimeMillis();
                CorrectnessChecker.checkCorrectness(Modeler.modelPath, n, 0, n, seed, true);
            }
        }
    }

    // SUBCIRCUIT
    @Test(groups="checker", timeOut=60000)
    public void testSubcircuit() {
        for (int n = 2; n < 25; n += 5) {
            for (int i = 0; i < 10; i++) {
                long seed = System.currentTimeMillis();
                CorrectnessChecker.checkCorrectness(Modeler.modelSubcircuit, n, 0, n - 1, seed, true);
            }
        }
    }

    // DIFFN
    @Test(groups="checker", timeOut=60000)
    public void testDiffn() {
        for (int n = 2; n < 25; n += 5) {
            for (int i = 0; i < 10; i++) {
                long seed = System.currentTimeMillis();
                CorrectnessChecker.checkCorrectness(Modeler.modelDiffn, 4 * n, 1, n * 2, seed, true);
            }
        }
    }

    // CUMULATIVE
    @Test(groups="checker", timeOut=60000)
    public void testCumulative() {
        int nBugSweep = 32;
        long seedBugSweep = 1368003588936l;
        CorrectnessChecker.checkCorrectness(Modeler.modelCumulative, 4 * nBugSweep + 1, 1, nBugSweep, seedBugSweep, false);
        for (int i = 0; i < 6; i++) {
            for (int n = 2; n < 25; n += 5) {
                long seed = System.currentTimeMillis();
                CorrectnessChecker.checkCorrectness(Modeler.modelCumulative, 4 * n + 1, 1, n, seed, false);
            }
        }
    }

    @Test(groups="checker", timeOut=60000)
    public void testIncrementalCumulative() {
        CorrectnessChecker.checkCorrectness(Modeler.modelCumulative, 4 * 32 + 1, 1, 32, 1368003588936l, true);
        CorrectnessChecker.checkCorrectness(Modeler.modelCumulative, 4 * 7 + 1, 1, 7, 29, true);
        for (int i = 0; i < 6; i++) {
            for (int n = 2; n < 25; n += 5) {
                long seed = i;//System.currentTimeMillis();
                CorrectnessChecker.checkCorrectness(Modeler.modelCumulative, 4 * n + 1, 1, n, seed, true);
            }
        }
    }

    // SORT
    @Test(groups="checker", timeOut=60000)
    public void testSORT() {
        for (int i = 0; i < 2; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 5) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelSortBC, n, -n, 2 * n, seed, true);
                CorrectnessChecker.checkCorrectness(Modeler.modelSortBC, n, -n, 2 * n, seed, false);
            }
        }
    }

    // MDD
    @Test(groups="checker", timeOut=300000)
    public void testMDD() {
        Random rnd = new Random();
        long seed = System.currentTimeMillis();
        rnd.setSeed(seed);
        for (int n = 2; n < 10; n++) {
            final int finalN = n;
            int[][] doms = new int[n][n];
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    doms[j][k] = k - n / 2;
                }
            }
            Tuples tuples = TuplesFactory.generateTuples(
                    new TupleValidator() {
                        int nb = 4 * finalN;
                        @Override
                        public boolean valid(int... values) {
                            return rnd.nextBoolean() && nb-- > 0;
                        }
                    }, true, doms);
            CorrectnessChecker.checkCorrectness(Modeler.modelmddcAC, n, -n / 2, n / 2, seed, new MultivaluedDecisionDiagram(doms, tuples));
        }
    }

    // INT VALUE PRECEDE CHAIN
    @Test(groups="checker", timeOut=60000)
    public void testIntValuePrecedeChain() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelivpcAC, n, -n, 2 * n, seed, false);
            }
        }
    }

    // MIN
    @Test(groups="checker", timeOut=60000)
    public void testMIN() {
        for (int i = 0; i < 3; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelminbc, n, -n, 2 * n, seed, false);
                CorrectnessChecker.checkCorrectness(Modeler.modelminbbc, n, 0, 1, seed, false);
            }
        }
    }

    // MAX
    @Test(groups="checker", timeOut=60000)
    public void testMAX() {
        for (int i = 0; i < 3; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelmaxbc, n, -n, 2 * n, seed, false);
                CorrectnessChecker.checkCorrectness(Modeler.modelmaxbbc, n, 0, 1, seed, false);
            }
        }
    }

    @Test(groups="checker", timeOut=60000)
    public void testPLUSBC() {
        for (int i = 0; i < 3; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 6) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelplusbc, 3, -n, 2 * n, seed, false);
                CorrectnessChecker.checkCorrectness(Modeler.modelplusbc, 3, 0, 1, seed, false);
            }
        }
    }

    @Test(groups="checker", timeOut=60000)
    public void testPLUSAC() {
        for (int i = 0; i < 4; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 7) + 1; n *= 2) {
                CorrectnessChecker.checkCorrectness(Modeler.modelplusac, 3, -n, 2 * n, seed, false);
            }
        }
    }
}
