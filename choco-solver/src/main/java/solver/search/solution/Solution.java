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

package solver.search.solution;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.variables.IntVar;
import solver.variables.RealVar;
import solver.variables.SetVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;

import java.util.LinkedList;

/**
 * <br/>
 *
 * @author Arnaud Malapert
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 * @since 19 juil. 2010
 */
public class Solution implements ICause {


    /* Reference to the solver */
    private Solver solver;

    /* Values of integer variables, in Solver internal order */
    private TIntArrayList intvalues;

    /* Values of double variables, in Solver internal order */
    private TDoubleArrayList realvalues;

    /* Values of graph variables, in Solver internal order */
    private LinkedList<boolean[][]> graphValues;

    /* Values of set variables, in Solver internal order */
    private LinkedList<int[]> setValues;

    public static Solution empty() {
        return new Solution();
    }

    private Solution() {
    }

    public Solution(Solver solver) {
        replace(solver);
    }

    public void replace(Solver solver) {
        this.solver = solver;
        Variable[] vars = solver.getVars();
        intvalues = new TIntArrayList();
        realvalues = new TDoubleArrayList();
        graphValues = new LinkedList<boolean[][]>();
        setValues = new LinkedList<int[]>();
        for (int i = 0; i < vars.length; i++) {
            assert (vars[i].instantiated()) : vars[i] + " is not instantiated"; // BEWARE only decision variables should be instantiated
            int kind = vars[i].getTypeAndKind() & Variable.KIND;
            switch (kind) {
                case Variable.INT:
                case Variable.BOOL:
                    intvalues.add(((IntVar) vars[i]).getValue());
                    break;
                case Variable.REAL:
                    realvalues.add(((RealVar) vars[i]).getLB());
                    realvalues.add(((RealVar) vars[i]).getUB());
                    break;
                case Variable.SET:
                    assert vars[i].instantiated() : vars[i] + " is not instantiated when recording a solution";
                    setValues.add(((SetVar) vars[i]).getValue());
                    break;
                case Variable.GRAPH:
                    assert vars[i].instantiated() : vars[i] + " is not instantiated when recording a solution";
                    graphValues.add(((GraphVar) vars[i]).getValue());
                    break;
            }
        }
//        measures = solver.getSearchLoop().getMeasures().
    }


    public void restore() {
        try {
            Variable[] vars = solver.getVars();
            int nbGV = 0;
            int nbSets = 0;
            int nbi = 0, nbr = 0;
            for (int i = 0; i < vars.length; i++) {
                int kind = vars[i].getTypeAndKind() & Variable.KIND;
                switch (kind) {
                    case Variable.INT:
                    case Variable.BOOL:
                        ((IntVar) vars[i]).instantiateTo(intvalues.get(nbi++), this);
                        break;
                    case Variable.REAL:
                        ((RealVar) vars[i]).updateBounds(realvalues.get(nbr++), realvalues.get(nbr++), this);
                        break;
                    case Variable.SET:
                        int[] sv = setValues.get(nbSets);
                        ((SetVar) vars[i]).instantiateTo(sv, this);
                        nbSets++;
                        break;
                    case Variable.GRAPH:
                        boolean[][] gv = graphValues.get(nbGV);
                        ((GraphVar) vars[i]).instantiateTo(gv, this);
                        nbGV++;
                        break;
                }

            }
        } catch (ContradictionException ex) {
            ex.printStackTrace();
            LoggerFactory.getLogger("solver").error("BUG in restoring solution !!");
            throw new SolverException("Restored solution not consistent !!");
        }
    }

    @Override
    public void explain(Deduction d, Explanation e) {
        e.add(Explanation.SYSTEM.get());
    }

    @Override
    public boolean reactOnPromotion() {
        return false;
    }

    @Override
    public String toString() {
        return "Solution";
    }
}
