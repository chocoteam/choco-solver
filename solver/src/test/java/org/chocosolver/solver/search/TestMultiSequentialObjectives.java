/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;

public class TestMultiSequentialObjectives {

	/**
	 * find highest a,b,c (in that order) with a<b<c and a+b+c<100<br />
	 * best solution is 32,33,34
	 */
	@Test(groups="1s", timeOut=60000)
	public void simpleLexTest1(){
		Model m = new Model();
		IntVar a = m.intVar("a", 0, 99), b = m.intVar("b", 0, 99), c = m.intVar("c", 0, 99);
		IntVar[] vals = new IntVar[] { a, b, c };
		m.sum(vals, "<", 100).post();// a+b+c<100
		m.arithm(a, "<", b).post();
		m.arithm(b, "<", c).post();// a<b<c
        m.getSolver().setSearch(Search.inputOrderLBSearch(a, b, c));
		Solution s = m.getSolver().findLexOptimalSolution(vals, true);
		Assert.assertNotNull(s);
		Assert.assertEquals(s.getIntVal(a), 32);
		Assert.assertEquals(s.getIntVal(b), 33);
		Assert.assertEquals(s.getIntVal(c), 34);
	}

	/**
	 * find highest a,b,c (in that order) with a<b<c and a+b+c<100 and a+b=c<br />
	 * best solution is 24,25,49
	 */
	@Test(groups="1s", timeOut=60000)
	public void simpleLexTest2(){
		Model m = new Model();
		IntVar a = m.intVar("a", 0, 99), b = m.intVar("b", 0, 99), c = m.intVar("c", 0, 99);
		IntVar[] vals = new IntVar[] { a, b, c };
		m.sum(vals, "<", 100).post();// a+b+c<100
		m.arithm(a, "<", b).post();
		m.arithm(b, "<", c).post();// a<b<c
		m.arithm(a,"+",b,"=",c).post();
        m.getSolver().setSearch(Search.inputOrderLBSearch(a, b));
		Solution s = m.getSolver().findLexOptimalSolution(vals, true);
		Assert.assertNotNull(s);
		Assert.assertEquals(s.getIntVal(a), 24);
		Assert.assertEquals(s.getIntVal(b), 25);
		Assert.assertEquals(s.getIntVal(c), 49);
	}


    /**
     * find highest a,b,c (in that order) with a<b<c and a+b+c<100<br />
     * best solution is 32,33,34
     */
	@Test(groups="1s", timeOut=60000)
    public void simpleLexTest3(){
        Model m = new Model();
        IntVar a = m.intVar("a", 0, 99), b = m.intVar("b", 0, 99), c = m.intVar("c", 0, 99);
        IntVar[] vals = new IntVar[] { a, b, c };
        m.sum(vals, "<", 100).post();// a+b+c<100
        m.arithm(a, "<", b).post();
        m.arithm(b, "<", c).post();// a<b<c
        m.getSolver().setSearch(Search.inputOrderUBSearch(a, b, c));
        Solution s = m.getSolver().findLexOptimalSolution(vals, false);
        Assert.assertNotNull(s);
        Assert.assertEquals(s.getIntVal(a), 0);
        Assert.assertEquals(s.getIntVal(b), 1);
        Assert.assertEquals(s.getIntVal(c), 2);
    }

	@Test(groups="1s", timeOut=60000)
	public void simpleLexTest4(){
		Model m = new Model();
		SetVar sv = m.setVar(new int[]{}, new int[]{0,1,2,3,4,5});
		int[] size = new int[]{8,6,3,3,3};
		IntVar card = m.intVar("load", 0,5);
		IntVar load = m.intVar("card", 0,10);
		sv.setCard(card);
		m.sumElements(sv,size,load).post();
        m.getSolver().setSearch(Search.setVarSearch(sv),inputOrderLBSearch(card,load));
		Solution s = m.getSolver().findLexOptimalSolution(new IntVar[]{load,m.intMinusView(card)}, true);
		Assert.assertNotNull(s);

		Assert.assertEquals(s.getIntVal(load), 9);
		Assert.assertEquals(s.getIntVal(card), 2);
	}

}
