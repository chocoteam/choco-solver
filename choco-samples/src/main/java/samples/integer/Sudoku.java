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

package samples.integer;

import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.tools.ArrayUtils;

/**
 * <a href="">wikipedia</a>:<br/>
 * "The objective is to fill a 9?9 grid with digits so that
 * each column, each row, and each of the nine 3?3 sub-grids that compose the grid
 * contains all of the digits from 1 to 9.
 * The puzzle setter provides a partially completed grid, which typically has a unique solution."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class Sudoku extends AbstractProblem {

    @Option(name = "-g", aliases = "--grid", usage = "Sudoku grid ID.", required = false)
    Data data = Data.level1;

    private final int n = 9;
    IntVar[][] rows, cols, carres;

    @Override
    public void createSolver() {
        solver = new Solver("Sudoku");
    }

    @Override
    public void buildModel() {

        rows = new IntVar[n][n];
        cols = new IntVar[n][n];
        carres = new IntVar[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (data.grid(i, j) > 0) {
                    rows[i][j] = VariableFactory.fixed(data.grid(i, j), solver);
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
            solver.post(IntConstraintFactory.alldifferent(rows[i], "AC"));
            solver.post(IntConstraintFactory.alldifferent(cols[i], "AC"));
            solver.post(IntConstraintFactory.alldifferent(carres[i], "AC"));
        }


    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.firstFail_InDomainMin(ArrayUtils.append(rows)));

    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("Sudoku -- {}", data.name());
        StringBuilder st = new StringBuilder();
        st.append("\t");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                st.append(rows[i][j].getValue()).append(" ");
            }
            st.append("\n\t");
        }

        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new Sudoku().execute(args);
    }

    /////////////////////////////////// DATA //////////////////////////////////////////////////
    static enum Data {
        level1(
                new int[][]{
                        {0, 0, 0, 2, 0, 0, 0, 0, 0},
                        {0, 8, 0, 0, 3, 0, 0, 7, 0},
                        {3, 0, 0, 5, 0, 4, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 2, 8},
                        {8, 3, 0, 0, 1, 0, 0, 0, 0},
                        {0, 4, 0, 7, 2, 0, 3, 5, 1},
                        {0, 7, 0, 0, 5, 6, 0, 0, 4},
                        {0, 0, 3, 0, 0, 0, 0, 0, 0},
                        {2, 0, 5, 4, 0, 1, 6, 0, 3}
                }
        ),
        level2(
                new int[][]{
                        {3, 0, 4, 0, 2, 0, 0, 7, 0},
                        {1, 5, 0, 0, 0, 0, 0, 4, 0},
                        {0, 0, 0, 0, 0, 1, 0, 8, 3},
                        {0, 0, 0, 0, 0, 6, 1, 0, 0},
                        {2, 0, 5, 0, 3, 0, 0, 0, 8},
                        {7, 0, 0, 1, 0, 0, 3, 0, 0},
                        {0, 0, 0, 0, 0, 0, 6, 0, 0},
                        {5, 6, 0, 0, 0, 7, 0, 0, 0},
                        {0, 0, 0, 8, 0, 0, 0, 1, 4}
                }
        ),
        level3(
                new int[][]{
                        {0, 1, 0, 0, 0, 0, 0, 0, 0},
                        {8, 0, 0, 0, 0, 2, 1, 7, 0},
                        {0, 0, 4, 0, 0, 0, 0, 0, 0},
                        {0, 2, 0, 0, 0, 6, 0, 1, 3},
                        {0, 5, 3, 0, 7, 0, 6, 0, 2},
                        {1, 0, 0, 8, 0, 0, 5, 4, 0},
                        {0, 0, 0, 3, 1, 5, 0, 2, 6},
                        {0, 4, 0, 2, 0, 0, 0, 0, 7},
                        {0, 0, 0, 4, 8, 0, 3, 0, 0}
                }
        ),
        level4(
                new int[][]{
                        {0, 4, 0, 8, 0, 0, 0, 0, 0},
                        {0, 1, 0, 7, 2, 0, 5, 0, 4},
                        {8, 0, 0, 4, 0, 0, 0, 0, 0},
                        {1, 0, 5, 3, 0, 0, 4, 2, 0},
                        {0, 3, 0, 0, 0, 0, 0, 0, 0},
                        {4, 0, 0, 0, 5, 0, 7, 0, 1},
                        {6, 0, 0, 0, 0, 0, 1, 7, 0},
                        {0, 0, 0, 2, 1, 0, 8, 6, 0},
                        {2, 0, 0, 0, 3, 7, 0, 0, 0}
                }
        ),
        level5(
                new int[][]{
                        {0, 0, 0, 2, 0, 0, 0, 1, 5},
                        {3, 0, 0, 0, 0, 0, 7, 8, 0},
                        {0, 0, 0, 7, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 5, 7},
                        {7, 2, 0, 0, 4, 0, 0, 0, 0},
                        {8, 6, 0, 1, 0, 3, 0, 4, 0},
                        {4, 0, 0, 0, 1, 0, 0, 0, 0},
                        {2, 1, 0, 0, 0, 7, 8, 3, 0},
                        {0, 5, 0, 3, 0, 0, 0, 0, 0}
                }
        ),
        level6(
                new int[][]{
                        {0, 0, 0, 1, 0, 5, 4, 0, 0},
                        {0, 6, 0, 2, 0, 8, 0, 0, 7},
                        {0, 5, 2, 0, 0, 0, 1, 0, 0},
                        {0, 1, 5, 6, 0, 2, 0, 0, 0},
                        {2, 0, 0, 0, 0, 7, 5, 1, 0},
                        {0, 7, 8, 4, 0, 0, 0, 3, 2},
                        {0, 0, 3, 0, 1, 4, 7, 0, 6},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {6, 0, 0, 5, 0, 0, 0, 8, 0}
                }
        ),;
        final int[][] grid;

        Data(int[][] grid) {
            this.grid = grid;
        }

        int grid(int i, int j) {
            return grid[i][j];
        }
    }
}
