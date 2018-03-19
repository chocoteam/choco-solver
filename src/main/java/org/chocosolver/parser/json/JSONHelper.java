/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json;

import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.util.regex.Pattern;

/**
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 21/09/2017.
 */
public class JSONHelper {

    private JSONHelper() {
    }

    private static final Pattern p2 = Pattern.compile("\\.\\.");
    private static final Pattern p3 = Pattern.compile(",");
    /**
     * Prefix for variables' ID
     */
    private static final String ID_PREFIX = "#";

    public static String varId(int i) {
        return ID_PREFIX + i;
    }

    public static IntIterableRangeSet convert(String str) {
        IntIterableRangeSet set = new IntIterableRangeSet();
        String dom = str.substring(1, str.length() - 1);
        String[] ranges = dom.split(p3.pattern());
        for (String range1 : ranges) {
            String[] range = range1.split(p2.pattern());
            if (range.length == 1) {
                set.add(Integer.parseInt(range[0]));
            } else if (range.length == 2) {
                set.addBetween(Integer.parseInt(range[0]), Integer.parseInt(range[1]));
            } else {
                break;
            }
        }
        return set;
    }
}
