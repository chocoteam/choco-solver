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

package samples.knapsack;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27 juil. 2010
 */
public class ParseurKS {


    public static int[][] instances;
    public static int[] bounds;


    public static void parseFile(String filename, int n) throws IOException {

        ArrayList<Integer> weight = new ArrayList<Integer>(16);
        ArrayList<Integer> profit = new ArrayList<Integer>(16);
        int min = 0;
        int max = 0;

        FileReader f = new FileReader(filename);
        BufferedReader r = new BufferedReader(f);

        String line;

        Pattern wPattern = Pattern.compile(" *poids.*\\+(\\d+)");
        Pattern pPattern = Pattern.compile(" *calorie.*\\+(\\d+)");
        Pattern cmaxPattern = Pattern.compile(" *cmax.*\\+(\\d+)");
        Pattern cminPattern = Pattern.compile(" *cmin.*\\+(\\d+)");


        while ((line = r.readLine()) != null) {

            Matcher mw = wPattern.matcher(line);
            Matcher mp = pPattern.matcher(line);
            Matcher mc1 = cmaxPattern.matcher(line);
            Matcher mc2 = cminPattern.matcher(line);

            if (mw.matches()) {
                weight.add(Integer.parseInt(mw.group(1)));
            } else if (mp.matches()) {
                profit.add(Integer.parseInt(mp.group(1)));
            } else if (mc1.matches()) {
                max = Integer.parseInt(mc1.group(1));
            } else if (mc2.matches()) {
                min = Integer.parseInt(mc2.group(1));
            }


        }
        if(n < 0){
            n = weight.size();
        }
        int[] weights = new int[n];
        int[] profits = new int[n];

        for (int i = 0; i < n; i++) {
            weights[i] = weight.get(i);
            profits[i] = profit.get(i);

        }
        instances = new int[][]{profits, weights};
        bounds  = new int[]{min, max};

    }


}
