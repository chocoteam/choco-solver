package samples.graph.dcmstp;
/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import samples.AbstractProblem;
import samples.graph.input.DCMST_Utils;
import samples.graph.output.TextWriter;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.gary.IGraphRelaxation;
import solver.constraints.gary.degree.PropNodeDegree_AtMost;
import solver.constraints.gary.trees.PropTreeCostScalar;
import solver.constraints.gary.trees.lagrangianRelaxation.PropLagr_DCMST;
import solver.objective.ObjectiveStrategy;
import solver.objective.OptimizationPolicy;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.GraphStrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.FindAndProve;
import solver.search.strategy.strategy.StrategiesSequencer;
import solver.search.strategy.strategy.graph.GraphStrategies;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphVar;
import solver.variables.graph.UndirectedGraphVar;
import util.objects.setDataStructures.SetType;

import java.io.File;

/**
 * Solves the Degree Constrained Minimum Spanning Tree Problem
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class DCMST extends AbstractProblem {

    //***********************************************************************************
    // BENCHMARK
    //***********************************************************************************

    public static void main(String[] args) {
        //DE,DR,instanciasT
        String dir = "/Users/jfages07/Desktop/work/CPAIOR13/instances";
        DCMST.TIMELIMIT = 300000;
        String suffix = "_choco";
        execute(dir + "/DR", "DR" + suffix + ".csv");
        execute(dir + "/ANDINST", "ANDINST" + suffix + ".csv");
//		execute(dir+"/DE", "DE"+suffix+".csv"); //(much harder to solver, a cutting plane propagator is advised)
    }

    public static void execute(String dir, String output) {
        TextWriter.clearFile(output);
        TextWriter.writeTextInto("instance;sols;fails;nodes;time;obj;\n", output);
        File folder = new File(dir);
        String[] list = folder.list();
        int nMin = 100;
        int nMax = 2000;
        for (String s : list) {
            File file = new File(dir + "/" + s);
            if ((!file.isHidden()) && (!s.contains("bounds.csv")) && (!s.contains("bug"))) {
                System.out.println(s);
                DCMST_Utils inst = new DCMST_Utils();
                if (inst.parse_T_DE_DR(file, nMin, nMax, dir, s)) {
                    DCMST run = new DCMST(s, inst, output);
                    run.execute();
                }
                System.gc();
            }
        }
    }

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // input
    private int n;
    private int[] dMax;
    private int[][] dist;
    private String instanceName;
    private int lb, ub, optimum;
    private String outFile;
    // model
    private IntVar totalCost;
    private UndirectedGraphVar graph;
    private IGraphRelaxation relax;
    // parameters
    public static long TIMELIMIT = 60000;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public DCMST(String name, DCMST_Utils inst, String output) {
        n = inst.n;
        dMax = inst.dMax;
        dist = inst.costs;
        instanceName = name;
        lb = inst.lb;
        ub = inst.ub;
        optimum = inst.optimum;
        outFile = output;
//		this.level = Level.QUIET;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void createSolver() {
        solver = new Solver("DCMSTP");
    }

    @Override
    public void buildModel() {
        totalCost = VariableFactory.bounded("obj", lb, ub, solver);
        graph = new UndirectedGraphVar("G", solver, n, SetType.SWAP_ARRAY, SetType.LINKED_LIST, true);
        for (int i = 0; i < n; i++) {
            graph.getKernelGraph().activateNode(i);
            for (int j = i + 1; j < n; j++) {
                if (dist[i][j] != -1 && !(dMax[i] == 1 && dMax[j] == 1)) {
                    graph.getEnvelopGraph().addEdge(i, j);
                }
            }
        }
        // tree constraint
        Constraint gc = new GraphConstraintFactory().spanning_tree(graph);
        // cost constraint
        gc.addPropagators(new PropTreeCostScalar(graph, totalCost, dist));
        // max degree constraint
        gc.addPropagators(new PropNodeDegree_AtMost(graph, dMax));
        gc.addPropagators(new PropLowDegrees(graph, dMax));
        // redundant lagrangian constraint
        relax = new PropLagr_DCMST(graph, totalCost, dMax, dist, ub != optimum);
        gc.addPropagators((Propagator) relax);
        solver.post(gc);
    }

    @Override
    public void configureSearch() {
        GraphStrategies firstSol = new GraphStrategies(graph, dist, relax);
        firstSol.configure(GraphStrategies.MIN_COST, true);
        AbstractStrategy<GraphVar> nextSol = GraphStrategyFactory.graphStrategy(graph, null, new NextSol(graph, dMax, relax), GraphStrategy.NodeArcPriority.ARCS);
        AbstractStrategy<GraphVar> strat = new FindAndProve<>(new GraphVar[]{graph}, firstSol, nextSol);
        // bottom-up optimization
        solver.set(new StrategiesSequencer(new ObjectiveStrategy(totalCost, OptimizationPolicy.BOTTOM_UP, true), strat));
        SearchMonitorFactory.limitSolution(solver, 2);
        SearchMonitorFactory.limitTime(solver, TIMELIMIT);
    }

    @Override
    public void solve() {
		for(int i=0;i<n;i++)
			if(graph.getEnvelopGraph().edgeExists(i,i))throw new UnsupportedOperationException();
        // resolution
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
        if (solver.getMeasures().getSolutionCount() == 0
                && solver.getMeasures().getTimeCount() < TIMELIMIT) {
            throw new UnsupportedOperationException();
        }
        if (solver.getMeasures().getTimeCount() < TIMELIMIT &&
			totalCost.getValue() != optimum && optimum != 100000 // when the optimum is not given in bounds.csv
        ) {
            throw new UnsupportedOperationException("wrong optimum ? " + solver.getSearchLoop().getObjectivemanager().getBestSolutionValue() + " != " + optimum);
        }
        if (solver.getMeasures().getSolutionCount() > 1
                && (ub == optimum)) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void prettyOut() {
        int bestCost = solver.getSearchLoop().getObjectivemanager().getBestSolutionValue().intValue();
        String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
                + solver.getMeasures().getNodeCount() + ";" + (int) (solver.getMeasures().getTimeCount()) + ";" + bestCost + ";\n";
        TextWriter.writeTextInto(txt, outFile);
    }
}