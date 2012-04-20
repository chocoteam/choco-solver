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
    public static int it;

    public static void main(String[] args) throws IOException {

        BufferedWriter results = new BufferedWriter(new FileWriter(fileIt("results", ".csv")));
        //TestProbaBAllDiff.debugAlone(results);
        //TestProbaBAllDiff.debugFull(results);

        it = 30;
        TestProbaBAllDiff.graceFulGraphsTest(13,results);
        /*TestProbaBAllDiff.gc();
        TestProbaBAllDiff.nQueensTest(300, results); // tres bon en solve => pas de fails
        TestProbaBAllDiff.gc();
        TestProbaBAllDiff.allIntervalSeriesTest(300, results); // bon en solve : 2 propag gagnees mais pas de temps => pas de fails
        TestProbaBAllDiff.gc(); //*/

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

    public static void debugFull(BufferedWriter results) throws IOException {
        it = 1;
        TestProbaBAllDiff.graceFulGraphsTest(13,results);
        TestProbaBAllDiff.gc();
        /*TestProbaBAllDiff.nQueensTest(80, results); // tres bon en solve => pas de fails
        TestProbaBAllDiff.gc();
        TestProbaBAllDiff.allIntervalSeriesTest(100, results); // bon en solve : 2 propag gagnees mais pas de temps => pas de fails
        TestProbaBAllDiff.gc();
        TestProbaBAllDiff.golombRulerTest(10, results);  // mauvais en optim et en solve => on explose le nombre de sommets !!!
        TestProbaBAllDiff.gc();
        TestProbaBAllDiff.magicSquareTest(5, results); // trop dur des taille 7 => donc c trop petit
        TestProbaBAllDiff.gc();
        TestProbaBAllDiff.partitionTest(24, results);  // mauvais en solve on economise 1 seule propag => bcp de fails !!!
        TestProbaBAllDiff.gc();
        TestProbaBAllDiff.langfordTest(3, 11, results); // mauvais, on economise des propags mais le temps de calcul explose => bcp de fails !!!
        TestProbaBAllDiff.gc();
        TestProbaBAllDiff.sortingChordsTest(100, results); // en solve, 1 seule propag economisee, mais pas explosion du temps => pas de fails
        TestProbaBAllDiff.hamiltonianCycleTest(300, results); // mauvais peu de propag gagnees... mais pas d'explosion du temps
        TestProbaBAllDiff.gc(); // */
    }

    public static void debugAlone(BufferedWriter results) throws IOException {
        int size = 10;
        String name = "debug";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("debug", ".csv")));
        OneAllDiffBenchProbas gac = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, it, seed, false);
        //OneAllDiffBenchProbas bounded = new OneAllDiffBenchProbas(size, AllDifferent.Type.BC, it, seed, false);
        OneAllDiffBenchProbas probaGAC = new OneAllDiffBenchProbas(size, AllDifferent.Type.AC, it, seed, true);
        //OneAllDiffBenchProbas probaBC = new OneAllDiffBenchProbas(size, AllDifferent.Type.BC, it, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, /*bounded, probaBC,*/ probaGAC};
        launchEval(name, size, problems, out, results);
        out.close();
    }

    public static void graceFulGraphsTest(int size, BufferedWriter results) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("graceFulGraphs", ".csv")));
        int max = (size*(size-1))/2;
        //for (int nbEgdes = size-1; nbEgdes <= (size*(size-1))/2; nbEgdes++) {
        int nbEgdes = 2*size;
            String name = "gfg-" + size + "-" + nbEgdes;
            GracefulGraphBenchProbas gac = new GracefulGraphBenchProbas(size, nbEgdes, AllDifferent.Type.AC, it, seed, false);
            GracefulGraphBenchProbas probaGAC = new GracefulGraphBenchProbas(size, nbEgdes, AllDifferent.Type.AC, it, seed, true);
            AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, probaGAC};
            launchEval(name, size, problems, out, results);
        //}
        out.close();
    }

    public static void nQueensTest(int size, BufferedWriter results) throws IOException {
        String name = "nQueens";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("nQueens", ".csv")));
        NqueensBenchProbas gac = new NqueensBenchProbas(size, AllDifferent.Type.AC, it, seed, false);
        //NqueensBenchProbas bounded = new NqueensBenchProbas(size, AllDifferent.Type.BC, it, seed, false);
        //NqueensBenchProbas clique = new NqueensBenchProbas(size, AllDifferent.Type.NEQS, it, seed, false);
        NqueensBenchProbas probaGAC = new NqueensBenchProbas(size, AllDifferent.Type.AC, it, seed, true);
        //NqueensBenchProbas probaBC = new NqueensBenchProbas(size, AllDifferent.Type.BC, it, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,/* bounded, clique,*/ probaGAC/*, probaBC*/};
        launchEval(name, size, problems, out, results);
        out.close();
    }

    public static void allIntervalSeriesTest(int size, BufferedWriter results) throws IOException {
        String name = "allIntervalSeries";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("allIntervalSeries", ".csv")));
        AllIntervalSeriesBenchProbas gac = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.AC, it, seed, false);
        //AllIntervalSeriesBenchProbas bounded = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.BC, it, seed, false);
        //AllIntervalSeriesBenchProbas clique = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.NEQS, it, seed, false);
        AllIntervalSeriesBenchProbas probaGAC = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.AC, it, seed, true);
        //AllIntervalSeriesBenchProbas probaBC = new AllIntervalSeriesBenchProbas(size, AllDifferent.Type.BC, it, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, /*bounded, clique,*/ probaGAC/*, probaBC*/};
        launchEval(name, size, problems, out, results);
        out.close();
    }

    public static void magicSquareTest(int size, BufferedWriter results) throws IOException {
        String name = "magicSquare";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("magicSquare", ".csv")));
        MagicSquareBenchProbas gac = new MagicSquareBenchProbas(size, AllDifferent.Type.AC, it, seed, false);
        //MagicSquareBenchProbas bounded = new MagicSquareBenchProbas(size, AllDifferent.Type.BC, it, seed, false);
        //MagicSquareBenchProbas clique = new MagicSquareBenchProbas(size, AllDifferent.Type.NEQS, it, seed, false);
        MagicSquareBenchProbas probaGAC = new MagicSquareBenchProbas(size, AllDifferent.Type.AC, it, seed, true);
        //MagicSquareBenchProbas probaBC = new MagicSquareBenchProbas(size, AllDifferent.Type.BC, it, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,/* bounded, clique,*/ probaGAC/*, probaBC*/};
        launchEval(name, size, problems, out, results);
        out.close();
    }

    public static void hamiltonianCycleTest(int size, BufferedWriter results) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("hamiltonianCycle", ".csv")));
        for (int i = 10; i < 40; i = i + 10) {
            HamiltonianCycleBenchProbas.GenerateStrat strat = HamiltonianCycleBenchProbas.GenerateStrat.NEIGHBOR;
            int neighbors = i;
            String name = "ham-" + size + "-" + neighbors;
            HamiltonianCycleBenchProbas gac = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.AC, it, seed, neighbors, strat, false);
            //HamiltonianCycleBenchProbas bounded = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.BC, it, seed, neighbors, strat, false);
            //HamiltonianCycleBenchProbas clique = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.NEQS, it, seed, neighbors, strat, false);
            HamiltonianCycleBenchProbas probaGAC = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.AC, it, seed, neighbors, strat, true);
            //HamiltonianCycleBenchProbas probaBC = new HamiltonianCycleBenchProbas(size, AllDifferent.Type.BC, it, seed, neighbors, strat, true);
            AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,/* bounded, clique,*/ probaGAC/*, probaBC*/};
            launchEval(name, size, problems, out, results);
        }
        out.close();
    }

    public static void partitionTest(int size, BufferedWriter results) throws IOException {
        String name = "partition";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("partition", ".csv")));
        PartitionBenchProbas gac = new PartitionBenchProbas(size, AllDifferent.Type.AC, it, seed, false);
        //PartitionBenchProbas bounded = new PartitionBenchProbas(size, AllDifferent.Type.BC, it, seed, false);
        //PartitionBenchProbas clique = new PartitionBenchProbas(size, AllDifferent.Type.NEQS, it, seed, false);
        PartitionBenchProbas probaGAC = new PartitionBenchProbas(size, AllDifferent.Type.AC, it, seed, true);
        //PartitionBenchProbas probaBC = new PartitionBenchProbas(size, AllDifferent.Type.BC, it, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,/* bounded, clique,*/ probaGAC/*, probaBC*/};
        launchEval(name, size, problems, out, results);
        out.close();
    }

    public static void golombRulerTest(int size, BufferedWriter results) throws IOException {
        String name = "golombRuler";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("golombRuler", ".csv")));
        GolombRulerBenchProbas bounded = new GolombRulerBenchProbas(size, AllDifferent.Type.BC, it, seed, false);
        //GolombRulerBenchProbas clique = new GolombRulerBenchProbas(size, AllDifferent.Type.NEQS, it, seed, false);
        GolombRulerBenchProbas probaBC = new GolombRulerBenchProbas(size, AllDifferent.Type.BC, it, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{bounded/*, clique*/, probaBC};
        launchEval(name, size, problems, out, results);
        out.close();
    }

    public static void langfordTest(int k, int n, BufferedWriter results) throws IOException {
        String name = "langford";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("langford", ".csv")));
        LangfordBenchProbas gac = new LangfordBenchProbas(k, n, AllDifferent.Type.AC, it, seed, false);
        //LangfordBenchProbas bounded = new LangfordBenchProbas(k, n, AllDifferent.Type.BC, it, seed, false);
        //LangfordBenchProbas clique = new LangfordBenchProbas(k, n, AllDifferent.Type.NEQS, it, seed, false);
        LangfordBenchProbas probaGAC = new LangfordBenchProbas(k, n, AllDifferent.Type.AC, it, seed, true);
        //LangfordBenchProbas probaBC = new LangfordBenchProbas(k, n, AllDifferent.Type.BC, it, seed, true);
        AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac,/* bounded, clique,*/ probaGAC/*, probaBC*/};
        launchEval(name, k * n, problems, out, results);
        out.close();
    }

    public static void sortingChordsTest(int nbchords, BufferedWriter results) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(fileIt("sortingChords", ".csv")));
        for (int j = 4; j < 20; j = j + 2) {
            String name = "sortingChords-" + nbchords + "-" + j;
            SortingChordsBenchProbas gac = new SortingChordsBenchProbas(nbchords, j, AllDifferent.Type.AC, it, seed, false);
            //SortingChordsBenchProbas bounded = new SortingChordsBenchProbas(nbchords, j, AllDifferent.Type.BC, it, seed, false);
            //SortingChordsBenchProbas clique = new SortingChordsBenchProbas(nbchords, j, AllDifferent.Type.NEQS, it, seed, false);
            SortingChordsBenchProbas probaGAC = new SortingChordsBenchProbas(nbchords, j, AllDifferent.Type.AC, it, seed, true);
            //SortingChordsBenchProbas probaBC = new SortingChordsBenchProbas(nbchords, j, AllDifferent.Type.BC, it, seed, true);
            AbstractBenchProbas[] problems = new AbstractBenchProbas[]{gac, /*bounded, clique,*/ probaGAC/*, probaBC*/};
            launchEval(name, nbchords, problems, out, results);
        }
        out.close();
    }


    private static void launchEval(String name, int size, AbstractBenchProbas[] problems,
                                   BufferedWriter out, BufferedWriter results) throws IOException {
        String entete = sep+"-"+sep;
        for (AbstractBenchProbas pb : problems) {
            String type = pb.toString();
            String solutions = type + "-sols";
            String nodes = type + "-nodes";
            String fails = type + "-fails";
            String propag = type + "-prop";
            String ratio = type + "-ratio";
            String time = type + "-time";
            entete += solutions + sep + nodes + sep + fails + sep + propag + sep + ratio + sep + time + sep + "-" + sep;

        }
        out.write(entete);
        results.write(entete);
        out.newLine();
        results.newLine();
        String inst = name + "-" + size;
        String writeInst = inst + sep + "-" + sep;
        results.write(writeInst);
        for (AbstractBenchProbas pb : problems) {
            String data = pb.executionLoop();
            results.write(data+"-"+sep);
            results.flush();
        }
        results.newLine();
        for (int i = 0; i < it; i++) {
            out.write(inst + "-" + (seed+i) + sep + "-" + sep);
            String data = "";
            for (AbstractBenchProbas pb : problems) {
                String[] data_pb = pb.getDetails();
                data += data_pb[i]+"-"+sep;
            }
            out.write(data);
            out.newLine();
            out.flush();
        }
    }


}
