/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.flatzinc;

import org.chocosolver.parser.SetUpException;
import org.chocosolver.parser.flatzinc.Flatzinc;
import org.chocosolver.solver.search.SearchState;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/09/2020
 */
//@Listeners(PerformanceListener.class)
public class PerformanceTest {
    private static final String ROOT = "/flatzinc/";
    private static final String COMMENT = "#";
    private static final String DELIMITER = ",";

    private static final String LEVEL = "JSON";
    

    public Object[][] getInstances(String name) {
        List<Object[]> parameters = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(
                Paths.get(Objects.requireNonNull(this.getClass().getResource(
                        ROOT + name)).getPath()))) {
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
                        "_".equals(columns[3]) ? null : Integer.parseInt(columns[3]), // best
                        Integer.parseInt(columns[4]), // nodes
                        Integer.parseInt(columns[5]) // failures
                });
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return parameters.toArray(new Object[0][0]);
    }

    @DataProvider()
    public Object[][] instances() {
        return getInstances("instances.csv");
    }

    @Test(groups = "mzn", dataProvider = "instances", timeOut = 60_000, priority = 2)
    public void testThemAll(String path, int solutions, Integer bst, int nodes, int failures) throws SetUpException {
        String file = Objects.requireNonNull(this.getClass().getResource(path)).getFile();
        String[] args = new String[]{
                file,
                "--disable-shutdown-hook",
                "-limit", "[50s]", // but, problems are expected to end within 15s max
                "-lvl", LEVEL,
                "-p", "1",
        };
        Flatzinc fzn = new Flatzinc();
        fzn.setUp(args);
        fzn.createSolver();
        fzn.buildModel();
        fzn.configureSearch();
        //fzn.getModel().displayVariableOccurrences();
        //fzn.getModel().displayPropagatorOccurrences();
        fzn.solve();
        Assert.assertEquals(fzn.getModel().getSolver().getSearchState(), SearchState.TERMINATED, "Unexpected search state");
        if (bst != null) {
            Assert.assertEquals(fzn.getModel().getSolver().getObjectiveManager().getBestSolutionValue(), bst, "Unexpected best solution");
        } else {
            Assert.assertEquals(fzn.getModel().getSolver().getSolutionCount(), solutions, "Unexpected number of solutions");
        }
        Assert.assertEquals(fzn.getModel().getSolver().getSolutionCount(), solutions, "Unexpected number of solutions");
        Assert.assertEquals(fzn.getModel().getSolver().getNodeCount(), nodes, "Unexpected number of nodes");
        Assert.assertEquals(fzn.getModel().getSolver().getFailCount(), failures, "Unexpected number of failures");
    }

    @DataProvider()
    public Object[][] hardInstances() {
        return getInstances("hard_instances.csv");
    }

    @Test(groups = "mzn", dataProvider = "hardInstances", timeOut = 120_000, priority = 2)
    public void testHardInstances(String path, int solutions, Integer bst, int nodes, int failures) throws SetUpException {
        String file = Objects.requireNonNull(this.getClass().getResource(path)).getFile();
        String[] args = new String[]{
                file,
                "--disable-shutdown-hook",
                "-limit", "[110s]", // but, problems are expected to end within 30s max
                "-lvl", LEVEL,
                "-p", "1"
        };
        Flatzinc fzn = new Flatzinc();
        fzn.setUp(args);
        fzn.getSettings().setLCG(true).setWarnUser(false);
        fzn.createSolver();
        fzn.buildModel();
        fzn.configureSearch();
        //fzn.getModel().displayVariableOccurrences();
        //fzn.getModel().displayPropagatorOccurrences();
        fzn.solve();
        Assert.assertEquals(fzn.getModel().getSolver().getSearchState(), SearchState.TERMINATED, "Unexpected search state");
        if (bst != null) {
            Assert.assertEquals(fzn.getModel().getSolver().getObjectiveManager().getBestSolutionValue(), bst, "Unexpected best solution");
        }
        Assert.assertEquals(fzn.getModel().getSolver().getSolutionCount(), solutions, "Unexpected number of solutions");
        Assert.assertEquals(fzn.getModel().getSolver().getNodeCount(), nodes, "Unexpected number of nodes");
        Assert.assertEquals(fzn.getModel().getSolver().getFailCount(), failures, "Unexpected number of failures");
    }


    @Test(groups = "mzn", timeOut = 240_000, priority = 2)
    public void testCellda_y_10s() throws SetUpException {
        // Specific to bnn+cellda_y_10s which take more time on Travis
        // 2020,bnn+cellda_y_10s.fzn,1,6,16582,16581
        String file = Objects.requireNonNull(this.getClass().getResource("/flatzinc/2020/bnn+cellda_y_10s.fzn")).getFile();
        String[] args = new String[]{
                file,
                "--disable-shutdown-hook",
                "-limit", "[180s]", // but, problems are expected to end within 21s max
                "-lvl", LEVEL,
                "-p", "1"
        };
        Flatzinc fzn = new Flatzinc();
        fzn.setUp(args);
        //fzn.getSettings().setLCG(true).setWarnUser(true);
        fzn.createSolver();
        fzn.buildModel();
        fzn.configureSearch();
        //fzn.getModel().displayVariableOccurrences();
        //fzn.getModel().displayPropagatorOccurrences();
        fzn.solve();
        Assert.assertEquals(fzn.getModel().getSolver().getSearchState(), SearchState.TERMINATED, "Unexpected search state");
        Assert.assertEquals(fzn.getModel().getSolver().getSolutionCount(), 1, "Unexpected number of solutions");
        Assert.assertEquals(fzn.getModel().getSolver().getNodeCount(), 16582, "Unexpected number of nodes");
        Assert.assertEquals(fzn.getModel().getSolver().getFailCount(), 16581, "Unexpected number of failures");
        Assert.assertEquals(fzn.getModel().getSolver().getObjectiveManager().getBestSolutionValue(), 6, "Unexpected best solution");
    }

    @Test(groups = "mzn", timeOut = 240_000, priority = 2)
    public void test_is_A3PZaPjnUz() throws SetUpException {
        // Specific to is_A3PZaPjnUz which is faster when implication is enabled
        String file = Objects.requireNonNull(this.getClass().getResource("/flatzinc/2020/is+A3PZaPjnUz_new.fzn")).getFile();
        String[] args = new String[]{
                file,
                "--disable-shutdown-hook",
                "-limit", "[180s]", // but, problems are expected to end within 33s max
                "-lvl", LEVEL,
                "-p", "1"
        };
        Flatzinc fzn = new Flatzinc();
        fzn.setUp(args);
        //fzn.getSettings().setLCG(true).setWarnUser(true);
        fzn.createSolver();
        fzn.buildModel();
        fzn.configureSearch();
        //fzn.getModel().displayVariableOccurrences();
        //fzn.getModel().displayPropagatorOccurrences();
        fzn.solve();
        Assert.assertEquals(fzn.getModel().getSolver().getSearchState(), SearchState.TERMINATED, "Unexpected search state");
        Assert.assertEquals(fzn.getModel().getSolver().getObjectiveManager().getBestSolutionValue(), 103_936, "Unexpected best solution");
        Assert.assertEquals(fzn.getModel().getSolver().getNodeCount(), 2_164_075, "Unexpected number of nodes");
        Assert.assertEquals(fzn.getModel().getSolver().getFailCount(), 2_164_010, "Unexpected number of failures");
        Assert.assertEquals(fzn.getModel().getSolver().getSolutionCount(), 33, "Unexpected number of solutions");
    }

    @Test(groups = "mzn", timeOut = 240_000, priority = 2)
    public void test_lot_sizing_cp_pigment15b() throws SetUpException {
        // Specific to lot_sizing_cp_pigment15b which takes less time when element+(fast = adaptive)
        String file = Objects.requireNonNull(this.getClass().getResource("/flatzinc/2020/lot_sizing_cp+pigment15b.psp.fzn")).getFile();
        String[] args = new String[]{
                file,
                "--disable-shutdown-hook",
                "-limit", "[180s]", // but, problems are expected to end within 40s max
                "-lvl", LEVEL,
                "-p", "1"
        };
        Flatzinc fzn = new Flatzinc();
        fzn.setUp(args);
        //fzn.getSettings().setLCG(true).setWarnUser(true);
        fzn.createSolver();
        fzn.buildModel();
        fzn.configureSearch();
        //fzn.getModel().displayVariableOccurrences();
        //fzn.getModel().displayPropagatorOccurrences();
        fzn.solve();
        Assert.assertEquals(fzn.getModel().getSolver().getSearchState(), SearchState.TERMINATED, "Unexpected search state");
        Assert.assertEquals(fzn.getModel().getSolver().getObjectiveManager().getBestSolutionValue(), 1123, "Unexpected best solution");
        Assert.assertEquals(fzn.getModel().getSolver().getSolutionCount(), 35, "Unexpected number of solutions");
        Assert.assertEquals(fzn.getModel().getSolver().getNodeCount(), 822_035, "Unexpected number of nodes");
        Assert.assertEquals(fzn.getModel().getSolver().getFailCount(), 821_966, "Unexpected number of failures");
    }

    @Test(groups = "mzn", timeOut = 60_000, priority = 1)
    public void test_steiner_tree_es10fst03() throws SetUpException {
        // Specific to 2018/steiner-tree+es10fst03.stp.fzn for which the search MUST be complet
        String file = Objects.requireNonNull(this.getClass().getResource("/flatzinc/2018/steiner-tree+es10fst03.stp.fzn")).getFile();
        String[] args = new String[]{
                file,
                "--disable-shutdown-hook",
                "-limit", "[50s]",
                "-lvl", LEVEL,
                "-p", "1",
                "-ocs", "ALL" // required for this problem, otherwise the solution is not correct
        };
        Flatzinc fzn = new Flatzinc();
        fzn.setUp(args);
        //fzn.getSettings().setLCG(true).setWarnUser(true);
        fzn.createSolver();
        fzn.buildModel();
        fzn.configureSearch();
        //fzn.getModel().displayVariableOccurrences();
        //fzn.getModel().displayPropagatorOccurrences();
        fzn.solve();
        Assert.assertEquals(fzn.getModel().getSolver().getSearchState(), SearchState.TERMINATED, "Unexpected search state");
        Assert.assertEquals(fzn.getModel().getSolver().getObjectiveManager().getBestSolutionValue(), 26003678, "Unexpected best solution");
        Assert.assertEquals(fzn.getModel().getSolver().getSolutionCount(), 2, "Unexpected number of solutions");
        Assert.assertEquals(fzn.getModel().getSolver().getNodeCount(), 90_150, "Unexpected number of nodes");
        Assert.assertEquals(fzn.getModel().getSolver().getFailCount(), 90_147, "Unexpected number of failures");
    }

    @Test(groups = "mzn", timeOut = 60_000, priority = 1)
    public void test_arithmetic_target_814() throws SetUpException {
        // Specific to 2022/arithmetic-target_814.fzn for modulo is extensively used and XplusYeqZ needs accurate isEntailed to be reified
        String file = Objects.requireNonNull(this.getClass().getResource("/flatzinc/2022/arithmetic-target_814.fzn")).getFile();
        String[] args = new String[]{
                file,
                "--disable-shutdown-hook",
                "-limit", "[50s]",
                "-lvl", LEVEL,
                "-p", "1"
        };
        Flatzinc fzn = new Flatzinc();
        fzn.setUp(args);
        //fzn.getSettings().setLCG(true).setWarnUser(true);
        fzn.createSolver();
        fzn.buildModel();
        fzn.configureSearch();
        //fzn.getModel().displayVariableOccurrences();
        //fzn.getModel().displayPropagatorOccurrences();
        fzn.getModel().getSolver().limitSolution(22);
        fzn.solve();
        Assert.assertEquals(fzn.getModel().getSolver().getSearchState(), SearchState.STOPPED, "Unexpected search state");
        Assert.assertEquals(fzn.getModel().getSolver().getObjectiveManager().getBestSolutionValue(), 7422, "Unexpected best solution");
        Assert.assertEquals(fzn.getModel().getSolver().getSolutionCount(), 22, "Unexpected number of solutions");
        Assert.assertEquals(fzn.getModel().getSolver().getNodeCount(), 313, "Unexpected number of nodes");
        Assert.assertEquals(fzn.getModel().getSolver().getFailCount(), 257, "Unexpected number of failures");
    }

    @DataProvider()
    public Object[][] lcg() {
        return getInstances("lcg.csv");
    }

    @Test(groups = "mzn", dataProvider = "lcg", timeOut = 60_000, priority = 2)
    public void testLCG(String path, int solutions, Integer bst, int nodes, int failures) throws SetUpException {
        String file = Objects.requireNonNull(this.getClass().getResource(path)).getFile();
        String[] args = new String[]{
                file,
                "--disable-shutdown-hook",
                "-limit", "[50s]", // but, problems are expected to end within 15s max
                "-lvl", LEVEL,
                "-p", "1",
        };
        Flatzinc fzn = new Flatzinc();
        fzn.setUp(args);
        fzn.getSettings().setLCG(true).setWarnUser(false);
        fzn.createSolver();
        fzn.buildModel();
        fzn.configureSearch();
        //fzn.getModel().displayVariableOccurrences();
        //fzn.getModel().displayPropagatorOccurrences();
        fzn.solve();
        Assert.assertEquals(fzn.getModel().getSolver().getSearchState(), SearchState.TERMINATED, "Unexpected search state");
        if (bst != null) {
            Assert.assertEquals(fzn.getModel().getSolver().getObjectiveManager().getBestSolutionValue(), bst, "Unexpected best solution");
        } else {
            Assert.assertEquals(fzn.getModel().getSolver().getSolutionCount(), solutions, "Unexpected number of solutions");
        }
        Assert.assertEquals(fzn.getModel().getSolver().getSolutionCount(), solutions, "Unexpected number of solutions");
        Assert.assertEquals(fzn.getModel().getSolver().getNodeCount(), nodes, "Unexpected number of nodes");
        Assert.assertEquals(fzn.getModel().getSolver().getFailCount(), failures, "Unexpected number of failures");
    }
}
