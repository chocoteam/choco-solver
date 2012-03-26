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
package samples;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.binary.DistanceXYC;
import solver.propagation.generator.*;
import solver.propagation.generator.sorter.evaluator.EvtRecEvaluators;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * <a href="http://www.inra.fr/mia/T/schiex/Doc/CELAR.shtml">CELAR Radio Link Frequency Assignment Problem</a>:
 * <br/>
 * "The Radio Link frequency Assignment Problem consists in assigning frequencies to a set of radio links
 * defined between pairs of sites in order to avoid interferences.
 * Each radio link is represented by a variable whose domain is the set of all frequences
 * that are available for this link."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/03/12
 */
public class RLFAP extends AbstractProblem {

    private static String DOM = "dom.txt";
    private static String VAR = "var.txt";
    private static String CTR = "ctr.txt";

    @Option(name = "-d", aliases = "--directory", usage = "RLFAP instance directory.", required = false)
    String dir = "/Users/cprudhom/Downloads/FullRLFAP/CELAR/scen05";

    int[][] _dom, _ctr;
    int[][] _var;

    IntVar[] vars;


    @Override
    public void buildModel() {
        _dom = readDOM(dir + File.separator + DOM);
        _var = readVAR(dir + File.separator + VAR);
        _ctr = readCTR(dir + File.separator + CTR);

        solver = new Solver("RLFAP "+dir);
        vars = new IntVar[_var.length];

        vars = new IntVar[_var[_var.length - 1][0]];
        int prev = 0;
        for (int i = 0; i < _var.length; i++) {
            int vidx = _var[i][0] - 1;
            if (vidx > prev) {
                for (; prev < vidx; ) {
                    vars[prev++] = Views.fixed(0, solver);
                }
            }
            int didx = _var[i][1];
            if (_var[i].length > 2) {
                vars[vidx] = Views.fixed(_var[i][2], solver);
            } else {
                vars[vidx] = VariableFactory.enumerated("v_" + vidx, _dom[didx], solver);
            }
            prev = vidx + 1;
        }
        for (int i = 0; i < _ctr.length; i++) {
            int[] ci = _ctr[i];
            solver.post(new DistanceXYC(vars[ci[0] - 1], vars[ci[1] - 1],
                    (ci[2] == 0 ? DistanceXYC.Op.EQ : DistanceXYC.Op.GT),
                    ci[3], solver));
        }

    }

    @Override
    public void configureSearch() {
//        solver.set(StrategyFactory.inputOrderMinVal(vars, solver.getEnvironment()));
//        solver.set(StrategyFactory.domwdeginputorderMindom(vars, solver));
        solver.set(StrategyFactory.domddegMinDom(vars));
    }

    @Override
    public void configureEngine() {
       solver.set(new Sort(
           new Queue(new PCoarse(solver.getCstrs())),
           new SortDyn2(EvtRecEvaluators.MinDomSize, new PVar(solver.getVars()))
       ));

    }

    @Override
    public void solve() {
//        SearchMonitorFactory.log(solver, true, false);
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("RLFAP {}", dir);
        StringBuilder st = new StringBuilder();
        if (solver.isFeasible() == Boolean.TRUE) {
            st.append("\t");
            for (int i = 0; i < vars.length; i++) {
                st.append(vars[i].getValue()).append(" ");
                if (i % 10 == 9) {
                    st.append("\n\t");
                }
            }
        } else {
            st.append("\tINFEASIBLE");
        }
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new RLFAP().execute(args);
    }

    /////////////////////

    protected int[][] readDOM(String filename) {
        FileReader f = null;
        String line;
        TIntList values = new TIntArrayList();
        try {
            f = new FileReader(filename);
            BufferedReader r = new BufferedReader(f);
            List<int[]> domains = new ArrayList<int[]>();
            while ((line = r.readLine()) != null) {
                Scanner sc = new Scanner(line);
                sc.nextInt();
                while (sc.hasNextInt()) {
                    values.add(sc.nextInt());
                }
                domains.add(values.toArray());
                values.clear();
            }
            int[][] data = new int[domains.size()][];
            for (int i = 0; i < domains.size(); i++) {
                data[i] = domains.get(i);
                Arrays.sort(data[i]);
            }

            return data;
        } catch (IOException
                e) {
            e.printStackTrace();
        }
        return null;
    }

    protected int[][] readVAR(String filename) {
        FileReader f = null;
        String line;
        TIntList values = new TIntArrayList();
        try {
            f = new FileReader(filename);
            BufferedReader r = new BufferedReader(f);
            List<int[]> ints = new ArrayList<int[]>();
            while ((line = r.readLine()) != null) {
                Scanner sc = new Scanner(line);
                while (sc.hasNextInt()) {
                    values.add(sc.nextInt());
                }
                ints.add(values.toArray());
                values.clear();
            }
            int[][] data = new int[ints.size()][];
            for (int i = 0; i < ints.size(); i++) {
                data[i] = ints.get(i);
            }

            return data;
        } catch (IOException
                e) {
            e.printStackTrace();
        }
        return null;
    }


    protected int[][] readCTR(String filename) {
        FileReader f = null;
        String line;
        TIntList values = new TIntArrayList();
        try {
            f = new FileReader(filename);
            BufferedReader r = new BufferedReader(f);
            List<int[]> ints = new ArrayList<int[]>();
            while ((line = r.readLine()) != null) {
                Scanner sc = new Scanner(line);
                values.add(sc.nextInt());
                values.add(sc.nextInt());
                sc.next();
                values.add(sc.next().equals("=") ? 0 : 1);
                values.add(sc.nextInt());
                ints.add(values.toArray());
                values.clear();
            }
            int[][] data = new int[ints.size()][];
            for (int i = 0; i < ints.size(); i++) {
                data[i] = ints.get(i);
            }
            return data;
        } catch (IOException
                e) {
            e.printStackTrace();
        }
        return null;
    }


}
