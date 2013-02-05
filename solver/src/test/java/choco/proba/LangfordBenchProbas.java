package choco.proba;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 29/02/12
 */
public class LangfordBenchProbas extends AbstractBenchProbas {

    IntVar[] position;
    int k;
    int n;

    public LangfordBenchProbas(int k, int n, AllDifferent.Type type, int nbTests, int seed, boolean isProba) throws IOException {
        super(new Solver(), k * n, type, nbTests, seed, isProba);
        this.k = k;
        this.n = n;
    }

    @Override
    void configSearchStrategy() {
        solver.set(IntStrategyFactory.inputOrderMinVal(position, solver.getEnvironment()));
    }

    @Override
    void buildProblem(int size, boolean proba) {
        Collection<Constraint> allCstrs = new ArrayList<Constraint>();
        position = VariableFactory.enumeratedArray("p", n * k, 0, k * n - 1, solver);
        this.vars = this.allVars = position;
        for (int i = 0; i < k - 1; i++) {
            for (int j = 0; j < n; j++) {
                allCstrs.add(IntConstraintFactory.arithm(position[j + (i + 1) * n], "=", position[j + i * n], "+", j + 2));
            }
        }
        allCstrs.add(IntConstraintFactory.arithm(position[0], "<", position[n * k - 1]));
        allCstrs.add(new AllDifferent(position, solver, type));

        this.cstrs = allCstrs.toArray(new Constraint[allCstrs.size()]);
    }
}
