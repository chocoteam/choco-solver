/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.nary;

import org.testng.annotations.Test;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.constraints.nary.cumulative.Cumulative;
import solver.constraints.nary.cumulative.PropFullCumulative;
import solver.constraints.nary.cumulative.PropGraphCumulative;
import solver.search.loop.monitors.SMF;
import solver.search.strategy.ISF;
import solver.variables.IntVar;
import solver.variables.Task;
import solver.variables.VF;

/**
 * Tests the various filtering algorithms of the cumulative constraint
 * @author Thierry Petit, Jean-Guillaume Fages
 */
public class CumulativeTest {

	public static final boolean VERBOSE = true;
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

	@Test(groups = "1s")
	public void test1(){
		test(4,5,0,2,0,0);
		test(4,5,0,2,0,1);
		test(4,5,1,2,0,0);
		test(4,5,1,2,0,1);
		test(4,5,2,2,0,0);
		test(4,5,2,2,0,1);
	}

	@Test(groups = "1s")
	public void test2(){
		test(4,9,2,4,2,1);
	}

	@Test(groups = "1s")
	public void test3(){
		test(32,3,0,2,3,0);
	}

	@Test(groups = "1s")
	public void test4(){
		test(16,6,2,4,9,0);
	}

	@Test(groups = "1s")
	public void test5(){
		test(32,3,2,4,1,0);
	}

	@Test(groups = "10m")
	public void testMed(){
		for(int mode:new int[]{0,1})
			for(int n=1;n<100;n*=2){
				for(int dmin = 0; dmin<5;dmin+=2){
					for(int hmax = 0; hmax<5;hmax+=2){
						for(int capamax = 0; capamax<10;capamax+=3){
							for(long seed = 0; seed<10;seed++){
								test(n,capamax,dmin,hmax,seed,mode);
							}
						}
					}
				}
			}
	}

	public void test(int n, int capamax, int dmin, int hmax, long seed, int mode){
		if(VERBOSE)System.out.println(n+" - "+capamax+" - "+dmin+" - "+hmax+" - "+seed+" - "+mode);
		Cumulative.Filter[][] filters = new Cumulative.Filter[][]{
				{Cumulative.Filter.TIME},
//				{Cumulative.Filter.TIME,Cumulative.Filter.NRJ},
				{Cumulative.Filter.SWEEP}
//				{Cumulative.Filter.SWEEP,Cumulative.Filter.NRJ},
//				{Cumulative.Filter.TIME,Cumulative.Filter.SWEEP,Cumulative.Filter.NRJ}
		};
		long ref = solve(n,capamax,dmin,hmax,seed,true,1, filters[0],mode);
		if(ref==-1)return;
		for(boolean g : new boolean[]{true,false})	// graph-based
			for(int b: new int[]{0})//,1,2})			// fast mode
				for(int f=0;f<filters.length;f++){
					long val = solve(n,capamax,dmin,hmax,seed,g,b,filters[f],mode);
					if(val!=ref)System.exit(0);
					assert ref == val :"filter "+f+" failed";
				}
	}

	public static long solve(int n, int capamax, int dmin, int hmax, long seed,
							 boolean graph, int nbFast, Cumulative.Filter[] f, int mode) {
		Solver solver = new Solver();
		int dmax = 5+dmin*2;
		IntVar[] s = VF.enumeratedArray("s",n,0,n*dmax,solver);
		IntVar[] d = VF.enumeratedArray("d",n,dmin,dmax,solver);
//		IntVar[] d = VF.enumeratedArray("d",n,dmin,dmin,solver);
		IntVar[] e = VF.enumeratedArray("e",n,0,n*dmax,solver);
		IntVar[] h = VF.enumeratedArray("h",n,0,hmax,solver);
//		IntVar[] h = VF.enumeratedArray("h",n,hmax,hmax,solver);
		IntVar capa = VF.enumerated("capa", 0, capamax, solver);
//		IntVar capa = VF.enumerated("capa", capamax, capamax, solver);
		Task[] t = new Task[n];
		IntVar last = VF.bounded("last",0,n*dmax,solver);
		for(int i=0;i<n;i++){
			t[i] = new Task(s[i],d[i],e[i]);
			solver.post(ICF.arithm(e[i],"<=",last));
		}
		Constraint c = new Constraint(solver);
		boolean b1 = nbFast>0;
		boolean b2 = nbFast>1;
		if(graph) {
			c.setPropagators(
					new PropGraphCumulative(s,d,e,h,capa,b1, f),
					new PropGraphCumulative(s,d,e,h,capa,b2, f)
			);
		}else{
			c.setPropagators(
					new PropFullCumulative(s,d,e,h,capa,b1, f),
					new PropFullCumulative(s,d,e,h,capa,b2, f)
			);
		}
		solver.post(c);
//		solver.set(ISF.force_InputOrder_InDomainMin(solver.retrieveIntVars()));
		solver.set(ISF.random(solver.retrieveIntVars(), seed));
		SMF.limitTime(solver,5000);
		switch (mode){
			case 0:	solver.findSolution();
				print(solver,last,nbFast,graph,f);
				if(solver.hasReachedLimit())return -1;
				return solver.getMeasures().getSolutionCount();
			case 1:	solver.findOptimalSolution(ResolutionPolicy.MINIMIZE,last);
				print(solver,last,nbFast,graph,f);
				if(solver.hasReachedLimit())return -1;
				return solver.getMeasures().getBestSolutionValue().longValue();
			case 2:	solver.findAllSolutions();
				print(solver,last,nbFast,graph,f);
				if(solver.hasReachedLimit())return -1;
				return solver.getMeasures().getSolutionCount();
			default:throw new UnsupportedOperationException();
		}
	}

	private static void print(Solver solver, IntVar obj, int nbFast, boolean graph, Cumulative.Filter[] f){
		if(VERBOSE){
			String st = "";
			for(Cumulative.Filter fi:f){
				st+=fi.name()+",";
			}
			long nbSol = solver.getMeasures().getSolutionCount();
			System.out.println((graph?"graph":"\t")
					+"\t"+(int)solver.getMeasures().getTimeCount()+" ms"
					+"\t"+(int)solver.getMeasures().getFailCount()+" fs"
					+"\t"+nbSol+" ss"
					+"\t"+(nbSol>0?obj.getValue()+" obj":"")
					+"\t"+st+" // "+nbFast
			);
		}
	}
}