/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.dimacs;

import org.chocosolver.solver.DefaultSettings;
import org.chocosolver.solver.Settings;

import java.util.Properties;

/**
 * @author Charles Prud'homme
 * @since 04/03/2021
 */
public class DIMACSSettings extends DefaultSettings {

    private final boolean DEBUG = false;

    private boolean print = false;

    public DIMACSSettings() {
        loadProperties();
    }

    private void loadProperties() {
        this.setPrintConstraints(DEBUG);
        this.setWarnUser(DEBUG);
        this.setCheckDeclaredConstraints(DEBUG);
        this.setModelChecker(solver -> true);
        this.setPrintConstraints(DEBUG);
    }

    public boolean printConstraints() {
        return false;
    }

    public Settings setPrintConstraints(boolean print) {
        this.print = print;
        return this;
    }


    @Override
    public Settings load(Properties properties) {
        super.load(properties);
        this.loadProperties();
        return this;
    }

    @Override
    public Properties store() {
        Properties properties = super.store();
        properties.setProperty("constraints.print", Boolean.toString(print));
        return properties;
    }

}
