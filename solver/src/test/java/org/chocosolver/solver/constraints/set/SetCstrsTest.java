/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.annotations.Test;

import static java.lang.System.out;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Jean-Guillaume Fages
 * @since 22/01/16
 * Created by IntelliJ IDEA.
 */
public class SetCstrsTest {

	@Test(groups="1s", timeOut=60000)
	public static void testEq() {
		IntVar[] v1 = eqFilter("offset");
		IntVar[] v2 = eqFilter("allEqual");
		for (int i = 0; i < v1.length; i++) {
			assertEquals(v1[i].getDomainSize(), v2[i].getDomainSize());
			for (int v = v1[i].getLB(); v <= v1[i].getUB(); v = v1[i].nextValue(v)) {
				assertTrue(v2[i].contains(v));
			}
		}
		while (v1[0].getModel().getSolver().solve()) ;
		while (v2[0].getModel().getSolver().solve()) ;
		assertEquals(
				v1[0].getModel().getSolver().getSolutionCount(),
				v2[0].getModel().getSolver().getSolutionCount()
		);
	}

	public static IntVar[] eqFilter(String mode) {
		Model s = new Model();
		IntVar x = s.intVar("x", 0, 10, false);
		IntVar y = s.intVar("y", 0, 10, false);
		// set view of A
		SetVar xset = s.setVar("x as a set", new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
		SetVar yset = s.setVar("y as a set", new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
		s.union(new IntVar[]{x}, xset).post();
		s.union(new IntVar[]{y}, yset).post();
		// X +9 <= Y or Y + 9 <= X
		SetVar Xleft = s.setVar(new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
		SetVar tmpLeft = s.setVar(new int[]{}, new int[]{9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
		s.offSet(Xleft, tmpLeft, 9).post();
		SetVar Yleft = s.setVar("", new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
		eq(tmpLeft, Yleft, mode).post();

		SetVar Yright = s.setVar(new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
		SetVar tmpRight = s.setVar(new int[]{}, new int[]{9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
		s.offSet(Yright, tmpRight, 9).post();
		SetVar Xright = s.setVar(new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
		eq(tmpRight, Xright, mode).post();

		//
		s.union(new SetVar[]{Xleft, Xright}, xset).post();
		s.union(new SetVar[]{Yleft, Yright}, yset).post();
		// link to booleans
		BoolVar b1 = s.notEmpty(Yleft).reify();
		BoolVar b2 = s.notEmpty(Yright).reify();
		// ---
		s.addClausesBoolOrArrayEqualTrue(new BoolVar[]{b1, b2});

		s.getMinisat().getPropSat().initialize();
		try {
			s.getSolver().propagate();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		out.println(mode);
		out.println(x);
		out.println(y);
		out.println("%%%%%%");
		return new IntVar[]{x, y};
	}

	public static Constraint eq(SetVar x, SetVar y, String mode){
		switch (mode){
			case "offset":return x.getModel().offSet(x, y, 0);
			default:
			case "allEqual":return x.getModel().allEqual(x, y);
		}
	}
}
