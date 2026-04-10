/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/10/2019
 */
module org.chocosolver.examples {
    requires org.chocosolver.solver;
    requires org.chocosolver.parsers;
    requires args4j;
    requires java.desktop;
    requires trove4j;
    opens org.chocosolver.examples.integer to args4j;
    opens org.chocosolver.examples.nqueen to args4j;
}