/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/10/2019
 */
module org.chocosolver.parsers {
    exports org.chocosolver.parser to args4j;
    requires transitive org.chocosolver.pf4cs;
    requires transitive org.chocosolver.solver;
    requires choco.geost;
    requires xcsp3.tools;
    requires args4j;
    requires gson;
    requires java.sql;
    requires antlr4.runtime;
    requires trove4j;
}