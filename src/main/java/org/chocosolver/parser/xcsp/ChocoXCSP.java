/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017-01-06T09:54:20Z, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.xcsp;

import org.chocosolver.parser.flatzinc.FznSettings;

/**
 * Created by cprudhom on 01/09/15.
 * Project: choco-parsers.
 */
public class ChocoXCSP {

    public static void main(String[] args) throws Exception {
        XCSP xscp = new XCSP();
//        fzn.addListener(new BaseFlatzincListener(fzn));
        xscp.parseParameters(args);
        xscp.defineSettings(new FznSettings());
        xscp.createSolver();
        xscp.parseInputFile();
        xscp.configureSearch();
        xscp.solve();
    }
}
