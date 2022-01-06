/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.dimacs;

/**
 * @author Charles Prud'homme
 * @since 04/03/2021
 */
public class ChocoDIMACS {

    public static void main(String[] args) throws Exception {
        DIMACS dimacs = new DIMACS();
        if (dimacs.setUp(args)) {
            dimacs.createSolver();
            dimacs.buildModel();
            dimacs.configureSearch();
            dimacs.solve();
        }
    }
}
