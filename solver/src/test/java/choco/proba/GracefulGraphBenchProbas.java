package choco.proba;

import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.binary.Absolute;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 */
public class GracefulGraphBenchProbas extends AbstractBenchProbas {

    boolean[][] graph;
    int edges;

    GracefulGraphBenchProbas(int size, int edges, AllDifferent.Type type, int nbTests, int seed, boolean isProba) {
        super(new Solver(), size, type, nbTests, seed, isProba);
        this.edges = edges;
    }

    /*@Override
    void solveProcess() {
        solver.findAllSolutions();
    }*/

    @Override
    void buildProblem(int size, boolean proba) {
        /*System.out.println("\n///////// start generate");
        System.out.println(size+","+edges);  */
        GraphGenerator gen = new GraphGenerator(size,seed, GraphGenerator.InitialProperty.Tree);
        this.graph = gen.edgeBasedGenerator(edges);
        /*System.out.println(gen.toString());
        System.out.println("end generate ///////// "); */
        Collection<IntVar> allVars = new ArrayList<IntVar>();
        IntVar[] nodeLabel = VariableFactory.enumeratedArray("nodeLabel",size,0,edges,solver);
        allVars.addAll(ArrayUtils.toList(nodeLabel));
        this.vars = nodeLabel;
        IntVar[] edgeLabel = VariableFactory.enumeratedArray("edgeLabel",edges,1,edges,solver);
        allVars.addAll(ArrayUtils.toList(edgeLabel));
        this.allVars = allVars.toArray(new IntVar[allVars.size()]);
        this.cstrs = new Constraint[edges+2];
        int c = 0;
        int e = 0;
        for (int i = 0; i < size; i++) {
            for (int j = i+1; j < size; j++) {
                if (graph[i][j]) {
                    IntVar tmp = Views.sum(nodeLabel[i], Views.minus(nodeLabel[j]));
                    this.cstrs[c++] = new Absolute(edgeLabel[e++],tmp,solver);
                }
            }
        }
        assert e == edges: e+":"+edges;

        this.cstrs[c++] = new AllDifferent(nodeLabel, solver, type);
        this.cstrs[c] = new AllDifferent(edgeLabel, solver, type);
    }

}
