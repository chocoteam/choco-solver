/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.assignments.GraphDecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.values.graph.edge.GraphEdgeSelector;
import org.chocosolver.solver.search.strategy.selectors.values.graph.node.GraphNodeSelector;
import org.chocosolver.solver.search.strategy.selectors.values.graph.priority.GraphNodeOrEdgeSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.GraphVar;

/**
 * @author Dimitri Justeau-Allaire
 * @since 19/04/2021
 *
 * Strategy for branching on graph variables, replacement of choco-graph's original
 * implementation for a consistent design with other variables' kinds strategies.
 */
public class GraphStrategy extends AbstractStrategy<GraphVar> {

    protected VariableSelector<GraphVar> varSelector;

    private final GraphNodeOrEdgeSelector nodeOrEdgeSelector;

    protected GraphNodeSelector nodeSelector;

    protected GraphEdgeSelector edgeSelector;

    protected GraphDecisionOperator operator;

    public GraphStrategy(GraphVar[] scope, VariableSelector<GraphVar> varSelector, GraphNodeOrEdgeSelector nodeOrEdgeSelector, GraphNodeSelector nodeSelector, GraphEdgeSelector edgeSelector, boolean enforceFirst) {
        super(scope);
        this.varSelector = varSelector;
        this.nodeOrEdgeSelector = nodeOrEdgeSelector;
        this.nodeSelector = nodeSelector;
        this.edgeSelector = edgeSelector;
        this.operator = enforceFirst ? DecisionOperatorFactory.makeGraphEnforce() : DecisionOperatorFactory.makeGraphRemove();
    }

    @Override
    public Decision<GraphVar> getDecision() {
        GraphVar variable = varSelector.getVariable(vars);
        return computeDecision(variable);
    }

    @Override
    public Decision<GraphVar> computeDecision(GraphVar g) {
        if (g == null) {
            return null;
        }
        assert !g.isInstantiated();
        if (nodeOrEdgeSelector.nextIsNode(g)) {
            return g.getModel().getSolver().getDecisionPath().makeGraphNodeDecision(g, operator, nodeSelector.selectNode(g));
        } else {
            int[] edge = edgeSelector.selectEdge(g);
            assert edge.length == 2;
            return g.getModel().getSolver().getDecisionPath().makeGraphEdgeDecision(g, operator, edge[0], edge[1]);
        }
    }
}
