/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.spec;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.spec.ModelSpec;
import org.chocosolver.solver.spec.Recorder;
import org.chocosolver.solver.spec.SharedObjects;
import org.chocosolver.solver.spec.SpecException;
import org.chocosolver.solver.spec.Step;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Equivalence bench of {@link ModelSpec}: each instance is parsed into a plain model and into a model recorded
 * with {@link Model#record}, whose spec is instantiated twice. All the models must have the same structure and the
 * same search (solutions, nodes, fails), and the instances must not share mutable objects with each other or with
 * the recorded model.
 * <p>
 * Instances which cannot be journalized are skipped and reported in {@code target/spec-coverage.md}.
 * <p>
 * System properties:
 * <ul>
 *     <li>{@code spec.dir}: a directory of instances (.fzn, .xml, .xml.lzma) to use instead of the test
 *     resources;</li>
 *     <li>{@code spec.nodes}: node limit of each resolution (default: 1000);</li>
 *     <li>{@code spec.sharing}: whether shared objects are looked for (default: true);</li>
 *     <li>{@code spec.sharing.maxsize}: maximum number of variables and constraints of a model in which shared
 *     objects are looked for (default: 20000);</li>
 *     <li>{@code spec.filter}: regular expression selecting the instances by file name (default: all).</li>
 * </ul>
 *
 * @author Charles Prud'homme
 */
public class SpecEquivalenceTest {

    private static final long NODES = Long.getLong("spec.nodes", 1000);

    /**
     * Whether shared objects are looked for (memory consuming on large instances).
     */
    private static final boolean SHARING = Boolean.parseBoolean(System.getProperty("spec.sharing", "true"));

    /**
     * Shared objects are not looked for in models with more variables and constraints (memory consuming).
     */
    private static final int SHARING_MAX_SIZE = Integer.getInteger("spec.sharing.maxsize", 20_000);

    /**
     * Regular expression selecting the instances by name.
     */
    private static final String FILTER = System.getProperty("spec.filter", ".*");

    /**
     * Two models parsed independently own their tuples and diagrams: not worth traversing to find the (global)
     * objects they already share.
     */
    private static final Set<Class<?>> BASELINE_ALLOWED = Set.of(PrintStream.class,
            org.chocosolver.solver.constraints.extension.Tuples.class,
            org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram.class);

    /**
     * Types which may be shared by models: immutable, or shared by design.
     */
    private static final Set<Class<?>> ALLOWED = Set.of(PrintStream.class);

    // coverage, aggregated over instances
    private final Map<String, Integer> unsupported = new TreeMap<>();
    private final List<String> skipped = new ArrayList<>();
    private final Map<String, Integer> usage = new TreeMap<>();
    private final Map<String, Integer> baseline = new TreeMap<>();
    private final List<String> sharingSkipped = new ArrayList<>();
    private int passed;

    @DataProvider
    public Object[][] instances() throws IOException {
        List<Path> roots = new ArrayList<>();
        String dir = System.getProperty("spec.dir");
        if (dir != null) {
            roots.add(Paths.get(dir));
        } else {
            roots.add(Paths.get(Objects.requireNonNull(getClass().getResource("/flatzinc")).getPath()));
            roots.add(Paths.get(Objects.requireNonNull(getClass().getResource("/xcsp")).getPath()));
        }
        List<Object[]> res = new ArrayList<>();
        for (Path root : roots) {
            try (Stream<Path> files = Files.walk(root)) {
                files.map(Path::toString)
                        .filter(f -> f.endsWith(".fzn") || f.endsWith(".xml") || f.endsWith(".xml.lzma"))
                        .filter(f -> Paths.get(f).getFileName().toString().matches(FILTER))
                        .sorted()
                        .forEach(f -> res.add(new Object[]{root.relativize(Paths.get(f)).toString(), f}));
            }
        }
        return res.toArray(new Object[0][]);
    }

    @Test(groups = "spec", dataProvider = "instances", timeOut = 300_000)
    public void testEquivalence(String name, String file) throws Exception {
        progress(name);
        // the models are kept alive only when needed, to save memory on large instances
        Model direct = build(file, false);
        String expected = structure(direct);
        Set<Object> baseline = Set.of();
        boolean sharing = SHARING && direct.getNbVars() + direct.getNbCstrs() <= SHARING_MAX_SIZE;
        if (!sharing) {
            reportSharingSkipped(name);
        } else {
            // objects already shared by two models parsed independently (global singletons): excused by identity
            baseline = SharedObjects.common(direct, build(file, false), BASELINE_ALLOWED, List.of());
            reportBaseline(baseline);
        }
        String stats = solve(direct);
        direct = null;

        Model recorded = build(file, true);
        ModelSpec spec;
        try {
            spec = recorded.getRecorder().snapshot();
        } catch (SpecException e) {
            reportUnsupported(name, recorded.getRecorder());
            throw new SkipException("cannot be journalized: " + recorded.getRecorder().unsupported().get(0));
        }
        assertSame(structure(recorded), expected, "recording changes the model");
        List<Object> excused = new ArrayList<>(SharedObjects.constantsOf(spec));
        excused.addAll(baseline);
        List<SharedObjects.Shared> shared = new ArrayList<>();
        Model i1 = spec.instantiate().model();
        assertSame(structure(i1), expected, "the instantiated model differs");
        if (sharing) {
            shared.addAll(SharedObjects.between(recorded, i1, ALLOWED, excused));
        }
        recorded = null;
        Model i2 = spec.instantiate().model();
        if (sharing) {
            shared.addAll(SharedObjects.between(i1, i2, ALLOWED, excused));
            Assert.assertTrue(shared.isEmpty(), "shared mutable objects: " + shared);
        }

        // same search, the instances being solved concurrently
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<String> f1 = pool.submit(() -> solve(i1));
            Future<String> f2 = pool.submit(() -> solve(i2));
            Assert.assertEquals(f1.get(), stats, "the instantiated model searches differently");
            Assert.assertEquals(f2.get(), stats, "the instantiated model searches differently");
        } finally {
            pool.shutdownNow();
        }
        reportUsage(spec);
    }

    /**
     * Log the instance being tested, to identify it if the JVM crashes.
     */
    private static void progress(String name) throws IOException {
        if (!Boolean.parseBoolean(System.getProperty("spec.progress", "true"))) {
            return;
        }
        Path log = Paths.get(System.getProperty("user.dir"), "target", "spec-progress.log");
        Files.createDirectories(log.getParent());
        Files.writeString(log, name + "\n", java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
    }

    /**
     * Compare two structures, reporting the first different line.
     */
    private static void assertSame(String actual, String expected, String message) {
        if (!actual.equals(expected)) {
            String[] a = actual.split("\n");
            String[] e = expected.split("\n");
            int i = 0;
            while (i < a.length && i < e.length && a[i].equals(e[i])) {
                i++;
            }
            Assert.fail(message + ", line " + i + ": expected <" + (i < e.length ? e[i] : "EOF")
                    + "> but was <" + (i < a.length ? a[i] : "EOF") + ">");
        }
    }

    /**
     * Parse <i>file</i> into a plain model, or into a recorded model.
     */
    private static Model build(String file, boolean record) {
        try {
            return SpecParsers.parse(file, 1, record).getModel();
        } catch (IllegalStateException e) {
            throw new SkipException(e.getMessage());
        }
    }

    /**
     * @return a description of the structure of the model: variables and constraints, in order
     */
    private static String structure(Model m) {
        StringBuilder sb = new StringBuilder();
        sb.append(m.getNbVars()).append(" vars, ").append(m.getNbCstrs()).append(" cstrs, objective: ")
                .append(m.getObjective() == null ? "none" : m.getObjective().getName()).append('\n');
        for (Variable v : m.getVars()) {
            sb.append(v.getClass().getSimpleName()).append(' ').append(v.getName());
            if (v instanceof IntVar) {
                sb.append(' ').append(((IntVar) v).getLB()).append("..").append(((IntVar) v).getUB())
                        .append(" #").append(((IntVar) v).getDomainSize());
            }
            sb.append('\n');
        }
        m.getGroups().forEach(g -> sb.append("group ").append(g.getName()).append(Arrays.stream(g.getVariables())
                .map(Variable::getName).collect(Collectors.joining(",", "[", "]"))).append('\n'));
        for (Constraint c : m.getCstrs()) {
            sb.append(c.getName()).append(Arrays.stream(c.getPropagators())
                    .map(p -> p.getClass().getSimpleName() + "/" + p.getNbVars())
                    .collect(Collectors.joining(",", "[", "]"))).append('\n');
        }
        return sb.toString();
    }

    /**
     * Solve with the default (deterministic) search, limited in nodes.
     */
    private static String solve(Model m) {
        Solver s = m.getSolver();
        Search.defaultSearch(m);
        s.limitNode(NODES);
        Number best = null;
        while (s.solve()) {
            if (m.getObjective() != null) {
                best = s.getBestSolutionValue();
            }
        }
        return s.getSolutionCount() + " sols, " + s.getNodeCount() + " nodes, " + s.getFailCount() + " fails"
                + (best == null ? "" : ", best=" + best);
    }

    private synchronized void reportUnsupported(String name, Recorder recorder) {
        skipped.add(name + " (" + recorder.unsupported().size() + ")");
        Set<String> reasons = new HashSet<>();
        for (Recorder.Unsupported u : recorder.unsupported()) {
            reasons.add(u.operation() + ": " + u.reason().replaceAll("variable \\S+", "variable _"));
        }
        reasons.forEach(r -> unsupported.merge(r, 1, Integer::sum));
    }

    private synchronized void reportSharingSkipped(String name) {
        sharingSkipped.add(name);
    }

    private synchronized void reportBaseline(Set<Object> objects) {
        objects.stream().map(o -> o.getClass().getName()).distinct().forEach(t -> baseline.merge(t, 1, Integer::sum));
    }

    private synchronized void reportUsage(ModelSpec spec) {
        passed++;
        Set<String> used = new HashSet<>();
        for (Step s : spec.steps()) {
            used.add(s instanceof Step.Call ? ((Step.Call) s).method().name() : s.getClass().getSimpleName());
        }
        used.forEach(u -> usage.merge(u, 1, Integer::sum));
    }

    /**
     * @return the kinds of steps used by the journalized instances, and the instances whose sharing was not
     * checked
     */
    synchronized String usageSummary() {
        return String.join(",", usage.keySet()) + (sharingSkipped.isEmpty() ? "" : " [sharing not checked]");
    }

    @AfterClass(alwaysRun = true)
    public void writeReport() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# ModelSpec coverage\n\n")
                .append("- journalized and equivalent: ").append(passed).append('\n')
                .append("- not journalizable: ").append(skipped.size()).append("\n\n")
                .append("## Unsupported operations (number of instances)\n\n");
        unsupported.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> sb.append("- ").append(e.getValue()).append(" × ").append(e.getKey()).append('\n'));
        sb.append("\n## Not journalizable instances\n\n");
        skipped.forEach(s -> sb.append("- ").append(s).append('\n'));
        sb.append("\n## Shared objects not looked for (large models): ").append(sharingSkipped.size()).append("\n\n");
        sharingSkipped.forEach(n -> sb.append("- ").append(n).append('\n'));
        sb.append("\n## Objects shared by two models parsed independently (number of instances)\n\n");
        baseline.forEach((k, v) -> sb.append("- ").append(k).append(": ").append(v).append('\n'));
        sb.append("\n## Steps used by the journalized instances (number of instances)\n\n");
        usage.forEach((k, v) -> sb.append("- ").append(k).append(": ").append(v).append('\n'));
        Path out = Paths.get(System.getProperty("user.dir"), "target", "spec-coverage.md");
        Files.createDirectories(out.getParent());
        Files.writeString(out, sb.toString());
        System.out.println(sb);
    }
}
