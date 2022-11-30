/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

import org.chocosolver.parser.flatzinc.Flatzinc;
import org.chocosolver.parser.xcsp.XCSP;
import org.chocosolver.solver.Model;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/09/2020
 */
@Listeners(PerformanceListener.class)
public class PerformanceTest {
    private static final String ROOT = "/benchmarks";

    private StringBuilder writer;

    @BeforeClass(alwaysRun = true, groups = "benchmark")
    public void beforeStart() {
        writer = new StringBuilder();
        writer.append("name;status;solutions;buildingTime(sec);totalTime(sec);timeToBest(sec);" +
                "objective;nodes;backtracks;backjumps;fails;restarts;memory(MB)\n");
    }

    @AfterClass(groups = "benchmark")
    public void afterStart() throws IOException {
        String pathTemp = System.getProperty("user.dir");
        Path path = Paths.get(pathTemp, "target", "benchmark_results.csv");
        Files.write(path, writer.toString().getBytes());
    }

    private void logPerf(String name, Model model, long mem) {
        writer.append(name).append(";");
        writer.append(model.getSolver().toCSV());
        writer.append(mem).append("\n");
    }

    private void add(List<Object[]> parameters, int rootCount, Path path) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path p : stream) {
                if (!Files.isDirectory(p)) {
                    String name = p.getFileName().toString();
                    if (name.endsWith(".fzn")) {
                        parameters.add(new Object[]{p.subpath(rootCount, p.getNameCount()).toString(), Flatzinc.class});
                    } else if (name.endsWith(".lzma") || name.endsWith(".xml")) {
                        parameters.add(new Object[]{p.subpath(rootCount, p.getNameCount()).toString(), XCSP.class});
                    }
                } else {
                    add(parameters, rootCount, p);
                }
            }
        }
    }

    @DataProvider()
    public Object[][] collect() throws IOException {
        List<Object[]> parameters = new ArrayList<>();
        Path root = Paths.get(Objects.requireNonNull(this.getClass().getResource(ROOT)).getPath());
        add(parameters, root.getNameCount(), root);
        return parameters.toArray(new Object[0][0]);
    }

    @Test(groups = "benchmark", dataProvider = "collect")
    public void testMzn(String path, Class<RegParser> c) throws SetUpException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String file = Objects.requireNonNull(this.getClass().getResource(ROOT + File.separator + path)).getFile();
        String[] args = new String[]{
                file,
                "-lvl", "SILENT",
                "-p", "1"
        };
        //RegParser.PRINT_LOG = false;
        RegParser parser = c.getConstructor().newInstance();
        parser.setUp(args);
        parser.createSolver();
        parser.buildModel();
        long mem = getReallyUsedMemory();
        parser.configureSearch();
        parser.solve();
        logPerf(path, parser.getModel(), mem);
    }

    long getGcCount() {
        long sum = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = b.getCollectionCount();
            if (count != -1) {
                sum += count;
            }
        }
        return sum;
    }

    long getReallyUsedMemory() {
        long before = getGcCount();
        System.gc();
        while (getGcCount() == before) ;
        return getCurrentlyAllocatedMemory();
    }

    long getCurrentlyAllocatedMemory() {
        final Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }
}
