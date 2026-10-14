/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.docs.modeling;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Examples shown in the user documentation, page "Modeling / Constraints over graph variables"
 * (docs/content/docs/Modeling/GraphConstraints.md).
 * The regions between {@code tag::name[]} and {@code end::name[]} are included in the page.
 * Except in the first example, {@code g} is an undirected graph variable and {@code dg} a directed one,
 * both on 5 nodes, with an empty kernel and the complete graph as envelope.
 *
 * @author Charles Prud'homme
 */
public class GraphConstraintsExamples {

    private static final int N = 5;

    private static UndirectedGraphVar undirected(Model model) {
        UndirectedGraph lb = GraphFactory.makeStoredUndirectedGraph(model, N, SetType.BITSET, SetType.BITSET);
        UndirectedGraph ub = GraphFactory.makeCompleteStoredUndirectedGraph(model, N, SetType.BITSET, SetType.BITSET, false);
        return model.graphVar("g", lb, ub);
    }

    private static DirectedGraphVar directed(Model model) {
        DirectedGraph lb = GraphFactory.makeStoredDirectedGraph(model, N, SetType.BITSET, SetType.BITSET);
        DirectedGraph ub = GraphFactory.makeCompleteStoredDirectedGraph(model, N, SetType.BITSET, SetType.BITSET, false);
        return model.digraphVar("dg", lb, ub);
    }

