/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Settings;
import org.testng.annotations.DataProvider;

/**
 * @author Alexandre LEBRUN
 */
public class TestData {


    private static Settings buildSettings(final boolean withViews) {
        return Settings.init().setEnableViews(withViews);
    }

    @DataProvider(name = "boundsAndViews")
    public static Object[][] boundsAndViews() {
        Settings withViews = buildSettings(true);
        Settings withoutViews = buildSettings(false);
        return new Object[][]{
                new Object[]{true, withViews},
                new Object[]{true, withoutViews},
                new Object[]{false, withViews},
                new Object[]{false, withoutViews}
        };
    }


}
