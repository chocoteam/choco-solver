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

import static choco.checker.ConsistencyChecker.checkConsistency;

/**
 * Created by IntelliJ IDEA.
 * User: xlorca
 */
public class TestCompletenessConsistency {

    private int slType; // search loop type default value

    public TestCompletenessConsistency() {
        this.slType = 0;
    }

    public TestCompletenessConsistency(int peType) {
        this.slType = peType;
    }

    @BeforeTest(alwaysRun = true)
    private void beforeTest() {
        SearchLoops._DEFAULT = slType;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // EQ *******************************************************
    @Test(groups = "1s")
    public void testEQ1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelEqAC, 2, 0, 2, null, seed + i, "ac");
    }

    @Test(groups = "1m")
    public void testEQ2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelEqAC, 2, 0, 100, null, seed + i, "ac");
    }

    // NEQ *******************************************************
    @Test(groups = "1s")
    public void testNEQ2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelNeqAC, 2, 0, 2, null, seed + i, "ac");
    }

    @Test(groups = "1m")
    public void testNEQ() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelNeqAC, 2, 0, 100, null, seed + i, "ac");
    }

    // AllDifferent AC *******************************************************
    @Test(groups = "1s")
    public void testALLDIFFERENT1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffAC, 1, 0, 10, null, seed + i, "ac");
    }

    @Test(groups = "1s")
    public void testALLDIFFERENT2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffAC, 2, 0, 2, null, seed + i, "ac");
    }

    @Test(groups = "1m")
    public void testALLDIFFERENT3() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffAC, 5, 2, 50, null, seed + i, "ac");
    }

    @Test(groups = ">30m")
    public void testALLDIFFERENT4() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffAC, 10, 0, 100, null, seed + i, "ac");
    }

    @Test(groups = "1m")
    public void testALLDIFFERENTGRAPHAC() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffGraph, 5, 2, 50, null, seed + i, "ac");
    }

    // InverseChanneling *******************************************************
    @Test(groups = "10s")
    public void testINVERSECHANNELING1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelInverseChannelingAC, 20, 0, 40, null, seed + i, "ac");
    }

    @Test(groups = "10s")
    public void testINVERSECHANNELING2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelInverseChannelingAC, 10, 10, 120, null, seed + i, "ac");
    }

    @Test(groups = "10s")
    public void testINVERSECHANNELING3() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelInverseChannelingAC, 10, -10, 120, null, seed + i, "ac");
    }

    // AllDifferent BC *******************************************************
    @Test(groups = "1s")
    public void testALLDIFFERENTBC1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffBC, 1, 0, 10, null, seed + i, "bc");
    }

    @Test(groups = "1s")
    public void testALLDIFFERENTBC2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffBC, 2, 0, 2, null, seed + i, "bc");
    }

    @Test(groups = "10s")
    public void testALLDIFFERENTBC3() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffBC, 5, 2, 50, null, seed + i, "bc");
    }

    @Test(groups = "10s")
    public void testALLDIFFERENTBC4() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffBC, 10, 0, 100, null, seed + i, "bc");
    }

    @Test(groups = "10s")
    public void testALLDIFFERENTGRAPHBC() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAllDiffGraphBc, 5, 2, 50, null, seed + i, "bc");
    }

    @Test(groups = "10s")
    public void testTIMES1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelTimes, 3, 2, 50, null, seed + i, "bc");
    }

    @Test(groups = "10s")
    public void testTIMES2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelTimes, 3, -25, 25, null, seed + i, "bc");
    }


    @Test(groups = "10s")
    public void testTIMES3() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelTimes, 3, -50, -3, null, seed + i, "bc");
    }

    @Test(groups = "10s")
    public void testABSOLUTEBC1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAbsolute, 2, 2, 50, null, seed + i, "bc");
    }

    @Test(groups = "10s")
    public void testABSOLUTEBC2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAbsolute, 2, -25, 25, null, seed + i, "bc");
    }


    @Test(groups = "10s")
    public void testABSOLUTEBC3() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAbsolute, 2, -50, 50, null, seed + i, "bc");
    }

    @Test(groups = "1m")
    public void testABSOLUTEAC1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAbsolute, 2, 2, 50, null, seed + i, "ac");
    }

    @Test(groups = "1m")
    public void testABSOLUTEAC2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAbsolute, 2, -25, 25, null, seed + i, "ac");
    }


    @Test(groups = "1m")
    public void testABSOLUTEAC3() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelAbsolute, 2, -50, 50, null, seed + i, "ac");
    }

    @Test(groups = "1m")
    public void testCOUNTBC1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelCountBC, 2, 2, 50, new int[]{0,1}, seed + i, "bc");
    }

    @Test(groups = "1m")
    public void testCOUNTAC1() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelCountAC, 2, 2, 50, new int[]{0,1}, seed + i, "ac");
    }

    @Test(groups = "1m")
    public void testCOUNTBC2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelCountBC, 5, -10, 10, new int[]{0,1}, seed + i, "bc");
    }

    @Test(groups = "1m")
    public void testCOUNTAC2() {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 20; i++)
            checkConsistency(Modeler.modelCountAC, 5, -10, 10, new int[]{0,1}, seed + i, "ac");
    }

    /*@Test
    public void runner() throws ClassNotFoundException, IOException, ContradictionException {
        Solver s = Solver.readFromFile("/Users/cprudhom/Documents/Projects/Sources/Galak/trunk/SOLVER_ERROR.ser");
        s.getEnvironment().worldPopUntil(0);
        s.getEnvironment().worldPush();
        s.findSolution();
    }*/
}

