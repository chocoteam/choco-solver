/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.criteria;

/**
 * An interface which defines a criterion with lambda
 *
 * Created by cprudhom on 04/09/15.
 * Project: choco.
 */
public interface LongCriterion {

    boolean isMet(long value);
}
