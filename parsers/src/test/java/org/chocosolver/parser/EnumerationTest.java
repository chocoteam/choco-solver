/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

import org.chocosolver.parser.flatzinc.Flatzinc;
import org.chocosolver.parser.xcsp.XCSP;
import org.chocosolver.solver.search.SearchState;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

/**
 * Checks that the enumeration of all the solutions of a satisfaction problem terminates,
 * whatever the search options are.
 *
 * @author Charles Prud'homme
 * @since 12/10/2026
 */
public class EnumerationTest {

    @DataProvider(name = "options")
    public Object[][] options() {
        return new Object[][]{
                {new String[]{"-a"}},
                {new String[]{"-a", "-f"}},
                {new String[]{"-a", "-lcg"}},
                {new String[]{"-a", "-f", "-lcg"}},
        };
    }

    private static String[] args(String file, String[] options) {
        String[] args = new String[]{file, "--disable-shutdown-hook", "-limit", "[20s]", "-lvl", "SILENT", "-p", "1"};
        String[] all = new String[args.length + options.length];
        System.arraycopy(args, 0, all, 0, args.length);
        System.arraycopy(options, 0, all, args.length, options.length);
        return all;
    }

    @Test(groups = "xcsp", dataProvider = "options", timeOut = 60000)
    public void testXCSP(String[] options) throws Exception {
        // x % 3 = y has 7 solutions
        File file = File.createTempFile("xcsp", ".xml");
        file.deleteOnExit();
        Files.writeString(file.toPath(), """
                <instance format="XCSP3" type="CSP">
                  <variables>
                    <var id="x"> 0..6 </var>
                    <var id="y"> 0..6 </var>
                  </variables>
                  <constraints>
                    <intension> eq(mod(x,3),y) </intension>
                  </constraints>
                </instance>
                """);
        XCSP xcsp = new XCSP();
        xcsp.setUp(args(file.getAbsolutePath(), options));
        xcsp.createSolver();
        xcsp.buildModel();
        xcsp.configureSearch();
        xcsp.solve();
        Assert.assertEquals(xcsp.getModel().getSolver().getSearchState(), SearchState.TERMINATED, "Unexpected search state");
        Assert.assertEquals(xcsp.getModel().getSolver().getSolutionCount(), 7, "Unexpected number of solutions");
    }

    @Test(groups = "mzn", dataProvider = "options", timeOut = 60000)
    public void testFlatzinc(String[] options) throws Exception {
        // this problem has a unique solution
        String file = Objects.requireNonNull(this.getClass().getResource("/flatzinc/alpha/alpha.fzn")).getFile();
        Flatzinc fzn = new Flatzinc();
        fzn.setUp(args(file, options));
        fzn.createSolver();
        fzn.buildModel();
        fzn.configureSearch();
        fzn.solve();
        Assert.assertEquals(fzn.getModel().getSolver().getSearchState(), SearchState.TERMINATED, "Unexpected search state");
        Assert.assertEquals(fzn.getModel().getSolver().getSolutionCount(), 1, "Unexpected number of solutions");
    }
}
