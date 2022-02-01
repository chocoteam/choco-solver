/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.mps;

import org.chocosolver.parser.SetUpException;
import org.chocosolver.parser.mps.MPS;
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
 * @since 24/01/2018.
 */
public class ParserTest {

    private static final String ROOT = "/mps/";
    private static final String COMMENT = "#";
    private static final String DELIMITER = ",";

    @DataProvider(name = "small")
    public Object[][] mps() {
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
                        Integer.parseInt(columns[1]), // solutions
                        Double.parseDouble(columns[2]), // best
                        Integer.parseInt(columns[3]), // nodes
                        Integer.parseInt(columns[4]), // failures
                        Boolean.parseBoolean(columns[5]) // failures
                });
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return parameters.toArray(new Object[0][0]);

    }

    @Test(groups = "mps", timeOut = 120000, dataProvider = "small")
    public void test1(String path, int solutions, Double bst, int nodes, int failures, boolean comp) throws SetUpException {
        String file = this.getClass().getResource(path).getFile();
        String[] args = new String[]{
                file,
                "-limit", "[30s]",
                "-lvl","COMPET",
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
        if(comp){
            Assert.assertEquals(mps.getModel().getSolver().getSearchState(), SearchState.TERMINATED, "Unexpected search state");
            Assert.assertEquals(mps.getModel().getSolver().getNodeCount(), nodes, "Unexpected number of nodes");
            Assert.assertEquals(mps.getModel().getSolver().getFailCount(), failures, "Unexpected number of failures");
        }
        Assert.assertEquals(mps.getModel().getSolver().getSolutionCount(), solutions, "Unexpected number of solutions");
        if (mps.getModel().getSolver().getObjectiveManager().getBestSolutionValue() instanceof Integer) {
            Assert.assertEquals(mps.getModel().getSolver().getObjectiveManager().getBestSolutionValue(), bst.intValue(), "Unexpected best solution");
        } else {
            Assert.assertEquals(mps.getModel().getSolver().getObjectiveManager().getBestSolutionValue(), bst, "Unexpected best solution");
        }
    }

}
