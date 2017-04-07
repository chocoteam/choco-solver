/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.xscp;

import org.chocosolver.parser.flatzinc.FznSettings;
import org.chocosolver.parser.xcsp.XCSP;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;

/**
 * <p>
 * Project: choco-parsers.
 *
 * @author Charles Prud'homme
 * @since 21/04/2016.
 */
public class FastTest {

    @Test(groups = "2012,close<1m,xcsp", timeOut = 120000, dataProvider = "close<1m")
    public void testFast(String name, int nbsol, int bval, int nbnod, boolean complet) throws Exception {
        execute(name,nbsol,bval,nbnod,complet,false);
    }

    @Test(groups = "2012,close<1m,xcsp,cbj", timeOut = 180000, dataProvider = "close<1m")
    public void testFastCBJ(String name, int nbsol, int bval, int nbnod, boolean complet) throws Exception {
        System.out.println("solving with explanation");
        execute(name,nbsol,bval,nbnod,complet,true);
    }

    private void execute(String name, int nbsol, int bval, int nbnod, boolean complet, boolean exp) throws Exception {
        ClassLoader cl = this.getClass().getClassLoader();
        String file = cl.getResource(name).getFile();
        String[] args = new String[]{
                file,
                "-tl", "110s",
                "-stat",
                "-p", "1"
        };

        XCSP xscp = new XCSP();
        xscp.setUp(args);
        xscp.defineSettings(new FznSettings());
        xscp.createSolver();
        xscp.buildModel();
        xscp.configureSearch();
        if(exp) {
            xscp.getModel().getSolver().setCBJLearning(false, false);
        }
        xscp.solve();

        Assert.assertEquals(xscp.getModel().getSolver().isStopCriterionMet(), !complet, "Unexpected completeness information");
        if(complet){
            Assert.assertEquals(xscp.getModel().getSolver().getSolutionCount(), nbsol, "Unexpected number of solutions");
            if(exp){
                Assert.assertTrue(xscp.getModel().getSolver().getNodeCount() <= nbnod, "Unexpected number of nodes");
            }else{
                Assert.assertEquals(xscp.getModel().getSolver().getNodeCount(), nbnod, "Unexpected number of nodes");
            }
            if (xscp.getModel().getObjective() != null) {
                Assert.assertEquals(xscp.getModel().getSolver().getObjectiveManager().getBestSolutionValue(), bval, "Unexpected best solution");
            }
        }else{
            double i = nbsol * .05;
            Assert.assertTrue(xscp.getModel().getSolver().getSolutionCount() >  (nbsol*1.0 - i), "Unexpected number of solutions");
            Assert.assertTrue(xscp.getModel().getSolver().getSolutionCount() <  (nbsol*1.0 + i), "Unexpected number of solutions");
        }
    }

    public static final String basics = "xcsp" + File.separator + "basics" + File.separator;

