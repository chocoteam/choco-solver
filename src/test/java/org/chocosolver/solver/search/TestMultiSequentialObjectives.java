package org.chocosolver.solver.search;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestMultiSequentialObjectives {

	/**
	 * find highest a,b,c (in that order) with a<b<c and a+b+c<100<br />
	 * best solution is 32,33,34
	 */
	@Test
	public void simpleSumTest(){
		Model m = new Model();
		IntVar a = m.intVar("a", 0, 99), b = m.intVar("b", 0, 99), c = m.intVar("c", 0, 99);
		IntVar[] vals = new IntVar[] { a, b, c };
		m.sum(vals, "<", 100).post();// a+b+c<100
		m.arithm(a, "<", b).post();
		m.arithm(b, "<", c).post();// a<b<c
		Solution s = m.findOptimalSolution(vals, true);
		Assert.assertNotNull(s);
		Assert.assertEquals((int) s.getIntVal(a), 32);
		Assert.assertEquals((int) s.getIntVal(b), 33);
		Assert.assertEquals((int) s.getIntVal(c), 34);
	}

}
