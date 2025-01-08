/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/06/2023
 */
public class Providers {
    @DataProvider
    public static Object[][] trueOrFalse() {
        List<Object[]> args = new ArrayList<>();
        args.add(new Object[]{true});
        args.add(new Object[]{false});
        return args.toArray(new Object[0][0]);
    }
}
