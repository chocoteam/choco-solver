/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json;

/**
 * Utility class to parse and solve a JSON file which describe a model
 *
 * Created by cprudhom on 27/09/17.
 * Project: choco-parsers.
 */
public class ChocoJSON {

    public static void main(String[] args) throws Exception {
        JSONParser json = new JSONParser();
        json.setUp(args);
        json.createSolver();
        json.buildModel();
        json.configureSearch();
        json.solve();
    }
}
