/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Install Choco-solver as a MiniZinc solver, from the jar file it is run from.
 * <p>
 * The files of the 'minizinc' directory embedded in the jar (the MiniZinc library and the Python wrapper)
 * are extracted in 'choco-VERSION' in the MiniZinc solvers directory,
 * the wrapper is set to run this jar file,
 * and the solver configuration file 'choco-VERSION.msc' is written next to it, with absolute paths.
 * <p>
 * Usage: {@code java -jar choco-solver-VERSION-light.jar --install-minizinc [solvers-dir]}
 *
 * <p> Project: choco-parsers.
 *
 * @author Charles Prud'homme
 * @since 21/10/2026.
 */
public class MiniZincInstaller {

    private static final String RESOURCES = "minizinc";
    private static final String MSC = "choco.msc";
    private static final String WRAPPER = "fzn-choco.py";

    public static void main(String[] args) throws IOException, URISyntaxException {
        Path solvers = args.length > 0 ? Paths.get(args[0]) : defaultSolversDir();
        Path jar = Paths.get(MiniZincInstaller.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path msc = install(jar, solvers.toAbsolutePath());
        System.out.printf("Choco-solver is installed for MiniZinc: %s\n", msc);
        System.out.println("Restart the MiniZincIDE, or run `minizinc --solvers`, to check it is listed.");
    }

    /**
     * @return the directory in which MiniZinc looks for user solver configurations
     */
    static Path defaultSolversDir() {
        String appData = System.getenv("APPDATA");
        if (isWindows() && appData != null) {
            return Paths.get(appData, "MiniZinc", "solvers");
        }
        return Paths.get(System.getProperty("user.home"), ".minizinc", "solvers");
    }

    /**
     * Install Choco-solver in <i>solvers</i>.
     *
     * @param jar     the jar file (or classes directory) which embeds the 'minizinc' directory
     * @param solvers the MiniZinc solvers directory
     * @return the solver configuration file written
     * @throws IOException if a file cannot be read or written
     */
    static Path install(Path jar, Path solvers) throws IOException {
        String version = version();
        Path home = solvers.resolve("choco-" + version);
        Files.createDirectories(home);
        String msc;
        if (Files.isDirectory(jar)) {
            msc = extract(jar.resolve(RESOURCES), home);
        } else {
            try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + jar.toUri()), Collections.emptyMap())) {
                msc = extract(fs.getPath("/" + RESOURCES), home);
            }
        }
        // the wrapper runs the jar file the installer is run from
        Path wrapper = home.resolve(WRAPPER);
        String py = new String(Files.readAllBytes(wrapper), StandardCharsets.UTF_8);
        py = py.replaceFirst("(?m)^JAR_FILE=.*$",
                Matcher.quoteReplacement("JAR_FILE=r'" + jar.toAbsolutePath() + "'"));
        Files.write(wrapper, py.getBytes(StandardCharsets.UTF_8));
        Path executable = home.resolve(isWindows() ? "fzn-choco.bat" : "fzn-choco.sh");
        if (!executable.toFile().setExecutable(true)) {
            throw new IOException("Unable to make " + executable + " executable");
        }
        msc = setField(msc, "version", version);
        msc = setField(msc, "mznlib", home.resolve("mzn_lib").toString() + home.getFileSystem().getSeparator());
        msc = setField(msc, "executable", executable.toString());
        Path target = solvers.resolve("choco-" + version + ".msc");
        Files.write(target, msc.getBytes(StandardCharsets.UTF_8));
        return target;
    }

    /**
     * Copy the content of <i>from</i> into <i>to</i>, except the solver configuration file.
     *
     * @return the content of the solver configuration file
     */
    private static String extract(Path from, Path to) throws IOException {
        if (!Files.isDirectory(from)) {
            throw new IOException("No '" + RESOURCES + "' directory in " + from.getFileSystem() +
                    ", run the installer from the choco-solver jar file");
        }
        try (Stream<Path> files = Files.walk(from)) {
            for (Path file : (Iterable<Path>) files::iterator) {
                Path dest = to.resolve(from.relativize(file).toString());
                if (Files.isDirectory(file)) {
                    Files.createDirectories(dest);
                } else if (!file.getFileName().toString().equals(MSC)) {
                    Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        return new String(Files.readAllBytes(from.resolve(MSC)), StandardCharsets.UTF_8);
    }

    private static String setField(String msc, String field, String value) {
        String json = value.replace("\\", "\\\\");
        return msc.replaceFirst("\"" + field + "\"\\s*:\\s*\"[^\"]*\"",
                Matcher.quoteReplacement("\"" + field + "\": \"" + json + "\""));
    }

    /**
     * @return the version of the jar file, or the one of the solver configuration file if unknown
     */
    private static String version() throws IOException {
        try (InputStream in = MiniZincInstaller.class.getResourceAsStream(
                "/META-INF/maven/org.choco-solver/choco-parsers/pom.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                return props.getProperty("version");
            }
        }
        try (InputStream in = MiniZincInstaller.class.getResourceAsStream("/" + RESOURCES + "/" + MSC)) {
            if (in == null) {
                throw new IOException("No '" + RESOURCES + "/" + MSC + "' resource, run the installer from the choco-solver jar file");
            }
            String msc = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            Matcher m = Pattern.compile("\"version\"\\s*:\\s*\"([^\"]*)\"").matcher(msc);
            if (!m.find()) {
                throw new IOException("No version in " + MSC);
            }
            return m.group(1);
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }
}
