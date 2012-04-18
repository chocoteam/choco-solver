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

    public static int seed = 1986;
    public static int it = 5;

    public static void main(String[] args) throws IOException {
        BufferedWriter results = new BufferedWriter(new FileWriter(fileIt("results", ".csv")));
        //TestProbaBAllDiff.debug(results);
        TestProbaBAllDiff.nQueensTest(100, results);
        TestProbaBAllDiff.gc();
        TestProbaBAllDiff.magicSquareTest(5, results);
        TestProbaBAllDiff.gc();
        TestProbaBAllDiff.allIntervalSeriesTest(10, results);
        TestProbaBAllDiff.gc();
        TestProbaBAllDiff.partitionTest(24, results);
        TestProbaBAllDiff.gc();
        TestProbaBAllDiff.golombRulerTest(9, results);
        TestProbaBAllDiff.gc();  //*/
        TestProbaBAllDiff.langfordTest(3,17, results);
        TestProbaBAllDiff.gc();
        TestProbaBAllDiff.hamiltonianCycleTest(200, results);
        TestProbaBAllDiff.gc();
        TestProbaBAllDiff.sortingChordsTest(10, results); //*/
        results.close();
    }

    public static void gc() {
        for (int i = 0; i < 20; i++) {
            System.gc();
        }
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
        int size = 10;
        String name = "debug";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("debug", ".csv")));
        //OneAllDiffBenchProbas gac = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, out, seed, false);
        //OneAllDiffBenchProbas bounded = new OneAllDiffBenchProbas(size, AllDifferent.Type.BC, out, seed, false);
        OneAllDiffBenchProbas probaGAC = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, out, seed, true);
        //OneAllDiffBenchProbas probaBC = new OneAllDiffBenchProbas(size, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{/*gac, bounded, probaBC,*/ probaGAC};
        //AbstractBenchProbas[] problems = new AbstractBenchProbas[]{probaGAC};
        launchEval(name, it, size, problems, out, results);
        out.close();
    }

    public static void nQueensTest(int size, BufferedWriter results) throws IOException {
        String name = "nQueens";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("nQueens", ".csv")));
        NqueensBenchProbas gac = new NqueensBenchProbas(size, AllDifferent.Type.AC, out, seed, false);
        //NqueensBenchProbas bounded = new NqueensBenchProbas(size, AllDifferent.Type.BC, out, seed, false);
        //NqueensBenchProbas clique = new NqueensBenchProbas(size, AllDifferent.Type.NEQS, out, seed, false);
        NqueensBenchProbas probaGAC = new NqueensBenchProbas(size, AllDifferent.Type.AC, out, seed, true);
        //NqueensBenchProbas probaBC = new NqueensBenchProbas(size, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,/* bounded, clique,*/ probaGAC/*, probaBC*/};
        launchEval(name, it, size, problems, out, results);
        out.close();
    }

    public static void allIntervalSeriesTest(int size, BufferedWriter results) throws IOException {
        String name = "allIntervalSeries";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("allIntervalSeries", ".csv")));
        AllIntervalSeriesBenchProbas gac = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.AC, out, seed, false);
        //AllIntervalSeriesBenchProbas bounded = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.BC, out, seed, false);
        //AllIntervalSeriesBenchProbas clique = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.NEQS, out, seed, false);
        AllIntervalSeriesBenchProbas probaGAC = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.AC, out, seed, true);
        //AllIntervalSeriesBenchProbas probaBC = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,/* bounded, clique,*/ probaGAC/*, probaBC*/};
        launchEval(name, it, size, problems, out, results);
        out.close();
    }

    public static void magicSquareTest(int size, BufferedWriter results) throws IOException {
        String name = "magicSquare";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("magicSquare", ".csv")));
        MagicSquareBenchProbas gac = new MagicSquareBenchProbas(size, AllDifferent.Type.AC, out, seed, false);
        //MagicSquareBenchProbas bounded = new MagicSquareBenchProbas(size, AllDifferent.Type.BC, out, seed, false);
        //MagicSquareBenchProbas clique = new MagicSquareBenchProbas(size, AllDifferent.Type.NEQS, out, seed, false);
        MagicSquareBenchProbas probaGAC = new MagicSquareBenchProbas(size, AllDifferent.Type.AC, out, seed, true);
        //MagicSquareBenchProbas probaBC = new MagicSquareBenchProbas(size, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,/* bounded, clique,*/ probaGAC/*, probaBC*/};
        launchEval(name, it, size, problems, out, results);
        out.close();
    }

    public static void hamiltonianCycleTest(int size, BufferedWriter results) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("hamiltonianCycle", ".csv")));
        for (int i = 5; i < 41; i = i + 5) {
            HamiltonianCycleBenchProbas.GenerateStrat strat = HamiltonianCycleBenchProbas.GenerateStrat.NEIGHBOR;
            int neighbors = i;
            String name = "ham-" + size + "-" + neighbors;
            HamiltonianCycleBenchProbas gac = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.AC, out, seed, neighbors, strat, false);
            //HamiltonianCycleBenchProbas bounded = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.BC, out, seed, neighbors, strat, false);
            //HamiltonianCycleBenchProbas clique = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.NEQS, out, seed, neighbors, strat, false);
            HamiltonianCycleBenchProbas probaGAC = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.AC, out, seed, neighbors, strat, true);
            //HamiltonianCycleBenchProbas probaBC = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.BC, out, seed, neighbors, strat, true);
            AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,/* bounded, clique,*/ probaGAC/*, probaBC*/};
            launchEval(name, it, size, problems, out, results);
            results.flush();
            out.flush();
        }
        out.close();
    }

    public static void partitionTest(int size, BufferedWriter results) throws IOException {
        String name = "partition";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("partition", ".csv")));
        PartitionBenchProbas gac = new PartitionBenchProbas(size, AllDifferent.Type.AC, out, seed, false);
        //PartitionBenchProbas bounded = new PartitionBenchProbas(size, AllDifferent.Type.BC, out, seed, false);
        //PartitionBenchProbas clique = new PartitionBenchProbas(size, AllDifferent.Type.NEQS, out, seed, false);
        PartitionBenchProbas probaGAC = new PartitionBenchProbas(size, AllDifferent.Type.AC, out, seed, true);
        //PartitionBenchProbas probaBC = new PartitionBenchProbas(size, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,/* bounded, clique,*/ probaGAC/*, probaBC*/};
        launchEval(name, it, size, problems, out, results);
        out.close();
    }

    public static void golombRulerTest(int size, BufferedWriter results) throws IOException {
        String name = "golombRuler";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("golombRuler", ".csv")));
        GolombRulerBenchProbas bounded = new GolombRulerBenchProbas(size, AllDifferent.Type.BC, out, seed, false);
        //GolombRulerBenchProbas clique = new GolombRulerBenchProbas(size, AllDifferent.Type.NEQS, out, seed, false);
        GolombRulerBenchProbas probaBC = new GolombRulerBenchProbas(size, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{bounded/*, clique*/, probaBC};
        launchEval(name, it, size, problems, out, results);
        out.close();
    }

    public static void langfordTest(int k, int n, BufferedWriter results) throws IOException {
        String name = "langford";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("langford", ".csv")));
        LangfordBenchProbas gac = new LangfordBenchProbas(k, n, AllDifferent.Type.AC, out, seed, false);
        //LangfordBenchProbas bounded = new LangfordBenchProbas(k, n, AllDifferent.Type.BC, out, seed, false);
        //LangfordBenchProbas clique = new LangfordBenchProbas(k, n, AllDifferent.Type.NEQS, out, seed, false);
        LangfordBenchProbas probaGAC = new LangfordBenchProbas(k, n, AllDifferent.Type.AC, out, seed, true);
        //LangfordBenchProbas probaBC = new LangfordBenchProbas(k, n, AllDifferent.Type.BC, out, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,/* bounded, clique,*/ probaGAC/*, probaBC*/};
        launchEval(name, it, k * n, problems, out, results);
        out.close();
    }

    public static void sortingChordsTest(int nbchords, BufferedWriter results) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("sortingChords", ".csv")));
        for (int j = 4; j < 15; j = j + 2) {
            String name = "sortingChords-" + nbchords + "-" + j;
            System.out.println(name);
            SortingChordsBenchProbas gac = new SortingChordsBenchProbas(nbchords, j, AllDifferent.Type.AC, out, seed, false);
            //SortingChordsBenchProbas bounded = new SortingChordsBenchProbas(nbchords, j, AllDifferent.Type.BC, out, seed, false);
            //SortingChordsBenchProbas clique = new SortingChordsBenchProbas(nbchords, j, AllDifferent.Type.NEQS, out, seed, false);
            SortingChordsBenchProbas probaGAC = new SortingChordsBenchProbas(nbchords, j, AllDifferent.Type.AC, out, seed, true);
            //SortingChordsBenchProbas probaBC = new SortingChordsBenchProbas(nbchords, j, AllDifferent.Type.BC, out, seed, true);
            AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, /*bounded, clique,*/ probaGAC/*, probaBC*/};
            launchEval(name, it, nbchords, problems, out, results);
            results.flush();
            out.flush();
        }
        out.close();
    }


    private static void launchEval(String name, int step, int size, AbstractBenchProbas[] problems,
                                   BufferedWriter out, BufferedWriter results) throws IOException {

        out.write(sep);
        for (AbstractBenchProbas pb : problems) {
            String type = pb.toString();
            String solutions = type + "-sols";
            String nodes = type + "-nodes";
            String propag = type + "-prop";
            String ratio = type + "-ratio";
            String time = type + "-time";
            String all = solutions + sep + nodes + sep + propag + sep + ratio + sep + time + sep;
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
            String propag = type + "-prop";
            String ratio = type + "-ratio";
            String time = type + "-time";
            String all = solutions + sep + nodes + sep + propag + sep + ratio + sep + time + sep;
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
