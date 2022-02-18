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

import org.chocosolver.util.objects.graphs.IGraph;

public interface IGraphRelaxation {

    /**
     * @return true iff arc (i,j) belongs to the current solution of the relaxation
     */
    boolean contains(int i, int j);

    /**
     * @return the cost augmentation induced by the removal of arc (i,j)
     * assumes that (i,j) belongs to the current solution of the relaxation
     */
    double getReplacementCost(int i, int j);

    /**
     * @return the cost augmentation induced by the enforcing of arc (i,j)
     * assumes that (i,j) does not belong to the current solution of the relaxation
     */
    double getMarginalCost(int i, int j);

    /**
     * @return the (graph) support of the relaxation
     */
    IGraph getSupport();
}
