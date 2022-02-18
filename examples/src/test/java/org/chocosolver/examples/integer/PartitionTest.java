/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.parser.SetUpException;
import org.chocosolver.solver.Model;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/03/11
 */
public class PartitionTest {

    protected Model modeler(int size) throws SetUpException {
        Partition pb;
        pb = new Partition();
        pb.setUp("-n", Integer.toString(size));
        pb.buildModel();
        pb.configureSearch();
        return pb.getModel();
    }

    @Test(groups="5m", timeOut=300000)
    public void test4to14() throws SetUpException {
        int[] size = {8, 12, 16, 20, 24, 28};
        int[] sols = {1, 1, 7, 24, 296, 1443};
//        int[] nodes = {3, 22, 189, 1739, 17889, 189944};

        for (int i = 0; i < size.length; i++) {
            Model sol = modeler(size[i]);
            while (sol.getSolver().solve()) ;
            assertEquals(sol.getSolver().getSolutionCount(), sols[i]);
//            Assert.assertEquals(sol.getResolver().getMeasures().getNodeCount(), nodes[i]);
        }
    }

    @Test(groups="5m", timeOut=300000)
    public void test16to32() throws SetUpException {
        int[] size = {32, 36, 40, 44, 48, 52, 56, 60, 64};
        int[] sols = {1, 1, 1, 1, 1, 1, 1, 1, 1};
//        int[] nodes = {633, 760, 2250, 6331, 19832, 19592, 60477, 139296, 180302};

        for (int i = 0; i < size.length; i++) {
            Model sol = modeler(size[i]);
            sol.getSolver().solve();
            Assert.assertEquals(sol.getSolver().getSolutionCount(), sols[i]);
//            Assert.assertEquals(sol.getResolver().getMeasures().getNodeCount(), nodes[i]);
        }
    }

}
