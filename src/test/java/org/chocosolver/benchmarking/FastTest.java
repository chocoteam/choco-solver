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
package org.chocosolver.benchmarking;

import org.chocosolver.parser.flatzinc.BaseFlatzincListener;
import org.chocosolver.parser.flatzinc.Flatzinc;
import org.chocosolver.parser.flatzinc.FznSettings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * <p>
 * Project: choco-parsers.
 *
 * @author Charles Prud'homme
 * @since 21/04/2016.
 */
public class FastTest {

    @Test(groups = "2012,close<1m,mzn", timeOut = 120000, dataProvider = "close<1m")
    public void testFast(String name, int nbsol, int bval, int nbnod, boolean complet) throws InterruptedException, IOException, URISyntaxException {
        ClassLoader cl = this.getClass().getClassLoader();
        String file = cl.getResource(name).getFile();
        String[] args = new String[]{
                file,
                "-tl", "90s",
                "-stat",
                "-p", "1"
        };

        Flatzinc fzn = new Flatzinc();
        fzn.addListener(new BaseFlatzincListener(fzn));
        fzn.parseParameters(args);
        fzn.defineSettings(new FznSettings());
        fzn.createSolver();
        fzn.parseInputFile();
        fzn.configureSearch();
        fzn.solve();

        Assert.assertEquals(fzn.getModel().getSolver().isStopCriterionMet(), !complet, "Unexpected completeness information");
        if(complet){
            Assert.assertEquals(fzn.getModel().getSolver().getSolutionCount(), nbsol, "Unexpected number of solutions");
            Assert.assertEquals(fzn.getModel().getSolver().getNodeCount(), nbnod, "Unexpected number of nodes");
            if (fzn.getModel().getObjective() != null) {
                Assert.assertEquals(fzn.getModel().getSolver().getObjectiveManager().getBestSolutionValue(), bval, "Unexpected best solution");
            }
        }else{
            double i = nbsol * .05;
            Assert.assertTrue(fzn.getModel().getSolver().getSolutionCount() >  (nbsol*1.0 - i), "Unexpected number of solutions");
            Assert.assertTrue(fzn.getModel().getSolver().getSolutionCount() <  (nbsol*1.0 - i), "Unexpected number of solutions");
        }
    }


    public static final String pre2012 = "benchmarking" + File.separator + "2012" + File.separator;

