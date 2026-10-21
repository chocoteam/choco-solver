/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <p> Project: choco-parsers.
 *
 * @author Charles Prud'homme
 * @since 21/10/2026.
 */
public class MiniZincInstallerTest {

    @Test(groups = "1s")
    public void testInstall() throws Exception {
        Path classes = Paths.get(MiniZincInstaller.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path solvers = Files.createTempDirectory("solvers");
        Path msc = MiniZincInstaller.install(classes, solvers);

        String content = new String(Files.readAllBytes(msc), StandardCharsets.UTF_8);
        Path home = msc.resolveSibling(msc.getFileName().toString().replace(".msc", ""));
        Assert.assertTrue(content.contains("\"mznlib\": \"" + home.resolve("mzn_lib")), content);
        Assert.assertTrue(content.contains("\"executable\": \"" + home.resolve("fzn-choco")), content);
        Assert.assertFalse(Files.exists(home.resolve("choco.msc")));
        Assert.assertTrue(Files.isRegularFile(home.resolve("mzn_lib").resolve("redefinitions.mzn")));

        String py = new String(Files.readAllBytes(home.resolve("fzn-choco.py")), StandardCharsets.UTF_8);
        Assert.assertTrue(py.contains("JAR_FILE=r'" + classes.toAbsolutePath() + "'"), py);
    }
}
