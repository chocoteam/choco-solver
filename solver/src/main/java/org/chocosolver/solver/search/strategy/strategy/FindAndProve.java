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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.Variable;

/**
 * Enables to switch from one heuristic to another once a solution has been found
 * @author Jean-Guillaume Fages
 * @since 07/11/13
 * @param <V>
 */
public class FindAndProve<V extends Variable> extends AbstractStrategy<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private final AbstractStrategy<V> find;
	private final AbstractStrategy<V> prove;
	private final Model model;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Heuristic which switches from one heuristic (heurToFindASol) to another (heurToProveOpt)
	 * once a solution has been found
	 *
	 * @param vars				variables to branch on
	 * @param heurToFindASol	a heuristic to branch on vars, to find a (good) solution easily
	 * @param heurToProveOpt	a heuristic to branch on vars, to prove the optimality of the solution
	 */
	public FindAndProve(V[] vars, AbstractStrategy<V> heurToFindASol, AbstractStrategy<V> heurToProveOpt) {
		super(vars);
		this.find = heurToFindASol;
		this.prove= heurToProveOpt;
		this.model = vars[0].getModel();
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean init(){
		return find.init() & prove.init();
	}

	@Override
	public Decision<V> getDecision() {
		if (model.getSolver().getSolutionCount() == 0) {
			return find.getDecision();
		}
		return prove.getDecision();
	}

	@Override
	public Decision<V> computeDecision(V variable) {
		if (model.getSolver().getSolutionCount() == 0) {
			return find.computeDecision(variable);
		}
		return prove.computeDecision(variable);
	}
}
