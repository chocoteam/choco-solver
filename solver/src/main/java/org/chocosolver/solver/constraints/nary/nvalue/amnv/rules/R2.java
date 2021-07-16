/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue.amnv.rules;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.mis.F;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;

import java.util.BitSet;

/**
 * R2 filtering rule (back-propagation)
 *
 * @since 01/01/2014
 * @author Jean-Guillaume Fages
 */
public class R2 implements R {

	private BitSet valInMIS;

	public void filter(IntVar[] vars, UndirectedGraph graph, F heur, Propagator aCause) throws ContradictionException{
		int n = vars.length-1;
		BitSet mis = heur.getMIS();
		if(mis.cardinality()==vars[n].getUB()){
			if(valInMIS == null) valInMIS = new BitSet();
			valInMIS.clear();
			for (int i = mis.nextSetBit(0); i >= 0; i = mis.nextSetBit(i + 1)) {
				int ub = vars[i].getUB();
				for (int k = vars[i].getLB(); k <= ub; k = vars[i].nextValue(k)) {
					valInMIS.set(k);
				}
			}
			for (int i = mis.nextClearBit(0); i < n; i = mis.nextClearBit(i + 1)) {
				int ub = vars[i].getUB();
				for (int k = vars[i].getLB(); k <= ub; k = vars[i].nextValue(k)) {
					if (!valInMIS.get(k)) {
						vars[i].removeValue(k, aCause);
					}
				}
			}
		}
	}
}
