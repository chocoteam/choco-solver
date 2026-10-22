/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.xcsp;

import org.chocosolver.parser.ParserException;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
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

    private static boolean rel(int a, String op, int b) {
        switch (op) {
            case "lt":
                return a < b;
            case "le":
                return a <= b;
            case "ge":
                return a >= b;
            case "gt":
                return a > b;
            default:
                throw new IllegalArgumentException(op);
        }
    }

    @Test(groups = "xcsp", timeOut = 60000)
    public void testOrderedWithLengths() throws Exception {
        Var[] x = {var("x0", 0, 2), var("x1", 0, 2), var("x2", 0, 2)};
        for (String op : new String[]{"lt", "le", "ge", "gt"}) {
            check("<ordered><list> x0 x1 x2 </list><operator> " + op + " </operator></ordered>",
                    s -> rel(s[0], op, s[1]) && rel(s[1], op, s[2]), x);
            for (int[] l : new int[][]{{1, 1}, {0, 0}, {-1, -1}, {2, -1}}) {
                check("<ordered><list> x0 x1 x2 </list><lengths> " + l[0] + " " + l[1] + " </lengths>"
                                + "<operator> " + op + " </operator></ordered>",
                        s -> rel(s[0] + l[0], op, s[1]) && rel(s[1] + l[1], op, s[2]), x);
            }
            check("<ordered><list> x0 x1 x2 </list><lengths> y0 y1 </lengths><operator> " + op + " </operator></ordered>",
                    s -> rel(s[0] + s[3], op, s[1]) && rel(s[1] + s[4], op, s[2]),
                    x[0], x[1], x[2], var("y0", -1, 1), var("y1", 0, 1));
        }
    }

    @Test(groups = "xcsp", timeOut = 60000)
    public void testSumWithSetCondition() throws Exception {
        Var[] x = {var("x0", 0, 2), var("x1", 0, 2), var("x2", 0, 2), var("x3", 0, 2)};
        String list = "<sum><list> x0 x1 x2 x3 </list>";
        check(list + "<condition> (notin,2..4) </condition></sum>",
                s -> Arrays.stream(s).sum() < 2 || Arrays.stream(s).sum() > 4, x);
        check(list + "<condition> (notin,{1,3,5}) </condition></sum>",
                s -> !Set.of(1, 3, 5).contains(Arrays.stream(s).sum()), x);
        check(list + "<condition> (notin,0..8) </condition></sum>",
                s -> false, x);
        check(list + "<coeffs> 1 2 3 4 </coeffs><condition> (notin,{5,6}) </condition></sum>",
                s -> !Set.of(5, 6).contains(s[0] + 2 * s[1] + 3 * s[2] + 4 * s[3]), x);
        check(list + "<condition> (in,2..4) </condition></sum>",
                s -> Arrays.stream(s).sum() >= 2 && Arrays.stream(s).sum() <= 4, x);
        check(list + "<condition> (in,{1,3,5}) </condition></sum>",
                s -> Set.of(1, 3, 5).contains(Arrays.stream(s).sum()), x);
    }

    private static int count(int[] values, int value) {
        return (int) Arrays.stream(values).filter(v -> v == value).count();
    }

    @Test(groups = "xcsp", timeOut = 60000)
    public void testCountWithSetCondition() throws Exception {
        Var[] x = {var("x0", 0, 2), var("x1", 0, 2), var("x2", 0, 2)};
        String list = "<count><list> x0 x1 x2 </list><values> 1 </values>";
        check(list + "<condition> (in,1..2) </condition></count>",
                s -> count(s, 1) >= 1 && count(s, 1) <= 2, x);
        check(list + "<condition> (notin,1..2) </condition></count>",
                s -> count(s, 1) < 1 || count(s, 1) > 2, x);
        check(list + "<condition> (in,5..8) </condition></count>",
                s -> false, x);
        check(list + "<condition> (notin,5..8) </condition></count>",
                s -> true, x);
        check(list + "<condition> (in,{1,2}) </condition></count>",
                s -> count(s, 1) >= 1 && count(s, 1) <= 2, x);
        check(list + "<condition> (notin,{0,3}) </condition></count>",
                s -> count(s, 1) >= 1 && count(s, 1) <= 2, x);
    }

    @Test(groups = "xcsp", timeOut = 60000)
    public void testMod() throws Exception {
        // rounding towards 0, as in Java
        check("<intension> eq(mod(x,3),y) </intension>",
                s -> s[0] % 3 == s[1], var("x", -3, 3), var("y", -3, 3));
        check("<intension> eq(y,mod(x,2)) </intension>",
                s -> s[1] == s[0] % 2, var("x", -3, 3), var("y", -3, 3));
        check("<intension> eq(mod(x,-3),y) </intension>",
                s -> s[0] % -3 == s[1], var("x", -3, 3), var("y", -3, 3));
        check("<intension> eq(div(x,2),y) </intension>",
                s -> s[0] / 2 == s[1], var("x", -3, 3), var("y", -3, 3));
    }

    private static int nValues(int... values) {
        return (int) Arrays.stream(values).distinct().count();
    }

    @Test(groups = "xcsp", timeOut = 60000)
    public void testNValuesOnExpressions() throws Exception {
        Var[] x = {var("x0", 0, 2), var("x1", 0, 2), var("x2", 0, 2)};
        check("<nValues><list> add(x0,1) add(x1,1) add(x2,1) </list><condition> (gt,1) </condition></nValues>",
                s -> nValues(s) > 1, x);
        check("<nValues><list> add(x0,1) add(x1,1) add(x2,1) </list><condition> (le,2) </condition></nValues>",
                s -> nValues(s) <= 2, x);
        check("<nValues><list> add(x0,1) add(x1,1) add(x2,1) </list><condition> (eq,2) </condition></nValues>",
                s -> nValues(s) == 2, x);
        check("<nValues><list> x0 x1 add(x2,1) </list><condition> (gt,1) </condition></nValues>",
                s -> nValues(s[0], s[1], s[2] + 1) > 1, x);
        check("<nValues><list> x0 x1 x2 </list><condition> (gt,1) </condition></nValues>",
                s -> nValues(s) > 1, x);
    }

    private static boolean lex(int[] a, String op, int[] b) {
        int c = Arrays.compare(a, b);
        return rel(c, op, 0);
    }

    private static boolean lexMatrix(int[] s, int rows, int cols, String op) {
        int[][] m = new int[rows][cols];
        int[][] t = new int[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m[i][j] = t[j][i] = s[i * cols + j];
            }
        }
        for (int i = 0; i < rows - 1; i++) {
            if (!lex(m[i], op, m[i + 1])) {
                return false;
            }
        }
        for (int j = 0; j < cols - 1; j++) {
            if (!lex(t[j], op, t[j + 1])) {
                return false;
            }
        }
        return true;
    }

    @Test(groups = "xcsp", timeOut = 60000)
    public void testLexMatrix() throws Exception {
        for (int[] size : new int[][]{{2, 2}, {2, 3}, {3, 2}, {3, 3}}) {
            for (String op : new String[]{"lt", "le", "ge", "gt"}) {
                check("<lex><matrix> " + rows("x", size[0], size[1]) + " </matrix><operator> " + op + " </operator></lex>",
                        s -> lexMatrix(s, size[0], size[1], op), matrix("x", size[0], size[1], 0, 1));
            }
        }
    }

    private static int in12(int v) {
        return v == 1 || v == 2 ? 1 : 0;
    }

    @Test(groups = "xcsp", timeOut = 60000)
    public void testNaryIffAndXor() throws Exception {
        Var[] vars = {var("x", -3, 3), var("y", -3, 3), var("b", 0, 1)};
        // all the arguments are equivalent
        check("<intension> iff(gt(x,0),gt(y,0),b) </intension>",
                v -> (v[0] > 0) == (v[1] > 0) && (v[1] > 0) == (v[2] == 1), vars);
        // an odd number of arguments are true
        check("<intension> xor(gt(x,0),gt(y,0),b) </intension>",
                v -> ((v[0] > 0 ? 1 : 0) + (v[1] > 0 ? 1 : 0) + v[2]) % 2 == 1, vars);
    }

    @Test(groups = "xcsp", timeOut = 60000)
    public void testSumAndCountOfMembership() throws Exception {
        Var[] x = {var("x0", 0, 2), var("x1", 0, 2)};
        check("<sum><list> in(x0,set(1,2)) in(x1,set(1,2)) </list><condition> (le,1) </condition></sum>",
                s -> in12(s[0]) + in12(s[1]) <= 1, x);
        check("<sum><list> in(x0,set(1,2)) in(x1,set(1,2)) </list><coeffs> 2 1 </coeffs>"
                        + "<condition> (le,1) </condition></sum>",
                s -> 2 * in12(s[0]) + in12(s[1]) <= 1, x);
        check("<sum><list> in(x0,set(1,2)) x1 </list><condition> (le,0) </condition></sum>",
                s -> in12(s[0]) + s[1] <= 0, x);
        check("<count><list> in(x0,set(1,2)) in(x1,set(1,2)) </list><values> 1 </values>"
                        + "<condition> (eq,1) </condition></count>",
                s -> in12(s[0]) + in12(s[1]) == 1, x);
        check("<sum><list> notin(x0,set(1,2)) notin(x1,set(1,2)) </list><condition> (le,1) </condition></sum>",
                s -> (1 - in12(s[0])) + (1 - in12(s[1])) <= 1, x);
        check("<intension> le(add(in(x0,set(1,2)),in(x1,set(1,2))),1) </intension>",
                s -> in12(s[0]) + in12(s[1]) <= 1, x);
    }

    @DataProvider(name = "reifiable")
    public Object[][] reifiable() {
        String l = "<list> x0 x1 x2 </list>";
        return new Object[][]{
                {"<element %s>" + l + "<value> 1 </value></element>",
                        (Predicate<int[]>) v -> v[0] == 1 || v[1] == 1 || v[2] == 1},
                {"<element %s>" + l + "<condition> (ge,2) </condition></element>",
                        (Predicate<int[]>) v -> v[0] >= 2 || v[1] >= 2 || v[2] >= 2},
                {"<element %s>" + l + "<condition> (ne,1) </condition></element>",
                        (Predicate<int[]>) v -> v[0] != 1 || v[1] != 1 || v[2] != 1},
                {"<element %s>" + l + "<condition> (lt,1) </condition></element>",
                        (Predicate<int[]>) v -> v[0] < 1 || v[1] < 1 || v[2] < 1},
                {"<allDifferent %s> x0 x1 x2 </allDifferent>",
                        (Predicate<int[]>) v -> v[0] != v[1] && v[0] != v[2] && v[1] != v[2]},
                {"<allEqual %s> x0 x1 x2 </allEqual>",
                        (Predicate<int[]>) v -> v[0] == v[1] && v[1] == v[2]},
                {"<intension %s> lt(x0,add(x1,x2)) </intension>",
                        (Predicate<int[]>) v -> v[0] < v[1] + v[2]},
                {"<intension %s> and(lt(x0,x1),ne(x1,x2)) </intension>",
                        (Predicate<int[]>) v -> v[0] < v[1] && v[1] != v[2]},
                {"<extension %s>" + l + "<supports> (0,1,2)(1,1,1)(2,0,0) </supports></extension>",
                        (Predicate<int[]>) v -> (v[0] == 0 && v[1] == 1 && v[2] == 2) || (v[0] == 1 && v[1] == 1 && v[2] == 1) || (v[0] == 2 && v[1] == 0 && v[2] == 0)},
                {"<extension %s>" + l + "<conflicts> (0,1,2)(1,1,1) </conflicts></extension>",
                        (Predicate<int[]>) v -> !((v[0] == 0 && v[1] == 1 && v[2] == 2) || (v[0] == 1 && v[1] == 1 && v[2] == 1))},
                {"<sum %s>" + l + "<condition> (le,3) </condition></sum>",
                        (Predicate<int[]>) v -> v[0] + v[1] + v[2] <= 3},
                {"<sum %s>" + l + "<coeffs> 1 2 3 </coeffs><condition> (eq,6) </condition></sum>",
                        (Predicate<int[]>) v -> v[0] + 2 * v[1] + 3 * v[2] == 6},
                {"<sum %s><list> x0 x1 </list><condition> (eq,x2) </condition></sum>",
                        (Predicate<int[]>) v -> v[0] + v[1] == v[2]},
                {"<ordered %s>" + l + "<operator> lt </operator></ordered>",
                        (Predicate<int[]>) v -> v[0] < v[1] && v[1] < v[2]},
                {"<ordered %s>" + l + "<operator> le </operator></ordered>",
                        (Predicate<int[]>) v -> v[0] <= v[1] && v[1] <= v[2]},
                {"<count %s>" + l + "<values> 1 </values><condition> (eq,2) </condition></count>",
                        (Predicate<int[]>) v -> (v[0] == 1 ? 1 : 0) + (v[1] == 1 ? 1 : 0) + (v[2] == 1 ? 1 : 0) == 2},
                {"<nValues %s>" + l + "<condition> (eq,2) </condition></nValues>",
                        (Predicate<int[]>) v -> Arrays.stream(v, 0, 3).distinct().count() == 2},
                {"<maximum %s>" + l + "<condition> (eq,1) </condition></maximum>",
                        (Predicate<int[]>) v -> Math.max(v[0], Math.max(v[1], v[2])) == 1},
                {"<element %s>" + l + "<index> x0 </index><value> 1 </value></element>",
                        (Predicate<int[]>) v -> v[v[0]] == 1},
                {"<lex %s><list> x0 x1 </list><list> x1 x2 </list><operator> lt </operator></lex>",
                        (Predicate<int[]>) v -> v[0] < v[1] || (v[0] == v[1] && v[1] < v[2])},
                {"<cardinality %s>" + l + "<values> 0 1 2 </values><occurs> 1 1 1 </occurs></cardinality>",
                        (Predicate<int[]>) v -> v[0] != v[1] && v[0] != v[2] && v[1] != v[2]},
                {"<channel %s>" + l + "</channel>",
                        (Predicate<int[]>) v -> v[v[0]] == 0 && v[v[1]] == 1 && v[v[2]] == 2},
                {"<noOverlap %s><origins> x0 x1 x2 </origins><lengths> 1 1 1 </lengths></noOverlap>",
                        (Predicate<int[]>) v -> v[0] != v[1] && v[0] != v[2] && v[1] != v[2]},
                {"<sum %s>" + l + "<condition> (in,2..3) </condition></sum>",
                        (Predicate<int[]>) v -> v[0] + v[1] + v[2] >= 2 && v[0] + v[1] + v[2] <= 3},
                {"<sum %s>" + l + "<condition> (notin,2..3) </condition></sum>",
                        (Predicate<int[]>) v -> v[0] + v[1] + v[2] < 2 || v[0] + v[1] + v[2] > 3},
                {"<sum %s>" + l + "<coeffs> 1 2 3 </coeffs><condition> (in,{2,5,9}) </condition></sum>",
                        (Predicate<int[]>) v -> v[0] + 2 * v[1] + 3 * v[2] == 2 || v[0] + 2 * v[1] + 3 * v[2] == 5
                                || v[0] + 2 * v[1] + 3 * v[2] == 9},
                {"<sum %s>" + l + "<condition> (notin,{0,3,6}) </condition></sum>",
                        (Predicate<int[]>) v -> (v[0] + v[1] + v[2]) % 3 != 0},
                {"<count %s>" + l + "<values> 1 </values><condition> (ge,1) </condition></count>",
                        (Predicate<int[]>) v -> v[0] == 1 || v[1] == 1 || v[2] == 1},
                {"<count %s>" + l + "<values> 0 2 </values><condition> (in,1..2) </condition></count>",
                        (Predicate<int[]>) v -> Arrays.stream(v, 0, 3).filter(a -> a != 1).count() >= 1
                                && Arrays.stream(v, 0, 3).filter(a -> a != 1).count() <= 2},
                {"<count %s><list> x0 x1 </list><values> 1 </values><condition> (ne,x2) </condition></count>",
                        (Predicate<int[]>) v -> (v[0] == 1 ? 1 : 0) + (v[1] == 1 ? 1 : 0) != v[2]},
                {"<nValues %s>" + l + "<condition> (gt,1) </condition></nValues>",
                        (Predicate<int[]>) v -> Arrays.stream(v, 0, 3).distinct().count() > 1},
                {"<minimum %s>" + l + "<condition> (ge,1) </condition></minimum>",
                        (Predicate<int[]>) v -> Math.min(v[0], Math.min(v[1], v[2])) >= 1},
                {"<minimum %s><list> x0 x1 </list><condition> (lt,x2) </condition></minimum>",
                        (Predicate<int[]>) v -> Math.min(v[0], v[1]) < v[2]},
                {"<maximum %s>" + l + "<condition> (notin,{1}) </condition></maximum>",
                        (Predicate<int[]>) v -> Math.max(v[0], Math.max(v[1], v[2])) != 1},
                {"<intension %s> eq(add(x0,mul(x1,x2)),3) </intension>",
                        (Predicate<int[]>) v -> v[0] + v[1] * v[2] == 3},
                {"<intension %s> or(eq(x0,x1),gt(x1,x2)) </intension>",
                        (Predicate<int[]>) v -> v[0] == v[1] || v[1] > v[2]},
                {"<intension %s> imp(eq(x0,1),ne(dist(x1,x2),1)) </intension>",
                        (Predicate<int[]>) v -> v[0] != 1 || Math.abs(v[1] - v[2]) != 1},
                {"<intension %s> iff(gt(x0,0),gt(x1,0),gt(x2,0)) </intension>",
                        (Predicate<int[]>) v -> (v[0] > 0) == (v[1] > 0) && (v[1] > 0) == (v[2] > 0)},
                {"<intension %s> xor(gt(x0,0),gt(x1,0),gt(x2,0)) </intension>",
                        (Predicate<int[]>) v -> ((v[0] > 0 ? 1 : 0) + (v[1] > 0 ? 1 : 0) + (v[2] > 0 ? 1 : 0)) % 2 == 1},
                {"<count %s>" + l + "<values> 1 </values><condition> (le,1) </condition></count>",
                        (Predicate<int[]>) v -> Arrays.stream(v, 0, 3).filter(a -> a == 1).count() <= 1},
        };
    }

    @Test(groups = "xcsp", dataProvider = "reifiable")
    public void testReification(String constraint, Predicate<int[]> c) throws Exception {
        Var[] vars = {var("x0", 0, 2), var("x1", 0, 2), var("x2", 0, 2), var("r", 0, 1)};
        // r <=> c
        check(String.format(constraint, "reifiedBy=\"r\""), v -> (v[3] == 1) == c.test(v), vars);
        // r => c
        check(String.format(constraint, "hreifiedFrom=\"r\""), v -> v[3] == 0 || c.test(v), vars);
        // c => r
        check(String.format(constraint, "hreifiedTo=\"r\""), v -> v[3] == 1 || !c.test(v), vars);
    }

    @DataProvider(name = "notReifiable")
    public Object[][] notReifiable() {
        String l = "<list> x0 x1 x2 </list>";
        return new Object[][]{
                {"<regular %s>" + l + "<transitions> (a,0,a)(a,1,b)(b,1,b) </transitions><start> a </start><final> b </final></regular>"},
                {"<slide %s>" + l + "<intension> lt(%%0,%%1) </intension></slide>"},
                {"<and %s><intension> lt(x0,x1) </intension><intension> lt(x1,x2) </intension></and>"},
        };
    }

    @Test(groups = "xcsp", dataProvider = "notReifiable", expectedExceptions = ParserException.class)
    public void testReificationNotSupported(String constraint) throws Exception {
        // constraints that rely on auxiliary variables and meta-constraints:
        // their reification must be refused, not ignored
        check(String.format(constraint, "reifiedBy=\"r\""), v -> true,
                var("x0", 0, 2), var("x1", 0, 2), var("x2", 0, 2), var("r", 0, 1));
    }

    @Test(groups = "xcsp")
    public void testElementWithCondition() throws Exception {
        Var[] vars = {var("x0", 0, 2), var("x1", 0, 2), var("x2", 0, 2)};
        String l = "<list> x0 x1 x2 </list>";
        check("<element>" + l + "<value> 1 </value></element>", v -> v[0] == 1 || v[1] == 1 || v[2] == 1, vars);
        check("<element>" + l + "<condition> (ge,2) </condition></element>", v -> v[0] >= 2 || v[1] >= 2 || v[2] >= 2, vars);
        check("<element>" + l + "<condition> (gt,1) </condition></element>", v -> v[0] > 1 || v[1] > 1 || v[2] > 1, vars);
        check("<element>" + l + "<condition> (le,0) </condition></element>", v -> v[0] <= 0 || v[1] <= 0 || v[2] <= 0, vars);
        check("<element>" + l + "<condition> (lt,1) </condition></element>", v -> v[0] < 1 || v[1] < 1 || v[2] < 1, vars);
        check("<element>" + l + "<condition> (ne,1) </condition></element>", v -> v[0] != 1 || v[1] != 1 || v[2] != 1, vars);
    }

    @Test(groups = "xcsp")
    public void testCardinalityClosedWithVariablesAsValues() throws Exception {
        // #1260
        Var[] vars = {var("x0", 0, 2), var("x1", 0, 2), var("x2", 0, 2), var("v", 0, 2), var("w", 0, 2)};
        String c = "<cardinality><list> x0 x1 x2 </list><values closed=\"true\"> v w </values><occurs> %s </occurs></cardinality>";
        Predicate<int[]> closed = s -> Arrays.stream(s, 0, 3).allMatch(x -> x == s[3] || x == s[4]);
        check(String.format(c, "0..3 0..3"), closed, vars);
        check(String.format(c, "1 0..3"), s -> closed.test(s) && Arrays.stream(s, 0, 3).filter(x -> x == s[3]).count() == 1, vars);
        check(String.format(c, "1 2"), s -> closed.test(s)
                && Arrays.stream(s, 0, 3).filter(x -> x == s[3]).count() == 1
                && Arrays.stream(s, 0, 3).filter(x -> x == s[4]).count() == 2, vars);
    }
}
