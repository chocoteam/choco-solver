/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.xscp;

import org.chocosolver.parser.SetUpException;
import org.chocosolver.parser.xcsp.XCSP;
import org.chocosolver.solver.search.SearchState;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/09/2020
 */
public class PerformanceTest {
    private static final String ROOT = "/xcsp/";
    private static final String COMMENT = "#";
    private static final String DELIMITER = ",";

    @DataProvider()
    public Object[][] instances() throws URISyntaxException, IOException {
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
                        ROOT + columns[0] + File.separator + columns[1], // path
                        Integer.parseInt(columns[2]), // solutions
                        Integer.getInteger(columns[3]), // best
                        Integer.parseInt(columns[4]), // nodes
                        Integer.parseInt(columns[5]) // failures
                });
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return parameters.toArray(new Object[0][0]);
    }

    @Test(groups = "xcsp", dataProvider = "instances", timeOut = 60000)
    public void testThemAll(String path, int solutions, Integer bst, int nodes, int failures) throws SetUpException {
        String file = this.getClass().getResource(path).getFile();
        String[] args = new String[]{
                file,
                "-limit", "[50s]", // but, problems are expected to end within 30s max
                "-lvl","COMPET",
                "-p", "1"
        };
        //RegParser.PRINT_LOG = false;
        XCSP xcsp = new XCSP();
        xcsp.setUp(args);
        xcsp.createSolver();
        xcsp.buildModel();
        //xcsp.getModel().displayVariableOccurrences();
        //xcsp.getModel().displayPropagatorOccurrences();
        xcsp.configureSearch();
        //xcsp.getModel().getSolver().showShortStatistics();
        xcsp.solve();
        /*System.out.println(path +
                "," + xcsp.getModel().getSolver().getSolutionCount() +
                "," + (xcsp.getModel().getSolver().getObjectiveManager().getPolicy() == ResolutionPolicy.SATISFACTION ? "_" :
                xcsp.getModel().getSolver().getObjectiveManager().getBestSolutionValue()) +
                "," + xcsp.getModel().getSolver().getNodeCount() +
                "," + xcsp.getModel().getSolver().getFailCount());*/
        Assert.assertEquals(xcsp.getModel().getSolver().getSearchState(), SearchState.TERMINATED, "Unexpected search state");
        Assert.assertEquals(xcsp.getModel().getSolver().getSolutionCount(), solutions, "Unexpected number of solutions");
        Assert.assertEquals(xcsp.getModel().getSolver().getNodeCount(), nodes, "Unexpected number of nodes");
        Assert.assertEquals(xcsp.getModel().getSolver().getFailCount(), failures, "Unexpected number of failures");
        if (bst != null) {
            Assert.assertEquals(xcsp.getModel().getSolver().getObjectiveManager().getBestSolutionValue(), bst, "Unexpected best solution");
        }
    }

}
