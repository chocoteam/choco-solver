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
                "-tl", "90s",
                "-stat",
                "-p", "1"
        };

        XCSP xscp = new XCSP();
        xscp.parseParameters(args);
        xscp.defineSettings(new FznSettings());
        xscp.createSolver();
        xscp.parseInputFile();
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
                {basics + "AllInterval-005.xml", 1, -1, 15, true},
                {basics + "KnightTour-06-ext02.xml", 1, -1, 8692, true},
                {basics + "KnightTour-08-ext03.xml", 1, -1, 161715, true},
                {basics + "mknap1-01.xml", 1, -1, 2, true},
                {basics + "QuadraticAssignment-qap.xml", 18, 4776, 601, true},
                {basics + "testExtension1.xml", 1, -1, 4, true},
                {basics + "testExtension2.xml", 1, -1, 4, true},
                {basics + "testExtension3.xml", 0, -1, 15, true},
                {basics + "testObjective1.xml", 2, 11, 10, true},
                {basics + "testPrimitive.xml", 1, -1, 3, true},
                {basics + "TravellingSalesman-13-13-0.xml.lzma", 14, 36, 137174, true},
        };
    }

}