    @Test(groups = "1s")
    public void creation() {
        Model model = new Model();
        // tag::creation[]
        // Kernel (lower bound): node 0 must belong to the graph
        UndirectedGraph lb = GraphFactory.makeStoredUndirectedGraph(model, 5, SetType.BITSET, SetType.BITSET,
                new int[]{0}, new int[][]{});
        // Envelope (upper bound): the nodes and edges that may belong to the graph
        UndirectedGraph ub = GraphFactory.makeStoredUndirectedGraph(model, 5, SetType.BITSET, SetType.BITSET,
                new int[]{0, 1, 2, 3, 4}, new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 0}});
        UndirectedGraphVar g = model.graphVar("g", lb, ub);

        // A directed graph variable, whose envelope is the complete directed graph on 5 nodes
        DirectedGraph dlb = GraphFactory.makeStoredDirectedGraph(model, 5, SetType.BITSET, SetType.BITSET);
        DirectedGraph dub = GraphFactory.makeCompleteStoredDirectedGraph(model, 5, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar dg = model.digraphVar("dg", dlb, dub);
        // end::creation[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertTrue(g.getValue().getNodes().contains(0));
        Assert.assertTrue(dg.isInstantiated());
    }

    @Test(groups = "1s")
    public void nbNodes() {
        Model model = new Model();
        UndirectedGraphVar g = undirected(model);
        // tag::nbNodes[]
        IntVar nbNodes = model.intVar("nbNodes", 2, 3);
        model.nbNodes(g, nbNodes).post();
        // end::nbNodes[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(g.getValue().getNodes().size(), nbNodes.getValue());
    }

    @Test(groups = "1s")
    public void nbEdges() {
        Model model = new Model();
        UndirectedGraphVar g = undirected(model);
        // tag::nbEdges[]
        IntVar nbEdges = model.intVar("nbEdges", 3, 4);
        model.nbEdges(g, nbEdges).post();
        // end::nbEdges[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void loops() {
        Model model = new Model();
        DirectedGraph lb = GraphFactory.makeStoredDirectedGraph(model, N, SetType.BITSET, SetType.BITSET);
        DirectedGraph ub = GraphFactory.makeCompleteStoredDirectedGraph(model, N, SetType.BITSET, SetType.BITSET, false);
        // tag::loopsEnvelope[]
        // the complete graph has no loop: add them to the envelope
        for (int i = 0; i < 5; i++) {
            ub.addEdge(i, i);
        }
        // end::loopsEnvelope[]
        DirectedGraphVar dg = model.digraphVar("dg", lb, ub);
        // tag::loops[]
        // loops is the set of nodes i such that (i, i) is in dg
        SetVar loops = model.setVar("loops", new int[]{}, new int[]{0, 1, 2, 3, 4});
        model.loopSet(dg, loops).post();
        // nbLoops is the number of such nodes
        IntVar nbLoops = model.intVar("nbLoops", 1, 2);
        model.nbLoops(dg, nbLoops).post();
        // end::loops[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(loops.getValue().size(), nbLoops.getValue());
    }

    @Test(groups = "1s")
    public void symmetry() {
        Model model = new Model();
        DirectedGraphVar dg = directed(model);
        // tag::symmetry[]
        model.symmetric(dg).post();       // (i, j) in dg => (j, i) in dg
        // end::symmetry[]
        Assert.assertTrue(model.getSolver().solve());

        model = new Model();
        dg = directed(model);
        // tag::antisymmetry[]
        model.antisymmetric(dg).post();   // (i, j) in dg => (j, i) not in dg
        // end::antisymmetry[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void transitivity() {
        Model model = new Model();
        DirectedGraphVar dg = directed(model);
        // tag::transitivity[]
        model.transitivity(dg).post();    // (i, j) and (j, k) in dg => (i, k) in dg
        // end::transitivity[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void subgraph() {
        Model model = new Model();
        UndirectedGraph lb = GraphFactory.makeStoredUndirectedGraph(model, N, SetType.BITSET, SetType.BITSET);
        UndirectedGraph ub = GraphFactory.makeCompleteStoredUndirectedGraph(model, N, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraph lb2 = GraphFactory.makeStoredUndirectedGraph(model, N, SetType.BITSET, SetType.BITSET);
        UndirectedGraph ub2 = GraphFactory.makeCompleteStoredUndirectedGraph(model, N, SetType.BITSET, SetType.BITSET, false);
        // tag::subgraph[]
        UndirectedGraphVar g1 = model.graphVar("g1", lb, ub);
        UndirectedGraphVar g2 = model.graphVar("g2", lb2, ub2);
        model.subgraph(g1, g2).post();    // every node and edge of g1 is in g2
        // end::subgraph[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void nodesChanneling() {
        Model model = new Model();
        UndirectedGraphVar g = undirected(model);
        // tag::nodesChanneling[]
        // i in nodes <=> node i in g
        SetVar nodes = model.setVar("nodes", new int[]{}, new int[]{0, 1, 2, 3, 4});
        model.nodesChanneling(g, nodes).post();
        // isIn2 = 1 <=> node 2 in g
        BoolVar isIn2 = model.boolVar("isIn2");
        model.nodeChanneling(g, isIn2, 2).post();
        // end::nodesChanneling[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(nodes.getValue().contains(2), isIn2.getValue() == 1);
    }

    @Test(groups = "1s")
    public void edgeChanneling() {
        Model model = new Model();
        UndirectedGraphVar g = undirected(model);
        // tag::edgeChanneling[]
        // isEdge = 1 <=> edge (0, 1) in g
        BoolVar isEdge = model.boolVar("isEdge");
        model.edgeChanneling(g, isEdge, 0, 1).post();
        // end::edgeChanneling[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void neighborsChanneling() {
        Model model = new Model();
        UndirectedGraphVar g = undirected(model);
        // tag::neighborsChanneling[]
        // j in neighbors[i] <=> edge (i, j) in g
        SetVar[] neighbors = model.setVarArray("neighbors", 5, new int[]{}, new int[]{0, 1, 2, 3, 4});
        model.neighborsChanneling(g, neighbors).post();
        // end::neighborsChanneling[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void successorsChanneling() {
        Model model = new Model();
        DirectedGraphVar dg = directed(model);
        // tag::successorsChanneling[]
        // j in successors[i] <=> edge (i, j) in dg
        SetVar[] successors = model.setVarArray("successors", 5, new int[]{}, new int[]{0, 1, 2, 3, 4});
        model.successorsChanneling(dg, successors).post();
        // j in predecessorsOf2 <=> edge (j, 2) in dg
        SetVar predecessorsOf2 = model.setVar("predecessorsOf2", new int[]{}, new int[]{0, 1, 2, 3, 4});
        model.predecessorsChanneling(dg, predecessorsOf2, 2).post();
        // end::successorsChanneling[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void degrees() {
        Model model = new Model();
        UndirectedGraphVar g = undirected(model);
        // tag::minMaxDegree[]
        model.minDegree(g, 2).post();     // every node of g has at least 2 neighbors
        model.maxDegree(g, 3).post();     // and at most 3
        // end::minMaxDegree[]
        // tag::degrees[]
        // degrees[i] is the degree of node i (0 if i is not in g)
        IntVar[] degrees = model.intVarArray("degrees", 5, 0, 4);
        model.degrees(g, degrees).post();
        // end::degrees[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void inOutDegrees() {
        Model model = new Model();
        DirectedGraphVar dg = directed(model);
        // tag::inOutDegrees[]
        IntVar[] inDegrees = model.intVarArray("inDegrees", 5, 0, 4);
        IntVar[] outDegrees = model.intVarArray("outDegrees", 5, 0, 4);
        model.inDegrees(dg, inDegrees).post();
        model.outDegrees(dg, outDegrees).post();
        model.minInDegree(dg, 1).post();  // every node of dg has at least 1 predecessor
        model.maxOutDegree(dg, 2).post(); // and at most 2 successors
        // end::inOutDegrees[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void connectedComponents() {
        Model model = new Model();
        UndirectedGraphVar g = undirected(model);
        // tag::connectedComponents[]
        IntVar nbCC = model.intVar("nbCC", 2);
        model.nbConnectedComponents(g, nbCC).post();
        // every connected component has between minSize and maxSize nodes
        IntVar minSize = model.intVar("minSize", 2, 5);
        IntVar maxSize = model.intVar("maxSize", 2, 5);
        model.sizeConnectedComponents(g, minSize, maxSize).post();
        // end::connectedComponents[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void connected() {
        Model model = new Model();
        UndirectedGraphVar g = undirected(model);
        // tag::connected[]
        model.nbNodes(g, model.intVar(5)).post();
        model.connected(g).post();
        model.biconnected(g).post();     // to use with connected(g)
        // end::connected[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void stronglyConnected() {
        Model model = new Model();
        DirectedGraphVar dg = directed(model);
        // tag::stronglyConnected[]
        model.stronglyConnected(dg).post();
        // end::stronglyConnected[]
        Assert.assertTrue(model.getSolver().solve());

        model = new Model();
        dg = directed(model);
        // tag::nbStronglyConnectedComponents[]
        IntVar nbSCC = model.intVar("nbSCC", 2, 3);
        model.nbStronglyConnectedComponents(dg, nbSCC).post();
        // end::nbStronglyConnectedComponents[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void reachability() {
        Model model = new Model();
        DirectedGraphVar dg = directed(model);
        // tag::reachability[]
        model.reachability(dg, 0).post(); // every node of dg is reachable from node 0
        // end::reachability[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void acyclic() {
        Model model = new Model();
        UndirectedGraphVar g = undirected(model);
        DirectedGraphVar dg = directed(model);
        // tag::acyclic[]
        model.noCycle(g).post();          // undirected: no cycle
        model.noCircuit(dg).post();       // directed: no circuit
        // end::acyclic[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void cycle() {
        Model model = new Model();
        UndirectedGraphVar g = undirected(model);
        // tag::cycle[]
        model.nbNodes(g, model.intVar(5)).post();
        model.cycle(g).post();            // with all 5 nodes, a Hamiltonian cycle
        // end::cycle[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(g.getValue().getNodes().size(), 5);
    }

    @Test(groups = "1s")
    public void trees() {
        Model model = new Model();
        UndirectedGraphVar g = undirected(model);
        // tag::tree[]
        model.tree(g).post();             // connected and without cycle
        // end::tree[]
        Assert.assertTrue(model.getSolver().solve());

        model = new Model();
        g = undirected(model);
        // tag::forest[]
        model.forest(g).post();           // without cycle, possibly several components
        // end::forest[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void directedTrees() {
        Model model = new Model();
        DirectedGraphVar dg = directed(model);
        // tag::directedTree[]
        model.directedTree(dg, 0).post(); // an arborescence rooted in node 0
        // end::directedTree[]
        Assert.assertTrue(model.getSolver().solve());

        model = new Model();
        dg = directed(model);
        // tag::directedForest[]
        model.directedForest(dg).post();  // disjoint arborescences
        // end::directedForest[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void nbCliques() {
        Model model = new Model();
        UndirectedGraphVar g = undirected(model);
        // tag::nbCliques[]
        // g is partitioned into nbCliques cliques
        IntVar nbCliques = model.intVar("nbCliques", 2);
        model.nbCliques(g, nbCliques).post();
        // end::nbCliques[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void diameter() {
        Model model = new Model();
        UndirectedGraphVar g = undirected(model);
        // tag::diameter[]
        // the longest shortest path of g has diameter edges (g is connected)
        IntVar diameter = model.intVar("diameter", 2, 3);
        model.diameter(g, diameter).post();
        // end::diameter[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void tsp() {
        Model model = new Model();
        // tag::tsp[]
        int[][] costs = {
                {0, 2, 9, 10, 7},
                {2, 0, 6, 4, 3},
                {9, 6, 0, 8, 5},
                {10, 4, 8, 0, 6},
                {7, 3, 5, 6, 0}};
        // all nodes are mandatory, all edges are possible
        UndirectedGraph lb = GraphFactory.makeStoredAllNodesUndirectedGraph(model, 5, SetType.BITSET, SetType.BITSET, true);
        UndirectedGraph ub = GraphFactory.makeCompleteStoredUndirectedGraph(model, 5, SetType.BITSET, SetType.BITSET, true);
        UndirectedGraphVar tour = model.graphVar("tour", lb, ub);
        IntVar cost = model.intVar("cost", 0, 100);
        // tour is a Hamiltonian cycle of cost 'cost'; 0: no Lagrangian relaxation
        model.tsp(tour, cost, costs, 0).post();
        model.setObjective(Model.MINIMIZE, cost);
        // end::tsp[]
        while (model.getSolver().solve()) ;
        Assert.assertEquals(model.getSolver().getBestSolutionValue().intValue(), 26);
    }

    @Test(groups = "1s")
    public void dcmst() {
        Model model = new Model();
        int[][] costs = {
                {0, 2, 9, 10, 7},
                {2, 0, 6, 4, 3},
                {9, 6, 0, 8, 5},
                {10, 4, 8, 0, 6},
                {7, 3, 5, 6, 0}};
        UndirectedGraph lb = GraphFactory.makeStoredAllNodesUndirectedGraph(model, 5, SetType.BITSET, SetType.BITSET, true);
        UndirectedGraph ub = GraphFactory.makeCompleteStoredUndirectedGraph(model, 5, SetType.BITSET, SetType.BITSET, true);
        // tag::dcmst[]
        UndirectedGraphVar tree = model.graphVar("tree", lb, ub);
        // the degree of each node is at most 2
        IntVar[] degrees = model.intVarArray("degrees", 5, 1, 2);
        model.degrees(tree, degrees).post(); // dcmst expects the degrees to be channeled
        IntVar cost = model.intVar("cost", 0, 100);
        // tree is a spanning tree of cost 'cost'; 0: no Lagrangian relaxation
        model.dcmst(tree, degrees, cost, costs, 0).post();
        model.setObjective(Model.MINIMIZE, cost);
        // end::dcmst[]
        while (model.getSolver().solve()) ;
        Assert.assertTrue(model.getSolver().getSolutionCount() > 0);
    }

    @Test(groups = "1s")
    public void views() {
        Model model = new Model();
        UndirectedGraphVar g = undirected(model);
        DirectedGraphVar dg = directed(model);
        // tag::views[]
        // the set of nodes of g
        SetVar nodes = model.graphNodeSetView(g);
        // the neighbors of node 2 in g, the successors and predecessors of node 2 in dg
        SetVar neighborsOf2 = model.graphNeighborsSetView(g, 2);
        SetVar successorsOf2 = model.graphSuccessorsSetView(dg, 2);
        SetVar predecessorsOf2 = model.graphPredecessorsSetView(dg, 2);
        // end::views[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(nodes.getValue().size(), g.getValue().getNodes().size());
    }
}
