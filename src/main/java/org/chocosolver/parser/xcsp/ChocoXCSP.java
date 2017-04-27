/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.xcsp;

/**
 * Created by cprudhom on 01/09/15.
 * Project: choco-parsers.
 */
public class ChocoXCSP {

    public static void main(String[] args) throws Exception {
        XCSP xscp = new XCSP();
        xscp.addListener(new BaseXCSPListener(xscp));
        xscp.setUp(args);
        xscp.defineSettings(new XCSPSettings());
        xscp.createSolver();
        xscp.buildModel();
        xscp.configureSearch();
        xscp.solve();
    }
}
