/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.spec;

import org.chocosolver.parser.RegParser;
import org.chocosolver.parser.SetUpException;
import org.chocosolver.parser.flatzinc.Flatzinc;
import org.chocosolver.parser.xcsp.XCSP;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.SettingsBuilder;

/**
 * Builds the models of an instance (FlatZinc or XCSP) for the tests and benchmarks of this package.
 *
 * @author Charles Prud'homme
 */
final class SpecParsers {

    private SpecParsers() {
    }

    /**
     * Parse <i>file</i> into plain models, or into recorded models ({@link Model#record}).
     * The search declared by the instance is ignored (free search): it is not journalized.
     *
     * @param file   a FlatZinc or XCSP instance
     * @param cores  number of models to build
     * @param record <i>true</i> to record the models
     * @return the parser, whose models are built
     * @throws IllegalStateException if the parser cannot be set up
     */
    static RegParser parse(String file, int cores, boolean record) {
        RegParser parser = file.endsWith(".fzn")
                ? new Flatzinc(false, false, cores) {
            @Override
            protected Model newModel(String name, SettingsBuilder settings) {
                return record ? Model.record(name, settings.build()) : super.newModel(name, settings);
            }
        }
                : new XCSP() {
            @Override
            protected Model newModel(String name, SettingsBuilder settings) {
                return record ? Model.record(name, settings.build()) : super.newModel(name, settings);
            }
        };
        boolean ready;
        try {
            // no shutdown hook: it would retain the models until the end of the JVM
            ready = parser.setUp(file, "-lvl", "SILENT", "-p", String.valueOf(cores), "-f", "true",
                    "--disable-shutdown-hook", "true");
        } catch (SetUpException e) {
            throw new IllegalStateException("cannot set up the parser", e);
        }
        if (!ready) {
            throw new IllegalStateException("cannot set up the parser");
        }
        parser.createSolver();
        parser.buildModel();
        return parser;
    }
}
