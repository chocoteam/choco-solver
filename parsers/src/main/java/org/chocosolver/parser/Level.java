/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 25/05/2021
 */
public enum Level {
    /**
     * No log.
     */
    SILENT(0b000_0000),
    /**
     * Required level for competitions
     */
    COMPET(0b000_0001),
    /**
     * Required level for 'results-analyser'
     */
    RESANA(0b000_0010),
    /**
     * Required level to print verbose solving
     */
    VERBOSE(0b000_0100),
    /**
     * Required level to print verbose solving
     */
    JSON(0b000_1000),
    /**
     * Higher log level
     */
    INFO(0b001_0000),
    /**
     * Highest log level
     */
    FINE(0b011_0000);

    final int value;

    Level(int value) {
        this.value = value;
    }

    public boolean isLoggable(Level lvl) {
        return this.value >= lvl.value && (this.value & lvl.value) != 0;
    }

    public boolean is(Level lvl) {
        return this.value == lvl.value;
    }
}
