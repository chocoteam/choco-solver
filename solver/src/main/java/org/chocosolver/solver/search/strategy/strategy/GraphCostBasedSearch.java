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
 * Date: 08/08/12
 * Time: 15:27
 */

package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.assignments.GraphDecisionOperator;
import org.chocosolver.solver.search.strategy.decision.GraphDecision;
import org.chocosolver.solver.search.strategy.selectors.values.graph.priority.GraphEdgesOnly;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.util.objects.setDataStructures.ISet;

public class GraphCostBasedSearch extends GraphStrategy {


    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    // heuristics
    public static final int LEX = 0;
    public static final int MIN_P_DEGREE = 1;
    public static final int MAX_P_DEGREE = 2;
    public static final int MIN_M_DEGREE = 3;
    public static final int MAX_M_DEGREE = 4;
    public static final int MIN_DELTA_DEGREE = 5;
    public static final int MAX_DELTA_DEGREE = 6;
    public static final int MIN_COST = 7;
    public static final int MAX_COST = 8;

    // variables
    private final int n;
    private int mode;
    private final int[][] costs;
    private GraphDecisionOperator decisionType;
    private int from, to;
    private int value;
    private boolean useLC;
    private int lastFrom = -1;
    private final GraphVar g;

    /**
     * Search strategy for graphs
     *
     * @param graphVar varriable to branch on
     */
    public GraphCostBasedSearch(GraphVar graphVar) {
        this(graphVar, null);
    }

    /**
     * Search strategy for graphs
     *
     * @param graphVar   varriable to branch on
     * @param costMatrix can be null
     */
    public GraphCostBasedSearch(GraphVar graphVar, int[][] costMatrix) {
        super(new GraphVar[] {graphVar}, null, new GraphEdgesOnly(),null, null, true);
        costs = costMatrix;
        this.g = graphVar;
        n = g.getNbMaxNodes();
    }

    /**
     * Configures the search
     *
     * @param policy way to select arcs
     */
    public GraphCostBasedSearch configure(int policy) {
        return configure(policy, true);
    }

    /**
     * Configures the search
     *
     * @param policy  way to select arcs
     * @param enforce true if a decision is an arc enforcing
     *                false if a decision is an arc removal
     */
    public GraphCostBasedSearch configure(int policy, boolean enforce) {
        if (enforce) {
            decisionType = DecisionOperatorFactory.makeGraphEnforce();
        } else {
            decisionType = DecisionOperatorFactory.makeGraphRemove();
        }
        mode = policy;
        return this;
    }

    public GraphCostBasedSearch useLastConflict() {
        useLC = true;
        return this;
    }

    @Override
    public GraphDecision getDecision() {
        if (g.isInstantiated()) {
            return null;
        }
        computeNextArc();
        GraphDecision dec = g.getModel().getSolver().getDecisionPath().makeGraphEdgeDecision(g, decisionType, from, to);
        lastFrom = from;
        return dec;
    }

    private void computeNextArc() {
        to = -1;
        from = -1;
        if (useLC && lastFrom != -1) {
            evaluateNeighbors(lastFrom);
            if (to != -1) {
                return;
            }
        }
        for (int i = 0; i < n; i++) {
            if (evaluateNeighbors(i)) {
                return;
            }
        }
        if (to == -1) {
            throw new UnsupportedOperationException();
        }
    }

    private boolean evaluateNeighbors(int i) {
        ISet set = g.getPotentialSuccessorsOf(i);
        if (set.size() == g.getMandatorySuccessorsOf(i).size()) {
            return false;
        }
        for (int j : set) {
            if (!g.getMandatorySuccessorsOf(i).contains(j)) {
                int v = -1;
                switch (mode) {
                    case LEX:
                        from = i;
                        to = j;
                        return true;
                    case MIN_P_DEGREE:
                    case MAX_P_DEGREE:
                        v = g.getPotentialSuccessorsOf(i).size()
                                + g.getPotentialPredecessorOf(j).size();
                        break;
                    case MIN_M_DEGREE:
                    case MAX_M_DEGREE:
                        v = g.getMandatorySuccessorsOf(i).size()
                                + g.getMandatoryPredecessorsOf(j).size();
                        break;
                    case MIN_DELTA_DEGREE:
                    case MAX_DELTA_DEGREE:
                        v = g.getPotentialSuccessorsOf(i).size()
                                + g.getPotentialPredecessorOf(j).size()
                                - g.getMandatorySuccessorsOf(i).size()
                                - g.getMandatoryPredecessorsOf(j).size();
                        break;
                    case MIN_COST:
                    case MAX_COST:
                        v = costs[i][j];
                        break;
                    default:
                        throw new UnsupportedOperationException("mode " + mode + " does not exist");
                }
                if (select(v)) {
                    value = v;
                    from = i;
                    to = j;
                }
            }
        }
        return false;
    }

    private boolean select(double v) {
        return (from == -1 || (v < value && isMinOrIn(mode)) || (v > value && !isMinOrIn(mode)));
    }

    private static boolean isMinOrIn(int policy) {
        return (policy % 2 == 1);
    }

}
