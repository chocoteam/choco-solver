/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 4 oct. 2010
 */
public class Exit {

    public static void log() {
        System.err.println("Expression  unexpected call");
//        new Exception().printStackTrace();
        throw new UnsupportedOperationException();
    }

    public static void log(String msg) {
        System.err.println(msg);
//        new Exception().printStackTrace();
        throw new UnsupportedOperationException();
    }
}
