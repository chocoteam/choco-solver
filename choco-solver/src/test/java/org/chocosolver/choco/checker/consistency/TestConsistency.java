/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.choco.checker.consistency;

import org.chocosolver.choco.checker.Modeler;
import org.chocosolver.solver.constraints.extension.TupleValidator;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.choco.checker.consistency.ConsistencyChecker.checkConsistency;

/**
 * Created by IntelliJ IDEA.
 * User: xlorca
 */
public class TestConsistency {

    public TestConsistency() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // EQ *******************************************************
    @Test(groups = "consistency.1s")
    public void testEQ1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelEqAC, 2, 0, 2, null, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
    public void testEQ2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelEqAC, 2, 0, 100, null, seed + i, "ac");
    }

    // NEQ *******************************************************
    @Test(groups = "consistency.1s")
    public void testNEQ2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelNeqAC, 2, 0, 2, null, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
    public void testNEQ() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelNeqAC, 2, 0, 100, null, seed + i, "ac");
    }

    // AllDifferent AC *******************************************************
    @Test(groups = "consistency.1s")
    public void testALLDIFFERENT1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffAC, 1, 0, 10, null, seed + i, "ac");
    }

    @Test(groups = "consistency.1s")
    public void testALLDIFFERENT2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffAC, 2, 0, 2, null, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
    public void testALLDIFFERENT3() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffAC, 5, 2, 50, null, seed + i, "ac");
    }

    @Test(groups = "ignored")
    public void testALLDIFFERENT4() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffAC, 10, 0, 100, null, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
    public void testALLDIFFERENTGRAPHAC() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffGraph, 5, 2, 50, null, seed + i, "ac");
    }

    // InverseChanneling AC*******************************************************
//	// InverseChanneling no longer ensures AC but default (because it uses the allDifferent.DEFAULT, which is fast)
//    @Test(groups = "consistency.10s")
//    public void testINVERSECHANNELING_AC1() {
//        long seed = System.currentTimeMillis();
//        for (int i = 0; i < 20; i++)
//            checkConsistency(Modeler.modelInverseChannelingAC, 20, 0, 40, null, seed + i, "ac");
//    }
//
//    @Test(groups = "consistency.10s")
//    public void testINVERSECHANNELING_AC2() {
//        long seed = System.currentTimeMillis();
//        for (int i = 0; i < 20; i++)
//            checkConsistency(Modeler.modelInverseChannelingAC, 10, 10, 120, null, seed + i, "ac");
//    }
//
//    @Test(groups = "consistency.10s")
//    public void testINVERSECHANNELING_AC3() {
//        long seed = System.currentTimeMillis();
//        for (int i = 0; i < 20; i++)
//            checkConsistency(Modeler.modelInverseChannelingAC, 10, -10, 120, null, seed + i, "ac");
//    }

    // AllDifferent BC *******************************************************
    @Test(groups = "consistency.1s")
    public void testALLDIFFERENTBC1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffBC, 1, 0, 10, null, seed + i, "bc");
    }

    @Test(groups = "consistency.1s")
    public void testALLDIFFERENTBC2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffBC, 2, 0, 2, null, seed + i, "bc");
    }

    @Test(groups = "consistency.10s")
    public void testALLDIFFERENTBC3() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffBC, 5, 2, 50, null, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testALLDIFFERENTBC4() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffBC, 10, 0, 100, null, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testALLDIFFERENTGRAPHBC() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffGraphBc, 5, 2, 50, null, seed + i, "bc");
    }

