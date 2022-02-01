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
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.kohsuke.args4j.Option;

import static java.util.Arrays.fill;

/**
 * CSPLib prob049:<br/>
 * "This problem consists in finding a partition of numbers 1..N into two sets A and B such that:
 * <ul>
 * <li>A and B have the same cardinality</li>
 * <li>sum of numbers in A = sum of numbers in B</li>
 * <li>sum of squares of numbers in A = sum of squares of numbers in B</li>
 * </ul>
 * <p/>
 * More constraints can thus be added, e.g also impose the equality on the sum of cubes.
 * There is no solution for N < 8."
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class Partition extends AbstractProblem {
    @Option(name = "-n", usage = "Partition size.", required = false)
    int N = 2 * 32;

    IntVar[] vars;
    IntVar[] Ovars;

    Constraint[] heavy = new Constraint[3];

    @Override
    public void buildModel() {
        model = new Model("Partition " + N);
        int size = this.N / 2;
        IntVar[] x, y;
        x = model.intVarArray("x", size, 1, 2 * size, false);
        y = model.intVarArray("y", size, 1, 2 * size, false);

//        break symmetries
        for (int i = 0; i < size - 1; i++) {
            model.arithm(x[i], "<", x[i + 1]).post();
            model.arithm(y[i], "<", y[i + 1]).post();
        }
        model.arithm(x[0], "<", y[0]).post();
        model.arithm(x[0], "=", 1).post();

        IntVar[] xy = new IntVar[2 * size];
        for (int i = size - 1; i >= 0; i--) {
            xy[i] = x[i];
            xy[size + i] = y[i];
        }

        Ovars = new IntVar[2 * size];
        for (int i = 0; i < size; i++) {
            Ovars[i * 2] = x[i];
            Ovars[i * 2 + 1] = y[i];
        }

        int[] coeffs = new int[2 * size];
        for (int i = size - 1; i >= 0; i--) {
            coeffs[i] = 1;
            coeffs[size + i] = -1;
        }
        heavy[0] = model.scalar(xy, coeffs, "=", 0);
        heavy[0].post();

        IntVar[] sxy, sx, sy;
        sxy = new IntVar[2 * size];
        sx = new IntVar[size];
        sy = new IntVar[size];
        for (int i = size - 1; i >= 0; i--) {
            sx[i] = model.intVar("x^", 0, x[i].getUB() * x[i].getUB(), true);
            sxy[i] = sx[i];
            sy[i] = model.intVar("y^", 0, y[i].getUB() * y[i].getUB(), true);
            sxy[size + i] = sy[i];
            model.times(x[i], x[i], sx[i]).post();
            model.times(y[i], y[i], sy[i]).post();
            model.member(sx[i], 1, 4 * size * size).post();
            model.member(sy[i], 1, 4 * size * size).post();
        }
        heavy[1] = model.scalar(sxy, coeffs, "=", 0);
        heavy[1].post();

        coeffs = new int[size];
        fill(coeffs, 1);
        model.scalar(x, coeffs, "=", 2 * size * (2 * size + 1) / 4).post();
        model.scalar(y, coeffs, "=", 2 * size * (2 * size + 1) / 4).post();
        model.scalar(sx, coeffs, "=", 2 * size * (2 * size + 1) * (4 * size + 1) / 12).post();
        model.scalar(sy, coeffs, "=", 2 * size * (2 * size + 1) * (4 * size + 1) / 12).post();

        heavy[2] = model.allDifferent(xy, "BC");
        heavy[2].post();

        vars = xy;
    }

    @Override
    public void configureSearch() {
        model.getSolver().setSearch(Search.minDomLBSearch(Ovars));
    }

    @Override
    public void solve() {
        model.getSolver().solve();

        StringBuilder st = new StringBuilder();
        if (ESat.TRUE == model.getSolver().isFeasible()) {
            int sum1 = 0, sum2 = 0;
            int i = 0;
            st.append(vars[i].getValue());
            sum1 += vars[i].getValue();
            sum2 += vars[i].getValue() * vars[i++].getValue();
            for (; i < N / 2; i++) {
                st.append(", ").append(vars[i].getValue());
                sum1 += vars[i].getValue();
                sum2 += vars[i].getValue() * vars[i].getValue();
            }
            st.append(": (").append(sum1).append(")~(").append(sum2).append(")\n");
            sum1 = sum2 = 0;
            st.append(vars[i].getValue());
            sum1 += vars[i].getValue();
            sum2 += vars[i].getValue() * vars[i++].getValue();
            for (; i < N; i++) {
                st.append(", ").append(vars[i].getValue());
                sum1 += vars[i].getValue();
                sum2 += vars[i].getValue() * vars[i].getValue();
            }
            st.append(": (").append(sum1).append(")~(").append(sum2).append(")\n");
        } else {
            st.append("INFEASIBLE");
        }
        System.out.println(st);
    }

    public static void main(String[] args) {
        new Partition().execute(args);
    }

}
