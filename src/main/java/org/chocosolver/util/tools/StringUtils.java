/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