//    NO CONSISTENCY ON TIMES CONSTRAINT
//    @Test(groups = "consistency.10s")
//    public void testTIMES1() {
//        long seed = System.currentTimeMillis();
//        for (int i = 0; i < 20; i++)
//            checkConsistency(Modeler.modelTimes, 3, 2, 50, null, seed + i, "bc");
//    }
//
//    @Test(groups = "consistency.10s")
//    public void testTIMES2() {
//        long seed = System.currentTimeMillis();
//        for (int i = 0; i < 20; i++)
//            checkConsistency(Modeler.modelTimes, 3, -25, 25, null, seed + i, "bc");
//    }
//
//
//    @Test(groups = "consistency.10s")
//    public void testTIMES3() {
//        long seed = System.currentTimeMillis();
//        for (int i = 0; i < 20; i++)
//            checkConsistency(Modeler.modelTimes, 3, -50, -3, null, seed + i, "bc");
//    }

    @Test(groups = "consistency.1s")
    public void testABSOLUTEBC1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAbsolute, 2, 2, 50, null, seed + i, "bc");
    }

    @Test(groups = "consistency.1s")
    public void testABSOLUTEBC2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAbsolute, 2, -25, 25, null, seed + i, "bc");
    }


    @Test(groups = "consistency.10s")
    public void testABSOLUTEBC3() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAbsolute, 2, -50, 50, null, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testABSOLUTEAC1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAbsolute, 2, 2, 50, null, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
    public void testABSOLUTEAC2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAbsolute, 2, -25, 25, null, seed + i, "ac");
    }


    @Test(groups = "consistency.1m")
    public void testABSOLUTEAC3() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAbsolute, 2, -50, 50, null, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
    public void testCOUNTBC1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 999; i++)
            checkConsistency(Modeler.modelCountBC, 2, 2, 50, new int[]{0, 1}, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testCOUNTAC1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelCountAC, 2, 2, 50, new int[]{0, 1}, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
    public void testCOUNTBC2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 999; i++)
            checkConsistency(Modeler.modelCountBC, 5, -10, 10, new int[]{0, 1}, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testCOUNTAC2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelCountAC, 5, -10, 10, new int[]{0, 1}, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
    public void testLEX1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelLexAC, 6, -10, 10, true, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
    public void testLEX2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelLexAC, 6, -10, 10, false, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
    public void testLEXC1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelLexChainAC, 9, -10, 10, true, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
    public void testLEXC2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelLexChainAC, 9, -10, 10, false, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
    public void testELEMENTBC1() {
        long seed = 0;//System.currentTimeMillis();
        for (int i = 0; i < 999; i++) {
            checkConsistency(Modeler.modelNthBC, 2, -10, 10, null, seed + i, "bc");
        }
    }

    @Test(groups = "consistency.1m")
    public void testAMONGBC1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAmongBC, 2, 2, 50, new int[]{0, 1}, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testAMONGAC1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAmongAC, 2, 2, 50, new int[]{0, 1}, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
    public void testAMONGBC2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAmongBC, 5, -10, 10, new int[]{0, 1}, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testAMONGAC2() {
        long seed = 0;
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAmongAC, 5, -10, 10, new int[]{0, 1}, seed + i, "ac");
    }

    @Test(groups = "consistency.10s")
    public void testSORT1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelSortBC, 6, 0, 10, null, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testSORT2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelSortBC, 8, -20, 20, null, seed + i, "bc");
    }

    @Test(groups = "consistency.1s")
    public void testMDD1() {
        long seed = System.currentTimeMillis();
        Random rnd = new Random();
        for (int i = 0; i < 99; i++) {
            rnd.setSeed(seed + i);
            int[][] doms = new int[1][10];
            for (int j = 0; j < 1; j++) {
                for (int k = 0; k < 10; k++) {
                    doms[j][k] = k;
                }
            }
            Tuples tuples = TuplesFactory.generateTuples(
                    new TupleValidator() {
                        int nb = 4;

                        @Override
                        public boolean valid(int... values) {
                            return rnd.nextBoolean() && nb-- > 0;
                        }
                    }, true, doms);
            checkConsistency(Modeler.modelmddcAC, 1, 0, 10, new MultivaluedDecisionDiagram(doms, tuples), seed + i, "ac");
        }
    }

    @Test(groups = "consistency.1s")
    public void testMDD2() {
        long seed = System.currentTimeMillis();
        Random rnd = new Random();
        for (int i = 0; i < 99; i++) {
            rnd.setSeed(seed + i);
            int[][] doms = new int[2][2];
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    doms[j][k] = k;
                }
            }
            Tuples tuples = TuplesFactory.generateTuples(
                    new TupleValidator() {
                        int nb = 3;

                        @Override
                        public boolean valid(int... values) {
                            return rnd.nextBoolean() && nb-- > 0;
                        }
                    }, true, doms);
            checkConsistency(Modeler.modelmddcAC, 2, 0, 2, new MultivaluedDecisionDiagram(doms, tuples), seed + i, "ac");
        }
    }

    @Test(groups = "consistency.10s")
    public void testMDD3() {
        long seed = System.currentTimeMillis();
        Random rnd = new Random();
        for (int i = 0; i < 999; i++) {
            rnd.setSeed(seed + i);
            int[][] doms = new int[5][8];
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < 8; k++) {
                    doms[j][k] = k - 4;
                }
            }
            Tuples tuples = TuplesFactory.generateTuples(
                    new TupleValidator() {
                        int nb = 20;

                        @Override
                        public boolean valid(int... values) {
                            return rnd.nextBoolean() && nb-- > 0;
                        }
                    }, true, doms);
            checkConsistency(Modeler.modelmddcAC, 5, -4, 3, new MultivaluedDecisionDiagram(doms, tuples), seed + i, "ac");
        }
    }

    @Test(groups = "consistency.1m")
    public void testIVPC1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelivpcAC, 5, -5, 5, null, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
    public void testIVPC2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelivpcAC, 10, -2, 3, null, seed + i, "ac");
    }

    @Test(groups = "consistency.1m")
     public void testMAX1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelmaxbc, 5, -5, 5, null, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testMAX2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelmaxbc, 10, -2, 3, null, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testMIN1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelminbc, 5, -5, 5, null, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testMIN2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelminbc, 10, -2, 3, null, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testMAXB1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelmaxbbc, 5, 0, 1, null, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testMAXB2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelmaxbbc, 10, 0, 1, null, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testMINB1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelminbbc, 5, 0, 1, null, seed + i, "bc");
    }

    @Test(groups = "consistency.1m")
    public void testMINB2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 99; i++)
            checkConsistency(Modeler.modelminbbc, 10, 0, 1, null, seed + i, "bc");
    }

    /*@Test
    public void runner() throws ClassNotFoundException, IOException, ContradictionException {
        Solver s = Solver.readFromFile("/Users/kyzrsoze/Sources/Choco3/SOLVER_ERROR.ser");
        s.getEnvironment().worldPopUntil(0);
        s.getEnvironment().worldPush();
        Constraint[] constraints = s.getCstrs();
        for (int c = 0; c < constraints.length; c++) {
            Propagator[] propagators = constraints[c].getPropagators();
            for (int p = 0; p < propagators.length; p++) {
                propagators[p].forcePropagate(PropagatorEventType.FULL_PROPAGATION);
            }
        }
        s.propagate();
    }*/
}

