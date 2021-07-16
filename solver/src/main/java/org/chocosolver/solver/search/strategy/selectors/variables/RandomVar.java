/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.objects.setDataStructures.swapList.Set_Std_Swap;

import java.util.Random;

/**
 * Random variable selector & evaluator to be used with fast restart strategy
 * @author Jean-Guillaume FAGES (cosling)
 * @since 06/04/2019.
 */
public class RandomVar<T extends Variable> implements VariableSelector<T>, VariableEvaluator<T> {

	/** To store index of variable to select randomly */
	private Set_Std_Swap sets;

	/** Random number generator */
	private java.util.Random random;

	/**
	 * Random variable selector & evaluator more efficient with fast restart
	 * @param seed seed for random number generator.
	 * @param scope variables to branch on, must not be empty
	 */
	public RandomVar(long seed, IntVar[] scope) {
		sets = new Set_Std_Swap(scope[0].getEnvironment(), 0);
		for(int i=0;i<scope.length;i++)sets.add(i);
		random = new Random(seed);
	}

	@Override
	public T getVariable(T[] variables) {
		while (sets.size()>0){
			int nextIdx = random.nextInt(sets.size());
			int nextVal = sets.getNth(nextIdx);
			if(variables[nextVal].isInstantiated())sets.remove(nextVal);
			else return variables[nextVal];
		}
		return null;
	}

	@Override
	public double evaluate(T variable) {
		return random.nextDouble();
	}
}

