package choco.proba;

import org.slf4j.LoggerFactory;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.propagators.nary.alldifferent.proba.CondAllDiffBCProba;

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
        BufferedWriter results = new BufferedWriter(new FileWriter(fileIt("results", ".csv")));
        TestProbaBAllDiff.oneAllDiffTest(5, results);
        /*TestProbaBAllDiff.hamiltonianCycleTest(200, results);
        TestProbaBAllDiff.magicSquareTest(4, results);
        TestProbaBAllDiff.nQueensTest(10, results);
        TestProbaBAllDiff.allIntervalSeriesTest(8, results);
        TestProbaBAllDiff.partitionTest(24, results);
        TestProbaBAllDiff.golombRulerTest(8, results);
        TestProbaBAllDiff.langfordTest(3,17, results);*/
        results.close();
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


    public static void oneAllDiffTest(int size, BufferedWriter results) throws IOException {
        String name = "OneAllDiff";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("OneAllDiff", ".csv")));
        OneAllDiffBenchProbas gac = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        OneAllDiffBenchProbas rangeBC = new OneAllDiffBenchProbas(size, AllDifferent.Type.RC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        OneAllDiffBenchProbas clique = new OneAllDiffBenchProbas(size, AllDifferent.Type.NEQS, 0, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        OneAllDiffBenchProbas uniformGAC = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, 0, true, CondAllDiffBCProba.Distribution.UNIFORM, out, -1);
        OneAllDiffBenchProbas diracGAC = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, 0, true, CondAllDiffBCProba.Distribution.DIRAC, out, -1);
        OneAllDiffBenchProbas uniformBC = new OneAllDiffBenchProbas(size, AllDifferent.Type.RC, 0, true, CondAllDiffBCProba.Distribution.UNIFORM, out, -1);
        OneAllDiffBenchProbas diracBC = new OneAllDiffBenchProbas(size, AllDifferent.Type.RC, 0, true, CondAllDiffBCProba.Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, rangeBC, clique, uniformGAC, diracGAC, uniformBC, diracBC};
        launchEval(name, 50, size, problems, out, results);
        out.close();
    }

    public static void nQueensTest(int size, BufferedWriter results) throws IOException {
        String name = "nQueens";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("nQueens", ".csv")));
        NqueensBenchProbas gac = new NqueensBenchProbas(size, AllDifferent.Type.AC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        NqueensBenchProbas rangeBC = new NqueensBenchProbas(size, AllDifferent.Type.RC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        NqueensBenchProbas clique = new NqueensBenchProbas(size, AllDifferent.Type.NEQS, 0, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        NqueensBenchProbas uniformGAC = new NqueensBenchProbas(size, AllDifferent.Type.AC, 0, true, CondAllDiffBCProba.Distribution.UNIFORM, out, -1);
        NqueensBenchProbas diracGAC = new NqueensBenchProbas(size, AllDifferent.Type.AC, 0, true, CondAllDiffBCProba.Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, rangeBC, clique, uniformGAC, diracGAC};
        launchEval(name, 10, size, problems, out, results);
        out.close();
    }

    public static void allIntervalSeriesTest(int size, BufferedWriter results) throws IOException {
        String name = "allIntervalSeries";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("allIntervalSeries", ".csv")));
        AllIntervalSeriesBenchProbas gac = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.AC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        AllIntervalSeriesBenchProbas rangeBC = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.RC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        AllIntervalSeriesBenchProbas clique = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.NEQS, 0, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        AllIntervalSeriesBenchProbas uniformGAC = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.AC, 0, true, CondAllDiffBCProba.Distribution.UNIFORM, out, -1);
        AllIntervalSeriesBenchProbas diracGAC = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.AC, 0, true, CondAllDiffBCProba.Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, rangeBC, clique, uniformGAC, diracGAC};
        launchEval(name, 10, size, problems, out, results);
        out.close();
    }

    public static void magicSquareTest(int size, BufferedWriter results) throws IOException {
        String name = "magicSquare";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("magicSquare", ".csv")));
        MagicSquareBenchProbas gac = new MagicSquareBenchProbas(size, AllDifferent.Type.AC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        MagicSquareBenchProbas rangeBC = new MagicSquareBenchProbas(size, AllDifferent.Type.RC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        MagicSquareBenchProbas clique = new MagicSquareBenchProbas(size, AllDifferent.Type.NEQS, 0, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        MagicSquareBenchProbas uniformGAC = new MagicSquareBenchProbas(size, AllDifferent.Type.AC, 0, true, CondAllDiffBCProba.Distribution.UNIFORM, out, -1);
        MagicSquareBenchProbas diracGAC = new MagicSquareBenchProbas(size, AllDifferent.Type.AC, 0, true, CondAllDiffBCProba.Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, rangeBC, clique, uniformGAC, diracGAC};
        launchEval(name, 10, size, problems, out, results);
        out.close();
    }

    public static void hamiltonianCycleTest(int size, BufferedWriter results) throws IOException {
        String name = "hamiltonianCycle";
        int neighbors = 3; //4
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("hamiltonianCycle", ".csv")));
        HamiltonianCycleBenchProbas.GenerateStrat strat = HamiltonianCycleBenchProbas.GenerateStrat.ARC;
        HamiltonianCycleBenchProbas gac = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.AC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1, neighbors, strat);
        HamiltonianCycleBenchProbas rangeBC = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.RC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1, neighbors, strat);
        HamiltonianCycleBenchProbas clique = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.NEQS, 0, false, CondAllDiffBCProba.Distribution.NONE, out, -1, neighbors, strat);
        HamiltonianCycleBenchProbas uniformGAC = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.AC, 0, true, CondAllDiffBCProba.Distribution.UNIFORM, out, -1, neighbors, strat);
        HamiltonianCycleBenchProbas diracGAC = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.AC, 0, true, CondAllDiffBCProba.Distribution.DIRAC, out, -1, neighbors, strat);
        // end test
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, rangeBC, clique, uniformGAC, diracGAC};
        launchEval(name, 50, size, problems, out, results);
        out.close();
    }

    public static void partitionTest(int size, BufferedWriter results) throws IOException {
        String name = "partition";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("partition", ".csv")));
        PartitionBenchProbas gac = new PartitionBenchProbas(size, AllDifferent.Type.AC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        PartitionBenchProbas rangeBC = new PartitionBenchProbas(size, AllDifferent.Type.RC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        PartitionBenchProbas clique = new PartitionBenchProbas(size, AllDifferent.Type.NEQS, 0, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        PartitionBenchProbas uniformGAC = new PartitionBenchProbas(size, AllDifferent.Type.AC, 0, true, CondAllDiffBCProba.Distribution.UNIFORM, out, -1);
        PartitionBenchProbas diracGAC = new PartitionBenchProbas(size, AllDifferent.Type.AC, 0, true, CondAllDiffBCProba.Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, rangeBC, clique, uniformGAC, diracGAC};
        launchEval(name, 5, size, problems, out, results);
        out.close();
    }

    public static void golombRulerTest(int size, BufferedWriter results) throws IOException {
        String name = "golombRuler";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("golombRuler", ".csv")));
        GolombRulerBenchProbas bounded = new GolombRulerBenchProbas(size, AllDifferent.Type.BC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        GolombRulerBenchProbas clique = new GolombRulerBenchProbas(size, AllDifferent.Type.NEQS, 0, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        GolombRulerBenchProbas uniformBC = new GolombRulerBenchProbas(size, AllDifferent.Type.BC, 0, true, CondAllDiffBCProba.Distribution.UNIFORM, out, -1);
        GolombRulerBenchProbas diracBC = new GolombRulerBenchProbas(size, AllDifferent.Type.BC, 0, true, CondAllDiffBCProba.Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{bounded, clique, uniformBC, diracBC};
        launchEval(name, 5, size, problems, out, results);
        out.close();
    }

    public static void langfordTest(int k, int n, BufferedWriter results) throws IOException {
        String name = "langford";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("langford", ".csv")));
        LangfordBenchProbas gac = new LangfordBenchProbas(k, n, AllDifferent.Type.AC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        LangfordBenchProbas rangeBC = new LangfordBenchProbas(k, n, AllDifferent.Type.RC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        LangfordBenchProbas bounded = new LangfordBenchProbas(k, n, AllDifferent.Type.BC, -1, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        LangfordBenchProbas clique = new LangfordBenchProbas(k, n, AllDifferent.Type.NEQS, 0, false, CondAllDiffBCProba.Distribution.NONE, out, -1);
        LangfordBenchProbas uniformGAC = new LangfordBenchProbas(k, n, AllDifferent.Type.AC, 0, true, CondAllDiffBCProba.Distribution.UNIFORM, out, -1);
        LangfordBenchProbas diracGAC = new LangfordBenchProbas(k, n, AllDifferent.Type.AC, 0, true, CondAllDiffBCProba.Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, rangeBC, bounded, clique, uniformGAC, diracGAC};
        launchEval(name, 5, k * n, problems, out, results);
        out.close();
    }


    private static void launchEval(String name, int step, int size, AbstractBenchProbas[] problems,
                                   BufferedWriter out, BufferedWriter results) throws IOException {

        out.write("\t");
        for (AbstractBenchProbas pb : problems) {
            String type = pb.toString();
            String solutions = type + "-sols";
            String nodes = type + "-nodes";
            String bcks = type + "-bcks";
            String heavyPropag = type + "-Hprop";
            String propag = type + "-prop";
            String time = type + "-time";
            String all = solutions + "\t" + nodes + "\t" + bcks + "\t" + heavyPropag + "\t" + propag + "\t" + time + "\t";
            out.write(all + "-" + "\t");
        }
        out.newLine();
        for (int seed = 0; seed < step; seed++) {
            String inst = "inst" + seed + "-" + size + "\t";
            out.write(inst);
            for (AbstractBenchProbas pb : problems) {
                pb.restartProblem(size, seed);
                pb.execute();
                pb.recordResults();
            }
            out.newLine();
        }

        results.write("\t");
        for (AbstractBenchProbas pb : problems) {
            String type = pb.toString();
            String solutions = type + "-sols";
            String nodes = type + "-nodes";
            String bcks = type + "-bcks";
            String heavyPropag = type + "-Hprop";
            String propag = type + "-prop";
            String time = type + "-time";
            String all = solutions + "\t" + nodes + "\t" + bcks + "\t" + heavyPropag + "\t" + propag + "\t" + time + "\t";
            results.write(all + "-" + "\t");
        }
        results.newLine();
        String inst = name + "-" + size + "\t";
        results.write(inst);
        for (AbstractBenchProbas pb : problems) {
            pb.recordAverage(results);
        }
        results.flush();
        results.newLine();
    }


}
