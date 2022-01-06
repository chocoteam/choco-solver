/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue.amnv.graph;

import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * Backtrackable undirected graph
 *
 * @since 01/01/2014
 * @author Jean-Guillaume Fages
 */
public abstract class G extends UndirectedGraph{

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Creates a backtrackable undirected graph of nbNodes nodes
	 * @param model	solver providing the backtracking environment
	 * @param nbNodes	size of the graph (number of nodes)
	 */
	public G(Model model, int nbNodes) {
		super(model, nbNodes, SetType.BIPARTITESET, true);
	}

	//***********************************************************************************
	// ALGORITHMS
	//***********************************************************************************

	/** Initialises the graph */
	public abstract void build();

	/** Updates the graph */
	public abstract void update();

	public abstract void update(int idx);
}
