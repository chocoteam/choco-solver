package org.chocosolver.solver.search.restart;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.nary.nogood.NogoodStoreFromSolutions;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * @author Jean-Guillaume Fages
 * @since 17/12/14
 * Created by IntelliJ IDEA.
 */
public class NoGoodOnSolutionTest {

	final static int N = 15;
	final static int Z = 175;
	final static int NB_SOLS = 5;
	final static int MAX_NB_SOLS = 10;

	public static Solver makeProblem(){
		int n = N;
		Random rd = new Random(0);
		int[][] costs = new int[n][n];
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				costs[i][j] = rd.nextInt(100);
			}
		}
		Solver s = new Solver();
		IntVar z = VF.bounded("z",Z,Z+10,s);
		IntVar[] vars = VF.enumeratedArray("x",n,0,n-1,s);
		s.post(ICF.tsp(vars,z,costs));
		s.set(ISF.random_value(vars));
		SMF.limitSolution(s,MAX_NB_SOLS);
		return s;
	}

	@Test(groups = "1s")
	public void testNormal() {
		// no restarts (ok)
		Solver s = makeProblem();
		s.findAllSolutions();
		System.out.println(s.getMeasures());
		Assert.assertTrue(s.getMeasures().getSolutionCount() == NB_SOLS);
	}

	@Test(groups = "1s")
	public void testRoS() {
		// restarts on solutions (infinite loop)
		Solver s = makeProblem();
		SMF.restartAfterEachSolution(s);
		s.findAllSolutions();
		System.out.println(s.getMeasures());
		Assert.assertTrue(s.getMeasures().getSolutionCount() == MAX_NB_SOLS);
	}

	@Test(groups = "1s")
	public void testRoSNG() {
		// restarts on solutions with no goods on solutions (ok)
		Solver s = makeProblem();
		NogoodStoreFromSolutions ng = new NogoodStoreFromSolutions(s.retrieveIntVars());
		s.post(ng);
		s.plugMonitor(ng);
		SMF.restartAfterEachSolution(s);
		s.findAllSolutions();
		System.out.println(s.getMeasures());
		Assert.assertTrue(s.getMeasures().getSolutionCount() == NB_SOLS);
	}

	@Test(groups = "1s")
	public void testA() {
		// restarts on solutions and on fails (at activity presolve only) (loop infinitely)
		Solver s = makeProblem();
		s.set(ISF.activity(s.retrieveIntVars(),0));
		s.findAllSolutions();
		System.out.println(s.getMeasures());
		Assert.assertTrue(s.getMeasures().getSolutionCount() == MAX_NB_SOLS);
	}

	@Test(groups = "1s")
	public void testANG() {
		// restarts on solutions and on fails with restarts on solutions (ok)
		Solver s = makeProblem();
		NogoodStoreFromSolutions ng = new NogoodStoreFromSolutions(s.retrieveIntVars());
		s.post(ng);
		s.plugMonitor(ng);
		s.set(ISF.activity(s.retrieveIntVars(),0));
		s.findAllSolutions();
		System.out.println(s.getMeasures());
		Assert.assertTrue(s.getMeasures().getSolutionCount() == NB_SOLS);
	}
}
