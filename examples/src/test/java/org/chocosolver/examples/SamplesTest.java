/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples;

import org.chocosolver.examples.integer.*;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/09/11
 */
public class SamplesTest {

    AbstractProblem[] problems = {
            new BIBD(),
            new AllIntervalSeries(),
            new AirPlaneLanding(),
            new CarSequencing(),
            new Grocery(),
            new Knapsack(),
            new Langford(),
            new LatinSquare(),
            new MagicSquare(),
            new Nonogram(),
            new OrthoLatinSquare(),
            new Partition(),
            new SchurLemma(),
            new SocialGolfer(),
            new Sudoku(),
            new WarehouseLocation()
    };


    @Test(groups="5m", timeOut=300000)
    public void testAll() {
        for (AbstractProblem pb : problems) {
            pb.execute();
        }
    }
}
