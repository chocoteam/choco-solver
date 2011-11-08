package sandbox;

import choco.kernel.common.util.tools.MathUtils;
import org.testng.Assert;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.AllDifferent;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 3 nov. 2011
 */
public class TestProbaBAllDiff {

    public final void execute(Solver solver) {
        solver.findAllSolutions();
        System.out.println(solver.getMeasures()+"\n");
    }

    public void oneAllDiffTest() {
        Random random = new Random();
        for (int seed = 0; seed < 50; seed++) {
            random.setSeed(seed);
            Solver solver = new Solver();
            int n = 2 + random.nextInt(7);

            IntVar[] x = VariableFactory.enumeratedArray("x", n, 1, n, solver);

            // une contrainte alldiff
            Constraint[] cstrs = {new AllDifferent(x, solver, AllDifferent.Type.PROBABILISTIC)};

            solver.post(cstrs);
            solver.set(StrategyFactory.random(x, solver.getEnvironment()));

            execute(solver);

            Assert.assertEquals(solver.getMeasures().getSolutionCount(), MathUtils.factoriel(n));
        }
    }

    public static void main(String[] args) {
        TestProbaBAllDiff test = new TestProbaBAllDiff();
        test.oneAllDiffTest();
    }

}
