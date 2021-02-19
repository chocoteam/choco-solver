/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.StringUtils;
import org.kohsuke.args4j.Option;

import java.text.MessageFormat;

import static java.util.Arrays.fill;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;

/**
 * CSPLib prob019:<br/>
 * "An order n magic square is a n by n matrix containing the numbers 1 to n^2, with each row,
 * column and main diagonal equal the same sum.
 * As well as finding magic squares, we are interested in the number of a given size that exist."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class MagicSquare extends AbstractProblem {

    @Option(name = "-n", usage = "Magic square size.", required = false)
    int n = 5;

    IntVar[] vars;


    @Override
    public void buildModel() {
        model = new Model();
        int ms = n * (n * n + 1) / 2;

        IntVar[][] matrix = new IntVar[n][n];
        IntVar[][] invMatrix = new IntVar[n][n];
        vars = new IntVar[n * n];

        int k = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++, k++) {
                matrix[i][j] = model.intVar("square" + i + "," + j, 1, n * n, false);
                vars[k] = matrix[i][j];
                invMatrix[j][i] = matrix[i][j];
            }
        }

        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];
        for (int i = 0; i < n; i++) {
            diag1[i] = matrix[i][i];
            diag2[i] = matrix[(n - 1) - i][i];
        }

        model.allDifferent(vars, "BC").post();

        int[] coeffs = new int[n];
        fill(coeffs, 1);
        for (int i = 0; i < n; i++) {
            model.scalar(matrix[i], coeffs, "=", ms).post();
            model.scalar(invMatrix[i], coeffs, "=", ms).post();
        }
        model.scalar(diag1, coeffs, "=", ms).post();
        model.scalar(diag2, coeffs, "=", ms).post();

        // Symetries breaking
        model.arithm(matrix[0][n - 1], "<", matrix[n - 1][0]).post();
        model.arithm(matrix[0][0], "<", matrix[n - 1][n - 1]).post();
        model.arithm(matrix[0][0], "<", matrix[n - 1][0]).post();

    }

    @Override
    public void configureSearch() {
        model.getSolver().setSearch(inputOrderLBSearch(vars));
    }

    @Override
    public void solve() {
        model.getSolver().solve();

        StringBuilder st = new StringBuilder();
        String line = "+";
        for (int i = 0; i < n; i++) {
            line += "----+";
        }
        line += "\n";
        st.append(line);
        for (int i = 0; i < n; i++) {
            st.append("|");
            for (int j = 0; j < n; j++) {
                st.append(StringUtils.pad(vars[i * n + j].getValue() + "", -3, " ")).append(" |");
            }
            st.append(MessageFormat.format("\n{0}", line));
        }
        st.append("\n\n\n");
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new MagicSquare().execute(args);
    }
}
