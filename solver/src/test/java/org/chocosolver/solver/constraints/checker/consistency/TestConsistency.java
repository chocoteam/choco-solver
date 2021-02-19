/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.checker.consistency;

import org.chocosolver.solver.constraints.checker.Modeler;
import org.chocosolver.solver.constraints.extension.TupleValidator;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.constraints.checker.consistency.ConsistencyChecker.checkConsistency;

/**
 * Created by IntelliJ IDEA.
 * User: xlorca
 */
public class TestConsistency {

    // NO CONSISTENCY ON TIMES CONSTRAINT
    // NO CONSISTENCY ON InverseChanneling CONSTRAINT (because it uses the allDifferent.DEFAULT, which is fast)

    public TestConsistency() {}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // EQ *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testEQ() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            checkConsistency(Modeler.modelEqAC, 2, 0, 2, null, seed + i, "ac");
            checkConsistency(Modeler.modelEqAC, 2, 0, 50, null, seed + i, "ac");
        }
    }

    // NEQ *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testNEQ() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            checkConsistency(Modeler.modelNeqAC, 2, 0, 2, null, seed + i, "ac");
            checkConsistency(Modeler.modelNeqAC, 2, 0, 50, null, seed + i, "ac");
        }
    }

    // AllDifferent *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testALLDIFFERENT() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            checkConsistency(Modeler.modelAllDiffAC, 1, 0, 10, null, seed + i, "ac");
            checkConsistency(Modeler.modelAllDiffAC, 2, 0, 2, null, seed + i, "ac");
            checkConsistency(Modeler.modelAllDiffAC, 5, 2, 30, null, seed + i, "ac");
        }
    }
    @Test(groups="checker", timeOut=60000)
    public void testALLDIFFERENTBC() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            checkConsistency(Modeler.modelAllDiffBC, 1, 0, 10, null, seed + i, "bc");
            checkConsistency(Modeler.modelAllDiffBC, 2, 0, 2, null, seed + i, "bc");
            checkConsistency(Modeler.modelAllDiffBC, 5, 2, 50, null, seed + i, "bc");
            checkConsistency(Modeler.modelAllDiffBC, 10, 0, 50, null, seed + i, "bc");
        }
    }

    // Absolute *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testABSOLUTE() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            checkConsistency(Modeler.modelAbsolute, 2, 2, 50, null, seed + i, "bc");
            checkConsistency(Modeler.modelAbsolute, 2, -25, 25, null, seed + i, "bc");
            checkConsistency(Modeler.modelAbsolute, 2, -50, 50, null, seed + i, "bc");
            checkConsistency(Modeler.modelAbsolute, 2, 2, 50, null, seed + i, "ac");
            checkConsistency(Modeler.modelAbsolute, 2, -25, 25, null, seed + i, "ac");
            checkConsistency(Modeler.modelAbsolute, 2, -50, 50, null, seed + i, "ac");
        }
    }

    // Count *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testCOUNT() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 60; i++) {
            checkConsistency(Modeler.modelCountBC, 2, 2, 50, new int[]{0, 1}, seed + i, "bc");
            checkConsistency(Modeler.modelCountBC, 5, -10, 10, new int[]{0, 1}, seed + i, "bc");
            checkConsistency(Modeler.modelCountAC, 2, 2, 50, new int[]{0, 1}, seed + i, "ac");
            checkConsistency(Modeler.modelCountAC, 5, -10, 10, new int[]{0, 1}, seed + i, "ac");
        }
    }

    // LEX *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testLEX() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
            checkConsistency(Modeler.modelLexAC, 6, -10, 10, true, seed + i, "ac");
            checkConsistency(Modeler.modelLexAC, 6, -10, 10, false, seed + i, "ac");
        }
    }

    // LEX CHAIN *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testLEXCHAIN() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 25; i++) {
            checkConsistency(Modeler.modelLexChainAC, 9, -10, 10, true, seed + i, "ac");
            checkConsistency(Modeler.modelLexChainAC, 9, -10, 10, false, seed + i, "ac");
        }
    }

    // ELEMENT *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testELEMENT() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++) {
            checkConsistency(Modeler.modelNthBC, 2, -10, 10, null, seed + i, "bc");
        }
    }

    // AMONG *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testAMONG() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            checkConsistency(Modeler.modelAmongBC, 2, 2, 50, new int[]{0, 1}, seed + i, "bc");
            checkConsistency(Modeler.modelAmongBC, 5, -10, 10, new int[]{0, 1}, seed + i, "bc");
            checkConsistency(Modeler.modelAmongAC, 2, 2, 50, new int[]{0, 1}, seed + i, "ac");
            checkConsistency(Modeler.modelAmongAC, 5, -10, 10, new int[]{0, 1}, seed + i, "ac");
        }
    }

    // SORT *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testSORT() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 30; i++) {
            checkConsistency(Modeler.modelSortBC, 6, 0, 10, null, seed + i, "bc");
            checkConsistency(Modeler.modelSortBC, 8, -20, 20, null, seed + i, "bc");
        }
    }

    // MIN *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testMIN() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++) {
            checkConsistency(Modeler.modelminbc, 5, -5, 5, null, seed + i, "bc");
            checkConsistency(Modeler.modelminbc, 10, -2, 3, null, seed + i, "bc");
            checkConsistency(Modeler.modelminbbc, 5, 0, 1, null, seed + i, "bc");
            checkConsistency(Modeler.modelminbbc, 10, 0, 1, null, seed + i, "bc");
        }
    }

    // MAX *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testMAX() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++) {
            checkConsistency(Modeler.modelmaxbc, 5, -5, 5, null, seed + i, "bc");
            checkConsistency(Modeler.modelmaxbc, 10, -2, 3, null, seed + i, "bc");
            checkConsistency(Modeler.modelmaxbbc, 10, 0, 1, null, seed + i, "bc");
            checkConsistency(Modeler.modelmaxbbc, 5, 0, 1, null, seed + i, "bc");
        }
    }

    // INT VALUE PRECEDE CHAIN *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testIVPC() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
            checkConsistency(Modeler.modelivpcAC, 5, -5, 5, null, seed + i, "ac");
            checkConsistency(Modeler.modelivpcAC, 10, -2, 3, null, seed + i, "ac");
        }
    }

    // MDD *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testMDD() {
        long seed = System.currentTimeMillis();
        Random rnd = new Random();
        int[][] params = new int[][]{{1,10,4,0},{2,2,3,0},{5,8,20,-4}};
        for (int i = 0; i < 99; i++) {
            rnd.setSeed(seed + i);
            for(int[] p:params) {
                int[][] doms = new int[p[0]][p[1]];
                for (int j = 0; j < p[0]; j++) {
                    for (int k = 0; k < p[1]; k++) {
                        doms[j][k] = k+p[3];
                    }
                }
                Tuples tuples = TuplesFactory.generateTuples(
                        new TupleValidator() {
                            int nb = p[2];
                            @Override
                            public boolean valid(int... values) {
                                return rnd.nextBoolean() && nb-- > 0;
                            }
                        }, true, doms);
                checkConsistency(Modeler.modelmddcAC, p[0], p[3], p[1]+p[3], new MultivaluedDecisionDiagram(doms, tuples), seed + i, "ac");
            }
        }
    }

    // PLUS *******************************************************

    @Test(groups="checker", timeOut=60000)
    public void testPLUSBC() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++) {
            checkConsistency(Modeler.modelplusbc, 3, 0, 1, null, seed + i, "bc");
            checkConsistency(Modeler.modelplusbc, 3, -50, 50, null, seed + i, "bc");
        }
    }

    @Test(groups="checker", timeOut=60000)
    public void testPLUSAC() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++) {
            checkConsistency(Modeler.modelplusac, 3, -15, 15, null, seed + i, "ac");
        }
    }

}

