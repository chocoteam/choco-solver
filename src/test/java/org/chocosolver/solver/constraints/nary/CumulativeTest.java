/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.cumulative.CumulFilter;
import org.chocosolver.solver.constraints.nary.cumulative.Cumulative;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
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

		m.cumulative(new Task[]{t1, t2}, new IntVar[]{m.intVar(1), m.intVar(1)}, m.intVar(1), true, new CumulFilter(2) {
            private int[] time = new int[31];
            @Override
            public void filter(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, ISet tasks, Propagator<IntVar> aCause) throws ContradictionException {
                int min = Integer.MAX_VALUE / 2;
                int max = Integer.MIN_VALUE / 2;
                ISetIterator tIter = tasks.iterator();
                while (tIter.hasNext()){
                    int i = tIter.nextInt();
                    if (s[i].getUB() < e[i].getLB()) {
                        min = Math.min(min, s[i].getUB());
                        max = Math.max(max, e[i].getLB());
                    }
                }
                if (min < max) {
                    if(max-min>time.length){
                        time = new int[max-min];
                    }
                    else{
                        Arrays.fill(time, 0, max - min, 0);
                    }
                    int capaMax = capa.getUB();
                    // fill mandatory parts and filter capacity
                    int elb,hlb;
                    int maxC=0;

                    tIter = tasks.iterator();
                    while (tIter.hasNext()){
                        int i = tIter.nextInt();
                        elb = e[i].getLB();
                        hlb = h[i].getLB();
                        for (int t = s[i].getUB(); t < elb; t++) {
                            time[t - min] += hlb;
                            maxC = Math.max(maxC,time[t - min]);
                        }
                    }
                    capa.updateLowerBound(maxC, aCause);
                    // filter max height
                    int minH;

                    tIter = tasks.iterator();
                    while (tIter.hasNext()){
                        int i = tIter.nextInt();
                        if(!h[i].isInstantiated()){
                            minH = h[i].getUB();
                            elb = e[i].getLB();
                            hlb = h[i].getLB();
                            for (int t = s[i].getUB(); t < elb; t++) {
                                minH = Math.min(minH,capaMax-(time[t-min]-hlb));
                            }
                            h[i].updateUpperBound(minH,aCause);
                        }
                    }
                    for (int i : tasks) {
                        if (h[i].getLB() > 0) { // d[i].getLB() > 0 &&
                            // filters
                            System.out.println("filter "+i);
                            if (s[i].getLB() + d[i].getLB() > min) {
                                filterInf(s[i],e[i].getLB(),d[i].getLB(),h[i].getLB(), min, max, time, capaMax, aCause);
                            }
                            if (e[i].getUB() - d[i].getLB() < max) {
                                filterSup(s[i].getUB(),e[i],d[i].getLB(),h[i].getLB(), min, max, time, capaMax, aCause);
                            }
                        }
                    }
                }
            }

            protected void filterInf(IntVar start, int elb, int dlb, int hlb, int min, int max, int[] time, int capaMax, Propagator<IntVar> aCause) throws ContradictionException {
                int nbOk = 0;
                int sub = start.getUB();
                for (int t = start.getLB(); t < sub; t++) {
                    if (t < min || t >= max || hlb + time[t - min] <= capaMax) {
                        nbOk++;
                        if (nbOk == dlb) {
                            return;
                        }
                    } else {
                        if(dlb==0 && t >= elb)return;
                        nbOk = 0;
                        start.updateLowerBound(t + 1, aCause);
                    }
                }
            }

            protected void filterSup(int sub, IntVar end, int dlb, int hlb, int min, int max, int[] time, int capaMax, Propagator<IntVar> aCause) throws ContradictionException {
                int nbOk = 0;
                int elb = end.getLB();
                for (int t = end.getUB(); t > elb; t--) {
                    if (t - 1 < min || t - 1 >= max || hlb + time[t - min - 1] <= capaMax) {
                        nbOk++;
                        if (nbOk == dlb) {
                            return;
                        }
                    } else {
                        if(dlb==0 && t <= sub)return;
                        nbOk = 0;
                        end.updateUpperBound(t - 1, aCause);
                    }
                }
            }
        }).post();

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
}
