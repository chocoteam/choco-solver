/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.mps;

import org.chocosolver.parser.xcsp.XCSPSettings;

/**
 * Created by cprudhom on 01/09/15.
 * Project: choco-parsers.
 */
public class ChocoMPS {

    public static void main(String[] args) throws Exception {
        MPS mps = new MPS();
//        mps.addListener(new BaseXCSPListener(mps)); //todo
        mps.setUp(args);
        mps.defineSettings(new XCSPSettings());
        mps.createSolver();
        mps.buildModel();
        mps.configureSearch();
        mps.solve();
    }
}
