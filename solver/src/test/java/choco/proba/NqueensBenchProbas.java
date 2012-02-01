package choco.proba;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.AllDifferent;
import solver.recorders.conditions.CondAllDiffBCProba;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 29/11/11
 */
public class NqueensBenchProbas extends AbstractBenchProbas {

    public NqueensBenchProbas(boolean mode, int n, AllDifferent.Type type, int frequency, boolean active,
                              CondAllDiffBCProba.Distribution dist, BufferedWriter out, int seed) throws IOException {
        super(new Solver(), mode, n, type, frequency, active, dist, out, seed);
    }

    @Override
    void buildProblem(int size) {
        this.vars = new IntVar[size];
        IntVar[] diag1 = new IntVar[size];
        IntVar[] diag2 = new IntVar[size];
        this.allVars = new IntVar[3*size];
        int k = 0;
        for (int i = 0; i < size; i++) {
            vars[i] = VariableFactory.enumerated("Q_" + i, 1, size, solver);
            diag1[i] = Views.offset(vars[i], i);
            diag2[i] = Views.offset(vars[i], -i);
            allVars[k++] = vars[i];
            allVars[k++] = diag1[i];
            allVars[k++] = diag2[i];
        }
        AllDifferent alldiff = new AllDifferent(vars, solver, type);
        AllDifferent alldiffdiag1 = new AllDifferent(diag1, solver, type);
        AllDifferent alldiffdiag2 = new AllDifferent(diag2, solver, type);
        this.cstrs = new Constraint[]{alldiff, alldiffdiag1, alldiffdiag2};
        this.allDiffs = new AllDifferent[]{alldiff, alldiffdiag1, alldiffdiag2};
        this.nbAllDiff = 3;
        this.allDiffVars = new IntVar[][]{this.vars,diag1,diag2};
    }

}
