/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
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
}