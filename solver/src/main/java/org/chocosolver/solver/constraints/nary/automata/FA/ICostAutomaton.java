/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.FA;

import org.chocosolver.solver.constraints.nary.automata.FA.utils.ICounter;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Nov 19, 2010
 * Time: 3:25:16 PM
 */
public interface ICostAutomaton extends IAutomaton {

    double getCost(int i, int j);

    double getCostByState(int layer, int counter, int state);

    double getCostByResource(int layer, int value, int counter);

    int getNbResources();

    double getCostByResourceAndState(int layer, int value, int counter, int state);

    List<ICounter> getCounters();

}
