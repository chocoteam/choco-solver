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

import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.nary.AllDifferent;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class Sudoku extends AbstractProblem {

    /*
    int[][] grid = {
            {0, 0, 0, 0, 0, 3, 0, 6, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 0},
            {0, 9, 7, 5, 0, 0, 0, 8, 0},
            {0, 0, 0, 0, 9, 0, 2, 0, 0},
            {0, 0, 8, 0, 7, 0, 4, 0, 0},
            {0, 0, 3, 0, 6, 0, 0, 0, 0},
            {0, 1, 0, 0, 0, 2, 8, 9, 0},
            {0, 4, 0, 0, 0, 0, 0, 0, 0},
            {0, 5, 0, 1, 0, 0, 0, 0, 0}
    };*/
    int[][] grid = {
            {0, 0, 0, 2, 0, 5, 0, 0, 0},
            {0, 9, 0, 0, 0, 0, 7, 3, 0},
            {0, 0, 2, 0, 0, 9, 0, 6, 0},
            {2, 0, 0, 0, 0, 0, 4, 0, 9},
            {0, 0, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 9, 0, 0, 0, 0, 0, 1},
            {0, 8, 0, 4, 0, 0, 1, 0, 0},
            {0, 6, 3, 0, 0, 0, 0, 8, 0},
            {0, 0, 0, 6, 0, 8, 0, 0, 0}
    };


    int n = 9;

    IntVar[][] rows, cols, carres;

    @Override
    public void buildModel() {
        solver = new Solver();

        rows = new IntVar[n][n];
        cols = new IntVar[n][n];
        carres = new IntVar[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] > 0) {
                    rows[i][j] = VariableFactory.fixed(grid[i][j], solver);
                } else {
                    rows[i][j] = VariableFactory.enumerated("c_" + i + "_" + j, 1, n, solver);
                }
                cols[j][i] = rows[i][j];
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    carres[j + k * 3][i] = rows[k * 3][i + j * 3];
                    carres[j + k * 3][i + 3] = rows[1 + k * 3][i + j * 3];
                    carres[j + k * 3][i + 6] = rows[2 + k * 3][i + j * 3];
                }
            }
        }

        for (int i = 0; i < n; i++) {
            solver.post(new AllDifferent(rows[i], solver, AllDifferent.Type.AC));
            solver.post(new AllDifferent(cols[i], solver, AllDifferent.Type.AC));
            solver.post(new AllDifferent(carres[i], solver, AllDifferent.Type.AC));
        }


    }

    @Override
    public void configureSolver() {
        solver.set(StrategyFactory.minDomMinVal(ArrayUtils.append(rows), solver.getEnvironment()));

    }

    @Override
    public void solve() {
        solver.findSolution();

    }

    @Override
    public void prettyOut() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public static void main(String[] args) {
        new Sudoku().execute();
    }
}