    /**
     * @return Tests closed in less than 1m
     */
    @DataProvider(name = "close<1m")
    public Object[][] provider1() {
        return new Object[][]{
                {basics + "AllInterval-005.xml.lzma", 1, -1, 15, true},
                {basics + "Bibd-sc-06-050-25-03-10.xml.lzma", 1, -1, 99086, true},
                {basics + "Bibd-sum-06-050-25-03-10.xml.lzma", 1, -1, 348, true},
//                {basics + "BinPacking-mdd-n1c1w4a.xml.lzma", 0, -1, 15, false},
//                {basics + "BinPacking-sum-n1c1w4a.xml.lzma", 0, -1, 15, false},
//                {basics + "BinPacking-tab-n1c1w4a.xml.lzma", 0, -1, 15, false},
                {basics + "Blackhole-04-3-00.xml.lzma", 1, -1, 13, true},
                {basics + "BusScheduling-cnt-t1.xml.lzma", 1, 7, 263, true},
                {basics + "CarSequencing-dingbas.xml.lzma", 1, -1, 7, true},
                {basics + "ChessboardColoration-07-07.xml.lzma", 1, 2, 2536, true},
                {basics + "ColouredQueens-07.xml.lzma", 1, -1, 66, true},
                {basics + "CostasArray-12.xml.lzma", 1, -1, 66, true},
//                {basics + "Crossword-lex-vg-5-6.xml.lzma", 1, -1, 15, true},
                {basics + "CryptoPuzzle-cross-roads-danger.xml.lzma", 1, -1, 13, true},
                {basics + "Cutstock-small.xml.lzma", 4, 4, 29, true},
//                {basics + "DistinctVectors-30-050-02.xml.lzma", 1, -1, 15, true},
                {basics + "Domino-300-300.xml.lzma", 1, -1, 1, true},
                {basics + "driverlogw-09.xml.lzma", 1, -1, 3868, true},
                {basics + "Fastfood-ff10.xml.lzma", 67, 704, 51829, true},
                {basics + "GolombRuler-09-a3.xml.lzma", 8, 44, 23281, true},
                {basics + "GolombRuler-09-a4.xml.lzma", 12, 44, 52654, true},
                {basics + "GracefulGraph-K02-P04.xml.lzma", 1, -1, 40, true},
//                {basics + "GraphColoring-3-fullins-4.xml.lzma", 399, 6, 317263, false},
                {basics + "GraphColoring-qwhdec-o5-h10-1.xml.lzma", 1, 4, 1, true},
                {basics + "Hanoi-05.xml.lzma", 1, -1, 31, true},
                {basics + "Kakuro-easy-000-ext.xml.lzma", 1, -1, 1, true},
                {basics + "Kakuro-easy-000-sumdiff.xml.lzma", 1, -1, 6, true},
                {basics + "Knapsack-30-100-00.xml.lzma", 15, 709, 50909, true},
                {basics + "KnightTour-06-ext03.xml.lzma", 1, -1, 558, true},
//                {basics + "KnightTour-06-int.xml.lzma", 0, -1, 15, false},
                {basics + "Langford-3-10.xml.lzma", 1, -1, 288, true},
                {basics + "LangfordBin-08.xml.lzma", 1, -1, 17, true},
                {basics + "LowAutocorrelation-015.xml.lzma", 7, 15, 18739, true},
                {basics + "MagicSequence-008-ca.xml.lzma", 1, -1, 5, true},
                {basics + "MagicSequence-008-co.xml.lzma", 1, -1, 5, true},
                {basics + "MagicSquare-4-table.xml.lzma", 1, -1, 13, true},
                {basics + "MagicSquare-6-mdd.xml.lzma", 1, -1, 18147, true},
                {basics + "MagicSquare-6-sum.xml.lzma", 1, -1, 18925, true},
                {basics + "MagicSquare-9-f10-01.xml.lzma", 1, -1, 1120, true},
                {basics + "Mario-easy-4.xml.lzma", 4, 545, 2851, true},
                {basics + "MarketSplit-01.xml.lzma", 1, -1, 849007, true},
                {basics + "MultiKnapsack-1-0_X2.xml.lzma", 1, -1, 2, true},
                {basics + "MultiKnapsack-1-01.xml.lzma", 1, -1, 2, true},
                {basics + "Nonogram-001-regular.xml.lzma", 1, -1, 6, true},
                {basics + "Nonogram-001-table.xml.lzma", 1, -1, 6, true},
                {basics + "Opd-07-007-003.xml.lzma", 2, 1, 61, true},
                {basics + "Ortholatin-005.xml.lzma", 1, -1, 17, true},
                {basics + "Pb-gr-05.xml.lzma", 8, 11, 265, true},
                {basics + "Pb-robin08.xml.lzma", 1, -1, 103, true},
                {basics + "Primes-15-20-2-1.xml.lzma", 1, -1, 7, true},
                {basics + "qcp-15-120-00_X2.xml.lzma", 1, -1, 6189, true},
//                {basics + "QuadraticAssignment-bur26a.xml.lzma", 19, 2697796, 719024, false},
//                {basics + "QuadraticAssignment-qap.xml.lzma", 18, 4776, 601, false},
                {basics + "QuasiGroup-3-04.xml.lzma", 1, -1, 2, true},
                {basics + "QuasiGroup-7-09.xml.lzma", 1, -1, 1079, true},
//                {basics + "QueenAttacking-06.xml.lzma", 0, -1, 1148944, false},
                {basics + "Queens-0008-m1.xml.lzma", 1, -1, 112, true},
                {basics + "qwh-o30-h374-01.xml.lzma", 1, -1, 68053, true},
                {basics + "RadarSurveillance-8-24-3-2-00.xml.lzma", 1, -1, 84, true},
                {basics + "Ramsey-12.xml.lzma", 1, 1, 1442, true},
                {basics + "RoomMate-sr0050-int.xml.lzma", 1, -1, 7, true},
                {basics + "Sat-flat200-00-clause.xml.lzma", 1, -1, 3337, true},
                {basics + "SocialGolfers-4-3-4-cp.xml.lzma", 1, -1, 153, true},
                {basics + "SportsScheduling-08.xml.lzma", 1, -1, 407267, true},
//                {basics + "Steiner3-08.xml.lzma", 1, -1, 2512159, false},
                {basics + "StillLife-03-06.xml.lzma", 1, 10, 132, true},
                {basics + "StillLife-wastage-03.xml.lzma", 7, 6, 45, true},
//                {basics + "StripPacking-C1P1.xml.lzma", 1, -1, 2276250, false},
                {basics + "Subisomorphism-A-10.xml.lzma", 1, -1, 18, true},
                {basics + "Sudoku-s01a-alldiff.xml.lzma", 1, -1, 1, true},
//                {basics + "Taillard-js-015-15-0.xml.lzma", 0, -1, 224613, false},
                {basics + "Taillard-os-04-04-0.xml.lzma", 15, 193, 4905, true},
                {basics + "testExtension1.xml.lzma", 1, -1, 4, true},
                {basics + "testExtension2.xml.lzma", 1, -1, 4, true},
                {basics + "testExtension3.xml.lzma", 0, -1, 15, true},
                {basics + "testObjective1.xml.lzma", 2, 11, 10, true},
                {basics + "testPrimitive.xml.lzma", 1, -1, 3, true},
                {basics + "Tpp-3-3-20-1.xml.lzma", 17, 126, 164, true},
                {basics + "TravellingSalesman-13-13-0.xml.lzma", 14, 36, 137174, true},
//                {basics + "TravellingSalesman-20-30-00.xml.lzma", 20, 104, 876633, false},
//                {basics + "Vrp-A-n32-k5.xml.lzma", 47, 1762, 1749921, false},
//                {basics + "Vrp-P-n16-k8.xml.lzma", 25, 450, 4838579, true},
                {basics + "Warehouse-opl.xml.lzma", 11, 383, 4282, true},
//                {basics + "Zebra.xml.lzma", 1, -1, 15, true},
        };
    }

}
