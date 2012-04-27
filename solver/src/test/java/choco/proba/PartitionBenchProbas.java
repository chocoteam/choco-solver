package choco.proba;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.Sum;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.unary.Relation;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static solver.constraints.ConstraintFactory.lt;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 27/02/12
 */
public class PartitionBenchProbas extends AbstractBenchProbas {

    public PartitionBenchProbas(int n, AllDifferent.Type type, int nbTests, int seed, boolean isProba) throws IOException {
        super(new Solver(), n, type, nbTests, seed, isProba);
    }

    @Override
    void configSearchStrategy() {
        solver.set(StrategyFactory.minDomMinVal(vars, solver.getEnvironment()));
    }

    @Override
    void buildProblem(int size, boolean proba) {
        IntVar[] x, y;
        x = VariableFactory.enumeratedArray("x", size, 1, 2 * size, solver);
        y = VariableFactory.enumeratedArray("y", size, 1, 2 * size, solver);
        Collection<IntVar> allVars = new ArrayList<IntVar>();
        for (int i = 0; i < x.length + y.length; i++) {
            if (i < x.length) {
                allVars.add(x[i]);
            } else {
                allVars.add(y[i - x.length]);
            }
        }

        Collection<Constraint> allCstrs = new ArrayList<Constraint>();
        // break symmetries
        for (int i = 0; i < size - 1; i++) {
            allCstrs.add(lt(x[i], x[i + 1], solver));
            allCstrs.add(lt(y[i], y[i + 1], solver));
        }
        allCstrs.add(lt(x[0], y[0], solver));

        IntVar[] xy = new IntVar[2 * size];
        for (int i = size - 1; i >= 0; i--) {
            xy[i] = x[i];
            xy[size + i] = y[i];
        }

        int[] coeffs = new int[2 * size];
        for (int i = size - 1; i >= 0; i--) {
            coeffs[i] = 1;
            coeffs[size + i] = -1;
        }
        allCstrs.add(Sum.eq(xy, coeffs, 0, solver));

        IntVar[] sxy, sx, sy;
        sxy = new IntVar[2 * size];
        sx = new IntVar[size];
        sy = new IntVar[size];
        for (int i = size - 1; i >= 0; i--) {
            sx[i] = Views.sqr(x[i]);
            allVars.add(sx[i]);
            sxy[i] = sx[i];
            sy[i] = Views.sqr(y[i]);
            allVars.add(sy[i]);
            sxy[size + i] = sy[i];
            allCstrs.add(new Relation(sx[i], Relation.R.GQ, 1, solver));
            allCstrs.add(new Relation(sy[i], Relation.R.GQ, 1, solver));
            allCstrs.add(new Relation(sx[i], Relation.R.LQ, 4 * size * size, solver));
            allCstrs.add(new Relation(sy[i], Relation.R.LQ, 4 * size * size, solver));
        }
        allCstrs.add(Sum.eq(sxy, coeffs, 0, solver));

        coeffs = new int[size];
        Arrays.fill(coeffs, 1);
        allCstrs.add(Sum.eq(x, coeffs, 2 * size * (2 * size + 1) / 4, solver));
        allCstrs.add(Sum.eq(y, coeffs, 2 * size * (2 * size + 1) / 4, solver));
        allCstrs.add(Sum.eq(sx, coeffs, 2 * size * (2 * size + 1) * (4 * size + 1) / 12, solver));
        allCstrs.add(Sum.eq(sy, coeffs, 2 * size * (2 * size + 1) * (4 * size + 1) / 12, solver));

        allCstrs.add(new AllDifferent(xy, solver, type));

        this.cstrs = allCstrs.toArray(new Constraint[allCstrs.size()]);
        this.allVars = allVars.toArray(new IntVar[allVars.size()]);
        this.vars = xy;
    }
}
