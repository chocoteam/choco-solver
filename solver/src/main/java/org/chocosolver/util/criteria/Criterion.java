/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.criteria;

/**
 * An interface which defines a criterion with lambda
 *
 * Created by cprudhom on 04/09/15.
 * Project: choco.
 */
public interface Criterion {

    boolean isMet();
}
