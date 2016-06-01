/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 * must display the following acknowledgement:
 * This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
                {basics + "QuadraticAssignment-qap.xml", 18, 4776, 601, true},
                {basics + "testExtension1.xml", 1, -1, 4, true},
                {basics + "testExtension2.xml", 1, -1, 4, true},
                {basics + "testExtension3.xml", 0, -1, 15, true},
                {basics + "testObjective1.xml", 2, 11, 10, true},
                {basics + "testPrimitive.xml", 1, -1, 3, true},
        };
    }

}
