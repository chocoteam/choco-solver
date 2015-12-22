/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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
package org.chocosolver.util;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.StringUtils;

/**
 * A factory dedicated to problems creation.
 * Created by cprudhom on 20/11/2015.
 * Project: choco.
 */
public class ProblemMaker {

    /**
     * Creates a n-Queen problem with only binary constraints.
     * n queens must be placed on a nxn chessboard.
     * The variables can be accessed though the hook name "vars".
     * @param n number of queens (or size of the chessboard)
     * @return a solve-ready solver.
     */
    @SuppressWarnings("Duplicates")
    public static Solver makeNQueenWithBinaryConstraints(int n){
        Solver solver = new Solver();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("Q_" + i, 1, n, solver);
        }
        solver.addHook("vars", vars);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                solver.post(IntConstraintFactory.arithm(vars[i], "!=", vars[j]));
                solver.post(IntConstraintFactory.arithm(vars[i], "!=", vars[j], "+", -k));
                solver.post(IntConstraintFactory.arithm(vars[i], "!=", vars[j], "+", k));
            }
        }
        return solver;
    }

    /**
     * Creates a Costas array problem of size n.
     * Two AllDifferent constraints are used, on achieving AC, the other BC.
     * The variables can be accessed though the hook name "vars" and "vectors".
     * @param n size of the array.
     * @return a solve-ready solver
     */
    @SuppressWarnings("Duplicates")
    public static Solver makeCostasArrays(int n){
        Solver solver = new Solver();
        IntVar[] vars = VariableFactory.enumeratedArray("v", n, 0, n - 1, solver);
        IntVar[] vectors = new IntVar[(n*(n-1))/2];
        IntVar[][] diff = new IntVar[n][n];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i+1; j < n; j++) {
                IntVar k = VariableFactory.enumerated(StringUtils.randomName(), -n, n, solver);
                solver.post(ICF.arithm(k,"!=",0));
                solver.post(IntConstraintFactory.sum(new IntVar[]{vars[i],k},vars[j]));
                vectors[idx] = VariableFactory.offset(k, 2 * n * (j - i));
                diff[i][j] = k;
                idx++;
            }
        }
        solver.addHook("vars", vars);
        solver.addHook("vectors", vectors);

        solver.post(IntConstraintFactory.alldifferent(vars, "AC"));
        solver.post(IntConstraintFactory.alldifferent(vectors, "BC"));

        // symmetry-breaking
        solver.post(ICF.arithm(vars[0],"<",vars[n-1]));
        return solver;
    }

    /**
     * Creates a Golomb ruler problem of size m.
     * The variables can be accessed though the hook name "ticks" and "diffs".
     * @param m size of the rule
     * @return a solve-ready solver
     */
    @SuppressWarnings("Duplicates")
    public static Solver makeGolombRuler(int m){
        Solver solver = new Solver();
        IntVar[] ticks = VariableFactory.enumeratedArray("a", m, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), solver);
        solver.addHook("ticks", ticks);
        solver.post(IntConstraintFactory.arithm(ticks[0], "=", 0));

        for (int i = 0; i < m - 1; i++) {
            solver.post(IntConstraintFactory.arithm(ticks[i + 1], ">", ticks[i]));
        }

        IntVar[] diffs = VariableFactory.enumeratedArray("d", (m * m - m) / 2, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), solver);
        solver.addHook("diffs", diffs);
        IntVar[][] m_diffs = new IntVar[m][m];
        for (int k = 0, i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++, k++) {
                // d[k] is m[j]-m[i] and must be at least sum of first j-i integers
                // <cpru 04/03/12> it is worth adding a constraint instead of a view
                solver.post(IntConstraintFactory.scalar(new IntVar[]{ticks[j], ticks[i]}, new int[]{1, -1}, diffs[k]));
                solver.post(IntConstraintFactory.arithm(diffs[k], ">=", (j - i) * (j - i + 1) / 2));
                solver.post(IntConstraintFactory.arithm(diffs[k], "-", ticks[m - 1], "<=", -((m - 1 - j + i) * (m - j + i)) / 2));
                solver.post(IntConstraintFactory.arithm(diffs[k], "<=", ticks[m - 1], "-", ((m - 1 - j + i) * (m - j + i)) / 2));
                m_diffs[i][j] = diffs[k];
            }
        }
        solver.post(IntConstraintFactory.alldifferent(diffs, "BC"));
        // break symetries
        if (m > 2) {
            solver.post(IntConstraintFactory.arithm(diffs[0], "<", diffs[diffs.length - 1]));
        }
        solver.setObjectives(ticks[m-1]);
        return solver;
    }

}
