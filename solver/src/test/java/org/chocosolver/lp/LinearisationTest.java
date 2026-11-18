/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import gnu.trove.map.hash.TObjectIntHashMap;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.binary.PropEqualXY_C;
import org.chocosolver.solver.constraints.binary.element.ElementFactory;
import org.chocosolver.solver.constraints.binary.element.PropElement;
import org.chocosolver.solver.constraints.binary.element.PropElementBound;
import org.chocosolver.solver.constraints.binary.element.PropElementIn;
import org.chocosolver.solver.constraints.binary.PropEqualX_Y;
import org.chocosolver.solver.constraints.binary.PropEqualX_YC;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualXY_C;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_Y;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_YC;
import org.chocosolver.solver.constraints.binary.PropLessOrEqualXY_C;
import org.chocosolver.solver.constraints.binary.PropScale;
import org.chocosolver.solver.constraints.nary.alldifferent.PropAllDiffInst;
import org.chocosolver.solver.constraints.nary.alldifferent.conditions.PropCondAllDiffInst;
import org.chocosolver.solver.constraints.nary.min_max.PropBoolMax;
import org.chocosolver.solver.constraints.nary.min_max.PropBoolMin;
import org.chocosolver.solver.constraints.nary.min_max.PropMax;
import org.chocosolver.solver.constraints.nary.min_max.PropMin;
import org.chocosolver.solver.constraints.nary.sum.PropScalar;
import org.chocosolver.solver.constraints.reification.PropXeqCHalfReif;
import org.chocosolver.solver.constraints.reification.PropXeqYHalfReif;
import org.chocosolver.solver.constraints.reification.PropXgeCHalfReif;
import org.chocosolver.solver.constraints.reification.PropXinSHalfReif;
import org.chocosolver.solver.constraints.reification.PropXleCHalfReif;
import org.chocosolver.solver.constraints.reification.PropXleYHalfReif;
import org.chocosolver.solver.constraints.nary.sum.PropSum;
import org.chocosolver.solver.constraints.ternary.PropMaxBC;
import org.chocosolver.solver.constraints.ternary.PropMinBC;
import org.chocosolver.solver.constraints.ternary.PropTimesNaive;
import org.chocosolver.solver.constraints.ternary.PropXplusYeqZ;
import org.chocosolver.solver.constraints.unary.PropEqualXC;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Check that the linear form a propagator gives of itself, with
 * {@link Propagator#addLinearExpression(ILinearRelaxation, TObjectIntHashMap)}, is a relaxation: on small random
 * instances of each constraint, every solution must satisfy every row, and the linear relaxation must not
 * change the number of solutions.
 * <p>
 * A propagator that overrides that method and is not exercised here makes {@link #testAllLinearisationsAreCovered()}
 * fail: add a case for it in {@link #cases()}.
 *
 * @author Charles Prud'homme
 */
public class LinearisationTest {

    private static final int NB_SEEDS = 40;
    private static final double EPSILON = 1e-6;

    /**
     * The propagators that override the method but, as of today, add no row. Remove a class from this set when
     * its linearisation gets implemented.
     */
    private static final Set<Class<?>> NO_ROW = new HashSet<>(Arrays.asList(PropCondAllDiffInst.class));

    /**
     * Posts a random instance of a constraint.
     */
    private interface Poster {
        void post(Model model, Random rnd);
    }

    /**
     * A row {@code coeffs·x == rhs} or {@code coeffs·x <= rhs}.
     */
    private static final class Row {
        final double[] coeffs;
        final double rhs;
        final boolean equality;
        String origin;

        Row(int nbVars, double rhs, boolean equality) {
            this.coeffs = new double[nbVars];
            this.rhs = rhs;
            this.equality = equality;
        }
    }

    /**
     * A relaxation that only records the rows it is given.
     */
    private static final class Recorder implements ILinearRelaxation {
        final int nbVars;
        final List<Row> rows = new ArrayList<>();

        Recorder(int nbVars) {
            this.nbVars = nbVars;
        }

        private RowMutator add(double rhs, boolean equality) {
            Row row = new Row(nbVars, rhs, equality);
            rows.add(row);
            return (column, value) -> row.coeffs[column] += value;
        }

        @Override
        public RowMutator addEqualityConstraint(double rhs) {
            return add(rhs, true);
        }

        @Override
        public RowMutator addInequalityConstraint(double rhs) {
            return add(rhs, false);
        }

        @Override
        public void setObjective(int var, boolean maximise) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateRange(int var, int lower, int upper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Status solve() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getObjectiveValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double[] getReducedCosts() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double[] getSolution() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Number of rows added, over all the cases, by each class that overrides the method.
     */
    private final Map<Class<?>, Integer> rowsByClass = new HashMap<>();

    private static IntVar var(Model model, Random rnd) {
        int lb = rnd.nextInt(6) - 3;
        return model.intVar(lb, lb + 1 + rnd.nextInt(4), rnd.nextBoolean());
    }

    private static IntVar[] vars(Model model, Random rnd, int n) {
        IntVar[] xs = new IntVar[n];
        for (int i = 0; i < n; i++) {
            xs[i] = var(model, rnd);
        }
        return xs;
    }

    private static String operator(Random rnd, String... operators) {
        return operators[rnd.nextInt(operators.length)];
    }

    private static Object[] of(String name, boolean direct, Poster poster) {
        return new Object[]{name, direct, poster};
    }

    /**
     * Each case is a name, whether the rows can be checked one by one (false when the model holds constraints
     * that the linear relaxation is expected to leave out), and a constraint to post.
     */
    @DataProvider
    public Object[][] cases() {
        List<Object[]> cases = new ArrayList<>();
        cases.add(of("x = c", true, (m, r) -> {
            IntVar x = var(m, r);
            m.arithm(x, "=", x.getLB() + r.nextInt(3)).post();
        }));
        cases.add(of("x op y", true, (m, r) ->
                m.arithm(var(m, r), operator(r, "=", "<=", ">=", "<", ">"), var(m, r)).post()));
        cases.add(of("x op y + c", true, (m, r) ->
                m.arithm(var(m, r), operator(r, "=", "<=", ">=", "<", ">"), var(m, r),
                        operator(r, "+", "-"), 1 + r.nextInt(3)).post()));
        cases.add(of("x + y op c", true, (m, r) ->
                m.arithm(var(m, r), operator(r, "+", "-"), var(m, r),
                        operator(r, "=", "<=", ">=", "<", ">"), r.nextInt(7) - 3).post()));
        cases.add(of("x * c = z", true, (m, r) -> {
            int c = 2 + r.nextInt(2);
            IntVar x = var(m, r);
            m.times(x, c, m.intVar(c * x.getLB() - 2, c * x.getUB() + 2, r.nextBoolean())).post();
        }));
        cases.add(of("x * y = z", true, (m, r) -> {
            IntVar x = var(m, r);
            IntVar y = var(m, r);
            m.times(x, y, m.intVar(-20, 20, r.nextBoolean())).post();
        }));
        cases.add(of("element on constants", true, (m, r) -> {
            int[] table = new int[3 + r.nextInt(6)];
            for (int i = 0; i < table.length; i++) {
                table[i] = r.nextInt(13) - 6;
            }
            int offset = r.nextInt(3) - 1;
            IntVar index = m.intVar(offset - 1 + r.nextInt(2), offset + table.length - r.nextInt(2), false);
            IntVar value = m.intVar(-6 + r.nextInt(4), 6 - r.nextInt(4), true);
            switch (r.nextInt(3)) {
                case 0 -> new Constraint("ELEMENT", new PropElement(value, table, index, offset)).post();
                case 1 -> new Constraint("ELEMENT", new PropElementBound(value, table, index, offset)).post();
                default -> ElementFactory.buildReducedElementAC(value, table, m.intVar(offset, offset + table.length - 1), offset).post();
            }
        }));
        cases.add(of("x + y = z", true, (m, r) -> {
            IntVar x = var(m, r);
            IntVar y = var(m, r);
            IntVar z = m.intVar(x.getLB() + y.getLB() + 1, x.getUB() + y.getUB() - 1, r.nextBoolean());
            new Constraint("x+y=z", new PropXplusYeqZ(x, y, z)).post();
        }));
        cases.add(of("sum", true, (m, r) ->
                m.sum(vars(m, r, 3 + r.nextInt(2)), operator(r, "=", "<=", ">=", "!="), var(m, r)).post()));
        cases.add(of("sum of booleans, constant", true, (m, r) ->
                m.sum(m.boolVarArray(4 + r.nextInt(3)), operator(r, "=", "<=", ">=", "!="), 1 + r.nextInt(3)).post()));
        cases.add(of("sum of booleans, variable", true, (m, r) ->
                m.sum(m.boolVarArray(4 + r.nextInt(3)), operator(r, "=", "<=", ">=", "!="), m.intVar(1, 3)).post()));
        cases.add(of("scalar", true, (m, r) -> {
            IntVar[] xs = vars(m, r, 3 + r.nextInt(2));
            int[] coeffs = new int[xs.length];
            for (int i = 0; i < coeffs.length; i++) {
                coeffs[i] = r.nextInt(9) - 4;
            }
            m.scalar(xs, coeffs, operator(r, "=", "<=", ">=", "!="), r.nextBoolean() ? var(m, r) : m.intVar(r.nextInt(5) - 2)).post();
        }));
        cases.add(of("scalar of booleans", true, (m, r) -> {
            BoolVar[] bs = m.boolVarArray(4 + r.nextInt(3));
            int[] coeffs = new int[bs.length];
            for (int i = 0; i < coeffs.length; i++) {
                coeffs[i] = r.nextInt(9) - 4;
            }
            m.scalar(bs, coeffs, operator(r, "=", "<=", ">=", "!="), r.nextInt(5) - 2).post();
        }));
        cases.add(of("sum of views", true, (m, r) -> {
            IntVar[] xs = vars(m, r, 3);
            IntVar[] views = {m.intOffsetView(xs[0], 2), m.intMinusView(xs[1]), m.intScaleView(xs[2], 2), xs[0]};
            m.sum(views, operator(r, "=", "<=", ">="), r.nextInt(7) - 3).post();
        }));
        cases.add(of("max", true, (m, r) -> m.max(var(m, r), vars(m, r, 2 + r.nextInt(3))).post()));
        cases.add(of("min", true, (m, r) -> m.min(var(m, r), vars(m, r, 2 + r.nextInt(3))).post()));
        cases.add(of("binary max", true, (m, r) -> m.max(var(m, r), var(m, r), var(m, r)).post()));
        cases.add(of("binary min", true, (m, r) -> m.min(var(m, r), var(m, r), var(m, r)).post()));
        cases.add(of("max of booleans", true, (m, r) -> m.max(m.boolVar(), m.boolVarArray(2 + r.nextInt(3))).post()));
        cases.add(of("min of booleans", true, (m, r) -> m.min(m.boolVar(), m.boolVarArray(2 + r.nextInt(3))).post()));
        cases.add(of("b => x op c", true, (m, r) -> {
            IntVar x = var(m, r);
            int c = x.getLB() - 1 + r.nextInt(x.getUB() - x.getLB() + 3);
            BoolVar b = r.nextBoolean() ? m.boolVar() : m.boolVar().not();
            Propagator<IntVar> p = switch (r.nextInt(3)) {
                case 0 -> new PropXeqCHalfReif(x, c, b);
                case 1 -> new PropXleCHalfReif(x, c, b);
                default -> new PropXgeCHalfReif(x, c, b);
            };
            new Constraint("BASIC_REIF", p).post();
        }));
        cases.add(of("b => x op y", true, (m, r) -> {
            IntVar x = var(m, r);
            IntVar y = var(m, r);
            BoolVar b = m.boolVar();
            new Constraint("BASIC_REIF", r.nextBoolean() ? new PropXeqYHalfReif(x, y, b) : new PropXleYHalfReif(x, y, b)).post();
        }));
        cases.add(of("b => x in S", true, (m, r) -> {
            IntVar x = var(m, r);
            IntIterableRangeSet set = new IntIterableRangeSet();
            for (int v = x.getLB() - 1; v <= x.getUB() + 1; v++) {
                if (r.nextInt(3) == 0) {
                    set.add(v);
                }
            }
            if (set.isEmpty()) {
                // PropXinSHalfReif does not support an empty set
                set.add(x.getLB());
            }
            new Constraint("BASIC_REIF", new PropXinSHalfReif(x, set, m.boolVar())).post();
        }));
        cases.add(of("several b => x op c on the same variables", true, (m, r) -> {
            IntVar[] xs = vars(m, r, 3);
            BoolVar[] bs = m.boolVarArray(4);
            for (BoolVar b : bs) {
                IntVar x = xs[r.nextInt(xs.length)];
                int c = x.getLB() + r.nextInt(x.getUB() - x.getLB() + 1);
                new Constraint("BASIC_REIF", r.nextBoolean() ? new PropXeqCHalfReif(x, c, b) : new PropXleYHalfReif(x, xs[r.nextInt(xs.length)], b)).post();
            }
            m.sum(bs, ">=", 1 + r.nextInt(2)).post();
        }));
        cases.add(of("allDifferent", true, (m, r) ->
                m.allDifferent(vars(m, r, 3 + r.nextInt(2)), operator(r, "DEFAULT", "BC", "AC", "FC")).post()));
        cases.add(of("allDifferent, then sum", true, (m, r) -> {
            // the sum tightens the bounds the allDifferent relaxation is computed from
            IntVar[] xs = vars(m, r, 4);
            m.allDifferent(xs).post();
            m.sum(xs, operator(r, "<=", ">="), r.nextInt(9) - 4).post();
        }));
        cases.add(of("allDifferentExcept0", true, (m, r) -> m.allDifferentExcept0(vars(m, r, 4)).post()));
        cases.add(of("cumulative", true, (m, r) -> {
            int n = 3;
            Task[] tasks = new Task[n];
            IntVar[] heights = new IntVar[n];
            for (int i = 0; i < n; i++) {
                tasks[i] = m.taskVar(m.intVar(0, 3), m.intVar(1, 2), m.intVar(0, 5));
                heights[i] = m.intVar(1, 2);
            }
            m.cumulative(tasks, heights, m.intVar(2, 3)).post();
        }));
        // constraints the linear relaxation must leave out
        cases.add(of("reified sum", false, (m, r) -> {
            IntVar[] xs = vars(m, r, 3);
            BoolVar b = m.sum(xs, operator(r, "=", "<=", ">="), r.nextInt(7) - 3).reify();
            m.arithm(xs[0], operator(r, "<=", ">="), xs[1]).reifyWith(b);
        }));
        cases.add(of("disjunction", false, (m, r) -> {
            IntVar[] xs = vars(m, r, 3);
            m.or(m.arithm(xs[0], "+", xs[1], "<=", r.nextInt(5) - 2),
                    m.sum(xs, ">=", r.nextInt(5))).post();
        }));
        cases.add(of("implication", false, (m, r) -> {
            IntVar[] xs = vars(m, r, 3);
            m.ifThen(m.arithm(xs[0], "<=", xs[1]), m.sum(xs, "=", r.nextInt(7) - 3));
        }));
        cases.add(of("half reification", false, (m, r) -> {
            IntVar[] xs = vars(m, r, 3);
            m.sum(xs, operator(r, "=", "<=", ">="), r.nextInt(7) - 3).impliedBy(m.boolVar());
            // a constraint that always holds, so that the variables do belong to the relaxation
            m.sum(xs, ">=", -100).post();
        }));
        cases.add(of("opposite", true, (m, r) ->
                m.sum(vars(m, r, 3), operator(r, "=", "<=", ">=", "!="), r.nextInt(7) - 3).getOpposite().post()));
        return cases.toArray(new Object[0][]);
    }

    private static Class<?> declaringClass(Propagator<?> p) {
        try {
            return p.getClass().getMethod("addLinearExpression", ILinearRelaxation.class, TObjectIntHashMap.class)
                    .getDeclaringClass();
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Ask every propagator of the model for its linear form.
     *
     * @param columns filled with the variable of each column
     * @return the rows
     */
    private List<Row> linearise(Model model, List<IntVar> columns) {
        TObjectIntHashMap<IntVar> vmap = new TObjectIntHashMap<>();
        List<Propagator<?>> propagators = new ArrayList<>();
        for (Constraint c : model.getCstrs()) {
            for (Propagator<?> p : c.getPropagators()) {
                if (Arrays.stream(p.getVars()).allMatch(v -> v instanceof IntVar)) {
                    propagators.add(p);
                    for (Variable v : p.getVars()) {
                        if (!vmap.containsKey(v)) {
                            vmap.put((IntVar) v, columns.size());
                            columns.add((IntVar) v);
                        }
                    }
                }
            }
        }
        Recorder recorder = new Recorder(columns.size());
        for (Propagator<?> p : propagators) {
            int before = recorder.rows.size();
            int declared = p.addLinearExpression(recorder, vmap);
            int added = recorder.rows.size() - before;
            Assert.assertEquals(declared, added, p.getClass().getSimpleName() + ": number of rows returned");
            for (int i = before; i < recorder.rows.size(); i++) {
                recorder.rows.get(i).origin = p.getClass().getSimpleName();
            }
            Class<?> declaring = declaringClass(p);
            if (declaring != Propagator.class) {
                rowsByClass.merge(declaring, added, Integer::sum);
            }
        }
        return recorder.rows;
    }

    private static void check(String name, long seed, List<IntVar> columns, List<Row> rows) {
        for (Row row : rows) {
            double lhs = 0;
            for (int j = 0; j < row.coeffs.length; j++) {
                lhs += row.coeffs[j] * columns.get(j).getValue();
            }
            boolean satisfied = row.equality ? Math.abs(lhs - row.rhs) <= EPSILON : lhs <= row.rhs + EPSILON;
            if (!satisfied) {
                Assert.fail(String.format("%s, seed %d: the solution %s violates the row of %s: %s %s %s",
                        name, seed, columns, row.origin, Arrays.toString(row.coeffs), row.equality ? "==" : "<=", row.rhs));
            }
        }
    }

    private static Model model(String name, Poster poster, long seed, ILinearRelaxation.Solver relaxation) {
        SettingsBuilder settings = SettingsBuilder.init().setLinearRelaxation(relaxation);
        if (seed % 2 == 1) {
            // also exercise the incremental propagators of the sums of booleans
            settings.setIncrementalityOnBoolSumThreshold(2);
        }
        Model model = new Model(name, settings.build());
        poster.post(model, new Random(seed));
        return model;
    }

    @Test(groups = "10s", dataProvider = "cases")
    public void testLinearisationIsARelaxation(String name, boolean direct, Poster poster) {
        for (long seed = 0; seed < NB_SEEDS; seed++) {
            Model model = model(name, poster, seed, null);
            List<Row> rows = new ArrayList<>();
            List<IntVar> columns = new ArrayList<>();
            if (direct) {
                // the rows are computed twice, from the declared domains and from the domains at the root node
                rows.addAll(linearise(model, columns));
                Model atRoot = model(name, poster, seed, null);
                try {
                    atRoot.getSolver().propagate();
                    List<IntVar> rootColumns = new ArrayList<>();
                    rows.addAll(linearise(atRoot, rootColumns));
                    Assert.assertEquals(rootColumns.size(), columns.size());
                } catch (ContradictionException e) {
                    // no solution, nothing more to check
                }
            }
            long expected = 0;
            while (model.getSolver().solve()) {
                expected++;
                check(name, seed, columns, rows);
            }
            Model relaxed = model(name, poster, seed, ILinearRelaxation.Solver.OJALGO);
            long found = 0;
            while (relaxed.getSolver().solve()) {
                found++;
            }
            Assert.assertEquals(found, expected, name + ", seed " + seed + ": number of solutions with the relaxation");
        }
    }

    /**
     * @return the propagators of the solver that override the linearisation method
     */
    private static Set<Class<?>> overriders() throws IOException, URISyntaxException {
        Path root = new File(Propagator.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toPath();
        if (!Files.isDirectory(root)) {
            throw new SkipException("The classes of the solver are not in a directory: " + root);
        }
        Set<Class<?>> found = new HashSet<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".class")).collect(Collectors.toList())) {
                String name = root.relativize(file).toString();
                name = name.substring(0, name.length() - ".class".length()).replace(File.separatorChar, '.');
                if (name.endsWith("module-info")) {
                    continue;
                }
                try {
                    Class<?> c = Class.forName(name, false, Propagator.class.getClassLoader());
                    if (c != Propagator.class && Propagator.class.isAssignableFrom(c)) {
                        c.getDeclaredMethod("addLinearExpression", ILinearRelaxation.class, TObjectIntHashMap.class);
                        found.add(c);
                    }
                } catch (ClassNotFoundException | NoSuchMethodException | LinkageError e) {
                    // not a propagator that overrides the method, or a class that needs an optional library
                }
            }
        }
        return found;
    }

    /**
     * Every propagator that overrides the linearisation method must have been exercised by
     * {@link #testLinearisationIsARelaxation(String, boolean, Poster)}, and must have added rows unless it is
     * known not to.
     */
    @Test(groups = "10s", dependsOnMethods = "testLinearisationIsARelaxation")
    public void testAllLinearisationsAreCovered() throws IOException, URISyntaxException {
        Set<Class<?>> overriders = overriders();
        // guard against a scan that silently finds nothing
        for (Class<?> known : Arrays.asList(PropEqualXC.class, PropEqualX_Y.class, PropEqualX_YC.class,
                PropEqualXY_C.class, PropGreaterOrEqualX_Y.class, PropGreaterOrEqualX_YC.class,
                PropGreaterOrEqualXY_C.class, PropLessOrEqualXY_C.class, PropScale.class, PropXplusYeqZ.class,
                PropSum.class, PropScalar.class, PropMax.class, PropMin.class, PropBoolMax.class, PropBoolMin.class,
                PropMaxBC.class, PropMinBC.class, PropTimesNaive.class,
                PropElement.class, PropElementBound.class, PropElementIn.class,
                PropXeqCHalfReif.class, PropXleCHalfReif.class, PropXgeCHalfReif.class, PropXeqYHalfReif.class,
                PropXleYHalfReif.class, PropXinSHalfReif.class,
                PropAllDiffInst.class)) {
            Assert.assertTrue(overriders.contains(known), known.getSimpleName() + " not found by the scan");
        }
        Set<String> report = new TreeSet<>();
        for (Class<?> c : overriders) {
            Integer rows = rowsByClass.get(c);
            Assert.assertNotNull(rows, c.getSimpleName() + " overrides addLinearExpression but no case exercises it");
            if (NO_ROW.contains(c)) {
                Assert.assertEquals(rows.intValue(), 0, c.getSimpleName() + " now adds rows: remove it from NO_ROW");
            } else {
                Assert.assertTrue(rows > 0, c.getSimpleName() + " never added a row");
            }
            report.add(c.getSimpleName() + "=" + rows);
        }
        System.err.println("linearisations checked, rows per class: " + report);
    }
}
