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

/**
 * Created by cprudhom on 01/09/15.
 * Project: choco-parsers.
 */
public class ChocoXCSP {

    public static void main(String[] args) throws Exception {
        XCSP xscp = new XCSP();
        xscp.addListener(new BaseXCSPListener(xscp));
        if(xscp.setUp(args)) {
            xscp.createSolver();
            xscp.buildModel();
            xscp.configureSearch();
            xscp.solve();
        }
    }
}
