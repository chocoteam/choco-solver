/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.real;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;

import static java.lang.System.out;

/**
 * The cyclo hexan problem but hybrids finite/continuous problems
 * <br/>
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since 18/07/12
 */
public class HybridCycloHexan extends AbstractProblem {

	RealVar[] vars;
	RealVar x, y, z;
	IntVar intx;


	@Override
	public void buildModel() {
		model = new Model();
		out.println("The CycloHexan problem consists in finding the 3D configuration of a cyclohexane molecule.\n"
				+ "It is decribed with a system of three non linear equations : \n"
				+ " y^2 * (1 + z^2) + z * (z - 24 * y) = -13 \n" +
				" x^2 * (1 + y^2) + y * (y - 24 * x) = -13 \n" +
				" z^2 * (1 + x^2) + x * (x - 24 * z) = -13 \n" +
				"This example comes from the Elisa project (LINA) examples. \n" +
				"This example restricts x to be integer, as an illustration to hybrid finite/continuous problems. \n" +
				"It has no solution. \n");

		double precision = 1.0e-3;
		// finite domain
		intx = model.intVar("x", new int[]{-10, -9, 0, 2, 42});
		// continuous view
		x = model.realIntView(intx, precision);
		y = model.realVar("y", -1.0e8, 1.0e8, precision);
		z = model.realVar("z", -1.0e8, 1.0e8, precision);

		vars = new RealVar[]{x, y, z};
		model.realIbexGenericConstraint(
				"{1}^2 * (1 + {2}^2) + {2} * ({2} - 24 * {1}) = -13;" +
						"{0}^2 * (1 + {1}^2) + {1} * ({1} - 24 * {0}) = -13;" +
						"{2}^2 * (1 + {0}^2) + {0} * ({0} - 24 * {2}) = -13",
				vars).post();
	}

	@Override
	public void configureSearch() {
		model.getSolver().setSearch(Search.realVarSearch(vars));
		model.getSolver().limitTime(10000);
	}

	@Override
	public void solve() {
		model.getSolver().solve();
	}

	public static void main(String[] args) {
		new HybridCycloHexan().execute(args);
	}
}
