/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Guillaume Le Lou�t [guillaume.lelouet@gmail.com] 2016
 */
public class SetVarImplTest {

	@Test(groups="1s", timeOut=60000)
	public void testSetVarInstantiated() {
		Model m = new Model();
		Assert.assertTrue(m.setVar("var", new int[] { -1, -2, 3, 18 }).getCard().isInstantiatedTo(4));
		Assert.assertTrue(m.setVar("var", new int[] { -1, -1, -1, 0 }).getCard().isInstantiatedTo(2));
		Assert.assertTrue(m.setVar("var").getCard().isInstantiatedTo(0));
	}

	@Test(groups="1s", timeOut=60000)
	public void testPropagate() {
		Model m = new Model();
		SetVar s = m.setVar("var", new int[] {}, new int[] { 0, 1, 2, 3 });
		IntVar a = m.intVar("a", 0), b = m.intVar("b", 0, 3), c = m.intVar("c", 0, 3), d = m.intVar("d", 0, 3);
		m.member(a, s).reifyWith(m.member(b, s).getOpposite().reify());// a in s XOR b in s
		m.member(c, s).reifyWith(m.member(d, s).getOpposite().reify());// c in s XOR d in s
		m.allDifferent(a, b, c, d).post();
		// 4 different variables, among them two belong to s so c = 2. Which of a or b belong to s makes o difference
		s.setCard(c);
		Assert.assertTrue(m.getSolver().solve());
		Assert.assertTrue(c.isInstantiatedTo(2), "" + c);
	}

	@Test(groups="1s", timeOut=60000)
	public void testSetVarInstantiated2() {
		Model m = new Model();
		Assert.assertTrue(new SetVarImpl("var", SetFactory.makeConstantSet(-2,0), m).getCard().isInstantiatedTo(3));
		Assert.assertTrue(new SetVarImpl("var", SetFactory.makeConstantSet(new int[]{-1,1}),m).getCard().isInstantiatedTo(2));
		Assert.assertTrue(new SetVarImpl("var", SetFactory.makeConstantSet(new int[]{}), m).getCard().isInstantiatedTo(0));
	}

	@Test(groups="1s", timeOut=60000)
	public void testPropagate2() {
		Model m = new Model();
		ISet lb = SetFactory.makeStoredSet(SetType.LINKED_LIST,0,m);
		ISet ub = SetFactory.makeStoredSet(SetType.BITSET,0,m);
		for(int i:new int[]{0, 1, 2, 3}) {
			ub.add(i);
		}
		SetVar s = new SetVarImpl("var", lb, ub, m);
		IntVar a = m.intVar("a", 0), b = m.intVar("b", 0, 3), c = m.intVar("c", 0, 3), d = m.intVar("d", 0, 3);
		m.member(a, s).reifyWith(m.member(b, s).getOpposite().reify());// a in s XOR b in s
		m.member(c, s).reifyWith(m.member(d, s).getOpposite().reify());// c in s XOR d in s
		m.allDifferent(a, b, c, d).post();
		// 4 different variables, among them two belong to s so c = 2. Which of a or b belong to s makes o difference
		s.setCard(c);
		Assert.assertTrue(m.getSolver().solve());
		Assert.assertTrue(c.isInstantiatedTo(2), "" + c);
	}

}
