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
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.kohsuke.args4j.Option;

import java.util.stream.IntStream;

import static org.chocosolver.solver.search.strategy.Search.minDomLBSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/07/2019
 */
public class Takuzu extends AbstractProblem {

    @Option(name = "-g", aliases = "--grid", usage = "Takuzu grid ID.")
    Takuzu.Data data = Takuzu.Data.g10x10;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(name = "-r3", aliases = "--rule3", usage = "How to encode rule 3 (1*: keySort, 2: allDifferent.", required = false)
    private int r3 = 1;

    /**
     * Cells of the grid, some may be known in advanced
     */
    private BoolVar[][] cells;

    private FiniteAutomaton auto;

    @Override
    public void buildModel() {
        model = new Model(Settings.init().setEnableTableSubstitution(false));
        int n = data.grid.length;
        int m = data.grid[0].length;
        cells = new BoolVar[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int cell = data.grid(i, j);
                if (data.grid(i, j) < 2) {
                    cells[i][j] = model.boolVar("c_" + i + "_" + j, cell == 1);
                } else {
                    cells[i][j] = model.boolVar("c_" + i + "_" + j);
                }
            }
        }
        // Rules on lines
        rule1(cells);
        rule2(cells);
        switch (r3) {
            case 2:
                rule32(cells);
                break;
            default:
                rule31(cells);
                break;
        }
        // Rules on columns
        BoolVar[][] tcells = ArrayUtils.transpose(cells);
        rule1(tcells);
        rule2(tcells);
        switch (r3) {
            case 2:
                rule32(tcells);
                break;
            default:
                rule31(tcells);
                break;
        }
    }

    @Override
    public void configureSearch() {
        model.getSolver().setSearch(minDomLBSearch(append(cells)));
    }

    @Override
    public void solve() {
        Solver solver = model.getSolver();
        solver.showShortStatistics();
        solver.showSolutions();
        if (solver.solve()) {
            for (int i = 0; i < cells.length; i++) {
                for (int j = 0; j < cells[0].length; j++) {
                    System.out.printf("%d ", cells[i][j].getValue());
                }
                System.out.printf("%n");
            }
        }
    }

    /**
     * Each row and each column must contain an equal number of whie and black circles.
     */
    private void rule1(BoolVar[][] vars) {
        int size = vars.length;
        for (int i = 0; i < size; i++) {
            model.sum(vars[i], "=", size / 2).post();
        }
    }

    /**
     * Create the automaton which encodes rule 2.
     * <br>
     * <img src="takuzuR2.png" width="400"/>
     */
    private FiniteAutomaton getAuto() {
        if (auto == null) {
            auto = new FiniteAutomaton();
            int a = auto.addState();
            int b = auto.addState();
            int c = auto.addState();
            int d = auto.addState();
            int e = auto.addState();
            auto.setInitialState(a);
            auto.addTransition(a, b, 0);
            auto.addTransition(a, c, 1);
            auto.addTransition(b, c, 1);
            auto.addTransition(b, d, 0);
            auto.addTransition(c, b, 0);
            auto.addTransition(c, e, 1);
            auto.addTransition(e, b, 0);
            auto.addTransition(d, c, 1);
            auto.setFinal(b, c, d, e);
        }
        return auto;
    }

    /**
     * More than two circles of the same color can't be adjacent.
     */
    private void rule2(BoolVar[][] vars) {
        int size = vars.length;
        for (int i = 0; i < size; i++) {
            model.regular(vars[i], getAuto()).post();
        }
    }

    /**
     * Each row and column is unique.
     * Encoded with a stable keysort + lex chain constraints.
     */
    private void rule31(BoolVar[][] vars) {
        BoolVar[][] ordered = model.boolVarMatrix("o", vars.length, vars[0].length);
        model.keySort(vars, null, ordered, vars.length).post();
        model.lexChainLess(ordered).post();
    }

    /**
     * Each row and column is unique.
     * Encoded with an allDifferent constraint.
     * This version only works up to 10x10 grid.
     */
    private void rule32(BoolVar[][] vars) {
        int size = vars.length;
        IntVar[] res = model.intVarArray("r", size, 0, (int) Math.pow(2, size) - 1);
        int[] coeffs = IntStream.range(0, size).map(k -> (int) Math.pow(2, k)).toArray();
        for (int i = 0; i < size; i++) {
            model.scalar(vars[i], coeffs, "=", res[i]).post();
        }
        model.allDifferent(res).post();
    }


    public static void main(String[] args) {
        new Takuzu().execute(args);
    }


    enum Data {
        g4x4(
                new int[][]{
                        {2, 1, 2, 0},
                        {2, 2, 0, 2},
                        {2, 0, 2, 2},
                        {1, 1, 2, 0}
                }
        ),
        g10x10(
                new int[][]{
                        {2,2,1,2,2,2,0,2,2,0},
                        {2,2,0,2,1,2,2,2,1,1},
                        {2,2,2,2,2,2,1,2,1,2},
                        {2,2,0,2,1,2,1,2,2,0},
                        {2,2,2,2,2,2,2,2,2,2},
                        {2,1,1,2,2,2,2,2,0,0},
                        {2,2,2,2,2,2,2,2,2,2},
                        {2,1,2,2,0,0,2,2,1,1},
                        {0,2,2,2,2,2,2,2,2,1},
                        {1,2,0,0,2,0,2,1,1,2},
                }
        ),
        g14x14(
                new int[][]{
                        {2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 1, 2, 2, 0},
                        {2, 2, 1, 1, 2, 2, 1, 2, 0, 2, 2, 2, 1, 0},
                        {2, 2, 2, 1, 2, 2, 2, 2, 2, 0, 2, 2, 1, 2},
                        {2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2},
                        {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                        {2, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 2, 2, 2},
                        {2, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 0},
                        {2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2},
                        {2, 2, 0, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
                        {2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2},
                        {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2},
                        {0, 0, 2, 2, 0, 2, 2, 1, 1, 2, 2, 2, 1, 2},
                        {0, 2, 2, 1, 2, 2, 0, 2, 1, 2, 2, 0, 2, 2},
                        {2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 1, 2}
                }
        );
        final int[][] grid;

        Data(int[][] grid) {
            this.grid = grid;
        }

        int grid(int i, int j) {
            return grid[i][j];
        }
    }
}
