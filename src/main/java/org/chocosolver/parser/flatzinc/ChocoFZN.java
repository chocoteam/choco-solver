/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017-01-06T09:54:20Z, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
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
        fzn.parseParameters(args);
        fzn.defineSettings(new FznSettings());
        fzn.createSolver();
        fzn.parseInputFile();
        fzn.configureSearch();
        fzn.solve();
    }
}
