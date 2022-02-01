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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.arraycopy;


/**
 * Orthogonal latin square
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/06/11
 */
public class OrthoLatinSquare extends AbstractProblem {

    @Option(name = "-n", usage = "Ortho latin square size.", required = false)
    int m = 5;
    IntVar[] square1, square2, vars;
    Constraint[] ALLDIFFS;


    @Override
    public void buildModel() {
        model = new Model();
        int mm = m * m;
        square1 = model.intVarArray("s1", mm, 1, m, true);
        square2 = model.intVarArray("s2", mm, 1, m, true);
        vars = model.intVarArray("vars", mm, 0, mm - 1, false);

        List<Constraint> ADS = new ArrayList<>();

        Constraint cc = model.allDifferent(vars, "AC");
        cc.post();
        ADS.add(cc);

        int[] mod = new int[mm];
        int[] div = new int[mm];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                mod[i * m + j] = j + 1;
                div[i * m + j] = i + 1;
            }
        }
        for (int i = 0; i < mm; i++) {
            model.element(square1[i], mod, vars[i], 0).post();
            model.element(square2[i], div, vars[i], 0).post();
        }

        // Rows
        for (int i = 0; i < m; i++) {
            IntVar[] ry = new IntVar[m];
            arraycopy(square1, i * m, ry, 0, m);
            cc = model.allDifferent(ry, "BC");
            cc.post();
            ADS.add(cc);
            ry = new IntVar[m];
            arraycopy(square2, i * m, ry, 0, m);
            cc = model.allDifferent(ry, "BC");
            cc.post();
            ADS.add(cc);
        }
        for (int j = 0; j < m; j++) {
            IntVar[] cy = new IntVar[m];
            for (int i = 0; i < m; i++) {
                cy[i] = square1[i * m + j];
            }
            cc = model.allDifferent(cy, "BC");
            cc.post();
            ADS.add(cc);
            cy = new IntVar[m];
            for (int i = 0; i < m; i++) {
                cy[i] = square2[i * m + j];
            }
            cc = model.allDifferent(cy, "BC");
            cc.post();
            ADS.add(cc);
        }
        ALLDIFFS = new Constraint[ADS.size()];
        ADS.toArray(ALLDIFFS);
        //TODO: ajouter LEX
        for (int i = 1; i < m; i++) {
            IntVar[] ry1 = new IntVar[m];
            IntVar[] ry2 = new IntVar[m];
            for (int j = 0; j < m; j++) {
                ry1[j] = square1[(i - 1) * m + j];
                ry2[j] = square2[i * m + j];
            }
            model.lexLess(ry1, ry2).post();
        }
    }

    @Override
    public void configureSearch() {
    }

    @Override
    public void solve() {
        model.getSolver().solve();

        System.out.println(String.format("Ortho latin square(%s)", m));
        StringBuilder st = new StringBuilder();
        st.append("\t");
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                st.append(String.format("%d ", square1[i * m + j].getValue()));
            }
            st.append("\n\t");
        }
        st.append("\n\t");
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                st.append(String.format("%d ", square2[i * m + j].getValue()));
            }
            st.append("\n\t");
        }
        System.out.println(st);
    }

    public static void main(String[] args) {
        new OrthoLatinSquare().execute(args);
    }
}
