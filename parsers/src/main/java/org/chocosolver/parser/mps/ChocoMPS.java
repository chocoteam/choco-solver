/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.mps;

/**
 * Created by cprudhom on 01/09/15.
 * Project: choco-parsers.
 */
public class ChocoMPS {

    public static void main(String[] args) throws Exception {
        MPS mps = new MPS();
//        mps.addListener(new BaseXCSPListener(mps)); //todo
        if(mps.setUp(args)) {
            mps.getSettings().setMinCardinalityForSumDecomposition(mps.split);
            mps.createSolver();
            mps.buildModel();
            mps.configureSearch();
            mps.solve();
        }
    }
}
