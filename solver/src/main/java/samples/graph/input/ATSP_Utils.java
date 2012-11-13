/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 22/10/12
 * Time: 02:00
 */

package samples.graph.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;

/**
 * Parses and generates Asymmetric Traveling Salesman Problem instances
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class ATSP_Utils {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    public String instanceName;
    public int[][] distanceMatrix;
    public int n; // number of nodes
    public int noVal; // default value indicating the absence of arc
    public int optimum, initialUB;

    //***********************************************************************************
    // GENERATOR
    //***********************************************************************************

    public void generateInstance(int size, int maxCost, long seed) {
        instanceName = size + ";" + maxCost + ";" + seed;
        System.out.println("parsing instance " + instanceName + "...");
        n = size;
        Random rd = new Random(seed);
        double d;
        distanceMatrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                distanceMatrix[i][j] = rd.nextInt(maxCost + 1);
                d = distanceMatrix[i][j] / 10;
                distanceMatrix[j][i] = distanceMatrix[i][j] + (int) (d * rd.nextDouble());
            }
        }
        noVal = Integer.MAX_VALUE / 2;
        int maxVal = 0;
        for (int i = 0; i < n; i++) {
            distanceMatrix[i][n - 1] = distanceMatrix[i][0];
            distanceMatrix[n - 1][i] = noVal;
            distanceMatrix[i][0] = noVal;
            for (int j = 0; j < n; j++) {
                if (distanceMatrix[i][j] != noVal && distanceMatrix[i][j] > maxVal) {
                    maxVal = distanceMatrix[i][j];
                }
            }
        }
        initialUB = n * maxCost;
        optimum = -1;
    }

    public void generateWithSCCStructure(int n, int maxCost, int maxSCC, Random rd) {
        int[][] dist = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = rd.nextInt(maxCost);
            }
        }
        int[] sccs = generateSCCs(n, maxSCC, rd);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (sccs[i] > sccs[j]) {
                    dist[i][j] = maxCost;
                }
            }
        }
        distanceMatrix = dist;
        instanceName = n + ";" + maxCost + ";" + maxSCC;
        noVal = maxCost;
        initialUB = maxCost * n;
        optimum = -1;
        this.n = n;
    }

    private static int[] generateSCCs(int n, int max, Random rd) {
        int[] scc = new int[n];
        if (max == 0) return scc;
        scc[0] = 0;
        scc[n - 1] = max;
        for (int i = 1; i < n - 1; i++) {
            scc[i] = 1 + rd.nextInt(max);
        }
        return scc;
    }

    //***********************************************************************************
    // TSPLIB PARSER
    //***********************************************************************************

    public void loadTSPLIB(String url) {
        File file = new File(url);
        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line = buf.readLine();
            instanceName = line.split(":")[1].replaceAll(" ", "");
            System.out.println("parsing instance " + instanceName + "...");
            line = buf.readLine();
            line = buf.readLine();
            line = buf.readLine();
            n = Integer.parseInt(line.split(":")[1].replaceAll(" ", "")) + 1;
            distanceMatrix = new int[n][n];
            line = buf.readLine();
            line = buf.readLine();
            line = buf.readLine();
            String[] lineNumbers;
            for (int i = 0; i < n - 1; i++) {
                int nbSuccs = 0;
                while (nbSuccs < n - 1) {
                    line = buf.readLine();
                    line = line.replaceAll(" * ", " ");
                    lineNumbers = line.split(" ");
                    for (int j = 1; j < lineNumbers.length; j++) {
                        if (nbSuccs == n - 1) {
                            i++;
                            if (i == n - 1) break;
                            nbSuccs = 0;
                        }
                        distanceMatrix[i][nbSuccs] = Integer.parseInt(lineNumbers[j]);
                        nbSuccs++;
                    }
                }
            }
            noVal = distanceMatrix[0][0];
            if (noVal == 0) noVal = Integer.MAX_VALUE / 2;
            int maxVal = 0;
            for (int i = 0; i < n; i++) {
                distanceMatrix[i][n - 1] = distanceMatrix[i][0];
                distanceMatrix[n - 1][i] = noVal;
                distanceMatrix[i][0] = noVal;
                for (int j = 0; j < n; j++) {
                    if (distanceMatrix[i][j] != noVal && distanceMatrix[i][j] > maxVal) {
                        maxVal = distanceMatrix[i][j];
                    }
                }
            }
            line = buf.readLine();
            line = buf.readLine();
            initialUB = maxVal * n;
            optimum = Integer.parseInt(line.replaceAll(" ", ""));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    //***********************************************************************************
    // PARSER
    //***********************************************************************************

    public void loadNewInstancesBUG(String fileName, String optDirectory) {
        try {
            File file = new File(fileName);
            if (!canParse(file.getName())) {
                throw new UnsupportedOperationException("cannotParse : " + file.getName());
            }
            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line = buf.readLine();
            instanceName = file.getName();
            System.out.println("parsing instance " + instanceName + "...");
            n = Integer.parseInt(line.replaceAll(" ", "")) + 1;
            distanceMatrix = new int[n][n];
            String[] lineNumbers;
            for (int i = 0; i < n - 1; i++) {
                int nbSuccs = 0;
                while (nbSuccs < n - 1) {
                    line = buf.readLine();
                    line = line.replaceAll(" * ", " ");
                    lineNumbers = line.split(" ");
                    for (int j = 1; j < lineNumbers.length; j++) {
                        if (nbSuccs == n - 1) {
                            i++;
                            if (i == n - 1) break;
                            nbSuccs = 0;
                        }
                        distanceMatrix[i][nbSuccs] = Integer.parseInt(lineNumbers[j]);
                        nbSuccs++;
                    }
                }
            }
            noVal = distanceMatrix[0][0];
            if (noVal == 0) noVal = Integer.MAX_VALUE / 2;
            int maxVal = 0;
            for (int i = 0; i < n; i++) {
                distanceMatrix[i][n - 1] = distanceMatrix[i][0];
                distanceMatrix[n - 1][i] = noVal;
                distanceMatrix[i][0] = noVal;
                for (int j = 0; j < n; j++) {
                    if (distanceMatrix[i][j] != noVal && distanceMatrix[i][j] > maxVal) {
                        maxVal = distanceMatrix[i][j];
                    }
                }
            }
            initialUB = maxVal * n;
            loadOpt(optDirectory);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public int loadNewTime(String name, String url) {
        try {
            BufferedReader buf = new BufferedReader(new FileReader(url));
            String line = buf.readLine();
            String[] lineNumbers;
            while (line != null) {
                while (line != null && (line.equals("") || line.equals(";;;"))) {
                    line = buf.readLine();
                }
                if (line == null) {
                    throw new UnsupportedOperationException("time not found");
                }
                lineNumbers = line.split(";");
                if (instanceName.equals("N" + lineNumbers[0])) {
                    if (n == Integer.parseInt(lineNumbers[1]) + 1) {
                        return (int) Double.parseDouble(lineNumbers[3]);
                    } else {
                        throw new UnsupportedOperationException(n + " =/= " + (Integer.parseInt(lineNumbers[1]) + 1));
                    }
                }
                line = buf.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        throw new UnsupportedOperationException();
    }

    private void loadOpt(String optDirectory) {
        try {
            BufferedReader buf = new BufferedReader(new FileReader(optDirectory));//"/Users/jfages07/github/In4Ga/newATSP/optima.csv"
            String line = buf.readLine();
            String[] lineNumbers;
            while (line != null) {
                lineNumbers = line.split(";");
                if (instanceName.equals("N" + lineNumbers[0])) {
                    if (n == Integer.parseInt(lineNumbers[1]) + 1) {
                        optimum = Integer.parseInt(lineNumbers[2]);
                        return;
                    } else {
                        throw new UnsupportedOperationException();
                    }
                }
                line = buf.readLine();
            }
            throw new UnsupportedOperationException();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static boolean canParse(String s) {
        if (s.contains("ND122644m")
                || s.contains("ND122943m")
                || s.contains("ND163440")
                || s.contains("ND163742b")
                || s.contains("ND184040a")) {
            return false;
        }
        return true;
    }
}
