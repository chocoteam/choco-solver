package choco.proba;

import org.slf4j.LoggerFactory;
import solver.constraints.nary.AllDifferent;
import solver.recorders.conditions.CondAllDiffBCProba;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 3 nov. 2011
 */
public class TestProbaBAllDiff {

    public static void main(String[] args) throws IOException {
        TestProbaBAllDiff.oneAllDiffTest(10);
        TestProbaBAllDiff.hamiltonianCycleTest(400);
        TestProbaBAllDiff.magicSquareTest(4);
        TestProbaBAllDiff.nQueensTest(11);
        TestProbaBAllDiff.allIntervalSeriesTest(10);
    }

    public static String fileIt(String name, String ext) {
        String path = null;
        try {
            path = File.createTempFile(name, ext).toString();
            LoggerFactory.getLogger("solver").error("Tmp file : " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }


    public static void oneAllDiffTest(int size) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("OneAllDiff", ".csv")));
        OneAllDiffBenchProbas gac = new OneAllDiffBenchProbas(true, size, AllDifferent.Type.AC, -1, true, CondAllDiffBCProba.Distribution.NONE, out, -1);
        OneAllDiffBenchProbas rangeBC = new OneAllDiffBenchProbas(true, size, AllDifferent.Type.BC, -1, true, CondAllDiffBCProba.Distribution.NONE, out, -1);
        OneAllDiffBenchProbas uniform = new OneAllDiffBenchProbas(true, size, AllDifferent.Type.PROBABILISTIC, 0, true, CondAllDiffBCProba.Distribution.UNIFORM, out, -1);
        OneAllDiffBenchProbas clique = new OneAllDiffBenchProbas(true, size, AllDifferent.Type.CLIQUE_IN_ONE, 0, true, CondAllDiffBCProba.Distribution.NONE, out, -1);
        OneAllDiffBenchProbas dirac = new OneAllDiffBenchProbas(true, size, AllDifferent.Type.PROBABILISTIC, 0, true, CondAllDiffBCProba.Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,rangeBC, uniform, dirac, clique};
        launchEval(10, size, problems, out);
        out.close();
    }

    public static void nQueensTest(int size) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("nQueens", ".csv")));
        NqueensBenchProbas gac = new NqueensBenchProbas(true, size, AllDifferent.Type.AC, -1, true, CondAllDiffBCProba.Distribution.NONE, out, -1);
        NqueensBenchProbas rangeBC = new NqueensBenchProbas(true, size, AllDifferent.Type.RANGE, -1, true, CondAllDiffBCProba.Distribution.NONE, out, -1);
        NqueensBenchProbas uniform = new NqueensBenchProbas(true, size, AllDifferent.Type.PROBABILISTIC, 0, true, CondAllDiffBCProba.Distribution.UNIFORM, out, -1);
        NqueensBenchProbas clique = new NqueensBenchProbas(true, size, AllDifferent.Type.CLIQUE_IN_ONE, 0, true, CondAllDiffBCProba.Distribution.NONE, out, -1);
        NqueensBenchProbas dirac = new NqueensBenchProbas(true, size, AllDifferent.Type.PROBABILISTIC, 0, true, CondAllDiffBCProba.Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,rangeBC, uniform, dirac, clique};
        launchEval(10, size, problems, out);
        out.close();
    }

    public static void allIntervalSeriesTest(int size) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("allIntervalSeries", ".csv")));
        AllIntervalSeriesBenchProbas gac = new AllIntervalSeriesBenchProbas(true, size, AllDifferent.Type.AC, -1, true, CondAllDiffBCProba.Distribution.NONE, out, -1);
        AllIntervalSeriesBenchProbas rangeBC = new AllIntervalSeriesBenchProbas(true, size, AllDifferent.Type.RANGE, -1, true, CondAllDiffBCProba.Distribution.NONE, out, -1);
        AllIntervalSeriesBenchProbas uniform = new AllIntervalSeriesBenchProbas(true, size, AllDifferent.Type.PROBABILISTIC, 0, true, CondAllDiffBCProba.Distribution.UNIFORM, out, -1);
        AllIntervalSeriesBenchProbas clique = new AllIntervalSeriesBenchProbas(true, size, AllDifferent.Type.CLIQUE_IN_ONE, 0, true, CondAllDiffBCProba.Distribution.NONE, out, -1);
        AllIntervalSeriesBenchProbas dirac = new AllIntervalSeriesBenchProbas(true, size, AllDifferent.Type.PROBABILISTIC, 0, true, CondAllDiffBCProba.Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,rangeBC, uniform, dirac, clique};
        launchEval(10, size, problems, out);
        out.close();
    }

    public static void magicSquareTest(int size) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("magicSquare", ".csv")));
        MagicSquareBenchProbas gac = new MagicSquareBenchProbas(false, size, AllDifferent.Type.AC, -1, true, CondAllDiffBCProba.Distribution.NONE, out, -1);
        MagicSquareBenchProbas rangeBC = new MagicSquareBenchProbas(false, size, AllDifferent.Type.RANGE, -1, true, CondAllDiffBCProba.Distribution.NONE, out, -1);
        MagicSquareBenchProbas uniform = new MagicSquareBenchProbas(false, size, AllDifferent.Type.PROBABILISTIC, 0, true, CondAllDiffBCProba.Distribution.UNIFORM, out, -1);
        MagicSquareBenchProbas clique = new MagicSquareBenchProbas(false, size, AllDifferent.Type.CLIQUE_IN_ONE, 0, true, CondAllDiffBCProba.Distribution.NONE, out, -1);
        MagicSquareBenchProbas dirac = new MagicSquareBenchProbas(false, size, AllDifferent.Type.PROBABILISTIC, 0, true, CondAllDiffBCProba.Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,rangeBC, uniform, dirac, clique};
        launchEval(10, size, problems, out);
        out.close();
    }

    public static void hamiltonianCycleTest(int size) throws IOException {
        int neighbors = 3; //4
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("hamiltonianCycle", ".csv")));
        HamiltonianCycleBenchProbas.GenerateStrat strat = HamiltonianCycleBenchProbas.GenerateStrat.ARC;
        HamiltonianCycleBenchProbas gac = new HamiltonianCycleBenchProbas(false, size, AllDifferent.Type.AC, -1, true, CondAllDiffBCProba.Distribution.NONE, out, -1, neighbors, strat);
        HamiltonianCycleBenchProbas rangeBC = new HamiltonianCycleBenchProbas(false, size, AllDifferent.Type.RANGE, -1, true, CondAllDiffBCProba.Distribution.NONE, out, -1, neighbors, strat);
        HamiltonianCycleBenchProbas uniform = new HamiltonianCycleBenchProbas(false, size, AllDifferent.Type.PROBABILISTIC, 0, true, CondAllDiffBCProba.Distribution.UNIFORM, out, -1, neighbors, strat);
        HamiltonianCycleBenchProbas clique = new HamiltonianCycleBenchProbas(false, size, AllDifferent.Type.CLIQUE_IN_ONE, 0, true, CondAllDiffBCProba.Distribution.NONE, out, -1, neighbors, strat);
        HamiltonianCycleBenchProbas dirac = new HamiltonianCycleBenchProbas(false, size, AllDifferent.Type.PROBABILISTIC, 0, true, CondAllDiffBCProba.Distribution.DIRAC, out, -1, neighbors, strat);
        // end test
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,rangeBC, uniform, dirac, clique};
        launchEval(50, size, problems, out);
        out.close();
    }

    private static void launchEval(int step, int size, AbstractBenchProbas[] problems, BufferedWriter out) throws IOException {
        //Random random = new Random();
        for (int seed = 0; seed < step; seed++) {
            //int seed = 198; {
            //random.setSeed(seed);
            //System.out.println(seed);
            String inst = "inst" + seed + "-" + size + "\t";
            out.write(inst);
            for (AbstractBenchProbas pb : problems) {
                pb.restartProblem(size, seed);
                pb.execute();
                pb.recordResults();
            }
            out.newLine();
        }
        String inst = "average" + "-" + size + "\t";
        out.write(inst);
        for (AbstractBenchProbas pb : problems) {
            pb.recordAverage();
        }
    }


}
