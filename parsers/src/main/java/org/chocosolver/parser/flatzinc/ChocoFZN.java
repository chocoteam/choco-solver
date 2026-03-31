/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc;


/**
 * The main entry point
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/07/13
 */
public class ChocoFZN {

    public static void main(String[] args) throws Exception {
        Flatzinc fzn = new Flatzinc();
        if(fzn.setUp(args)) {
            fzn.createSolver();
            fzn.buildModel();
            fzn.configureSearch();
            fzn.solve();
        }
    }
}
