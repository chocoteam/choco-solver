package org.chocosolver.solver.search;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Jean-Guillaume Fages
 * @since 22/04/15
 * Created by IntelliJ IDEA.
 */
public class ImpactTest {

	@Test(groups = "1s")
	public void testCostas() {
		Solver s1 = costasArray(7,false);
		Solver s2 = costasArray(7,true);

		s1.findAllSolutions();
		System.out.println(s1.getMeasures().getSolutionCount());

		s2.findAllSolutions();

		System.out.println(s2.getMeasures().getSolutionCount());
		Assert.assertEquals(s1.getMeasures().getSolutionCount(), s2.getMeasures().getSolutionCount());
	}

	private Solver costasArray(int n, boolean impact){
		Solver solver = new Solver("CostasArrays");
		IntVar[] vars, vectors;
		vars = VariableFactory.enumeratedArray("v", n, 0, n - 1, solver);
		vectors = new IntVar[(n*(n-1))/2];
		IntVar[][] diff = new IntVar[n][n];
		int idx = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i+1; j < n; j++) {
				IntVar k = VariableFactory.enumerated(StringUtils.randomName(), -n, n, solver);
				solver.post(ICF.arithm(k, "!=", 0));
				solver.post(IntConstraintFactory.sum(new IntVar[]{vars[i], k}, vars[j]));
				vectors[idx] = VariableFactory.offset(k, 2 * n * (j - i));
				diff[i][j] = k;
				idx++;
			}
		}
		solver.post(ICF.alldifferent(vars, "AC"));
		solver.post(ICF.alldifferent(vectors, "BC"));
		// symmetry-breaking
		solver.post(ICF.arithm(vars[0],"<",vars[n-1]));
		SMF.limitTime(solver, 20000);

		if(impact){
			solver.set(ISF.impact(vectors, 0));
		}else{
			solver.set(ISF.domOverWDeg(vectors, 0));
		}
		return solver;
	}
}
