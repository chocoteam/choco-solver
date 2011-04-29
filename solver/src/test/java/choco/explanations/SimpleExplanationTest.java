package choco.explanations;

import choco.kernel.memory.IEnvironment;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 30 oct. 2010
 * Time: 17:09:26
 */
public class SimpleExplanationTest {

    static Solver s;
    static IEnvironment env;
    static IntVar[] vars;
    static Constraint[] lcstrs;


    public static void init() {
        s = new Solver();
        env = s.getEnvironment();

    }

    public static void setvars(boolean enumerated) {
        vars = new IntVar[3];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = enumerated ? VariableFactory.enumerated("x" + i, 1, vars.length, s)
                    : VariableFactory.bounded("x" + i, 1, vars.length + 1, s);
        }

    }

    public static void constraints() {

        lcstrs = new Constraint[3];

        lcstrs[0] = ConstraintFactory.lt(vars[0], vars[1], s);
        lcstrs[1] = ConstraintFactory.lt(vars[1], vars[2], s);
        lcstrs[2] = ConstraintFactory.neq(vars[0], vars[1], s);
    }

    private static void solve() {

        AbstractStrategy strategy = StrategyFactory.inputOrderInDomainMin(vars, env);

        s.post(lcstrs);
        s.set(strategy);

        s.findSolution();


        long sol = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sol, 1, "nb sol incorrect");

    }


    @Test(groups = "1s")
    public void test1() {
        init();
        setvars(true);
        constraints();
        solve();
    }


    @Test(groups = "1s")
    public void test2() {
        init();
        setvars(false);
        constraints();
        solve();
    }


}
