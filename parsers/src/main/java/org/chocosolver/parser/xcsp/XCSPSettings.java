/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.xcsp;

import org.chocosolver.solver.DefaultSettings;
import org.chocosolver.solver.Settings;

import java.util.Properties;

/**
 * Created by cprudhom on 01/09/15.
 * Project: choco-parsers.
 */
public class XCSPSettings extends DefaultSettings {

    public XCSPSettings() {
        loadProperties();
    }

    @Override
    protected String getPropertyName() {
        return "XCSPSettings.properties";
    }

    private void loadProperties() {
        boolean DEBUG = true;
        this.setEnableSAT(true);
        this.setWarnUser(DEBUG);
        this.setCheckDeclaredConstraints(DEBUG);
        this.setHybridizationOfPropagationEngine((byte) 0b00);
        this.setModelChecker(solver -> true);
    }


    @Override
    public Settings load(Properties properties) {
       super.load(properties);
        this.loadProperties();
        return this;
    }

}
