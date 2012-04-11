package choco.proba;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.Sum;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.nary.alldifferent.AllDifferentProba;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 20/12/11
 */
public class MagicSquareBenchProbas extends AbstractBenchProbas {

    public MagicSquareBenchProbas(int n, AllDifferent.Type type, int frequency, boolean active,
                                  AbstractBenchProbas.Distribution dist, BufferedWriter out, int seed) throws IOException {
        super(new Solver(), n, type, frequency, active, dist, out, seed);
    }

    @Override
    void buildProblem(int size, boolean proba) {
        int ms = size * (size * size + 1) / 2;

        IntVar[][] matrix = new IntVar[size][size];
        IntVar[][] invMatrix = new IntVar[size][size];
        this.vars = new IntVar[size * size];
        this.allVars = new IntVar[(size * size) + 2 * size];
//        this.allDiffVars = new IntVar[][]{vars};
//        this.nbAllDiff = 1;
//        this.allDiffs = new AllDifferent[this.nbAllDiff];
        this.cstrs = new Constraint[1 + (2 * size) + 5];

        int k = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++, k++) {
                matrix[i][j] = VariableFactory.enumerated("square" + i + "," + j, 1, size * size, solver);
                this.vars[k] = matrix[i][j];
                this.allVars[k] = matrix[i][j];
                invMatrix[j][i] = matrix[i][j];
            }
        }

        IntVar[] diag1 = new IntVar[size];
        IntVar[] diag2 = new IntVar[size];
        for (int i = 0; i < size; i++) {
            diag1[i] = matrix[i][i];
            this.allVars[k++] = diag1[i];
            diag2[i] = matrix[(size - 1) - i][i];
            this.allVars[k++] = diag2[i];
        }

        int c = 0;
        if (proba) {
            this.cstrs[c++] = new AllDifferentProba(vars, solver, type, this.count);
        } else {
            this.cstrs[c++] = new AllDifferent(vars, solver, type);
        }

        int[] coeffs = new int[size];
        Arrays.fill(coeffs, 1);
        for (int i = 0; i < size; i++) {
            this.cstrs[c++] = Sum.eq(matrix[i], coeffs, ms, solver);
            this.cstrs[c++] = Sum.eq(invMatrix[i], coeffs, ms, solver);
        }
        this.cstrs[c++] = Sum.eq(diag1, coeffs, ms, solver);
        this.cstrs[c++] = Sum.eq(diag2, coeffs, ms, solver);

        // Symetries breaking
        this.cstrs[c++] = ConstraintFactory.lt(matrix[0][size - 1], matrix[size - 1][0], solver);
        this.cstrs[c++] = ConstraintFactory.lt(matrix[0][0], matrix[size - 1][size - 1], solver);
        this.cstrs[c] = ConstraintFactory.lt(matrix[0][0], matrix[size - 1][0], solver);
    }

}
