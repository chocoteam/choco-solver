/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/03/2021
 */
public class SatTest {

    @Test(groups = "1s")
    public void test1() {
        MiniSat sat = new MiniSat();
        int a = sat.newVariable();
        int b = sat.newVariable();
        sat.addClause(MiniSat.makeLiteral(a), MiniSat.neg(MiniSat.makeLiteral(b)));
        Assert.assertEquals(sat.solve(), ESat.TRUE);
    }

    private static final String ROOT = "/dimacs/";
    private static final String COMMENT = "#";
    private static final String DELIMITER = ",";

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
                        Boolean.parseBoolean(columns[1]) // sat
                });
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return parameters.toArray(new Object[0][0]);
    }

    @Test(groups = "10s", timeOut = 1200000, dataProvider = "small")
    public void test2(String file, boolean sat) throws FileNotFoundException {
        run(file, sat);
    }


    private void run(String path, boolean sat) throws FileNotFoundException {
        String file = this.getClass().getResource(path).getFile();
        MiniSat solver = new MiniSat();
        solver.solve();
        solver.parse(file);
        ESat ret = solver.solve();
        Assert.assertEquals(ret, sat ? ESat.TRUE : ESat.FALSE, "Unexpected search state");
    }

}
