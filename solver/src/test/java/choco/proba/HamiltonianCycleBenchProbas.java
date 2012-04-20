package choco.proba;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.InverseChanneling;
import solver.constraints.nary.NoSubTours;
import solver.constraints.nary.alldifferent.AllDifferent;
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

    public HamiltonianCycleBenchProbas(int n, AllDifferent.Type type, int nbTests, int seed, int neighbor,
                                       GenerateStrat strat, boolean isProba) throws IOException {
        super(new Solver(), n, type, nbTests, seed, isProba);
        this.neighbor = neighbor;
        this.strat = strat;
    }

    @Override
    void buildProblem(int size, boolean proba) {
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
        int[][] graph = gen.toInteger();
        int[][] invGraph = gen.invToInteger();

        this.vars = new IntVar[size];
        IntVar[] preds = new IntVar[size];
        this.allVars = new IntVar[2 * size];
        this.cstrs = new Constraint[4];
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
        this.cstrs[0] = new AllDifferent(vars, solver, type);
        this.cstrs[2] = new AllDifferent(preds, solver, type);
        this.cstrs[1] = new NoSubTours(vars, solver);
        this.cstrs[3] = new InverseChanneling(this.vars, preds, solver, AllDifferent.Type.BC);
    }
}

