/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.FA;

import org.chocosolver.solver.constraints.nary.automata.FA.utils.ICounter;

import java.util.List;

/**
 * A deterministic finite automaton whose transitions have costs, as required by
 * {@code costRegular} and {@code multiCostRegular}.
 * <p>
 * The costs are given by one or more counters ({@link ICounter}), one per resource.
 * The cost of a transition depends on the layer (the index of the variable in the sequence),
 * on the value assigned to this variable and, optionally, on the state of the automaton
 * at which the transition starts. The cost of a word is the sum of the costs of its transitions.
 * <p>
 * Use {@link CostAutomaton#makeSingleResource} for {@code costRegular} and
 * {@link CostAutomaton#makeMultiResources} for {@code multiCostRegular}.
 * <p>
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Nov 19, 2010
 * Time: 3:25:16 PM
 */
public interface ICostAutomaton extends IAutomaton {

    /**
     * @param layer index of the variable in the sequence
     * @param value value assigned to this variable
     * @return the cost, for the first resource, of assigning <i>value</i> to the variable at <i>layer</i>
     */
    double getCost(int layer, int value);

    /**
     * @param layer index of the variable in the sequence
     * @param value value assigned to this variable
     * @param state state of the automaton at which the transition starts
     * @return the cost, for the first resource, of assigning <i>value</i> to the variable at <i>layer</i>
     * from <i>state</i>
     */
    double getCostByState(int layer, int value, int state);

    /**
     * @param layer   index of the variable in the sequence
     * @param value   value assigned to this variable
     * @param counter index of the resource
     * @return the cost, for the resource <i>counter</i>, of assigning <i>value</i> to the variable at <i>layer</i>
     */
    double getCostByResource(int layer, int value, int counter);

    /**
     * @return the number of resources, i.e. of counters
     */
    int getNbResources();

    /**
     * @param layer   index of the variable in the sequence
     * @param value   value assigned to this variable
     * @param counter index of the resource
     * @param state   state of the automaton at which the transition starts
     * @return the cost, for the resource <i>counter</i>, of assigning <i>value</i> to the variable at <i>layer</i>
     * from <i>state</i>
     */
    double getCostByResourceAndState(int layer, int value, int counter, int state);

    /**
     * @return the counters, one per resource
     */
    List<ICounter> getCounters();

}
