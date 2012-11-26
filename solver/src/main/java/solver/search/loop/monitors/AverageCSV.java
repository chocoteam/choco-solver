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
package solver.search.loop.monitors;

import solver.Solver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A search monitor that trace the results in a CSV format file
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/09/12
 */
public class AverageCSV extends VoidSearchMonitor implements ISearchMonitor {

    final String prefix;
    final String fileName;

    Solver currentSolver;

    int nb_probes;
    double[] mA;
    long nbExecutions;
    double[] sA;


    public AverageCSV(String prefix, String fileName, long nbExecutions) {
        this.prefix = prefix;
        this.fileName = fileName;
        this.mA = new double[13];
        nb_probes = nbExecutions > 1 ? -1 : 0;
        this.sA = new double[13];
    }

    public void setSolver(Solver aSolver) {
        currentSolver = aSolver;
        aSolver.getSearchLoop().plugSearchMonitor(this);
    }

    @Override
    public void afterClose() {
        nb_probes++;
        if (nb_probes > 0) {
            double[] A = currentSolver.getMeasures().toArray();
            for (int i = 0; i < A.length; i++) {
                double activity = A[i];
                double oldmA = mA[i];

                double U = activity - oldmA;
                mA[i] += (U / nb_probes);
                sA[i] += (U * (activity - mA[i]));
            }
        }
    }

    /**
     * Record results
     */
    public void record() {
        StringBuilder st = new StringBuilder(prefix);
        st.append(";");
        for (int i = 0; i < mA.length; i++) {
            st.append(String.format("%.4f;", mA[i]));
        }
        for (int i = 0; i < mA.length; i++) {
            st.append(String.format("%.4f;", Math.sqrt(sA[i] / (nb_probes - 1))));
        }
        st.append("\n");
        writeTextInto(st.toString(), fileName);
    }

    /**
     * Add text at the end of file
     *
     * @param text
     * @param file
     */
    protected void writeTextInto(String text, String file) {
        try {
            File aFile = new File(file);
            boolean exist = aFile.exists();
            FileWriter out = new FileWriter(aFile, true);
            if (!exist) {
                out.write(";AVERAGE;;;;;;;;;;;;;STD DEV;;;;;;;;;;;;;\n");
                out.write("instance;solutionCount;buildingTime(ms);initTime(ms);initPropag(ms);resolutionTime(ms);totalTime(s);objective;nodes;backtracks;fails;restarts;fineProp;coarseProp;");
                out.write("solutionCount;buildingTime(ms);initTime(ms);initPropag(ms);resolutionTime(ms);totalTime(s);objective;nodes;backtracks;fails;restarts;fineProp;coarseProp;\n");
            }
            out.write(text);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
