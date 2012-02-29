package choco.proba;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.binary.GreaterOrEqualX_YC;
import solver.constraints.nary.AllDifferent;
import solver.constraints.unary.Relation;
import solver.recorders.conditions.CondAllDiffBCProba;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 06/12/11
 */
public class AllIntervalSeriesBenchProbas extends AbstractBenchProbas {

    public AllIntervalSeriesBenchProbas(int n, AllDifferent.Type type, int frequency, boolean active,
                                        CondAllDiffBCProba.Distribution dist, BufferedWriter out, int seed) throws IOException {
        super(new Solver(), n, type, frequency, active, dist, out, seed);
    }

    @Override
    void solveProcess() {
        this.solver.findAllSolutions();
    }

    @Override
    void buildProblem(int size) {
        this.vars = VariableFactory.enumeratedArray("v", size, 0, size - 1, solver);
        IntVar[] dist = new IntVar[size-1];
        this.allVars = new IntVar[vars.length + dist.length];

        this.cstrs = new Constraint[2*(size-1)+4];
        for (int i = 0, k = 0; i < size - 1; i++, k++) {
            dist[i] = Views.abs(Views.sum(vars[i + 1], Views.minus(vars[i])));
            this.cstrs[k++] = new Relation(dist[i], Relation.R.GT, 0, solver);
            this.cstrs[k] = new Relation(dist[i], Relation.R.LT, size, solver);
        }
        this.allVars[0] = vars[0];
        for (int i = 1, k = 1; i < vars.length - 1; i++, k++) {
            this.allVars[k++] = vars[i];
            this.allVars[k] = dist[i - 1];
        }
        AllDifferent a1 = new AllDifferent(vars, solver, type);
        AllDifferent a2 = new AllDifferent(dist, solver, type);
        Constraint o1 = new GreaterOrEqualX_YC(vars[1], vars[0], 1, solver);
        Constraint o2 = new GreaterOrEqualX_YC(dist[0], dist[size - 2], 1, solver);
        this.allDiffs = new AllDifferent[]{a1,a2};
        this.nbAllDiff = 2;
        this.allDiffVars = new IntVar[][]{this.vars,dist};
        this.cstrs[2*(size-1)] = a1;
        this.cstrs[2*(size-1)+1] = a2;
        this.cstrs[2*(size-1)+2] = o1;
        this.cstrs[2*(size-1)+3] = o2;
    }
}
