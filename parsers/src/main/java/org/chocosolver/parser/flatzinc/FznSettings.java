/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc;

import org.chocosolver.solver.DefaultSettings;
import org.chocosolver.solver.Settings;

import java.util.Properties;

/**
 * Basic settings for Fzn
 * Created by cprudhom on 08/12/14.
 * Project: choco-parsers.
 */
public class FznSettings extends DefaultSettings {

    private boolean print = false;

    private boolean adhocReification = true;

    public FznSettings() {
        loadProperties();
    }

    @Override
    protected String getPropertyName() {
        return "FZNSettings.properties";
    }

    private void loadProperties() {
        this.setCheckDeclaredConstraints(false);
        //        this.setModelChecker(solver -> true);
        this.setLearntClausesDominancePerimeter(0);
        this.setNbMaxLearntClauses(Integer.MAX_VALUE);
        this.setRatioForClauseStoreReduction(.66f);
    }

    public boolean printConstraints() {
        return print;
    }

    public Settings setPrintConstraints(boolean print) {
        this.print = print;
        return this;
    }

    public boolean adhocReification() {
        return adhocReification;
    }

    public Settings setAdHocReification(boolean adhoc) {
        this.adhocReification = adhoc;
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
        properties.setProperty("reification.adhoc", Boolean.toString(adhocReification));
        return properties;
    }
}
