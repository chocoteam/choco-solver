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
import org.chocosolver.util.ESat;
import org.kohsuke.args4j.Option;

/**
 * CSPLib prob024:<br/>
 * "Consider two sets of the numbers from 1 to 4.
 * The problem is to arrange the eight numbers in the two sets into a single sequence in which
 * the two 1's appear one number apart,
 * the two 2's appear two numbers apart,
 * the two 3's appear three numbers apart,
 * and the two 4's appear four numbers apart.
 * <p/>
 * The problem generalizes to the L(k,n) problem,
 * which is to arrange k sets of numbers 1 to n,
 * so that each appearance of the number m is m numbers on from the last.
 * <br/>
 * For example, the L(3,9) problem is to arrange 3 sets of the numbers 1 to 9 so that
 * the first two 1's and the second two 1's appear one number apart,
 * the first two 2's and the second two 2's appear two numbers apart, etc."
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/08/11
 */
public class Langford extends AbstractProblem {

    @Option(name = "-k", usage = "Number of sets.", required = false)
    private int k = 3;

    @Option(name = "-n", usage = "Upper bound.", required = false)
    private int n = 9;

    IntVar[] position;


    @Override
    public void buildModel() {
        model = new Model();
        // position of the colors
        // position[i], position[i+k], position[i+2*k]... occurrence of the same color
        position = model.intVarArray("p", n * k, 0, k * n - 1, false);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < this.k - 1; j++) {
                position[i + j * n].sub(i+2).eq(position[i + (j + 1) * n]).post();
            }
        }
        position[0].lt(position[n * k - 1]).post();
        model.allDifferent(position, "AC").post();
    }

    @Override
    public void configureSearch() {
    }

    @Override
    public void solve() {
        model.getSolver().solve();

        StringBuilder st = new StringBuilder(String.format("Langford's number (%s,%s)\n", k, n));
        if (model.getSolver().isFeasible() == ESat.TRUE) {
            int[] values = new int[k * n];
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < n; j++) {
                    values[position[i * n + j].getValue()] = j + 1;
                }
            }
            st.append("\t");
            for (int i = 0; i < values.length; i++) {
                st.append(values[i]).append(" ");
            }
            st.append("\n");
        } else {
            st.append("\tINFEASIBLE");
        }
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new Langford().execute(args);
    }

}
