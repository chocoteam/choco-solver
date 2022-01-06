/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.cumulative.Cumulative;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.chocosolver.solver.search.strategy.Search.lastConflict;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;

/**
 * Tests the various filtering algorithms of the cumulative constraint
 * @author Thierry Petit, Jean-Guillaume Fages
 */
public class CumulativeTest {

	public static final boolean VERBOSE = false;
	// too long, but can be used manually
	public void testLong(){
		for(int mode:new int[]{1})
			for(int n=1;n<100;n*=2){
				for(int dmin = 0; dmin<5;dmin++){
					for(int hmax = 0; hmax<5;hmax++){
						for(int capamax = 0; capamax<6;capamax++){
							for(long seed = 0; seed<5;seed++){
								test(n,capamax,dmin,hmax,seed,mode);
							}
						}
					}
				}
			}
	}

	@Test(groups="1s", timeOut=60000)
	public void testDur0() {
		Model m = new Model();

		Task t1 = new Task(
				m.intVar(9),
				m.intVar(6),
				m.intVar(15)
		);
		Task t2 = new Task(
				m.intVar(8),
				m.intVar(new int[]{0,6}),
				m.intVar(8,14)
		);

		m.cumulative(new Task[]{t1, t2}, new IntVar[]{m.intVar(1), m.intVar(1)}, m.intVar(1), true, Cumulative.Filter.TIME.make(2)).post();

		Solver s = m.getSolver();

		try {
			s.propagate();
		} catch (ContradictionException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
		Assert.assertTrue(t2.getDuration().isInstantiatedTo(0));
	}

	@Test(groups="1s", timeOut=60000)
	public void test1(){
		test(4,5,0,2,0,0);
		test(4,5,0,2,0,1);
		test(4,5,1,2,0,0);
		test(4,5,1,2,0,1);
		test(4,5,2,2,0,0);
		test(4,5,2,2,0,1);
	}

	@Test(groups="1s", timeOut=60000)
	public void test2(){
		test(4,9,2,4,2,1);
	}

	@Test(groups="1s", timeOut=60000)
	public void test3(){
		test(32,3,0,2,3,0);
	}

	@Test(groups="1s", timeOut=60000)
	public void test4(){
		test(16,6,2,4,9,0);
	}

	@Test(groups="1s", timeOut=60000)
	public void test5(){
		test(32,3,2,4,1,0);
	}

	@Test(groups="10s", timeOut=60000)
	public void test6(){
		// this tests raises an exception which is in fact due to the time limit
		// and unlucky random heuristic (fixed by adding last conflict)
		test(16,3,2,4,4,1);
		test(32,3,2,2,3,0);
	}

	@Test(groups="10s", timeOut=60000)
	public void testMed(){
		for(int mode:new int[]{0,1})
			for(int n=1;n<15;n*=2){
				for(int dmin = 0; dmin<5;dmin+=2){
					for(int hmax = 0; hmax<5;hmax+=2){
						for(int capamax = 0; capamax<10;capamax+=3){
							test(n,capamax,dmin,hmax,0,mode);
						}
					}
				}
			}
	}

	public void test(int n, int capamax, int dmin, int hmax, long seed, int mode){
		if(VERBOSE)System.out.println(n+" - "+capamax+" - "+dmin+" - "+hmax+" - "+seed+" - "+mode);
		Cumulative.Filter[][] filters = new Cumulative.Filter[][]{
				{Cumulative.Filter.TIME},
				{Cumulative.Filter.TIME,Cumulative.Filter.NRJ},
				{Cumulative.Filter.HEIGHTS,Cumulative.Filter.SWEEP},
				{Cumulative.Filter.HEIGHTS,Cumulative.Filter.SWEEP_HEI_SORT},
				{Cumulative.Filter.HEIGHTS,Cumulative.Filter.SWEEP,Cumulative.Filter.NRJ},
				{Cumulative.Filter.HEIGHTS,Cumulative.Filter.SWEEP_HEI_SORT,Cumulative.Filter.NRJ},
				{Cumulative.Filter.TIME,Cumulative.Filter.HEIGHTS,Cumulative.Filter.SWEEP,Cumulative.Filter.NRJ},
				{Cumulative.Filter.TIME,Cumulative.Filter.HEIGHTS,Cumulative.Filter.SWEEP_HEI_SORT,Cumulative.Filter.NRJ},
				{Cumulative.Filter.TIME,Cumulative.Filter.HEIGHTS,Cumulative.Filter.SWEEP,Cumulative.Filter.SWEEP_HEI_SORT,Cumulative.Filter.NRJ}
		};
		long ref = solve(n,capamax,dmin,hmax,seed,true, mode);
		if(ref==-1)return;
		for(boolean g : new boolean[]{true,false})	// graph-based
			for(int f=1;f<filters.length;f++){
				long val = solve(n,capamax,dmin,hmax,seed,g, mode);
				assert ref == val || val==-1 :"filter "+f+" failed (can be due to the heuristic in case of timeout)";
			}
	}

	public static long solve(int n, int capamax, int dmin, int hmax, long seed,
							 boolean graph, int mode) {
		final Model model = new Model();
		int dmax = 5 + dmin * 2;
		final IntVar[] s = model.intVarArray("s", n, 0, n * dmax, false);
		final IntVar[] d = model.intVarArray("d", n, dmin, dmax, false);
		final IntVar[] e = model.intVarArray("e", n, 0, n * dmax, false);
		final IntVar[] h = model.intVarArray("h", n, 0, hmax, false);
		final IntVar capa = model.intVar("capa", 0, capamax, false);
		final IntVar last = model.intVar("last", 0, n * dmax, false);
		Task[] t = new Task[n];
		for (int i = 0; i < n; i++) {
			t[i] = new Task(s[i], d[i], e[i]);
			model.arithm(e[i], "<=", last).post();
		}
		model.cumulative(t, h, capa, graph).post();
		Solver r = model.getSolver();
		r.setSearch(lastConflict(randomSearch(model.retrieveIntVars(false), seed)));
		model.getSolver().limitTime(5000);
		switch (mode) {
			case 0:
				model.getSolver().solve();
				if (r.isStopCriterionMet()) return -1;
				return r.getMeasures().getSolutionCount();
			case 1:
				model.setObjective(Model.MINIMIZE, last);
				while(model.getSolver().solve());
				if (r.isStopCriterionMet()) return -1;
				return r.getMeasures().getBestSolutionValue().longValue();
			case 2:
				while (model.getSolver().solve()) ;// too many solutions to be used
				if (r.isStopCriterionMet()) return -1;
				return r.getMeasures().getSolutionCount();
			default:
				throw new UnsupportedOperationException();
		}
	}

    @Test(groups="10s", timeOut=60000)
    public void testADelsol1(){
		int[] height = new int[]{0, 1, 3, 5, 1, 4, 4, 3, 4, 3, 0};
		int capaMax = 10;
		int[] duration = new int[11];
		Arrays.fill(duration, 1);
		// dÃ©claration du modÃ¨le
		Model model = new Model("test");
		// Ajout des starting times
		IntVar[] start = model.intVarArray("start",11,0,3);
        model.cumulative(start, duration, height, capaMax);

		Solver solver= model.getSolver();
        while(solver.solve()){
            for(int time = 0; time < 4; ++time) {
                int max_height = 0;
                for(int i = 0 ; i < 11; ++i) {
                    if(start[i].getValue() == time) max_height += height[i];
                }
                Assert.assertTrue(max_height <= capaMax);
            }
        }
	}
}
