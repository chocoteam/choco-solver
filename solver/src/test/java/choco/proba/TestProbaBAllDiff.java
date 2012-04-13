package choco.proba;

import org.slf4j.LoggerFactory;
import solver.constraints.nary.alldifferent.AllDifferent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static choco.proba.AbstractBenchProbas.sep;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 3 nov. 2011
 */
public class TestProbaBAllDiff {

    public static void main(String[] args) throws IOException {
        BufferedWriter results = new BufferedWriter(new FileWriter(fileIt("results", ".csv")));
//        TestProbaBAllDiff.debug(results);
//        TestProbaBAllDiff.oneAllDiffTest(15, results);
        /*TestProbaBAllDiff.hamiltonianCycleTest(200, results);
        TestProbaBAllDiff.magicSquareTest(5, results);
        TestProbaBAllDiff.nQueensTest(12, results);
        TestProbaBAllDiff.allIntervalSeriesTest(10, results);
        TestProbaBAllDiff.partitionTest(24, results);
        TestProbaBAllDiff.golombRulerTest(9, results);*/
        TestProbaBAllDiff.langfordTest(3,17, results);
        results.close();
    }

    public static String fileIt(String name, String ext) {
        String path = null;
        try {
//            File dir = new File("/private/var/folders/vh/cfkwx09174l9vr5bfjktdxp80000gp/T/probaResults");
//            path = File.createTempFile(name, ext, dir).toString();
            path = File.createTempFile(name, ext).toString();
            LoggerFactory.getLogger("solver").error("Tmp file : " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    public static void debug(BufferedWriter results) throws IOException {
        int size = 8;
        String name = "debug";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("debug", ".csv")));
        OneAllDiffBenchProbas gac = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, -1, false, out, -1, false);
        //OneAllDiffBenchProbas uniformGAC = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, 0, true, AbstractBenchProbas.Distribution.UNIFORM, out, -1);
        OneAllDiffBenchProbas uniformRC = new OneAllDiffBenchProbas(size, AllDifferent.Type.RC, 0, true, out, -1, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{uniformRC,gac/*, uniformGAC*/};
        //AbstractBenchProbas[] problems = new AbstractBenchProbas[]{uniformGAC};
        launchEval(name, 10, size, problems, out, results);
        out.close();
    }


    public static void oneAllDiffTest(int size, BufferedWriter results) throws IOException {
        String name = "OneAllDiff";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("OneAllDiff", ".csv")));
        OneAllDiffBenchProbas gac = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, -1, false, out, -1, false);
        OneAllDiffBenchProbas rangeBC = new OneAllDiffBenchProbas(size, AllDifferent.Type.RC, -1, false, out, -1, false);
        OneAllDiffBenchProbas clique = new OneAllDiffBenchProbas(size, AllDifferent.Type.NEQS, 0, false, out, -1, false);
        OneAllDiffBenchProbas uniformGAC = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, 0, true, out, -1, true);
        OneAllDiffBenchProbas uniformRC = new OneAllDiffBenchProbas(size, AllDifferent.Type.RC, 0, true, out, -1, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, rangeBC, clique, uniformGAC, uniformRC};
        launchEval(name, 25, size, problems, out, results);
        out.close();
    }

    public static void nQueensTest(int size, BufferedWriter results) throws IOException {
        String name = "nQueens";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("nQueens", ".csv")));
        NqueensBenchProbas gac = new NqueensBenchProbas(size, AllDifferent.Type.AC, -1, false, out, -1, false);
        NqueensBenchProbas rangeBC = new NqueensBenchProbas(size, AllDifferent.Type.RC, -1, false, out, -1, false);
        NqueensBenchProbas clique = new NqueensBenchProbas(size, AllDifferent.Type.NEQS, 0, false, out, -1, false);
        NqueensBenchProbas uniformGAC = new NqueensBenchProbas(size, AllDifferent.Type.AC, 0, true, out, -1, true);
        NqueensBenchProbas uniformRC = new NqueensBenchProbas(size, AllDifferent.Type.RC, 0, true, out, -1, true);
        //NqueensBenchProbas diracGAC = new NqueensBenchProbas(size, AllDifferent.Type.AC, 0, true, Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, rangeBC, clique, uniformGAC, uniformRC};
        launchEval(name, 5, size, problems, out, results);
        out.close();
    }

    public static void allIntervalSeriesTest(int size, BufferedWriter results) throws IOException {
        String name = "allIntervalSeries";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("allIntervalSeries", ".csv")));
        AllIntervalSeriesBenchProbas gac = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.AC, -1, false, out, -1, false);
        AllIntervalSeriesBenchProbas rangeBC = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.RC, -1, false, out, -1, false);
        AllIntervalSeriesBenchProbas clique = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.NEQS, 0, false, out, -1, false);
        AllIntervalSeriesBenchProbas uniformGAC = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.AC, 0, true, out, -1, true);
        AllIntervalSeriesBenchProbas uniformRC = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.RC, 0, true, out, -1, true);
        //AllIntervalSeriesBenchProbas diracGAC = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.AC, 0, true, Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, rangeBC, clique, uniformGAC, uniformRC};
        launchEval(name, 5, size, problems, out, results);
        out.close();
    }

    public static void magicSquareTest(int size, BufferedWriter results) throws IOException {
        String name = "magicSquare";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("magicSquare", ".csv")));
        MagicSquareBenchProbas gac = new MagicSquareBenchProbas(size, AllDifferent.Type.AC, -1, false, out, -1, false);
        MagicSquareBenchProbas rangeBC = new MagicSquareBenchProbas(size, AllDifferent.Type.RC, -1, false, out, -1, false);
        MagicSquareBenchProbas clique = new MagicSquareBenchProbas(size, AllDifferent.Type.NEQS, 0, false, out, -1, false);
        MagicSquareBenchProbas uniformGAC = new MagicSquareBenchProbas(size, AllDifferent.Type.AC, 0, true, out, -1, true);
        MagicSquareBenchProbas uniformRC = new MagicSquareBenchProbas(size, AllDifferent.Type.RC, 0, true, out, -1, true);
        //MagicSquareBenchProbas diracGAC = new MagicSquareBenchProbas(size, AllDifferent.Type.AC, 0, true, Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, rangeBC, clique, uniformGAC, uniformRC};
        launchEval(name, 5, size, problems, out, results);
        out.close();
    }

    public static void hamiltonianCycleTest(int size, BufferedWriter results) throws IOException {
        String name = "hamiltonianCycle";
        for (int i = 30; i < 31; i = i + 10) {
            HamiltonianCycleBenchProbas.GenerateStrat strat = HamiltonianCycleBenchProbas.GenerateStrat.ARC;
            int neighbors = i;
            results = new BufferedWriter(new FileWriter(fileIt("ham-" + size + "-" + neighbors + "-" + HamiltonianCycleBenchProbas.GenerateStrat.ARC, ".csv")));
            BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("hamiltonianCycle", ".csv")));
            HamiltonianCycleBenchProbas gac = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.AC, -1, false, out, -1, neighbors, strat, false);
            //HamiltonianCycleBenchProbas bounded = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.BC, -1, false, Distribution.NONE, out, -1, neighbors, strat);
            HamiltonianCycleBenchProbas rangeBC = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.RC, -1, false, out, -1, neighbors, strat, false);
            HamiltonianCycleBenchProbas clique = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.NEQS, 0, false, out, -1, neighbors, strat, false);
            HamiltonianCycleBenchProbas uniformGAC = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.AC, 0, true, out, -1, neighbors, strat, true);
            HamiltonianCycleBenchProbas uniformRC = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.RC, 0, true, out, -1, neighbors, strat, true);
            //HamiltonianCycleBenchProbas diracGAC = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.AC, 0, true, AbstractBenchProbas.Distribution.DIRAC, out, -1, neighbors, strat);
            // end test
            AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, rangeBC, clique, uniformGAC, uniformRC};
            launchEval(name, 5, size, problems, out, results);
            out.close();
        }
    }

    public static void partitionTest(int size, BufferedWriter results) throws IOException {
        String name = "partition";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("partition", ".csv")));
        PartitionBenchProbas gac = new PartitionBenchProbas(size, AllDifferent.Type.AC, -1, false, out, -1, false);
        PartitionBenchProbas rangeBC = new PartitionBenchProbas(size, AllDifferent.Type.RC, -1, false, out, -1, false);
        PartitionBenchProbas clique = new PartitionBenchProbas(size, AllDifferent.Type.NEQS, 0, false, out, -1, false);
        PartitionBenchProbas uniformGAC = new PartitionBenchProbas(size, AllDifferent.Type.AC, 0, true, out, -1, true);
        PartitionBenchProbas uniformRC = new PartitionBenchProbas(size, AllDifferent.Type.RC, 0, true, out, -1, true);
        //PartitionBenchProbas diracGAC = new PartitionBenchProbas(size, AllDifferent.Type.AC, 0, true, Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, rangeBC, clique, uniformGAC, uniformRC};
        launchEval(name, 5, size, problems, out, results);
        out.close();
    }

    public static void golombRulerTest(int size, BufferedWriter results) throws IOException {
        String name = "golombRuler";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("golombRuler", ".csv")));
        GolombRulerBenchProbas bounded = new GolombRulerBenchProbas(size, AllDifferent.Type.BC, -1, false, out, -1, false);
        GolombRulerBenchProbas clique = new GolombRulerBenchProbas(size, AllDifferent.Type.NEQS, 0, false, out, -1, false);
        GolombRulerBenchProbas uniformBC = new GolombRulerBenchProbas(size, AllDifferent.Type.BC, 0, true, out, -1, true);
        //GolombRulerBenchProbas diracBC = new GolombRulerBenchProbas(size, AllDifferent.Type.BC, 0, true, Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{bounded, clique, uniformBC};
        //AbstractBenchProbas[] problems = new AbstractBenchProbas[]{bounded,uniformBC};
        launchEval(name, 5, size, problems, out, results);
        out.close();
    }

    public static void langfordTest(int k, int n, BufferedWriter results) throws IOException {
        String name = "langford";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("langford", ".csv")));
        LangfordBenchProbas gac = new LangfordBenchProbas(k, n, AllDifferent.Type.AC, -1, false, out, -1, false);
        LangfordBenchProbas rangeBC = new LangfordBenchProbas(k, n, AllDifferent.Type.RC, -1, false, out, -1, false);
        LangfordBenchProbas clique = new LangfordBenchProbas(k, n, AllDifferent.Type.NEQS, 0, false, out, -1, false);
        LangfordBenchProbas uniformGAC = new LangfordBenchProbas(k, n, AllDifferent.Type.AC, 0, true, out, -1, true);
        LangfordBenchProbas uniformRC = new LangfordBenchProbas(k, n, AllDifferent.Type.RC, 0, true, out, -1, true);//*/
        //LangfordBenchProbas diracGAC = new LangfordBenchProbas(k, n, AllDifferent.Type.AC, 0, true, Distribution.DIRAC, out, -1);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, rangeBC, clique, uniformGAC, uniformRC};
        launchEval(name, 1, k * n, problems, out, results);
        out.close();
    }


    private static void launchEval(String name, int step, int size, AbstractBenchProbas[] problems,
                                   BufferedWriter out, BufferedWriter results) throws IOException {

        out.write(sep);
        for (AbstractBenchProbas pb : problems) {
            String type = pb.toString();
            String solutions = type + "-sols";
            String nodes = type + "-nodes";
            String bcks = type + "-bcks";
            String propag = type + "-prop";
            String time = type + "-time";
            String all = solutions + sep + nodes + sep + bcks + sep + propag + sep + time + sep;
            out.write(all + "-" + sep);
        }
        out.newLine();
        for (int seed = 0; seed < step; seed++) {
            String inst = "inst" + seed + "-" + size + sep;
            out.write(inst);
            for (AbstractBenchProbas pb : problems) {
                pb.restartProblem(size, seed);
                pb.execute();
                pb.recordResults();
            }
            out.newLine();
        }

        results.write(sep);
        for (AbstractBenchProbas pb : problems) {
            String type = pb.toString();
            String solutions = type + "-sols";
            String nodes = type + "-nodes";
            String bcks = type + "-bcks";
            String propag = type + "-prop";
            String time = type + "-time";
            String all = solutions + sep + nodes + sep + bcks + sep + propag + sep + time + sep;
            results.write(all + "-" + sep);
        }
        results.newLine();
        String inst = name + "-" + size + sep;
        results.write(inst);
        for (AbstractBenchProbas pb : problems) {
            pb.recordAverage(results);
        }
        results.flush();
        results.newLine();
    }


}
