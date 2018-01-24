/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.xcsp;

import org.chocosolver.solver.DefaultSettings;

/**
 * Created by cprudhom on 01/09/15.
 * Project: choco-parsers.
 */
public class XCSPSettings extends DefaultSettings {

    boolean DEBUG = false;
    /**
     * Set to true to print constraint creation during parsing
     */
    public boolean printConstraint() {
        return DEBUG;
    }

    @Override
    public boolean enableTableSubstitution() {
        return true;
    }

    @Override
    public boolean enableSAT() {
        return true;
    }

    @Override
    public int getMaxTupleSizeForSubstitution() {
        return 10000;
    }

    @Override
    public boolean warnUser() {
        return DEBUG;
    }

    @Override
    public boolean checkDeclaredConstraints() {
        return DEBUG;
    }
}
