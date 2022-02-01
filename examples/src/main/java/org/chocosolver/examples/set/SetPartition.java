/*
 * This file is part of examples, http://choco-solver.org/
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
 * Date: 14/01/13
 * Time: 18:25
 */

package org.chocosolver.examples.set;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

/**
 * Small problem to illustrate how to use set variables
 * finds a partition a universe so that the sum of elements in universe
 * (restricted to the arbitrary interval [12,19]) is minimal
 *
 * @author Jean-Guillaume Fages
 */
public class SetPartition extends AbstractProblem {

	private SetVar x, y, z, universe;
	private IntVar sum;

	@Override
	public void buildModel() {
		model = new Model();

		///////////////
		// VARIABLES //
		///////////////

		// x initial domain
		int[] x_envelope = new int[]{1, 3, 2, 8}; // not necessarily ordered
		int[] x_kernel = new int[]{1};
		x = model.setVar("x", x_kernel, x_envelope);
		// y initial domain
		int[] y_envelope = new int[]{2, 6, 7};
		y = model.setVar("y", new int[]{}, y_envelope);
		// z initial domain
		int[] z_envelope = new int[]{2, 1, 3, 5, 7, 12};
		int[] z_kernel = new int[]{2};
		z = model.setVar("z", z_kernel, z_envelope);
		// universe initial domain (note that the universe is a variable)
		int[] universe_envelope = new int[]{1, 2, 3, 5, 7, 8, 42};
		universe = model.setVar("universe", new int[]{}, universe_envelope);
		// sum variable
		sum = model.intVar("sum of universe", 12, 19, true);

		/////////////////
		// CONSTRAINTS //
		/////////////////

		// partition constraint
		model.partition(new SetVar[]{x, y, z}, universe).post();
		// forbid empty sets
		model.nbEmpty(new SetVar[]{x, y, z, universe}, model.intVar(0)).post();
		// restricts the sum of elements in universe
		model.sum(universe, sum).post();
	}

	@Override
	public void solve() {
		// find the optimum
		model.setObjective(false, sum);
		while(model.getSolver().solve()){
			System.out.println("new solution found");
			System.out.println(x);
			System.out.println(y);
			System.out.println(z);
			System.out.println(universe);
			System.out.println(sum);
		}
	}

}
