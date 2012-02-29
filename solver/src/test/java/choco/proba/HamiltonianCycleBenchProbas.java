package choco.proba;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.InverseChanneling;
import solver.constraints.nary.NoSubTours;
import solver.recorders.conditions.CondAllDiffBCProba;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 20/12/11
 */
public class HamiltonianCycleBenchProbas extends AbstractBenchProbas {

    public enum GenerateStrat {
        ARC, NEIGHBOR
    }

    int neighbor;
    GenerateStrat strat;

    public HamiltonianCycleBenchProbas(int n, AllDifferent.Type type, int frequency, boolean active,
                                       CondAllDiffBCProba.Distribution dist, BufferedWriter out, int seed, int neighbor,
                                       GenerateStrat strat) throws IOException {
        super(new Solver(), n, type, frequency, active, dist, out, seed);
        this.neighbor = neighbor;
        this.strat = strat;
    }

    @Override
    void buildProblem(int size) {
        GraphGenerator.InitialProperty prop = GraphGenerator.InitialProperty.HamiltonianCircuit;
        GraphGenerator gen = new GraphGenerator(size, seed, prop);
        //System.out.println(gen);
        switch (strat) {
            case ARC:
                gen.arcBasedGenerator(neighbor);
                break;
            case NEIGHBOR:
                gen.neighborBasedGenerator(neighbor);
                break;
        }
        //System.out.println(gen);
        int[][] graph = gen.toInteger();
        /*for (int i = 0; i < size; i++) {
            System.out.print("graph[" + i + "]:=");
            for (int j = 0; j < graph[i].length; j++) {
                System.out.print(graph[i][j] + " ");
            }
            System.out.println();
        }*/
        int[][] invGraph = gen.invToInteger();
        /*for (int i = 0; i < size; i++) {
            System.out.print("anti-graph[" + i + "]:=");
            for (int j = 0; j < invGraph[i].length; j++) {
                System.out.print(invGraph[i][j] + " ");
            }
            System.out.println();
        }*/

        this.vars = new IntVar[size];
        IntVar[] preds = new IntVar[size];
        this.allVars = new IntVar[2 * size];
        this.allDiffVars = new IntVar[][]{vars, preds};
        this.nbAllDiff = 2;
        this.cstrs = new Constraint[4];
        this.allDiffs = new AllDifferent[this.nbAllDiff];
        // variables
        for (int i = 0; i < size; i++) {
            vars[i] = VariableFactory.enumerated("node" + i, graph[i], solver);
            preds[i] = VariableFactory.enumerated("pred" + i, invGraph[i], solver);
        }
        int k = 0;
        for (int i = 0; i < size; i++) {
            allVars[k++] = vars[i];
        }
        for (int i = 0; i < size; i++) {
            allVars[k++] = preds[i];
        }
        // contraintes
        this.allDiffs[0] = new AllDifferent(vars, solver, type);
        this.cstrs[0] = this.allDiffs[0];
        this.cstrs[1] = new NoSubTours(vars, solver);
        this.allDiffs[1] = new AllDifferent(preds, solver, type);
        this.cstrs[2] = this.allDiffs[1];
        this.cstrs[3] = new InverseChanneling(this.vars, preds, solver, AllDifferent.Type.NONE);
    }
}

class GraphGenerator {
    public enum InitialProperty {
        HamiltonianCircuit, Tree, None
    }

    Random rand;
    int size;
    boolean[][] graph;

