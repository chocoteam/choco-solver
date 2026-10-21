/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.Settings;
import org.chocosolver.solver.SettingsBuilder;

import java.util.Objects;

/**
 * What must be set on a model before replaying the steps of a {@link ModelSpec}.
 *
 * @param name     name of the model
 * @param settings settings of the model; immutable, hence shared by all instances, which may call its functions
 *                 ({@code modelChecker}, {@code defaultSearch}, {@code environmentSupplier}...) concurrently: they
 *                 must be stateless
 * @param seed     seed of the model; some propagators draw their random numbers from it when created
 * @author Charles Prud'homme
 */
public record Header(String name, Settings settings, long seed) {

    public Header {
        Objects.requireNonNull(name);
        Objects.requireNonNull(settings);
    }

    /**
     * @return a header with default settings and seed 0
     */
    public static Header of(String name) {
        return of(name, SettingsBuilder.init().build());
    }

    /**
     * @return a header with seed 0
     */
    public static Header of(String name, Settings settings) {
        return new Header(name, settings, 0L);
    }

    public Header withName(String name) {
        return new Header(name, settings, seed);
    }

    public Header withSeed(long seed) {
        return new Header(name, settings, seed);
    }

    public Header withSettings(Settings settings) {
        return new Header(name, settings, seed);
    }
}
