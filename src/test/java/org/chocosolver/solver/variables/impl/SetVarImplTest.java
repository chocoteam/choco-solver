/**
 *
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 */
public class SetVarImplTest {

	@Test
	public void testSetVarInstantiated() {
		Model m = new Model();
		Assert.assertTrue(m.setVar("var", new int[] { -1, -2, 3, 18 }).getCard().isInstantiatedTo(4));
		Assert.assertTrue(m.setVar("var", new int[] { -1, -1, -1, 0 }).getCard().isInstantiatedTo(2));
		Assert.assertTrue(m.setVar("var").getCard().isInstantiatedTo(0));
	}

	@Test
	public void testPropagate() {
		Model m = new Model();
		SetVar s = m.setVar("var", new int[] {}, new int[] { 0, 1, 2, 3 });
		IntVar a = m.intVar("a", 0), b = m.intVar("b", 0, 3), c = m.intVar("c", 0, 3), d = m.intVar("d", 0, 3);
		m.member(a, s).reifyWith(m.member(b, s).getOpposite().reify());// a in s XOR b in s
		m.member(c, s).reifyWith(m.member(d, s).getOpposite().reify());// c in s XOR d in s
		m.allDifferent(a, b, c, d).post();
		// 4 different variables, among them two belong to s so c = 2. Which of a or b belong to s makes o difference
		s.setCard(c);
		Assert.assertTrue(m.solve());
		Assert.assertTrue(c.isInstantiatedTo(2), "" + c);
	}

}