    /**
     * instance of graph generator
     *
     * @param size number of nodes in the directed graph generated
     * @param seed uniform random distribution from a given integer
     * @param prop property insured by the generator
     */
    public GraphGenerator(int size, int seed, InitialProperty prop) {
        this.size = size;
        this.rand = new Random(seed);
        this.graph = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.graph[i][j] = false;
            }
        }
        switch (prop) {
            case HamiltonianCircuit:
                this.generateInitialHamiltonianCircuit();
                break;
            case Tree:
                this.generateInitialTree();
                break;
            default:
                break;
        }
    }

    /**
     * randomly generate a boolean matrix representing a directed graph
     *
     * @param nbNeighbor average number of neighbors
     * @return a boolean matrix
     */
    public boolean[][] arcBasedGenerator(int nbNeighbor) {
        // on ajoute des arcs
        int nb = size * nbNeighbor;
        int cur = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (graph[i][j]) {
                    cur++;
                }
            }
        }
        nb = nb - cur;
        while (nb > 0) {
            int i = rand.nextInt(size);
            BitSet seti = new BitSet(size);
            for (int j = 0; j < size; j++) {
                if (graph[i][j]) {
                    seti.set(j, false);
                } else {
                    seti.set(j, true);
                }
            }
            seti.set(i, false);
            int y = rand.nextInt(size);
            int j = seti.nextSetBit(y);
            if (j == -1) {
                j = seti.nextSetBit(0);
            }
            if (j != -1) {
                seti.set(j, false);
                graph[i][j] = true;
                nb--;
            }
        }
        return this.graph;
    }

    /**
     * randomly generate a boolean matrix representing a directed graph
     *
     * @param nb number of neighbor for each node, necessarily < size
     * @return a boolean matrix
     */
    public boolean[][] neighborBasedGenerator(int nb) {
        // on ajoute des arcs: exactement nb pour chaque sommet
        for (int i = 0; i < size; i++) {
            int ni = 0;
            BitSet seti = new BitSet(size);
            for (int j = 0; j < size; j++) {
                if (graph[i][j]) {
                    seti.set(j, false);
                    ni++;
                } else {
                    seti.set(j, true);
                }
            }
            seti.set(i, false);
            int ti = nb - ni;
            while (ti > 0) {
                int y = rand.nextInt(size);
                int j = seti.nextSetBit(y);
                if (j == -1) {
                    j = seti.nextSetBit(0);
                }
                seti.set(j, false);
                graph[i][j] = true;
                ti--;
            }
        }
        return this.graph;
    }

    public String toString() {
        String g = "";
        for (int i = 0; i < size; i++) {
            g += i + ":";
            for (int j = 0; j < size; j++) {
                if (graph[i][j]) g += j + " ";
            }
            g += "\n";
        }
        return g;
    }

    public int[][] toInteger() {
        int[][] res = new int[size][size];
        for (int i = 0; i < size; i++) {
            int nbj = 0;
            for (int j = 0; j < size; j++) {
                if (graph[i][j]) {
                    nbj++;
                }
            }
            int[] nexti = new int[nbj];
            int k = 0;
            for (int j = 0; j < size; j++) {
                if (graph[i][j]) {
                    nexti[k++] = j;
                }
            }
            res[i] = nexti;
        }
        return res;
    }

    public int[][] invToInteger() {
        int[][] res = new int[size][size];
        for (int j = 0; j < size; j++) {
            int nbi = 0;
            for (int i = 0; i < size; i++) {
                if (graph[i][j]) {
                    nbi++;
                }
            }
            int[] predj = new int[nbi];
            int k = 0;
            for (int i = 0; i < size; i++) {
                if (graph[i][j]) {
                    predj[k++] = i;
                }
            }
            res[j] = predj;
        }
        return res;
    }

    /**
     * Provide an initial Hamiltonian circuit in graph
     */
    private void generateInitialHamiltonianCircuit() {
        //int[] perm = new int[size];
        BitSet nodes = new BitSet(size);
        for (int i = 0; i < size; i++) {
            nodes.set(i, true);
        }
        int start = rand.nextInt(size);
        int i = start;
        nodes.set(i, false);
        int j;
        do {
            int idj = rand.nextInt(size);
            j = nodes.nextSetBit(idj);
            if (j == -1) {
                j = nodes.nextSetBit(0);
            }
            if (j != -1) {
                nodes.set(j, false);
                graph[i][j] = true;
                i = j;
            }
        } while (j != -1);
        graph[i][start] = true;
    }

    /**
     * Provide an initial tree in graph
     */
    private void generateInitialTree() {
        BitSet notIn = new BitSet(size);
        BitSet in = new BitSet(size);
        for (int i = 0; i < size; i++) {
            notIn.set(i, true);
            in.set(i, false);
        }
        while (notIn.cardinality() > 0) {
            int i = pickOneTrue(notIn);
            notIn.set(i, false);
            // relier i a un sommet de in quelconque.
            int j;
            int sj = rand.nextInt(size);
            j = in.nextSetBit(sj);
            if (j == -1) {
                j = in.nextSetBit(0);
            }
            // cas du premier sommet ajoute dans in
            if (j > -1) {
                // pas de pbs car i et j ne peuvent pas etre tous deux dans in
                this.graph[i][j] = true;
            }
            in.set(i, true);
        }
    }

    private int pickOneTrue(BitSet tab) {
        int start = rand.nextInt(tab.length());
        int i = tab.nextSetBit(start);
        if (i == -1) {
            i = tab.nextSetBit(0);
        }
        return i;
    }

    public static void main(String[] args) {
        int n = 4;
        int neighbor = 2;
        int seed = 0;
        InitialProperty prop = InitialProperty.HamiltonianCircuit;
        GraphGenerator gen = new GraphGenerator(n, seed, prop);
        gen.arcBasedGenerator(2);
        System.out.println(gen.toString());
        //prop = InitialProperty.Tree;
        //gen = new GraphGenerator(n, seed, prop);
        //gen.neighborBasedGenerator(neighbor);
        //System.out.println(gen.toString());
    }

}
