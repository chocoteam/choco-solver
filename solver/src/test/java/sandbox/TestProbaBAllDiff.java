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

    public final void execute(Solver solver, int size, int seed, AllDifferent.Type type) {
        IntVar[] x = VariableFactory.enumeratedArray("x", size, 1, size, solver);
            Constraint[] cstrs = {new AllDifferent(x, solver, type)};
            solver.post(cstrs);
            solver.set(StrategyFactory.random(x, solver.getEnvironment(), seed));
        //SearchMonitorFactory.log(solver, true, true);
        solver.findAllSolutions();
    }

    public void oneAllDiffTest(BufferedWriter out) throws IOException {
        Random random = new Random();
        Solver solver;
        Solver solverProba;
        IMeasures mes;


        for (int seed = 0; seed < 1000; seed++){
            //int seed = 1;{
            random.setSeed(seed);
            int n = 2 + random.nextInt(7);
            //int n = 4;
            out.write("inst"+n+"\t");

            System.out.printf("%d vs. %d\n", seed, n);

            String s = "";
            /*solver = new Solver();
            execute(solver,n,seed,AllDifferent.Type.BC);
            mes = solver.getMeasures();
            s = mes.getNodeCount()+"\t";
            s += mes.getBackTrackCount()+"\t";
            s += mes.getPropagationsCount()+"\t";
            s += mes.getTimeCount()+"|\t";
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), MathUtils.factoriel(n));
            */
            solverProba = new Solver();
            execute(solverProba,n,seed,AllDifferent.Type.PROBABILISTIC);
            mes = solverProba.getMeasures();
            s += mes.getNodeCount()+"\t";
            s += mes.getBackTrackCount()+"\t";
            s += mes.getPropagationsCount()+"\t";
            s += ""+mes.getTimeCount();
            Assert.assertEquals(solverProba.getMeasures().getSolutionCount(), MathUtils.factoriel(n));

            //Assert.assertEquals(solverProba.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
            System.out.println(s+"\n");
            out.write(s);
            out.flush();
            out.newLine();
        }
        out.close();
    }

    public static void main(String[] args) throws IOException {
        String outDir = "/Users/cprudhom/Downloads/OneAllDiff.csv";
        BufferedWriter out = new BufferedWriter(new FileWriter(outDir));
        String s = "search nodes"+"\t"+"search bcks"+"\t"+"nb propag"+"\t"+"solving time";
        out.write("instance"+"\t"+s+"\t"+s);
        System.out.println("instance"+"\t"+s+"|\t"+s);
        out.newLine();
        out.flush();
        TestProbaBAllDiff test = new TestProbaBAllDiff();
        test.oneAllDiffTest(out);
    }

}
