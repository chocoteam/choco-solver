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

    public static int seed = -1;

    public static void main(String[] args) throws IOException {
        BufferedWriter results = new BufferedWriter(new FileWriter(fileIt("results", ".csv")));
        //TestProbaBAllDiff.debug(results);
        /*TestProbaBAllDiff.oneAllDiffTest(15, results);
        TestProbaBAllDiff.hamiltonianCycleTest(200, results);
        TestProbaBAllDiff.magicSquareTest(5, results);//*/
        TestProbaBAllDiff.nQueensTest(12, results);
        /*TestProbaBAllDiff.allIntervalSeriesTest(10, results);
        TestProbaBAllDiff.partitionTest(24, results);
        TestProbaBAllDiff.golombRulerTest(9, results);
        TestProbaBAllDiff.langfordTest(3,17, results); //*/
        results.close();
    }

    public static String fileIt(String name, String ext) {
        String path = null;
        try {
            File dir = new File("/private/var/folders/vh/cfkwx09174l9vr5bfjktdxp80000gp/T/probaResults");
            path = File.createTempFile(name, ext, dir).toString();
            //path = File.createTempFile(name, ext).toString();
            LoggerFactory.getLogger("solver").error("Tmp file : " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    public static void debug(BufferedWriter results) throws IOException {
        int size = 13;
        String name = "debug";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("debug", ".csv")));
        OneAllDiffBenchProbas gac = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, out, seed, false);
        OneAllDiffBenchProbas bounded = new OneAllDiffBenchProbas(size, AllDifferent.Type.BC, out, seed, false);
        OneAllDiffBenchProbas probaGAC = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, out, seed, true);
        OneAllDiffBenchProbas probaBC = new OneAllDiffBenchProbas(size, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, bounded, probaBC, probaGAC};
        //AbstractBenchProbas[] problems = new AbstractBenchProbas[]{probaGAC};
        launchEval(name, 30, size, problems, out, results);
        out.close();
    }


    public static void oneAllDiffTest(int size, BufferedWriter results) throws IOException {
        String name = "OneAllDiff";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("OneAllDiff", ".csv")));
        OneAllDiffBenchProbas gac = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, out, seed, false);
        OneAllDiffBenchProbas bounded = new OneAllDiffBenchProbas(size, AllDifferent.Type.BC, out, seed, false);
        OneAllDiffBenchProbas clique = new OneAllDiffBenchProbas(size, AllDifferent.Type.NEQS, out, seed, false);
        OneAllDiffBenchProbas probaGAC = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, out, seed, true);
        OneAllDiffBenchProbas probaBC = new OneAllDiffBenchProbas(size, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, bounded, clique, probaGAC, probaBC};
        launchEval(name, 25, size, problems, out, results);
        out.close();
    }

    public static void nQueensTest(int size, BufferedWriter results) throws IOException {
        String name = "nQueens";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("nQueens", ".csv")));
        NqueensBenchProbas gac = new NqueensBenchProbas(size, AllDifferent.Type.AC, out, seed, false);
        NqueensBenchProbas bounded = new NqueensBenchProbas(size, AllDifferent.Type.BC, out, seed, false);
        NqueensBenchProbas clique = new NqueensBenchProbas(size, AllDifferent.Type.NEQS, out, seed, false);
        //NqueensBenchProbas probaGAC = new NqueensBenchProbas(size, AllDifferent.Type.AC, out, seed, true);
        //NqueensBenchProbas probaBC = new NqueensBenchProbas(size, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, bounded, clique, /*probaGAC, probaBC*/};
        launchEval(name, 1, size, problems, out, results);  //5
        out.close();
    }

    public static void allIntervalSeriesTest(int size, BufferedWriter results) throws IOException {
        String name = "allIntervalSeries";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("allIntervalSeries", ".csv")));
        AllIntervalSeriesBenchProbas gac = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.AC, out, seed, false);
        AllIntervalSeriesBenchProbas bounded = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.BC, out, seed, false);
        AllIntervalSeriesBenchProbas clique = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.NEQS, out, seed, false);
        AllIntervalSeriesBenchProbas probaGAC = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.AC, out, seed, true);
        AllIntervalSeriesBenchProbas probaBC = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, bounded, clique, probaGAC, probaBC};
        launchEval(name, 5, size, problems, out, results);
        out.close();
    }

    public static void magicSquareTest(int size, BufferedWriter results) throws IOException {
        String name = "magicSquare";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("magicSquare", ".csv")));
        MagicSquareBenchProbas gac = new MagicSquareBenchProbas(size, AllDifferent.Type.AC, out, seed, false);
        MagicSquareBenchProbas bounded = new MagicSquareBenchProbas(size, AllDifferent.Type.BC, out, seed, false);
        MagicSquareBenchProbas clique = new MagicSquareBenchProbas(size, AllDifferent.Type.NEQS, out, seed, false);
        MagicSquareBenchProbas probaGAC = new MagicSquareBenchProbas(size, AllDifferent.Type.AC, out, seed, true);
        MagicSquareBenchProbas probaBC = new MagicSquareBenchProbas(size, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, bounded, clique, probaGAC, probaBC};
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
            HamiltonianCycleBenchProbas gac = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.AC, out, seed, neighbors, strat, false);
            HamiltonianCycleBenchProbas bounded = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.BC, out, seed, neighbors, strat, false);
            HamiltonianCycleBenchProbas clique = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.NEQS, out, seed, neighbors, strat, false);
            HamiltonianCycleBenchProbas probaGAC = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.AC, out, seed, neighbors, strat, true);
            HamiltonianCycleBenchProbas probaBC = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.BC, out, seed, neighbors, strat, true);
            // end test
            AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, bounded, clique, probaGAC, probaBC};
            launchEval(name, 5, size, problems, out, results);
            out.close();
        }
    }

    public static void partitionTest(int size, BufferedWriter results) throws IOException {
        String name = "partition";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("partition", ".csv")));
        PartitionBenchProbas gac = new PartitionBenchProbas(size, AllDifferent.Type.AC, out, seed, false);
        PartitionBenchProbas bounded = new PartitionBenchProbas(size, AllDifferent.Type.BC, out, seed, false);
        PartitionBenchProbas clique = new PartitionBenchProbas(size, AllDifferent.Type.NEQS, out, seed, false);
        PartitionBenchProbas probaGAC = new PartitionBenchProbas(size, AllDifferent.Type.AC, out, seed, true);
        PartitionBenchProbas probaBC = new PartitionBenchProbas(size, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, bounded, clique, probaGAC, probaBC};
        launchEval(name, 5, size, problems, out, results);
        out.close();
    }

    public static void golombRulerTest(int size, BufferedWriter results) throws IOException {
        String name = "golombRuler";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("golombRuler", ".csv")));
        GolombRulerBenchProbas bounded = new GolombRulerBenchProbas(size, AllDifferent.Type.BC, out, seed, false);
        GolombRulerBenchProbas clique = new GolombRulerBenchProbas(size, AllDifferent.Type.NEQS, out, seed, false);
        GolombRulerBenchProbas probaBC = new GolombRulerBenchProbas(size, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{bounded, clique, probaBC};
        launchEval(name, 5, size, problems, out, results);
        out.close();
    }

    public static void langfordTest(int k, int n, BufferedWriter results) throws IOException {
        String name = "langford";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("langford", ".csv")));
        LangfordBenchProbas gac = new LangfordBenchProbas(k, n, AllDifferent.Type.AC, out, seed, false);
        LangfordBenchProbas bounded = new LangfordBenchProbas(k, n, AllDifferent.Type.BC, out, seed, false);
        LangfordBenchProbas clique = new LangfordBenchProbas(k, n, AllDifferent.Type.NEQS, out, seed, false);
        LangfordBenchProbas probaGAC = new LangfordBenchProbas(k, n, AllDifferent.Type.AC, out, seed, true);
        LangfordBenchProbas probaBC = new LangfordBenchProbas(k, n, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, bounded, clique, probaGAC, probaBC};
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
