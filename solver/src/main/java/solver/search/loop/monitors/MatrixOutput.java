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
package solver.search.loop.monitors;

import gnu.trove.map.hash.TIntIntHashMap;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.variables.Variable;
import solver.variables.view.IView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/03/12
 */
public class MatrixOutput implements IMonitorClose {

    final Solver solver;
    final TIntIntHashMap v2idx, p2idx;
    final Output type;
    final String name;

    public enum Output {
        PDF, EPS, SVG
    }

    public MatrixOutput(Solver solver, String name, Output op) {
        this.solver = solver;
        v2idx = new TIntIntHashMap();
        p2idx = new TIntIntHashMap();
        type = op;
        this.name = name;
    }


    @Override
    public void beforeClose() {
    }

    @Override
    public void afterClose() {
        Constraint[] cstrs = solver.getCstrs();
        int N_PROP = 0;
        for (int i = 0; i < cstrs.length; i++) {
            Propagator[] props = cstrs[i].propagators;
            for (int j = 0; j < props.length; j++) {
                p2idx.put(props[j].getId(), N_PROP++);
            }
        }

        int N_VARS = 0;
        Variable[] vars = solver.getVars();
        for (int i = 0; i < vars.length; i++) {
            v2idx.put(vars[i].getId(), N_VARS++);
        }

        int[][] matrix = new int[N_PROP][N_VARS];
        for (int i = 0; i < cstrs.length; i++) {
            Propagator[] props = cstrs[i].propagators;
            for (int j = 0; j < props.length; j++) {
                int pidx = props[j].getId();
                int prio = props[j].getPriority().priority;
                //System.out.printf("%s\n", props[j]);
                vars = props[j].getVars();
                for (int k = 0; k < vars.length; k++) {
                    Variable var = retrieveVar(vars[k]);
                    matrix[p2idx.get(pidx)][v2idx.get(var.getId())] = prio;
                }
            }
        }
        StringBuilder st = new StringBuilder();
        switch (type) {
            case SVG:
                st.append("set terminal svg rounded\n");
                st.append("set output '").append(name).append(".svg'\n");
                break;
            case EPS:
                st.append("set terminal postscript color\n");
                st.append("set output '").append(name).append(".eps'\n");
                break;
            case PDF:
                st.append("set terminal postscript color\n");
                st.append("set output '").append(name).append(".eps'\n");
                st.append("set output \"| ps2pdf - '").append(name).append(".pdf'\"\n");

        }
        st.append("unset key\n");
        st.append("set tic scale 0\n");
        st.append("set palette rgbformula -21,-22,-23\n");
        st.append("set cbrange [0:7]\n");
        st.append("set cblabel \"Priority\"\n");
        st.append("unset cbtics\n");
        st.append("set title '").append(name).append("'\n");
        st.append("set xrange [-0.5:").append(N_VARS - 1).append(".5]\n");
        st.append("set xlabel \"Variable\"\n");
        st.append("set yrange [-0.5:").append(N_PROP - 1).append(".5]\n");
        st.append("set ylabel \"Propagators\"\n");
        st.append("set view map\n");
        st.append("splot '-' matrix with image\n");
        for (int i = 0; i < N_PROP; i++) {
            for (int j = 0; j < N_VARS; j++) {
                st.append(matrix[i][j]).append(" ");
            }
            st.append("\n");
        }
        st.append("e\ne\n");

        BufferedWriter out = null;
        try {
            File file = File.createTempFile("out", ".gnu");
            out = new BufferedWriter(new FileWriter(file));
            out.write(st.toString());
            out.close();
            System.out.printf("OUTPUT : %s\n", file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Variable retrieveVar(Variable v) {
        if ((v.getTypeAndKind() & Variable.VIEW) != 0) {
            return retrieveVar(((IView) v).getVariable());
        }
        return v;
    }
}
