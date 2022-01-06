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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.StringUtils;
import org.kohsuke.args4j.Option;

import java.text.MessageFormat;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;

/**
 * <a href="http://en.wikipedia.org/wiki/Latin_square">wikipedia</a>:<br/>
 * "A Latin square is an n x n array filled with n different Latin letters,
 * each occurring exactly once in each row and exactly once in each column"
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/06/11
 */
public class LatinSquare extends AbstractProblem {

    @Option(name = "-n", usage = "Latin square size.", required = false)
    int m = 20;
    IntVar[] vars;

    @Override
    public void buildModel() {
        model = new Model("Latin square");
        vars = new IntVar[m * m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                vars[i * m + j] = model.intVar("C" + i + "_" + j, 0, m - 1, false);
            }
        }
        // Constraints
        for (int i = 0; i < m; i++) {
            IntVar[] row = new IntVar[m];
            IntVar[] col = new IntVar[m];
            for (int x = 0; x < m; x++) {
                row[x] = vars[i * m + x];
                col[x] = vars[x * m + i];
            }
            model.allDifferent(col, "AC").post();
            model.allDifferent(row, "AC").post();
        }
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
        for (int i = 0; i < m; i++) {
            line += "----+";
        }
        line += "\n";
        st.append(line);
        for (int i = 0; i < m; i++) {
            st.append("|");
            for (int j = 0; j < m; j++) {
                st.append(StringUtils.pad((char) (vars[i * m + j].getValue() + 97) + "", -3, " ")).append(" |");
            }
            st.append(MessageFormat.format("\n{0}", line));
        }
        st.append("\n\n\n");
        System.out.println(st);
    }

    public static void main(String[] args) {
        new LatinSquare().execute(args);
    }
}
