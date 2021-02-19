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
        fzn.addListener(new BaseFlatzincListener(fzn));
        if(fzn.setUp(args)) {
            fzn.getSettings();
            fzn.createSolver();
            fzn.buildModel();
            fzn.configureSearch();
            fzn.solve();
        }
    }
}
