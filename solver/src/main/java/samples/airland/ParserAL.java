/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package samples.airland;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class ParserAL {

    private String groupSeparator = "\\,";
    private String decimalSeparator = "\\.";
    private String non0Digit = "[\\p{javaDigit}&&[^0]]";

    private Pattern decimalPattern;

    public int[][] parse(String fileName) {
        int[][] data = null;
        try {
            buildFloatAndDecimalPattern();
            Scanner sc = new Scanner(new File(fileName));
            int nb = sc.nextInt();
            data = new int[nb][6 + nb];
            sc.nextLine();
            for (int i = 0; i < nb; i++) {
                data[i][0] = sc.nextInt(); // appearance time
                data[i][1] = sc.nextInt(); // earliest landing time
                data[i][2] = sc.nextInt(); // target landing time
                data[i][3] = sc.nextInt(); // latest landing time
                Double tt = Double.parseDouble(sc.next(decimalPattern));
                data[i][4] = (int) Math.ceil(tt); // penalty cost per unit of time for landing before target
                tt = Double.parseDouble(sc.next(decimalPattern));
                data[i][5] = (int) Math.ceil(tt); // penalty cost per unit of time for landing after target
                for (int j = 0; j < nb; j++) {
                    data[i][6 + j] = sc.nextInt();
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }

    private void buildFloatAndDecimalPattern() {
        // \\p{javaDigit} may not be perfect, see above
        String digit = "([0-9])";
        String groupedNumeral = "(" + non0Digit + digit + "?" + digit + "?(" +
                groupSeparator + digit + digit + digit + ")+)";
        // Once again digit++ is used for performance, as above
        String numeral = "((" + digit + "++)|" + groupedNumeral + ")";
        String decimalNumeral = "(" + numeral + "|" + numeral +
                decimalSeparator + digit + "*+|" + decimalSeparator +
                digit + "++)";
        String decimal = "([-+]?" + decimalNumeral +")";
        decimalPattern = Pattern.compile(decimal);
    }
}
