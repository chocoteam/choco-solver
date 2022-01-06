/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.decision;

import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.GraphDecisionOperator;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.PoolManager;

/**
 * A class that creates decisions on demand and maintains pool manager
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 14/03/2016.
 */
public class DecisionMaker {

    /**
     * object recycling management
     */
    private final PoolManager<IntDecision> intDecisionPool;

    /**
     * object recycling management
     */
    private final PoolManager<RealDecision> realDecisionPool;

    /**
     * object recycling management
     */
    private final PoolManager<SetDecision> setDecisionPool;

    private final PoolManager<GraphDecision> graphDecisionPool;

    /**
     * Create a decision maker, that eases decision creation.
     */
    public DecisionMaker() {
        this.intDecisionPool = new PoolManager<>();
        this.realDecisionPool = new PoolManager<>();
        this.setDecisionPool = new PoolManager<>();
        this.graphDecisionPool = new PoolManager<>();
    }

    /**
     * Creates and returns an {@link IntDecision}: "{@code var} {@code dop} {@code value}".
     * @param var an integer variable
     * @param dop a decision operator
     * @param value a value
     * @return an IntDecision
     */
    public IntDecision makeIntDecision(IntVar var, DecisionOperator<IntVar> dop, int value) {
        IntDecision d = intDecisionPool.getE();
        if (d == null) {
            d = new IntDecision(intDecisionPool);
        }
        d.set(var, value, dop);
        return d;
    }

    /**
     * Creates and returns an {@link RealDecision}: "{@code var} &le; {@code value}"
     * <br/>
     * which is refuted as "{@code var} &ge; {@code value} + {@code epsilon}".
     * @param var a real variable
     * @param value a value
     * @param epsilon gap for refutation
     * @param leftFirst select left range first
     * @return an RealDecision
     */
    public RealDecision makeRealDecision(RealVar var, double value, double epsilon, boolean leftFirst) {
        RealDecision d = realDecisionPool.getE();
        if (d == null) {
            d = new RealDecision(realDecisionPool);
        }
        d.set(var, value, epsilon, leftFirst);
        return d;
    }

    /**
     * Creates and returns an {@link SetDecision}: "{@code var} {@code dop} {@code value}".
     * @param var a set variable
     * @param dop a decision operator
     * @param value a value
     * @return an SetDecision
     */
    public SetDecision makeSetDecision(SetVar var, DecisionOperator<SetVar> dop, int value) {
        SetDecision d = setDecisionPool.getE();
        if (d == null) {
            d = new SetDecision(setDecisionPool);
        }
        d.set(var, value, dop);
        return d;
    }

    public GraphDecision makeGraphNodeDecision(GraphVar var, GraphDecisionOperator dop, int node) {
        GraphDecision d = graphDecisionPool.getE();
        if (d == null) {
            d = new GraphDecision(graphDecisionPool);
        }
        d.setNode(var, node, dop);
        return d;
    }

    public GraphDecision makeGraphEdgeDecision(GraphVar var, GraphDecisionOperator dop, int from, int to) {
        GraphDecision d = graphDecisionPool.getE();
        if (d == null) {
            d = new GraphDecision(graphDecisionPool);
        }
        d.setEdge(var, from, to, dop);
        return d;
    }
}
