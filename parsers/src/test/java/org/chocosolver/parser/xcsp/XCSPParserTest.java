/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.xcsp;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Checks the solutions of small XCSP3 instances against the ones found by brute force.
 * Each instance is given by its variables and its constraints, the expected solutions
 * by a predicate over the values of the variables, in their order of declaration.
 *
 * @author Charles Prud'homme
 * @since 12/10/2026
 */
public class XCSPParserTest {

    /**
     * Declares a variable of an instance.
     *
     * @param name name of the variable, as found in the instance
     * @param lb   lower bound of its domain
     * @param ub   upper bound of its domain
     */
    private record Var(String name, int lb, int ub) {
        String xml() {
            return "<var id=\"" + name + "\"> " + lb + ".." + ub + " </var>";
        }
    }

    private static Var var(String name, int lb, int ub) {
        return new Var(name, lb, ub);
    }

    /**
     * Solves an instance and checks that the solutions found are the expected ones, each found once.
     *
     * @param constraints the constraints of the instance, in XCSP3
     * @param expected    the predicate satisfied by the expected solutions
     * @param vars        the variables of the instance
     */
    private static void check(String constraints, Predicate<int[]> expected, Var... vars) throws Exception {
        StringBuilder xml = new StringBuilder("<instance format=\"XCSP3\" type=\"CSP\">\n<variables>\n");
        for (Var v : vars) {
            xml.append(v.xml()).append('\n');
        }
        xml.append("</variables>\n<constraints>\n").append(constraints).append("\n</constraints>\n</instance>\n");
        File file = File.createTempFile("xcsp", ".xml");
        file.deleteOnExit();
        Files.writeString(file.toPath(), xml);

        Model model = new Model();
        new XCSPParser().model(model, file.getAbsolutePath());
        IntVar[] ivars = new IntVar[vars.length];
        for (IntVar iv : model.retrieveIntVars(true)) {
            for (int i = 0; i < vars.length; i++) {
                if (vars[i].name().equals(iv.getName())) {
                    ivars[i] = iv;
                }
            }
        }
        List<String> found = new ArrayList<>();
        Solver solver = model.getSolver();
        while (solver.solve()) {
            found.add(Arrays.toString(Arrays.stream(ivars).mapToInt(IntVar::getValue).toArray()));
        }

        Set<String> expectedSolutions = new HashSet<>();
        enumerate(vars, new int[vars.length], 0, expected, expectedSolutions);
        Set<String> foundSolutions = new HashSet<>(found);
        Assert.assertEquals(foundSolutions, expectedSolutions, constraints);
        Assert.assertEquals(found.size(), foundSolutions.size(), "solutions found several times for " + constraints);
    }

    private static void enumerate(Var[] vars, int[] values, int i, Predicate<int[]> expected, Set<String> solutions) {
        if (i == vars.length) {
            if (expected.test(values)) {
                solutions.add(Arrays.toString(values));
            }
            return;
        }
        for (int v = vars[i].lb(); v <= vars[i].ub(); v++) {
            values[i] = v;
            enumerate(vars, values, i + 1, expected, solutions);
        }
    }

    private static int pow(int x, int n) {
        int r = 1;
        for (int i = 0; i < n; i++) {
            r *= x;
        }
        return r;
    }

    @Test(groups = "xcsp", timeOut = 60000)
    public void testPow() throws Exception {
        check("<intension> eq(pow(x,3),y) </intension>",
                s -> pow(s[0], 3) == s[1], var("x", -3, 3), var("y", -30, 30));
        check("<intension> eq(y,pow(x,2)) </intension>",
                s -> s[1] == pow(s[0], 2), var("x", -3, 3), var("y", -3, 9));
        check("<intension> eq(pow(x,z),y) </intension>",
                s -> pow(s[0], s[2]) == s[1], var("x", -3, 3), var("y", -3, 3), var("z", 0, 2));
        check("<intension> eq(pow(x,2),add(y,3)) </intension>",
                s -> pow(s[0], 2) == s[1] + 3, var("x", -3, 3), var("y", -3, 9));
        check("<intension> le(pow(x,y),4) </intension>",
                s -> pow(s[0], s[1]) <= 4, var("x", -3, 3), var("y", 0, 3));
    }

    /**
     * Declares the variables of a matrix, row by row.
     */
    private static Var[] matrix(String name, int rows, int cols, int lb, int ub) {
        Var[] vars = new Var[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                vars[i * cols + j] = var(name + i + "_" + j, lb, ub);
            }
        }
        return vars;
    }

    /**
     * Lists the variables of a matrix, as rows of an XCSP3 matrix.
     */
    private static String rows(String name, int rows, int cols) {
        StringBuilder st = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            st.append('(');
            for (int j = 0; j < cols; j++) {
                st.append(j > 0 ? "," : "").append(name).append(i).append('_').append(j);
            }
            st.append(')');
        }
        return st.toString();
    }

    private static boolean allDifferentExcept(int[] values, Set<Integer> except) {
        Set<Integer> seen = new HashSet<>();
        for (int v : values) {
            if (!except.contains(v) && !seen.add(v)) {
                return false;
            }
        }
        return true;
    }

    private static boolean allDifferentMatrixExcept(int[] s, int rows, int cols, Set<Integer> except) {
        for (int i = 0; i < rows; i++) {
            if (!allDifferentExcept(Arrays.copyOfRange(s, i * cols, (i + 1) * cols), except)) {
                return false;
            }
        }
        for (int j = 0; j < cols; j++) {
            int[] col = new int[rows];
            for (int i = 0; i < rows; i++) {
                col[i] = s[i * cols + j];
            }
            if (!allDifferentExcept(col, except)) {
                return false;
            }
        }
        return true;
    }

    @Test(groups = "xcsp", timeOut = 60000)
    public void testAllDifferentMatrixExcept() throws Exception {
        check("<allDifferent><matrix> " + rows("x", 3, 3) + " </matrix><except> 0 </except></allDifferent>",
                s -> allDifferentMatrixExcept(s, 3, 3, Set.of(0)), matrix("x", 3, 3, 0, 2));
        check("<allDifferent><matrix> " + rows("x", 3, 3) + " </matrix><except> 0 2 </except></allDifferent>",
                s -> allDifferentMatrixExcept(s, 3, 3, Set.of(0, 2)), matrix("x", 3, 3, 0, 2));
        check("<allDifferent><matrix> " + rows("x", 2, 3) + " </matrix><except> 1 </except></allDifferent>",
                s -> allDifferentMatrixExcept(s, 2, 3, Set.of(1)), matrix("x", 2, 3, 0, 3));
        check("<allDifferent><matrix> " + rows("x", 3, 3) + " </matrix></allDifferent>",
                s -> allDifferentMatrixExcept(s, 3, 3, Set.of()), matrix("x", 3, 3, 0, 2));
    }
}
