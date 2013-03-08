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

package samples.nsp;

import java.io.*;
import java.util.ArrayList;

/**
 * @author Tanguy Lapegue
 */
public class NSParser {

    public static String[] titles;
    public static ArrayList<String[]> data = new ArrayList<String[]>();
    public static final String NB_DAYS = "nb_days";
    public static final String NB_EMPLOYEES = "nb_employees";
    public static final String NB_FULL_TIME = "nb_full_time";
    public static final String WORK = "work";
    public static final String SYMETRIC = "symetric_groups";
    public static final String ASSIGNMENTS = "assignments";

    public static final String SLIDING_P = "sliding_forbidden_pattern";
    public static final String FIXED_P = "fixed_forbidden_pattern";

    public static final String F_HORIZON = "full_time_horizon";
    public static final String F_WEEK = "full_time_week";
    public static final String F_WORK_SPAN = "full_time_work_span";

    public static final String P_HORIZON = "part_time_horizon";
    public static final String P_WEEK = "part_time_week";
    public static final String P_WORK_SPAN = "part_time_work_span";

    public static final String COVER = "cover";

    //READING

    /**
     * read the data_version.csv file and fill the data list
     */
    public static void read(int version) {
        data.removeAll(data);
        File dataToRead = new File("/Users/cprudhom/Library/Mail Downloads/TPchoco/data/Tanguy/data_" + version + ".csv");
        try {
            BufferedReader aLire = new BufferedReader(new FileReader(dataToRead));
            // The first line is the title
            titles = aLire.readLine().split(";");

            String line = aLire.readLine();
            while (line != null) {
                data.add(line.split(";"));
                line = aLire.readLine();
            }
            aLire.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // SEARCHING

    /**
     * Return a String[] from data[i] where i is the index of name
     * in the titles list
     */
    public static String[] searchStringTab(String name) {
        ArrayList<String> list = new ArrayList<String>();

        for (int i = 0; i < titles.length; i++) {
            if (titles[i].equals(name)) {
                for (int j = 0; j < data.size(); j++) {
                    String info = data.get(j)[i];
                    if (info.equals("")) {
                        break;
                    } else {
                        list.add(info);
                    }
                }
            }
        }
        String[] tab = new String[list.size()];
        for (int j = 0; j < list.size(); j++) {
            tab[j] = list.get(j);
        }
        return tab;
    }

    /**
     * Return an int from data[i] where i is the index of name
     * in the titles list
     */
    public static int searchInteger(String name) {
        for (int i = 0; i < titles.length; i++) {
            if (titles[i].equals(name)) {
                return Integer.parseInt(data.get(0)[i]);
            }
        }
        return 0;
    }

    // METHODE

    /**
     * Split a String at each "_" into an int[]
     */
    public static int[] splitIntoInt(String line) {
        String[] tab = line.split("_");
        int[] result = new int[tab.length];

        for (int i = 0; i < tab.length; i++) {
            result[i] = Integer.parseInt(tab[i]);
        }
        return result;
    }

    // WRITTING

    /**
     * Save the results of the run into the results.csv file
     */
    public static void saveResults(String texte, String pb) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(System.getProperty("user.dir") + "/results" + pb + ".csv");
            writer.write(texte);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