    /**
     * @return Tests closed in less than 1m
     */
    @DataProvider(name = "close<1m")
    public Object[][] provider1() {
        return new Object[][]{
                {pre2012 + "amaze+amaze+2012-03-15.fzn", 1, 1429, 139370, true},
                {pre2012 + "amaze+amaze+2012-03-19.fzn", 1, 447, 58121, true},
                {pre2012 + "amaze+amaze+2012-06-22.fzn", 1, 928, 5007, true},
                {pre2012 + "amaze+amaze+2012-07-04.fzn", 1, 481, 243, true},
                {pre2012 + "fast-food+fastfood+ff3.fzn", 195, 1330, 71699, true},
                {pre2012 + "fast-food+fastfood+ff59.fzn", 109, 242, 275063, true},
                {pre2012 + "fast-food+fastfood+ff61.fzn", 99, 152, 318924, true},
                {pre2012 + "fast-food+fastfood+ff63.fzn", 87, 103, 155177, true},
                {pre2012 + "filters+filter+ar_1_2.fzn", 4, 18, 67400, true},
                {pre2012 + "filters+filter+dct_1_1.fzn", 1, 34, 49, true},
                {pre2012 + "filters+filter+ewf_1_1.fzn", 1, 28, 37, true},
                {pre2012 + "filters+filter+fir_1_1.fzn", 1, 18, 24, true},
                {pre2012 + "filters+filter+fir16_1_1.fzn", 1, 35, 34, true},
                {pre2012 + "mspsp+mspsp+easy_01.fzn", 5, 26, 1251641, true},
                {pre2012 + "mspsp+mspsp+hard_01.fzn", 8, 35, 3719, true},
                {pre2012 + "mspsp+mspsp+medium_02.fzn", 2, 15, 2048096, true},
                {pre2012 + "mspsp+mspsp+medium_03.fzn", 5, 26, 1251641, true},
                {pre2012 + "mspsp+mspsp+medium_05.fzn", 3, 18, 2623938, true},
                {pre2012 + "nonogram+non+non_fast_4.fzn", 1, 0, 43069, true},
                {pre2012 + "nonogram+non+non_fast_8.fzn", 1, 0, 1932, true},
                {pre2012 + "nonogram+non+non_fast_11.fzn", 1, 0, 1886, true},
                {pre2012 + "parity-learning+parity-learning+44_22_5.2.fzn", 1, 2, 761677, true},
                {pre2012 + "parity-learning+parity-learning+44_22_5.3.fzn", 2, 2, 1185574, true},
                {pre2012 + "pattern-set-mining-k2+pattern_set_mining_k2+audiology.fzn", 38, 54, 212187, true},
                {pre2012 + "radiation+radiation+m06_15_15.fzn", 1, 711, 307742, true},
                {pre2012 + "ship-schedule+ship-schedule.cp+5Ships.fzn", 359, 483650, 3139, true},
                {pre2012 + "ship-schedule+ship-schedule.cp+6ShipsMixed.fzn", 237, 301650, 16089, true},
                {pre2012 + "solbat+sb+sb_12_12_5_1.fzn", 1, 0, 1315, true},
                {pre2012 + "solbat+sb+sb_13_13_5_4.fzn", 1, 0, 75419, true},
                {pre2012 + "solbat+sb+sb_14_14_6_0.fzn", 1, 0, 383, true},
                {pre2012 + "still-life-wastage+still-life+09.fzn", 5, 43, 138590, true},
                {pre2012 + "still-life-wastage+still-life+10.fzn", 7, 54, 217194, true},
                {pre2012 + "tpp+tpp+tpp_3_5_20_1.fzn", 74, 127, 3751495, true},
                {pre2012 + "tpp+tpp+tpp_5_3_20_1.fzn", 68, 141, 3526579, true},
                {pre2012 + "tpp+tpp+tpp_5_5_20_1.fzn", 54, 115, 3215964, true},
                {pre2012 + "tpp+tpp+tpp_7_5_20_1.fzn", 76, 105, 5869335, true},
        };
    }

