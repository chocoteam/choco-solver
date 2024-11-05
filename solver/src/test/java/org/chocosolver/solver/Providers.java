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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.ElementType.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/06/2023
 */
public class Providers {
    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @Target({METHOD, TYPE, CONSTRUCTOR})
    public @interface Arguments {
        String[] values() default {};
    }


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
        for (Object[][] data : datas) {
            result = merge(result, data);
        }
        return result;
    }

    @DataProvider
    public static Object[][] random(Method m) {
        String[] args = m.getAnnotation(Arguments.class).values();
        int from, to, step;
        switch (args.length) {
            case 2:
                from = Integer.parseInt(args[0]);
                to = Integer.parseInt(args[1]);
                step = 1;
                break;
            case 3:
                from = Integer.parseInt(args[0]);
                to = Integer.parseInt(args[1]);
                step = Integer.parseInt(args[2]);
                break;
            default:
            case 1:
                from = 0;
                to = Integer.parseInt(args[0]);
                step = 1;
                break;
        }
        List<Object[]> data = new ArrayList<>();
        for (int i = from; i < to; i += step) {
            data.add(new Object[]{i});
        }
        return data.toArray(new Object[0][0]);
    }

}
