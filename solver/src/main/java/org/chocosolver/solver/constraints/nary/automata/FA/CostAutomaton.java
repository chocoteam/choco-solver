/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.FA;

import org.chocosolver.solver.constraints.nary.automata.FA.utils.Counter;
import org.chocosolver.solver.constraints.nary.automata.FA.utils.CounterState;
import org.chocosolver.solver.constraints.nary.automata.FA.utils.ICounter;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link FiniteAutomaton} whose transitions have costs, given by counters, one per resource
 * (see {@link ICostAutomaton}).
 * <p>
 * In the cost arrays of the factory methods, a value is used as an index: {@code costs[layer][value]}
 * is the cost of assigning {@code value} to the variable at index {@code layer} in the sequence.
 * The second dimension must therefore cover the values of the domain of this variable.
 * <p>
 * Example: 3 variables in [0,2], the words of {@code (0|1|2)*}, the values 0, 1 and 2 cost 0, 1 and 5,
 * and the cost of a word is in [0,10]:
 * <pre>{@code
 * IntVar[] vars = model.intVarArray("x", 3, 0, 2);
 * IntVar cost = model.intVar("cost", 0, 10);
 * int[][] costs = new int[3][];
 * Arrays.fill(costs, new int[]{0, 1, 5});
 * ICostAutomaton auto = CostAutomaton.makeSingleResource(new FiniteAutomaton("(0|1|2)*"), costs,
 *         cost.getLB(), cost.getUB());
 * model.costRegular(vars, cost, auto).post();
 * }</pre>
 * <p>
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Nov 23, 2010
 * Time: 11:07:36 AM
 */
public class CostAutomaton extends FiniteAutomaton implements ICostAutomaton {

    protected ArrayList<ICounter> counters;

    /**
     * Creates an empty automaton, with no counter.
     */
    public CostAutomaton() {
        super();
        this.counters = new ArrayList<>();
    }


    /**
     * Creates a copy of <i>auto</i>, with no counter.
     *
     * @param auto a {@link FiniteAutomaton}
     */
    public CostAutomaton(IAutomaton auto) {
        super((FiniteAutomaton) auto);
        this.counters = new ArrayList<>();
    }

    /**
     * Creates a copy of <i>auto</i>, with the given counters, one per resource.
     *
     * @param auto     a {@link FiniteAutomaton}
     * @param counters the counters
     */
    public CostAutomaton(IAutomaton auto, List<ICounter> counters) {
        this(auto);
        this.counters.addAll(counters);
    }

    /**
     * Creates a copy of <i>auto</i>, with a single counter.
     *
     * @param auto    a {@link FiniteAutomaton}
     * @param counter the counter
     */
    public CostAutomaton(IAutomaton auto, ICounter counter) {
        this(auto);
        this.counters.add(counter);
    }


    @Override
    public double getCost(int layer, int value) {
        ICounter zero = counters.get(0);
        return zero.cost(layer, value);
    }

    @Override
    public double getCostByState(int layer, int value, int state) {
        ICounter zero = counters.get(0);
        return zero.cost(layer, value, state);
    }

    @Override
    public double getCostByResource(int layer, int value, int counter) {
        ICounter aCounter = counters.get(counter);
        return aCounter.cost(layer, value);
    }

    @Override
    public int getNbResources() {
        return counters.size();
    }

    @Override
    public double getCostByResourceAndState(int layer, int value, int counter, int state) {
        ICounter aCounter = counters.get(counter);
        return aCounter.cost(layer, value, state);
    }

    @Override
    public List<ICounter> getCounters() {
        return counters;
    }

    /**
     * Creates an automaton with a single resource, whose costs depend on the state, for {@code costRegular}.
     *
     * @param pi    a {@link FiniteAutomaton} defining the regular language
     * @param costs {@code costs[layer][value][state]}: cost of assigning {@code value} to the variable at
     *              {@code layer} from {@code state}
     * @param inf   lower bound of the cost of a word
     * @param sup   upper bound of the cost of a word
     * @return the automaton, or {@code null} if <i>pi</i> is {@code null}
     */
    public static ICostAutomaton makeSingleResource(IAutomaton pi, int[][][] costs, int inf, int sup) {
        ICounter c = new CounterState(costs, inf, sup);
        ArrayList<ICounter> tmp = new ArrayList<>();
        tmp.add(c);
        return (pi == null) ? null :
                new CostAutomaton(pi, tmp);
    }

    /**
     * Creates an automaton with a single resource, for {@code costRegular}.
     *
     * @param pi    a {@link FiniteAutomaton} defining the regular language
     * @param costs {@code costs[layer][value]}: cost of assigning {@code value} to the variable at {@code layer}
     * @param inf   lower bound of the cost of a word
     * @param sup   upper bound of the cost of a word
     * @return the automaton, or {@code null} if <i>pi</i> is {@code null}
     */
    public static ICostAutomaton makeSingleResource(IAutomaton pi, int[][] costs, int inf, int sup) {
        ICounter c = new Counter(costs, inf, sup);
        ArrayList<ICounter> tmp = new ArrayList<>();
        tmp.add(c);
        return (pi == null) ? null :
                new CostAutomaton(pi, tmp);
    }

