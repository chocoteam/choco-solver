/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.dimacs;

import org.chocosolver.parser.SetUpException;
import org.chocosolver.parser.dimacs.DIMACS;
import org.chocosolver.solver.search.SearchState;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * <p> Project: choco-parsers.
 *
 * @author Charles Prud'homme
 * @since 04/03/2021.
 */
public class ParserTest {

    private static final String ROOT = "/dimacs/";
    private static final String COMMENT = "#";
    private static final String DELIMITER = ",";

    @Test(groups = "1s", timeOut = 60000)
    public void test1() throws Exception {
        ClassLoader cl = this.getClass().getClassLoader();
        run("/dimacs/simple_v3_c2.cnf", true, 3, 0);
    }

    @DataProvider()
    public Object[][] small() {
        List<Object[]> parameters = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(
                Paths.get(this.getClass().getResource(
                        ROOT + "instances.csv").getPath()))) {
            // read the file line by line
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(COMMENT))
                    continue;
                // convert line into columns
                String[] columns = line.split(DELIMITER);
                parameters.add(new Object[]{
                        ROOT + columns[0], // path
                        Boolean.parseBoolean(columns[1]), // sat
                        Integer.parseInt(columns[2]), // nodes
                        Integer.parseInt(columns[3]) // failures
                });
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return parameters.toArray(new Object[0][0]);
    }

    @Test(groups = "dimacs", timeOut = 120000, dataProvider = "small")
    public void test2(String file, boolean sat, int nodes, int fails) throws SetUpException {
        run(file, sat, nodes, fails);
    }


    private void run(String path, boolean sat, int nodes, int fails) throws SetUpException {
        String file = this.getClass().getResource(path).getFile();
        List<String> args = new ArrayList<>();
        args.add(file);
        args.add("-limit");
        args.add("[60s]");
        args.add("-lvl");
        args.add("COMPET");
        args.add("-p");
        args.add("1");
        DIMACS dimacs = new DIMACS();
        dimacs.setUp(args.toArray(new String[0]));
        dimacs.createSolver();
        dimacs.buildModel();
        dimacs.configureSearch();
        dimacs.solve();
        Assert.assertEquals(dimacs.getModel().getSolver().getSearchState(), SearchState.TERMINATED, "Unexpected search state");
        Assert.assertEquals(dimacs.getModel().getSolver().getSolutionCount() > 0, sat, "Unexpected status");
        Assert.assertEquals(dimacs.getModel().getSolver().getNodeCount(), nodes, "Unexpected number of nodes");
        Assert.assertEquals(dimacs.getModel().getSolver().getFailCount(), fails, "Unexpected number of failures");
    }

}
