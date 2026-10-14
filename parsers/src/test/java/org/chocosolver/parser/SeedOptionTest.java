/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

import org.chocosolver.parser.xcsp.XCSP;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;

/**
 * Tests that the -seed option of the parsers reaches the models (#1207).
 *
 * @author Charles Prud'homme
 * @since 14/10/2026
 */
public class SeedOptionTest {

    private static long seedOf(String... options) throws Exception {
        File file = File.createTempFile("xcsp", ".xml");
        file.deleteOnExit();
        Files.writeString(file.toPath(), "<instance format=\"XCSP3\" type=\"CSP\">\n"
                + "<variables><var id=\"x\"> 0..2 </var></variables>\n"
                + "<constraints><intension> ne(x,1) </intension></constraints>\n"
                + "</instance>\n");
        String[] args = new String[options.length + 5];
        args[0] = file.getAbsolutePath();
        args[1] = "-p";
        args[2] = "1";
        args[3] = "--disable-shutdown-hook";
        args[4] = "true";
        System.arraycopy(options, 0, args, 5, options.length);
        XCSP xcsp = new XCSP();
        Assert.assertTrue(xcsp.setUp(args));
        xcsp.createSolver();
        return xcsp.getModel().getSeed();
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDefaultSeed() throws Exception {
        Assert.assertEquals(seedOf(), 0L);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSeedOption() throws Exception {
        Assert.assertEquals(seedOf("-seed", "42"), 42L);
    }
}