    /**
     * Creates an automaton with several resources, for {@code multiCostRegular}.
     *
     * @param pi                   a {@link FiniteAutomaton} defining the regular language
     * @param layer_value_resource {@code [layer][value][resource]}: cost, for {@code resource}, of assigning
     *                             {@code value} to the variable at {@code layer}
     * @param infs                 lower bound of the cost of a word, per resource
     * @param sups                 upper bound of the cost of a word, per resource
     * @return the automaton, or {@code null} if <i>pi</i> is {@code null}
     */
    public static ICostAutomaton makeMultiResources(IAutomaton pi, int[][][] layer_value_resource, int[] infs, int[] sups) {
        int[][][] ordered = new int[infs.length][layer_value_resource.length][];
        ArrayList<ICounter> tmp = new ArrayList<>();
        for (int k = 0; k < infs.length; k++) {
            for (int i = 0; i < layer_value_resource.length; i++) {
                ordered[k][i] = new int[layer_value_resource[i].length];
                for (int j = 0; j < layer_value_resource[i].length; j++) {
                    ordered[k][i][j] = layer_value_resource[i][j][k];
                }
            }
            tmp.add(new Counter(ordered[k], infs[k], sups[k]));
        }
        return (pi == null) ? null :
                new CostAutomaton(pi, tmp);
    }

    /**
     * Creates an automaton with several resources, whose costs may depend on the state, for {@code multiCostRegular}.
     *
     * @param pi                         a {@link FiniteAutomaton} defining the regular language
     * @param layer_value_resource_state {@code [layer][value][resource][state]}: cost, for {@code resource}, of
     *                                   assigning {@code value} to the variable at {@code layer} from {@code state}.
     *                                   A resource whose arrays all have length 1 does not depend on the state.
     * @param infs                       lower bound of the cost of a word, per resource
     * @param sups                       upper bound of the cost of a word, per resource
     * @return the automaton, or {@code null} if <i>pi</i> is {@code null}
     */
    public static ICostAutomaton makeMultiResources(IAutomaton pi, int[][][][] layer_value_resource_state, int[] infs, int[] sups) {
        int[][][][] ordered = new int[infs.length][layer_value_resource_state.length][][];
        ArrayList<ICounter> tmp = new ArrayList<>();
        for (int k = 0; k < infs.length; k++) {
            boolean stateDependant = true;
            for (int i = 0; i < layer_value_resource_state.length; i++) {
                ordered[k][i] = new int[layer_value_resource_state[i].length][];
                for (int j = 0; j < layer_value_resource_state[i].length; j++) {
                    ordered[k][i][j] = new int[layer_value_resource_state[i][j][k].length];
//                    for (int q = 0; q < layer_value_resource_state[i][j][k].length; q++) {
//                        ordered[k][i][j][q] = layer_value_resource_state[i][j][k][q];
//                    }
                    System.arraycopy(layer_value_resource_state[i][j][k], 0, ordered[k][i][j], 0, layer_value_resource_state[i][j][k].length);
                    if (ordered[k][i][j].length == 1) stateDependant = false;
                }
            }

            if (stateDependant)
                tmp.add(new CounterState(ordered[k], infs[k], sups[k]));
            else
                tmp.add(new Counter(ordered[k]));
        }
        return (pi == null) ? null :
                new CostAutomaton(pi, tmp);
    }

    /**
     * Same as {@link #makeMultiResources(IAutomaton, int[][][][], int[], int[])}, with the bounds of the
     * cost variables <i>z</i>.
     *
     * @param auto a {@link FiniteAutomaton} defining the regular language
     * @param c    {@code [layer][value][resource][state]} costs
     * @param z    the cost variables, one per resource
     * @return the automaton, or {@code null} if <i>auto</i> is {@code null}
     */
    public static ICostAutomaton makeMultiResources(IAutomaton auto, int[][][][] c, IntVar[] z) {
        int[][] bounds = getBounds(z);
        return makeMultiResources(auto, c, bounds[0], bounds[1]);
    }

    /**
     * Same as {@link #makeMultiResources(IAutomaton, int[][][], int[], int[])}, with the bounds of the
     * cost variables <i>z</i>.
     *
     * @param auto a {@link FiniteAutomaton} defining the regular language
     * @param c    {@code [layer][value][resource]} costs
     * @param z    the cost variables, one per resource
     * @return the automaton, or {@code null} if <i>auto</i> is {@code null}
     */
    public static ICostAutomaton makeMultiResources(IAutomaton auto, int[][][] c, IntVar[] z) {
        int[][] bounds = getBounds(z);
        return makeMultiResources(auto, c, bounds[0], bounds[1]);
    }

    private static int[][] getBounds(IntVar[] cr) {
        int[][] bounds = new int[2][cr.length];
        for (int i = 0; i < cr.length; i++) {
            bounds[0][i] = cr[i].getLB();
            bounds[1][i] = cr[i].getUB();
        }
        return bounds;
    }

    /**
     * Adds a counter, i.e. a resource.
     *
     * @param c the counter
     */
    public void addCounter(ICounter c) {
        this.counters.add(c);
    }


    @Override
    public FiniteAutomaton clone() throws CloneNotSupportedException {
        CostAutomaton auto = (CostAutomaton) super.clone();
        auto.counters = new ArrayList<>();
        //TODO: duplicate?
        this.counters.forEach(auto::addCounter);
        return auto;
    }
}
