/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete.set;

/**
 * Set operators for set expressions.
 * Relational and arithmetical operators are defined here.
 *
 * @author Gabriel Augusto David
 */
public enum SetOperator {
    SUBSET,
    EQ,
    NE,
    CONTAINS,
    NOT_EMPTY,
    NOT_CONTAINS,
    UNION,
    INTERSECTION
}