/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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
package org.chocosolver.solver.search;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.search.strategy.SearchStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestMultiSequentialObjectives {

	/**
	 * find highest a,b,c (in that order) with a<b<c and a+b+c<100<br />
	 * best solution is 32,33,34
	 */
	@Test
	public void simpleLexTest1(){
		Model m = new Model();
		IntVar a = m.intVar("a", 0, 99), b = m.intVar("b", 0, 99), c = m.intVar("c", 0, 99);
		IntVar[] vals = new IntVar[] { a, b, c };
		m.sum(vals, "<", 100).post();// a+b+c<100
		m.arithm(a, "<", b).post();
		m.arithm(b, "<", c).post();// a<b<c
        m.getSolver().set(SearchStrategyFactory.inputOrderLBSearch(a, b, c));
		Solution s = m.findLexOptimalSolution(vals, true);
		Assert.assertNotNull(s);
		Assert.assertEquals(s.getIntVal(a).intValue(), 32);
		Assert.assertEquals(s.getIntVal(b).intValue(), 33);
		Assert.assertEquals(s.getIntVal(c).intValue(), 34);
	}

	/**
	 * find highest a,b,c (in that order) with a<b<c and a+b+c<100 and a+b=c<br />
	 * best solution is 24,25,49
	 */
	@Test
	public void simpleLexTest2(){
		Model m = new Model();
		IntVar a = m.intVar("a", 0, 99), b = m.intVar("b", 0, 99), c = m.intVar("c", 0, 99);
		IntVar[] vals = new IntVar[] { a, b, c };
		m.sum(vals, "<", 100).post();// a+b+c<100
		m.arithm(a, "<", b).post();
		m.arithm(b, "<", c).post();// a<b<c
		m.arithm(a,"+",b,"=",c).post();
        m.getSolver().set(SearchStrategyFactory.inputOrderLBSearch(a, b));
		Solution s = m.findLexOptimalSolution(vals, true);
		Assert.assertNotNull(s);
		Assert.assertEquals(s.getIntVal(a).intValue(), 24);
		Assert.assertEquals(s.getIntVal(b).intValue(), 25);
		Assert.assertEquals(s.getIntVal(c).intValue(), 49);
	}


    /**
     * find highest a,b,c (in that order) with a<b<c and a+b+c<100<br />
     * best solution is 32,33,34
     */
    @Test
    public void simpleLexTest3(){
        Model m = new Model();
        IntVar a = m.intVar("a", 0, 99), b = m.intVar("b", 0, 99), c = m.intVar("c", 0, 99);
        IntVar[] vals = new IntVar[] { a, b, c };
        m.sum(vals, "<", 100).post();// a+b+c<100
        m.arithm(a, "<", b).post();
        m.arithm(b, "<", c).post();// a<b<c
        m.getSolver().set(SearchStrategyFactory.inputOrderUBSearch(a, b, c));
        Solution s = m.findLexOptimalSolution(vals, false);
        Assert.assertNotNull(s);
        Assert.assertEquals(s.getIntVal(a).intValue(), 0);
        Assert.assertEquals(s.getIntVal(b).intValue(), 1);
        Assert.assertEquals(s.getIntVal(c).intValue(), 2);
    }

}
