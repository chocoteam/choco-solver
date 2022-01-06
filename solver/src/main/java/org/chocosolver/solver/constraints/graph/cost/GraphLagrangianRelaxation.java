/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/12
 * Time: 01:03
 */

package org.chocosolver.solver.constraints.graph.cost;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.exception.ContradictionException;

public interface GraphLagrangianRelaxation extends IGraphRelaxation {


    /**
     * @param b true iff the relaxation should not be triggered before a first solution is found
     */
    void waitFirstSolution(boolean b);

    // mandatory arcs
    boolean isMandatory(int i, int j);

    TIntArrayList getMandatoryArcsList();

    // get a default minimal value
    double getMinArcVal();

    // some primitives
    void contradiction() throws ContradictionException;

    void remove(int i, int j) throws ContradictionException;

    void enforce(int i, int j) throws ContradictionException;
}
