/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017-01-06T09:54:20Z, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc;

import org.chocosolver.solver.Settings;

/**
 * Basic settings for Fzn
 * Created by cprudhom on 08/12/14.
 * Project: choco-parsers.
 */
public class FznSettings implements Settings {

    /**
     * Set to true to print constraint creation during parsing
     */
    public boolean printConstraint() {
        return false;
    }

    @Override
    public boolean enableTableSubstitution() {
        return true;
    }

    @Override
    public boolean enableSAT() {
        return false;
    }

    /**
     * Faster reification (but quite dirty)
     */
    public boolean adhocReification() {
        return true;
    }

    @Override
    public int getMaxTupleSizeForSubstitution() {
        return 10000;
    }
}
