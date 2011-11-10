package sandbox;

import choco.kernel.common.util.tools.MathUtils;
import org.testng.Assert;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.AllDifferent;
import solver.search.measure.IMeasures;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 3 nov. 2011
 */
public class TestProbaBAllDiff {

    public final void execute(Solver solver) {
        solver.findAllSolutions();
    }

    public void oneAllDiffTest(BufferedWriter out) throws IOException {
        Random random = new Random();
        Solver solver;
        Solver solverProba;
        IMeasures mes;


        for (int seed = 0; seed < 50; seed++) {
            random.setSeed(seed);
            int n = 2 + random.nextInt(7);
            out.write("inst"+n+"\t");

            solver = new Solver();
            IntVar[] x = VariableFactory.enumeratedArray("x", n, 1, n, solver);
            // une contrainte alldiff pour l'approche classique
            Constraint[] cstrs = {new AllDifferent(x, solver, AllDifferent.Type.BC)};
            solver.post(cstrs);
            solver.set(StrategyFactory.random(x, solver.getEnvironment(), seed));
            execute(solver);
            mes = solver.getMeasures();
            out.write(mes.getNodeCount()+"\t");
            out.write(mes.getBackTrackCount()+"\t");
            out.write(mes.getPropagationsCount()+"\t");
            out.write(mes.getTimeCount()+"\t");
            out.flush();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), MathUtils.factoriel(n));

            solverProba = new Solver();
            IntVar[] y = VariableFactory.enumeratedArray("y", n, 1, n, solverProba);
            // une contrainte alldiff pour la proba
            Constraint[] cstrsProba = {new AllDifferent(y, solverProba, AllDifferent.Type.PROBABILISTIC)};
            solverProba.post(cstrsProba);
            solverProba.set(StrategyFactory.random(y, solverProba.getEnvironment(), seed));
            execute(solverProba);
            mes = solverProba.getMeasures();
            out.write(mes.getNodeCount()+"\t");
            out.write(mes.getBackTrackCount()+"\t");
            out.write(mes.getPropagationsCount()+"\t");
            out.write(""+mes.getTimeCount());
            out.flush();
            Assert.assertEquals(solverProba.getMeasures().getSolutionCount(), MathUtils.factoriel(n));

            Assert.assertEquals(solverProba.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());

            out.newLine();
        }
        out.close();
    }

    public static void main(String[] args) throws IOException {
        String outDir = "/Users/chameau/Travail/2011-2012/Thse JDB-2/tests/OneAllDiff.csv";
        BufferedWriter out = new BufferedWriter(new FileWriter(outDir));
        out.write("instance"+"\t");
        out.write("search nodes"+"\t");
        out.write("search bcks"+"\t");
        out.write("nb propag"+"\t");
        out.write("solving time"+"\t");
        out.write("search nodes"+"\t");
        out.write("search bcks"+"\t");
        out.write("nb propag"+"\t");
        out.write("solving time");
        out.newLine();
        out.flush();
        TestProbaBAllDiff test = new TestProbaBAllDiff();
        test.oneAllDiffTest(out);
    }

}
