/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

/**
 * Bin packing example
 * put items into bins so that bin load is balanced
 *
 * Illustrates the enumeration of optimal solutions
 *
 * @author Jean-Guillaume Fages
 */
public class BinPacking extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	int nbItems;
	IntVar[] bins;
	int[] weights;
	int nbBins;
	IntVar[] loads;
	IntVar minLoad;
    List<Solution> solutions = new ArrayList<>();

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() {
		model = new Model("bin packing sample");
		// input
		nbItems = d1_w.length;
		weights = d1_w;
		nbBins = d1_nb;
		// variables
		bins = model.intVarArray("bin", nbItems, 0, nbBins - 1, false);
		loads = model.intVarArray("load", nbBins, 0, 1000, true);
		minLoad = model.intVar("minLoad", 0, 1000, true);
		model.binPacking(bins, weights, loads, 0).post();
		model.min(minLoad, loads).post();
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public void solve() {
		int nbOpt = 2;
		int mode = 0;
		switch (mode) {
			case 0:// to check
				model.arithm(minLoad, "=", 17).post();
				while (model.getSolver().solve()) {
					solutions.add(new Solution(model).record());
					nbOpt++;
				}
				out.println("There are " + nbOpt + " optima");
				for (Solution s : solutions) {
					out.println(s);
				}
				break;
			case 1:// one step approach (could be slow)
				// non-strict optimization
				model.setObjective(true, minLoad);
				while (model.getSolver().solve()) ;
				break;
			case 2:// two step approach (find and prove optimum, then enumerate)
				model.setObjective(true, minLoad);
				int opt = -1;
				while (model.getSolver().solve()) {
					out.println("better solution found : " + minLoad);
					opt = minLoad.getValue();
				}
				if (opt != -1) {
					model.getSolver().reset();
					model.arithm(minLoad, "=", opt).post();
					model.clearObjective();
					while (model.getSolver().solve()) {
						nbOpt++;
					}
					out.println("There are " + nbOpt + " optima");
				}
				break;
			default:
				throw new UnsupportedOperationException();
		}
	}

	//***********************************************************************************
	// DATA
	//***********************************************************************************

	public static void main(String[] args){
	    new BinPacking().execute(args);
	}
	private final static int[] d1_w = new int[]{
			2,5,3,4,12,9,1,0,5,6,2,4
	};
	private final static int d1_nb = 3;
}
