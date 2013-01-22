package choco.proba;

import solver.Solver;
import solver.constraints.Arithmetic;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.nary.Sum;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 06/12/11
 */
public class AllIntervalSeriesBenchProbas extends AbstractBenchProbas {

    public AllIntervalSeriesBenchProbas(int n, AllDifferent.Type type, int nbTests, int seed, boolean isProba) throws IOException {
        super(new Solver(), n, type, nbTests, seed, isProba);
    }

    /*@Override
    void solveProcess() {
        this.solver.findAllSolutions();
    }*/

    @Override
    void buildProblem(int size, boolean proba) {
        this.vars = VariableFactory.enumeratedArray("v", size, 0, size - 1, solver);
        IntVar[] dist = new IntVar[size - 1];
        this.allVars = new IntVar[vars.length + dist.length];

        this.cstrs = new Constraint[3 * (size - 1) + 4];
        for (int i = 0, k = 0; i < size - 1; i++, k++) {
            IntVar tmp = Sum.var(vars[i + 1], Views.minus(vars[i]));
            dist[i] = VariableFactory.enumerated("dist[" + i + "]", -size, size, solver);//Views.abs(tmp);
            this.cstrs[k++] = IntConstraintFactory.absolute(dist[i], tmp);
            this.cstrs[k++] = new Arithmetic(dist[i], ">", 0, solver);
            this.cstrs[k] = new Arithmetic(dist[i], "<", size, solver);
        }
        this.allVars[0] = vars[0];
        for (int i = 1, k = 1; i < vars.length - 1; i++, k++) {
            this.allVars[k++] = vars[i];
            this.allVars[k] = dist[i - 1];
        }
        this.cstrs[3 * (size - 1)] = new AllDifferent(vars, solver, type);
        this.cstrs[3 * (size - 1) + 1] = new AllDifferent(dist, solver, type);
        Constraint o1 = new Arithmetic(vars[1], ">", vars[0], solver);
        Constraint o2 = new Arithmetic(dist[0], ">", dist[size - 2], solver);

        this.cstrs[3 * (size - 1) + 2] = o1;
        this.cstrs[3 * (size - 1) + 3] = o2;
    }
}
