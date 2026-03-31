/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.FA.utils;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Nov 23, 2010
 * Time: 11:09:07 AM
 */
public interface ICounter {

    Bounds bounds();

    double cost(int layer, int value);

    double cost(int layer, int value, int state);


}
