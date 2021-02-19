/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.mps;

import org.chocosolver.parser.mps.MPS;
import org.chocosolver.parser.SetUpException;
import org.chocosolver.solver.search.SearchState;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * <p> Project: choco-parsers.
 *
 * @author Charles Prud'homme
 * @since 24/01/2018.
 */
public class ParserTest {


    @Test(groups = "1s", timeOut = 60000)
    public void test1() throws Exception {
        ClassLoader cl = this.getClass().getClassLoader();
        String file = cl.getResource("mps/example1.mps").getFile();
        run(file);
    }

    @DataProvider(name = "small")
    public Object[][] mps() throws IOException {
        ClassLoader cl = this.getClass().getClassLoader();
        String folder = cl.getResource("mps").getFile();
        return Files.walk(Paths.get(folder))
                .filter(Files::isRegularFile)
                .map(f -> new Object[]{f.toString()})
                .toArray(Object[][]::new);

    }

    @Test(groups = "mps", timeOut = 120000, dataProvider = "small")
    public void test2(String file) throws IOException, SetUpException {
        run(file);
    }

    private void run(String file) throws SetUpException {
        String[] args = new String[]{
                file,
                "-limit", "[60s]",
                "-stat",
                "-prec", "1.0E-4D",
                "-ninf", "-999.D",
                "-pinf", "999.D",
                "-p", "1"
        };
        MPS mps = new MPS();
        mps.setUp(args);
        mps.createSolver();
        mps.buildModel();
        mps.configureSearch();
        mps.solve();
        Assert.assertTrue(mps.getModel().getSolver().getSearchState().equals(SearchState.TERMINATED)
                || mps.getModel().getSolver().getSolutionCount()>0);
    }

}
