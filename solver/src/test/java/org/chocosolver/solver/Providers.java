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

import org.chocosolver.util.tools.ArrayUtils;
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

    public static Object[][] merge(Object[][] data1, Object[][] data2) {
        int totalRows = data1.length * data2.length;
        Object[][] result = new Object[totalRows][2];

        int resultRow = 0;
        for (Object[] row1 : data1) {
            for (Object[] row2 : data2) {
                result[resultRow++] = ArrayUtils.append(row1, row2);
            }
        }
        return result;
    }

    public static Object[][] merge(Object[][] data1, Object[][] data2, Object[][]... datas) {
        Object[][] result = merge(data1, data2);
        for(Object[][] data: datas){
            result = merge(result, data);
        }
        return result;
    }
}