    /**
     * @return Tests that do give any answer in a 1-minute execution
     */
    @DataProvider(name = "morethanoneminute")
    public Object[][] provider2() {
        return new Object[][]{
                // format: name, nb sol, best sol value, nb nod, complete
                {pre2012 + "amaze+amaze+2012-03-08.fzn", 0, 0, 0, false},
                {pre2012 + "amaze+amaze+2012-06-29.fzn", 0, 0, 0, false},
                {pre2012 + "amaze2+amaze2+2012-03-08.fzn", 0, 0, 0, false},
                {pre2012 + "amaze2+amaze2+2012-03-15.fzn", 0, 0, 0, false},
                {pre2012 + "amaze2+amaze2+2012-06-22.fzn", 0, 0, 0, false},
                {pre2012 + "amaze2+amaze2+2012-06-28.fzn", 0, 0, 0, false},
                {pre2012 + "amaze2+amaze2+2012-06-29.fzn", 0, 0, 0, false},
                {pre2012 + "amaze2+amaze2+2012-07-04.fzn", 0, 0, 0, false},
                {pre2012 + "nonogram+non+non_awful_3.fzn", 0, 0, 0, false},
                {pre2012 + "nonogram+non+non_awful_5.fzn", 0, 0, 0, false},
                {pre2012 + "parity-learning+parity-learning+48_24_6.1.fzn", 0, 0, 0, false},
                {pre2012 + "parity-learning+parity-learning+52_26_6.2.fzn", 0, 0, 0, false},
                {pre2012 + "parity-learning+parity-learning+52_26_6.3.fzn", 0, 0, 0, false},
                {pre2012 + "radiation+radiation+m07_07_20.fzn", 0, 0, 0, false},
                {pre2012 + "radiation+radiation+m12_10_20.fzn", 0, 0, 0, false},
                {pre2012 + "radiation+radiation+m18_12_05.fzn", 0, 0, 0, false},
                {pre2012 + "solbat+sb+sb_14_14_6_4.fzn", 0, 0, 0, false},
                {pre2012 + "solbat+sb+sb_15_15_6_3.fzn", 0, 0, 0, false},
                {pre2012 + "train+train+instance.12.fzn", 0, 0, 0, false},

                {pre2012 + "carpet-cutting+cc_base+mzn_rnd_test.05.fzn", 22, 1417, 0, false},
                {pre2012 + "carpet-cutting+cc_base+mzn_rnd_test.10.fzn", 81, 4520, 0, false},
                {pre2012 + "carpet-cutting+cc_base+mzn_rnd_test.14.fzn", 10, 1982, 0, false},
                {pre2012 + "carpet-cutting+cc_base+mzn_rnd_test.16.fzn", 37, 1355, 0, false},
                {pre2012 + "carpet-cutting+cc_base+mzn_rnd_test.17.fzn", 14, 2216, 0, false},
                {pre2012 + "fast-food+fastfood+ff58.fzn", 187, 1178, 0, false},
                {pre2012 + "league+league+model20-3-5.fzn", 8, 59985, 0, false},
                {pre2012 + "league+league+model30-4-6.fzn", 3, 149977, 0, false},
                {pre2012 + "league+league+model50-4-4.fzn", 3, 159976, 0, false},
                {pre2012 + "league+league+model55-3-12.fzn", 4, 159958, 0, false},
                {pre2012 + "league+league+model90-18-20.fzn", 7, 2089930, 0, false},
                {pre2012 + "league+league+model100-21-12.fzn", 3, 3139922, 0, false},
                {pre2012 + "mspsp+mspsp+hard_03.fzn", 5, 30, 0, false},
                {pre2012 + "parity-learning+parity-learning+48_24_6.2.fzn", 1, 5, 0, false},
                {pre2012 + "parity-learning+parity-learning+52_26_6.1.fzn", 1, 6, 0, false},
                {pre2012 + "project-planning+ProjectPlannertest_12_8.fzn", 1, 63, 0, false},
                {pre2012 + "project-planning+ProjectPlannertest_13_6.fzn", 4, 58, 0, false},
                {pre2012 + "project-planning+ProjectPlannertest_13_8.fzn", 1, 71, 0, false},
                {pre2012 + "project-planning+ProjectPlannertest_14_8.fzn", 2, 78, 0, false},
                {pre2012 + "project-planning+ProjectPlannertest_16_7.fzn", 22, 39, 0, false},
                {pre2012 + "project-planning+ProjectPlannertest_17_9.fzn", 1, 100, 0, false},
                {pre2012 + "ship-schedule+ship-schedule.cp+7ShipsMixed.fzn", 202, 39245, 0, false},
                {pre2012 + "ship-schedule+ship-schedule.cp+7ShipsMixedUnconst.fzn", 44, 384780, 0, false},
                {pre2012 + "ship-schedule+ship-schedule.cp+8ShipsUnconst.fzn", 95, 832170, 0, false},
                {pre2012 + "still-life-wastage+still-life+11.fzn", 6, 64, 0, false},
                {pre2012 + "still-life-wastage+still-life+12.fzn", 12, 75, 0, false},
                {pre2012 + "still-life-wastage+still-life+13.fzn", 7, 86, 0, false},
                {pre2012 + "tpp+tpp+tpp_3_3_30_1.fzn", 112, 225, 0, false},
                {pre2012 + "tpp+tpp+tpp_7_3_20_1.fzn", 58, 128, 0, false},
                {pre2012 + "tpp+tpp+tpp_7_5_30_1.fzn", 83, 203, 0, false},
                {pre2012 + "train+train+instance.2.fzn", 1, 72920, 0, false},
                {pre2012 + "train+train+instance.3.fzn", 4, 73320, 0, false},
                {pre2012 + "train+train+instance.6.fzn", 4, 32120, 0, false},
                {pre2012 + "train+train+instance.7.fzn", 2, 73420, 0, false},
                {pre2012 + "train+train+instance.15.fzn", 6, 84870, 0, false},
                {pre2012 + "vrp+vrp+A-n38-k5.vrp.fzn", 22, 2745, 0, false},
                {pre2012 + "vrp+vrp+A-n62-k8.vrp.fzn", 54, 6699, 0, false},
                {pre2012 + "vrp+vrp+B-n51-k7.vrp.fzn", 76, 4987, 0, false},
                {pre2012 + "vrp+vrp+P-n20-k2.vrp.fzn", 45, 706, 0, false},
                {pre2012 + "vrp+vrp+P-n60-k15.vrp.fzn", 33, 2621, 0, false}
        };
    }


}
