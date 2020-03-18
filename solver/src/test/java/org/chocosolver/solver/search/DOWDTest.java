/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ProblemMaker;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.domOverWDegSearch;

/**
 * @author Jean-Guillaume Fages
 * @since 22/04/15
 * Created by IntelliJ IDEA.
 */
public class DOWDTest {

	@Test(groups="10s", timeOut=60000)
	public void testCostas() {
//		Model s1 = costasArray(7, false);
		Model s2 = costasArray(7, true);

//		while (s1.getSolver().solve()) ;
//		out.println(s1.getSolver().getSolutionCount());

		while (s2.getSolver().solve()) ;

//		out.println(s2.getSolver().getSolutionCount());
//		assertEquals(s1.getSolver().getSolutionCount(), s2.getSolver().getSolutionCount());
	}

	private Model costasArray(int n, boolean chs){
		Model model = ProblemMaker.makeCostasArrays(n);
//		IntVar[] vars = (IntVar[]) model.getHook("vars");
		IntVar[] vectors = (IntVar[]) model.getHook("vectors");

		Solver r = model.getSolver();
		//r.limitTime(5000);
		if(chs){
			r.setSearch(domOverWDegSearch(ArrayUtils.append(/*vars, */vectors)));
			r.setGeometricalRestart(100, 1.1d, new FailCounter(model, 0), 1000);
            r.setNoGoodRecordingFromSolutions(vectors);
		}else{
			r.setSearch(Search.inputOrderLBSearch(vectors));
		}
		r.showShortStatistics();
		return model;
	}
}
