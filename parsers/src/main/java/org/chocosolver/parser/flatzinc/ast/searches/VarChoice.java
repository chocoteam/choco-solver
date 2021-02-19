/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast.searches;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/01/11
 */
public enum VarChoice {
    input_order,
    first_fail,
    anti_first_fail,
    smallest,
    largest,
    occurrence,
    most_constrained,
    max_regret
}
