/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
public class AverageCSV{

    /**
     * Record results
     */
    public void record(String filename, String prefix, String postfix, Number[] measures) {
        StringBuilder st = new StringBuilder(prefix);
        st.append(";");
        for (int i = 0; i < measures.length; i++) {
            st.append(String.format("%.4f;", measures[i].doubleValue()));
        }
        st.append(postfix);
        st.append("\n");
        writeTextInto(st.toString(), filename);
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
                out.write(";AVERAGE;;;;;;;;;;;;;");
                out.write("\n");
                out.write("instance;solutionCount;buildingTime(sec);initTime(sec);initPropag(sec);totalTime(sec);objective;nodes;backtracks;fails;restarts;fineProp;coarseProp;");
                out.write("\n");
            }
            out.write(text);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
