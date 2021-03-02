/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.search.strategy.assignments.GraphAssignment;
import org.chocosolver.solver.search.strategy.decision.GraphDecision;
import org.chocosolver.solver.search.strategy.selectors.values.GraphLexArc;
import org.chocosolver.solver.search.strategy.selectors.values.GraphRandomArc;
import org.chocosolver.solver.search.strategy.selectors.values.GraphLexNode;
import org.chocosolver.solver.search.strategy.selectors.values.GraphRandomNode;
import org.chocosolver.solver.search.strategy.selectors.values.GraphArcSelector;
import org.chocosolver.solver.search.strategy.selectors.values.GraphNodeSelector;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.util.PoolManager;

/**
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 1 April 2011
 */
public class GraphStrategy extends AbstractStrategy<GraphVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected GraphVar g;
	protected GraphNodeSelector nodeStrategy;
	protected GraphArcSelector arcStrategy;
	protected NodeArcPriority priority;
	protected PoolManager<GraphDecision> pool;

	public enum NodeArcPriority {
		NODES_THEN_ARCS,
		ARCS
	}

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Dedicated graph branching strategy.
	 *
	 * @param g        a graph variable to branch on
	 * @param ns       strategy over nodes
	 * @param as       strategy over arcs/edges
	 * @param priority enables to mention if it should first branch on nodes
	 */
	public GraphStrategy(GraphVar g, GraphNodeSelector ns, GraphArcSelector as, NodeArcPriority priority) {
		super(g);
		this.g = g;
		this.nodeStrategy = ns;
		this.arcStrategy = as;
		this.priority = priority;
		pool = new PoolManager<>();
	}

	/**
	 * Lexicographic graph branching strategy.
	 * Branch on nodes then arcs/edges.
	 * <br>
	 * <br> node branching:
	 * Let i be the first node such that
	 * i in envelope(g) and i not in kernel(g).
	 * The decision adds i to the kernel of g.
	 * It is fails, then i is removed from the envelope of g.
	 * <br>
	 * arc/edge branching:
	 * <br> node branching:
	 * Let (i,j) be the first arc/edge such that
	 * (i,j) in envelope(g) and (i,j) not in kernel(g).
	 * The decision adds (i,j) to the kernel of g.
	 * It is fails, then (i,j) is removed from the envelope of g
	 *
	 * @param g a graph variable to branch on
	 */
	public GraphStrategy(GraphVar g) {
		this(g, new GraphLexNode(g), new GraphLexArc(g), NodeArcPriority.NODES_THEN_ARCS);
	}

	/**
	 * Random graph branching strategy.
	 * Alternate randomly node and arc/edge decisions.
	 * <br>
	 * <br> node branching:
	 * Let i be a randomly selected node such that
	 * i in envelope(g) and i not in kernel(g).
	 * The decision adds i to the kernel of g.
	 * It is fails, then i is removed from the envelope of g.
	 * <br>
	 * arc/edge branching:
	 * <br> node branching:
	 * Let (i,j) be a randomly selected arc/edge arc/edge such that
	 * (i,j) in envelope(g) and (i,j) not in kernel(g).
	 * The decision adds (i,j) to the kernel of g.
	 * It is fails, then (i,j) is removed from the envelope of g
	 *
	 * @param g    a graph variable to branch on
	 * @param seed randomness seed
	 */
	public GraphStrategy(GraphVar g, long seed) {
		this(g, new GraphRandomNode(g, seed), new GraphRandomArc(g, seed), NodeArcPriority.NODES_THEN_ARCS);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public GraphDecision getDecision() {
		if (g.isInstantiated()) {
			return null;
		}
		GraphDecision dec = pool.getE();
		if (dec == null) {
			dec = new GraphDecision(pool);
		}
		switch (priority) {
			case NODES_THEN_ARCS:
				int node = nextNode();
				if (node != -1) {
					dec.setNode(g, node, GraphAssignment.graph_enforcer);
				} else {
					if (arcStrategy == null) {
						return null;
					}
					nextArc();
					dec.setArc(g, arcStrategy.getFrom(), arcStrategy.getTo(), GraphAssignment.graph_enforcer);
				}
				break;
			case ARCS:
			default:
				if (!nextArc()) {
					return null;
				}
				dec.setArc(g, arcStrategy.getFrom(), arcStrategy.getTo(), GraphAssignment.graph_enforcer);
				break;
		}
		return dec;
	}

	public int nextNode() {
		return nodeStrategy.nextNode();
	}

	public boolean nextArc() {
		return arcStrategy.computeNextArc();
	}
}
