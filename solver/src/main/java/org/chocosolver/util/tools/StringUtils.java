/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.tools;

import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;

/*
 * User : charles
 * Mail : cprudhom(a)emn.fr
 * Date : 3 juil. 2009
 * Since : Choco 2.1.0
 * Update : Choco 2.1.0
 *
 * Provides some short and usefull methods to deal with String object
 * and pretty print of IPretty objects.
 *
 */
public class StringUtils {


    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BOLD = "\u001B[1m";

    private StringUtils() {
    }

    /**
     * Pads out a string upto padlen with pad chars
     *
     * @param str    string to be padded
     * @param padlen length of pad (+ve = pad on right, -ve pad on left)
     * @param pad    character
     * @return padded string
     */
    public static String pad(String str, int padlen, String pad) {
        final StringBuilder padding = new StringBuilder(32);
        final int len = Math.abs(padlen) - str.length();
        if (len < 1) {
            return str;
        }
        for (int i = 0; i < len; ++i) {
            padding.append(pad);
        }
        return (padlen < 0 ? padding.append(str).toString() : padding.insert(0, str).toString());
    }

    /**
     * Convert a regexp formed with integer charachter into a char formed regexp
     * for instance, "12%12%" which stands for 1 followed by 2 followed by 12 would be misinterpreted by regular
     * regular expression parser. We use here the asci code to encode everything as a single char.
     * Due to char encoding limits, we cannot parse int greater than 2^16-1
     *
     * @param strRegExp a regexp of integer
     * @return a char regexp
     */
    public static String toCharExp(String strRegExp) {
        StringBuilder b = new StringBuilder(32);
        for (int i = 0; i < strRegExp.length(); i++) {
            char c = strRegExp.charAt(i);
            if (c == '<') {
                int out = strRegExp.indexOf('>', i + 1);
                int tmp = Integer.parseInt(strRegExp.substring(i + 1, out));
                b.append('\\').append(FiniteAutomaton.getCharFromInt(tmp));
                i = out;
            } else if (Character.isDigit(c)) {
                b.append(FiniteAutomaton.getCharFromInt(Character.getNumericValue(c)));

            } else if (c == '{') {
                int out = strRegExp.indexOf('}', i + 1);
                b.append(c);
                for (int d = i + 1; d <= out; d++)
                    b.append(strRegExp.charAt(d));
                i = out;
            } else {
                b.append(c);
            }
        }

        return b.toString();

    }
}
