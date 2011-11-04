package sandbox;

import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.memory.IEnvironment;
import org.testng.Assert;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.AllDifferent;
import solver.constraints.propagators.Propagator;
import solver.requests.ConditionnalRequest;
import solver.requests.conditions.AbstractCondition;
import solver.requests.conditions.CompletlyInstantiated;
import solver.requests.conditions.CondAllDiffBC;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 3 nov. 2011
 */
public class TestProbaBAllDiff {

    // duplique depuis la classe de test ConditionnalRequestTest : voir avec Charles pour comprendre en details
    private static void castRequests(Constraint[] constraints, IEnvironment environment) {
        try {
            Method m_unlink = Propagator.class.getDeclaredMethod("unlinkVariables");
            Field f_requests = Propagator.class.getDeclaredField("requests");
            Field f_vars = Propagator.class.getDeclaredField("vars");

            m_unlink.setAccessible(true);
            f_requests.setAccessible(true);
            f_vars.setAccessible(true);

            for (Constraint cstr : constraints) {
                // cstr == allDiif sauf qu'on recupere aussi les propagators lies ˆ la clique de neqs :: c'est LE MAL
                Propagator[] propagators = cstr.propagators;
                for (Propagator prop : propagators) {
                    m_unlink.invoke(prop);
                    IntVar[] ivars = (IntVar[]) f_vars.get(prop);
                    AbstractCondition cond = new CondAllDiffBC(environment, ivars); // requetes conditionnees par la proba que alldiff BC soit consistant
                    ConditionnalRequest[] requests = new ConditionnalRequest[ivars.length];
                    for (int i = 0; i < ivars.length; i++) {
                        ivars[i].updatePropagationConditions(prop, i);
                        requests[i] = new ConditionnalRequest(prop, ivars[i], i, cond, environment);
                        prop.addRequest(requests[i]);
                        ivars[i].addRequest(requests[i]);
                        cond.linkRequest(requests[i]);
                    }
                    f_requests.set(prop, requests);
                }
            }

            m_unlink.setAccessible(false);
            f_requests.setAccessible(false);
            f_vars.setAccessible(false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

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

            Constraint[] cstrs = {new AllDifferent(x, solver, AllDifferent.Type.PROBABILISTIC)};

            castRequests(cstrs, solver.getEnvironment());

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
