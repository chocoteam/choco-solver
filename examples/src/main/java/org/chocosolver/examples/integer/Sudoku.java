/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;

import static org.chocosolver.solver.search.strategy.Search.minDomLBSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;

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
    public void buildModel() {
        model = new Model();

        rows = new IntVar[n][n];
        cols = new IntVar[n][n];
        carres = new IntVar[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (data.grid(i, j) > 0) {
                    rows[i][j] = model.intVar(data.grid(i, j));
                } else {
                    rows[i][j] = model.intVar("c_" + i + "_" + j, 1, n, false);
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
            model.allDifferent(rows[i], "AC").post();
            model.allDifferent(cols[i], "AC").post();
            model.allDifferent(carres[i], "AC").post();
        }


    }

    @Override
    public void configureSearch() {
        model.getSolver().setSearch(minDomLBSearch(append(rows)));

    }

    @Override
    public void solve() {
//        model.getSolver().showStatistics();
//        model.getSolver().solve();
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }

        StringBuilder st = new StringBuilder(String.format("Sudoku -- %s\n", data.name()));
        st.append("\t");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                st.append(rows[i][j]).append("\t\t\t");
            }
            st.append("\n\t");
        }

        System.out.println(st);
    }

    public static void main(String[] args) {
        new Sudoku().execute(args);
    }

    /////////////////////////////////// DATA //////////////////////////////////////////////////
    enum Data {
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
