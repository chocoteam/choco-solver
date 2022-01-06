/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_Y;
import org.chocosolver.solver.constraints.graph.basic.*;
import org.chocosolver.solver.constraints.graph.channeling.edges.*;
import org.chocosolver.solver.constraints.graph.channeling.nodes.PropNodeBoolChannel;
import org.chocosolver.solver.constraints.graph.channeling.nodes.PropNodeBoolsChannel;
import org.chocosolver.solver.constraints.graph.channeling.nodes.PropNodeSetChannel;
import org.chocosolver.solver.constraints.graph.connectivity.*;
import org.chocosolver.solver.constraints.graph.cost.trees.PropMaxDegVarTree;
import org.chocosolver.solver.constraints.graph.cost.trees.PropTreeCostSimple;
import org.chocosolver.solver.constraints.graph.cost.trees.lagrangian.PropGenericLagrDCMST;
import org.chocosolver.solver.constraints.graph.cost.tsp.PropCycleCostSimple;
import org.chocosolver.solver.constraints.graph.cost.tsp.lagrangian.PropLagrOneTree;
import org.chocosolver.solver.constraints.graph.cycles.PropAcyclic;
import org.chocosolver.solver.constraints.graph.cycles.PropCycle;
import org.chocosolver.solver.constraints.graph.degree.PropNodeDegreeAtLeastIncr;
import org.chocosolver.solver.constraints.graph.degree.PropNodeDegreeAtMostIncr;
import org.chocosolver.solver.constraints.graph.degree.PropNodeDegreeVar;
import org.chocosolver.solver.constraints.graph.inclusion.PropInclusion;
import org.chocosolver.solver.constraints.graph.symmbreaking.PropIncrementalAdjacencyMatrix;
import org.chocosolver.solver.constraints.graph.symmbreaking.PropIncrementalAdjacencyUndirectedMatrix;
import org.chocosolver.solver.constraints.graph.symmbreaking.PropSymmetryBreaking;
import org.chocosolver.solver.constraints.graph.symmbreaking.PropSymmetryBreakingEx;
import org.chocosolver.solver.constraints.graph.tree.PropArborescence;
import org.chocosolver.solver.constraints.graph.tree.PropArborescences;
import org.chocosolver.solver.constraints.graph.tree.PropReachability;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.objects.graphs.Orientation;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Some usual graph constraints
 *
 * @author Jean-Guillaume Fages
 */
public interface IGraphConstraintFactory extends ISelf<Model> {


    //***********************************************************************************
    // BASIC CONSTRAINTS
    //***********************************************************************************

    // counting

    /**
     * Create a constraint to force the number of nodes in g to be equal to nb
     *
     * @param g  a graph variable
     * @param nb an integer variable indicating the expected number of nodes in g
     * @return A constraint to force the number of nodes in g to be equal to nb
     */
    default Constraint nbNodes(GraphVar g, IntVar nb) {
        return new Constraint("nbNodes", new PropNbNodes(g, nb));
    }

    /**
     * Create a constraint to force the number of edges in g to be equal to nb
     *
     * @param g  a graph variable
     * @param nb an integer variable indicating the expected number of edges in g
     * @return A constraint to force the number of edges in g to be equal to nb
     */
    default Constraint nbEdges(GraphVar g, IntVar nb) {
        return new Constraint("nbEdges", new PropNbEdges(g, nb));
    }

    // loops

    /**
     * Create a constraint which ensures that 'loops' denotes the set
     * of vertices in g which have a loop, i.e. an edge of the form f(i,i)
     * i.e. vertex i in g => edge (i,i) in g
     *
     * @param g a graph variable
     * @return A constraint which makes sure every node has a loop
     */
    default Constraint loopSet(GraphVar g, SetVar loops) {
        return new Constraint("loopSet", new PropLoopSet(g, loops));
    }

    /**
     * Create a constraint which ensures g has nb loops
     * |(i,i) in g| = nb
     *
     * @param g  a graph variable
     * @param nb an integer variable counting the number of loops in g
     * @return A constraint which ensures g has nb loops
     */
    default Constraint nbLoops(GraphVar g, IntVar nb) {
        return new Constraint("nbLoops", new PropNbLoops(g, nb));
    }


    //***********************************************************************************
    // SIMPLE PROPERTY CONSTRAINTS
    //***********************************************************************************


    // symmetry

    /**
     * Creates a constraint which ensures that g is a symmetric directed graph
     * This means (i,j) in g <=> (j,i) in g
     * Note that it may be preferable to use an undirected graph variable instead!
     *
     * @param g a directed graph variable
     * @return A constraint which ensures that g is a symmetric directed graph
     */
    default Constraint symmetric(DirectedGraphVar g) {
        return new Constraint("symmetric", new PropSymmetric(g));
    }

    /**
     * Creates a constraint which ensures that g is an antisymmetric directed graph
     * This means (i,j) in g => (j,i) notin g
     *
     * @param g a directed graph variable
     * @return A constraint which ensures that g is an antisymmetric directed graph
     */
    default Constraint antisymmetric(DirectedGraphVar g) {
        return new Constraint("antisymmetric", new PropAntiSymmetric(g));
    }

    // Transitivity

    /**
     * Create a transitivity constraint
     * (i,j) in g and (j,k) in g => (i,k) in g
     * Does not consider loops
     * Enables to make cliques
     *
     * @param g A graph variable
     * @return A transitivity constraint
     */
    default Constraint transitivity(GraphVar g) {
        return new Constraint("transitivity", new PropTransitivity(g));
    }

    //***********************************************************************************
    // INCLUSION CONSTRAINTS
    //***********************************************************************************

    /**
     * Create an inclusion constraint between g1 and g2 such that
     * g1 is a subgraph of g2
     * Note that node are labelled with their indexes :
     * the vertex 0 in g1 corresponds to the vertex 0 in g2
     *
     * @param g1 An undirected graph variable
     * @param g2 An undirected graph variable
     * @return a constraint which ensures that g1 is a subgraph of g2
     */
    default Constraint subgraph(UndirectedGraphVar g1, UndirectedGraphVar g2) {
        return new Constraint("subgraph", new PropInclusion(g1, g2));
    }

    /**
     * Create an inclusion constraint between g1 and g2 such that
     * g1 is a subgraph of g2
     * Note that node are labelled with their indexes :
     * the vertex 0 in g1 corresponds to the vertex 0 in g2
     *
     * @param g1 A directed graph variable
     * @param g2 A directed graph variable
     * @return a constraint which ensures that g1 is a subGraph of g2
     */
    default Constraint subgraph(DirectedGraphVar g1, DirectedGraphVar g2) {
        return new Constraint("subgraph", new PropInclusion(g1, g2));
    }


    //***********************************************************************************
    // CHANNELING CONSTRAINTS
    //***********************************************************************************

    // Vertices

    /**
     * Channeling constraint :
     * int i in nodes <=> vertex i in g
     *
     * @param g
     * @param nodes
     */
    default Constraint nodesChanneling(GraphVar g, SetVar nodes) {
        return new Constraint("nodesSetChanneling",
                new PropNodeSetChannel(nodes, g));
    }

    /**
     * Channeling constraint :
     * nodes[i] = 1 <=> vertex i in g
     *
     * @param g
     * @param nodes
     */
    default Constraint nodesChanneling(GraphVar g, BoolVar[] nodes) {
        return new Constraint("nodesBoolsChanneling",
                new PropNodeBoolsChannel(nodes, g));
    }

    /**
     * Channeling constraint :
     * isIn = 1 <=> vertex 'vertex' in g
     *
     * @param g
     * @param isIn
     * @param vertex
     */
    default Constraint nodeChanneling(GraphVar g, BoolVar isIn, int vertex) {
        return new Constraint("nodesBoolChanneling",
                new PropNodeBoolChannel(isIn, vertex, g));
    }

    // Directed edges

    /**
     * Channeling constraint :
     * isEdge = 1 <=> edge (from,to) in g
     *
     * @param g
     * @param isEdge
     * @param from
     * @param to
     */
    default Constraint edgeChanneling(DirectedGraphVar g, BoolVar isEdge, int from, int to) {
        return new Constraint("edgeChanneling",
                new PropEdgeBoolChannel(isEdge, from, to, g));
    }

    // Edge

    /**
     * Channeling constraint:
     * isEdge = 1 <=> edge (i,j) in g
     *
     * @param g
     * @param isEdge
     * @param i
     * @param j
     */
    default Constraint edgeChanneling(UndirectedGraphVar g, BoolVar isEdge, int i, int j) {
        return new Constraint("edgeChanneling",
                new PropEdgeBoolChannel(isEdge, i, j, g));
    }

    // Neighbors

    /**
     * Channeling constraint:
     * int j in neighbors[i] <=> edge (i,j) in g
     *
     * @param g
     * @param neighbors
     */
    default Constraint neighborsChanneling(UndirectedGraphVar g, SetVar[] neighbors) {
        return new Constraint("neighSetsChanneling",
                new PropNeighSetsChannel1(neighbors, g), new PropNeighSetsChannel2(neighbors, g));

    }

    /**
     * Channeling constraint:
     * neighbors[i][j] = 1 <=> edge (i,j) in g
     *
     * @param g
     * @param neighbors
     */
    default Constraint neighborsChanneling(UndirectedGraphVar g, BoolVar[][] neighbors) {
        return new Constraint("neighBoolsChanneling",
                new PropNeighBoolsChannel1(neighbors, g), new PropNeighBoolsChannel2(neighbors, g));
    }

    /**
     * Channeling constraint:
     * int j in neighborsOf <=> edge (node,j) in g
     *
     * @param g
     * @param neighborsOf
     * @param node
     */
    default Constraint neighborsChanneling(UndirectedGraphVar g, SetVar neighborsOf, int node) {
        return new Constraint("neighSetChanneling",
                new PropNeighSetChannel(neighborsOf, node, g, new IncidentSet.SuccessorsSet()));
    }

    /**
     * Channeling constraint:
     * neighborsOf[j] = 1 <=> edge (node,j) in g
     *
     * @param g
     * @param neighborsOf
     * @param node
     */
    default Constraint neighborsChanneling(UndirectedGraphVar g, BoolVar[] neighborsOf, int node) {
        return new Constraint("neighBoolChanneling",
                new PropNeighBoolChannel(neighborsOf, node, g, new IncidentSet.SuccessorsSet()));
    }

    // Successors

    /**
     * Channeling constraint:
     * int j in successors[i] <=> edge (i,j) in g
     *
     * @param g
     * @param successors
     */
    default Constraint successorsChanneling(DirectedGraphVar g, SetVar[] successors) {
        return new Constraint("succSetsChanneling",
                new PropNeighSetsChannel1(successors, g), new PropNeighSetsChannel2(successors, g));
    }

    /**
     * Channeling constraint:
     * successors[i][j] <=> edge (i,j) in g
     *
     * @param g
     * @param successors
     */
    default Constraint successorsChanneling(DirectedGraphVar g, BoolVar[][] successors) {
        return new Constraint("succBoolsChanneling",
                new PropNeighBoolsChannel1(successors, g), new PropNeighBoolsChannel2(successors, g));
    }

    /**
     * Channeling constraint:
     * int j in successorsOf <=> edge (node,j) in g
     *
     * @param g
     * @param successorsOf
     * @param node
     */
    default Constraint successorsChanneling(DirectedGraphVar g, SetVar successorsOf, int node) {
        return new Constraint("succSetChanneling",
                new PropNeighSetChannel(successorsOf, node, g, new IncidentSet.SuccessorsSet()));
    }

    /**
     * Channeling constraint:
     * successorsOf[j] = 1 <=> edge (node,j) in g
     *
     * @param g
     * @param successorsOf
     * @param node
     */
    default Constraint successorsChanneling(DirectedGraphVar g, BoolVar[] successorsOf, int node) {
        return new Constraint("succBoolChanneling",
                new PropNeighBoolChannel(successorsOf, node, g, new IncidentSet.SuccessorsSet()));
    }

    // Predecessors

    /**
     * Channeling constraint:
     * int j in predecessorsOf <=> edge (j,node) in g
     *
     * @param g
     * @param predecessorsOf
     * @param node
     */
    default Constraint predecessorsChanneling(DirectedGraphVar g, SetVar predecessorsOf, int node) {
        return new Constraint("predSetChanneling",
                new PropNeighSetChannel(predecessorsOf, node, g, new IncidentSet.PredecessorsSet()));
    }

    /**
     * Channeling constraint:
     * predecessorsOf[j] = 1 <=> edge (j,node) in g
     *
     * @param g
     * @param predecessorsOf
     * @param node
     */
    default Constraint predecessorsChanneling(DirectedGraphVar g, BoolVar[] predecessorsOf, int node) {
        return new Constraint("predBoolChanneling",
                new PropNeighBoolChannel(predecessorsOf, node, g, new IncidentSet.PredecessorsSet()));

    }


    //***********************************************************************************
    // DEGREE CONSTRAINTS
    //***********************************************************************************

    // degrees

    /**
     * Minimum degree constraint
     * for any vertex i in g, |(i,j)| >= minDegree
     * This constraint only holds on vertices that are mandatory
     *
     * @param g         undirected graph var
     * @param minDegree integer minimum degree of every node
     * @return a minimum degree constraint
     */
    default Constraint minDegree(UndirectedGraphVar g, int minDegree) {
        return new Constraint("minDegree", new PropNodeDegreeAtLeastIncr(g, minDegree));
    }

    /**
     * Minimum degree constraint
     * for any vertex i in g, |(i,j)| >= minDegree[i]
     * This constraint only holds on vertices that are mandatory
     *
     * @param g          undirected graph var
     * @param minDegrees integer array giving the minimum degree of each node
     * @return a minimum degree constraint
     */
    default Constraint minDegrees(UndirectedGraphVar g, int[] minDegrees) {
        return new Constraint("minDegrees", new PropNodeDegreeAtLeastIncr(g, minDegrees));
    }

    /**
     * Maximum degree constraint
     * for any vertex i in g, |(i,j)| <= maxDegree
     * This constraint only holds on vertices that are mandatory
     *
     * @param g         undirected graph var
     * @param maxDegree integer maximum degree
     * @return a maximum degree constraint
     */
    default Constraint maxDegree(UndirectedGraphVar g, int maxDegree) {
        return new Constraint("maxDegree", new PropNodeDegreeAtMostIncr(g, maxDegree));
    }

    /**
     * Maximum degree constraint
     * for any vertex i in g, |(i,j)| <= maxDegrees[i]
     * This constraint only holds on vertices that are mandatory
     *
     * @param g          undirected graph var
     * @param maxDegrees integer array giving the maximum degree of each node
     * @return a maximum degree constraint
     */
    default Constraint maxDegrees(UndirectedGraphVar g, int[] maxDegrees) {
        return new Constraint("maxDegrees", new PropNodeDegreeAtMostIncr(g, maxDegrees));
    }

    /**
     * Degrees constraint
     * for any vertex i in g, |(i,j)| = degrees[i]
     * A vertex which has been removed has a degree equal to 0
     * ENSURES EVERY VERTEX i FOR WHICH DEGREE[i]>0 IS MANDATORY
     *
     * @param g       undirected graph var
     * @param degrees integer array giving the degree of each node
     * @return a degree constraint
     */
    default Constraint degrees(UndirectedGraphVar g, IntVar[] degrees) {
        return new Constraint("degrees", new PropNodeDegreeVar(g, degrees));
    }

    // inDegrees

    /**
     * Minimum inner degree constraint
     * for any vertex i in g, |(j,i)| >= minDegree
     * This constraint only holds on vertices that are mandatory
     *
     * @param g         directed graph var
     * @param minDegree integer minimum degree of every node
     * @return a minimum inner degree constraint
     */
    default Constraint minInDegree(DirectedGraphVar g, int minDegree) {
        return new Constraint("minInDegree", new PropNodeDegreeAtLeastIncr(g, Orientation.PREDECESSORS, minDegree));
    }

    /**
     * Minimum inner degree constraint
     * for any vertex i in g, |(j,i)| >= minDegree[i]
     * This constraint only holds on vertices that are mandatory
     *
     * @param g          directed graph var
     * @param minDegrees integer array giving the minimum degree of each node
     * @return a minimum inner degree constraint
     */
    default Constraint minInDegrees(DirectedGraphVar g, int[] minDegrees) {
        return new Constraint("minInDegrees", new PropNodeDegreeAtLeastIncr(g, Orientation.PREDECESSORS, minDegrees));
    }

    /**
     * Maximum inner degree constraint
     * for any vertex i in g, |(j,i)| <= maxDegree
     * This constraint only holds on vertices that are mandatory
     *
     * @param g         directed graph var
     * @param maxDegree integer maximum degree
     * @return a maximum inner degree constraint
     */
    default Constraint maxInDegree(DirectedGraphVar g, int maxDegree) {
        return new Constraint("maxInDegree", new PropNodeDegreeAtMostIncr(g, Orientation.PREDECESSORS, maxDegree));
    }

    /**
     * Maximum inner degree constraint
     * for any vertex i in g, |(j,i)| <= maxDegrees[i]
     * This constraint only holds on vertices that are mandatory
     *
     * @param g          directed graph var
     * @param maxDegrees integer array giving the maximum degree of each node
     * @return a maximum inner degree constraint
     */
    default Constraint maxInDegrees(DirectedGraphVar g, int[] maxDegrees) {
        return new Constraint("maxInDegrees", new PropNodeDegreeAtMostIncr(g, Orientation.PREDECESSORS, maxDegrees));
    }

    /**
     * Degree inner constraint
     * for any vertex i in g, |(j,i)| = degrees[i]
     * A vertex which has been removed has a degree equal to 0
     * ENSURES EVERY VERTEX i FOR WHICH DEGREE[i]>0 IS MANDATORY
     *
     * @param g       directed graph var
     * @param degrees integer array giving the degree of each node
     * @return a degree inner constraint
     */
    default Constraint inDegrees(DirectedGraphVar g, IntVar[] degrees) {
        return new Constraint("inDegrees", new PropNodeDegreeVar(g, Orientation.PREDECESSORS, degrees));
    }

    // out-degrees

    /**
     * Minimum outer degree constraint
     * for any vertex i in g, |(i,j)| >= minDegree
     * This constraint only holds on vertices that are mandatory
     *
     * @param g         directed graph var
     * @param minDegree integer minimum degree of every node
     * @return a minimum outer degree constraint
     */
    default Constraint minOutDegree(DirectedGraphVar g, int minDegree) {
        return new Constraint("minOutDegrees", new PropNodeDegreeAtLeastIncr(g, Orientation.SUCCESSORS, minDegree));
    }

    /**
     * Minimum outer degree constraint
     * for any vertex i in g, |(i,j)| >= minDegree[i]
     * This constraint only holds on vertices that are mandatory
     *
     * @param g          directed graph var
     * @param minDegrees integer array giving the minimum degree of each node
     * @return a minimum outer degree constraint
     */
    default Constraint minOutDegrees(DirectedGraphVar g, int[] minDegrees) {
        return new Constraint("minOutDegrees", new PropNodeDegreeAtLeastIncr(g, Orientation.SUCCESSORS, minDegrees));
    }

    /**
     * Maximum outer degree constraint
     * for any vertex i in g, |(i,j)| <= maxDegree
     * This constraint only holds on vertices that are mandatory
     *
     * @param g         directed graph var
     * @param maxDegree integer maximum degree
     * @return a maximum outer degree constraint
     */
    default Constraint maxOutDegree(DirectedGraphVar g, int maxDegree) {
        return new Constraint("maxOutDegrees", new PropNodeDegreeAtMostIncr(g, Orientation.SUCCESSORS, maxDegree));
    }

    /**
     * Maximum outer degree constraint
     * for any vertex i in g, |(i,j)| <= maxDegrees[i]
     * This constraint only holds on vertices that are mandatory
     *
     * @param g          directed graph var
     * @param maxDegrees integer array giving the maximum outer degree of each node
     * @return a outer maximum degree constraint
     */
    default Constraint maxOutDegrees(DirectedGraphVar g, int[] maxDegrees) {
        return new Constraint("maxOutDegrees", new PropNodeDegreeAtMostIncr(g, Orientation.SUCCESSORS, maxDegrees));
    }

    /**
     * Outer degree constraint
     * for any vertex i in g, |(i,j)| = degrees[i]
     * A vertex which has been removed has a degree equal to 0
     * ENSURES EVERY VERTEX i FOR WHICH DEGREE[i]>0 IS MANDATORY
     *
     * @param g       directed graph var
     * @param degrees integer array giving the degree of each node
     * @return an outer degree constraint
     */
    default Constraint outDegrees(DirectedGraphVar g, IntVar[] degrees) {
        return new Constraint("outDegrees", new PropNodeDegreeVar(g, Orientation.SUCCESSORS, degrees));
    }


    //***********************************************************************************
    // CYCLE CONSTRAINTS
    //***********************************************************************************

    /**
     * g must form a cycle
     * Empty graph is accepted
     * @param g an undirected graph variable
     * @return a cycle constraint
     */
    default Constraint cycle(UndirectedGraphVar g) {
        int m = 0;
        int n = g.getNbMaxNodes();
        for (int i = 0; i < n; i++) {
            m += g.getPotentialNeighborsOf(i).size();
        }
        m /= 2;
        Propagator pMaxDeg = new PropNodeDegreeAtMostIncr(g, 2);
        if (g.getMandatoryNodes().size() <= 1) {
            // Graphs with one node and a loop must be accepted
            IntVar nbNodes = g.getModel().intVar(g.getMandatoryNodes().size(), g.getPotentialNodes().size());
            g.getModel().ifThenElse(
                    g.getModel().intGeView(nbNodes, 2),
                    new Constraint("minDeg >= 2", new PropNodeDegreeAtLeastIncr(g, 2)),
                    new Constraint("minDeg >= 1", new PropNodeDegreeAtLeastIncr(g, 1))
            );
            return new Constraint("cycle",
                    new PropNbNodes(g, nbNodes),
                    pMaxDeg,
                    new PropConnected(g),
                    new PropCycle(g)
            );
        }
        return new Constraint("cycle",
                new PropNodeDegreeAtLeastIncr(g, 2),
                pMaxDeg,
                new PropConnected(g),
                new PropCycle(g)
        );
    }

    /**
     * Cycle elimination constraint
     * Prevent the graph from containing cycles
     * e.g. an edge set of the form {(i1,i2),(i2,i3),(i3,i1)}
     *
     * @param g a graph variable
     * @return A cycle elimination constraint
     */
    default Constraint noCycle(UndirectedGraphVar g) {
        return new Constraint("noCycle", new PropAcyclic(g));
    }

    /**
     * Cycle elimination constraint
     * Prevent the graph from containing circuits
     * e.g. an edge set of the form {(i1,i2),(i2,i3),(i3,i1)}
     *
     * @param g a graph variable
     * @return A cycle elimination constraint
     */
    default Constraint noCircuit(DirectedGraphVar g) {
        return new Constraint("noCycle", new PropAcyclic(g));
    }


    //***********************************************************************************
    // CONNECTIVITY CONSTRAINTS
    //***********************************************************************************

    /**
     * Creates a connectedness constraint which ensures that g is connected
     *
     * BEWARE : empty graphs or graph with 1 node are allowed (they are not disconnected...)
     * if one wants a graph with >= 2 nodes he should use the node number constraint (nbNodes)
     * connected only focuses on the graph structure to prevent two nodes not to be connected
     * if there is 0 or only 1 node, the constraint is therefore not violated
     *
     * The purpose of CP is to compose existing constraints, and nbNodes already exists
     *
     * @param g an undirected graph variable
     * @return A connectedness constraint which ensures that g is connected
     */
    default Constraint connected(UndirectedGraphVar g) {
        return new Constraint("connected", new PropConnected(g));
    }

    /**
     * Creates a connectedness constraint which ensures that g is biconnected
     * Beware : should be used in addition to connected
     * The empty graph is not considered biconnected.
     *
     * @param g an undirected graph variable
     * @return A connectedness constraint which ensures that g is biconnected
     */
    default Constraint biconnected(UndirectedGraphVar g) {
        return new Constraint("connected", new PropBiconnected(g));
    }

    /**
     * Creates a connectedness constraint which ensures that g has nb connected components
     *
     * @param g  an undirected graph variable
     * @param nb an integer variable indicating the expected number of connected components in g
     * @return A connectedness constraint which ensures that g has nb connected components
     */
    default Constraint nbConnectedComponents(UndirectedGraphVar g, IntVar nb) {
        return new Constraint("NbCC", new PropNbCC(g, nb));
    }

    /**
     * Creates a constraint which ensures that every connected component of g has a number of nodes bounded by
     * sizeMinCC and sizeMaxCC.
     *
     * @param g         an undirected graph variable.
     * @param sizeMinCC An IntVar to be equal to the smallest connected component of g.
     * @param sizeMaxCC An IntVar to be equal to the largest connected component of g.
     * @return A SizeCC constraint.
     */
    default Constraint sizeConnectedComponents(UndirectedGraphVar g, IntVar sizeMinCC, IntVar sizeMaxCC) {
        return new Constraint("SizeCC",
                new PropGreaterOrEqualX_Y(new IntVar[]{sizeMaxCC, sizeMinCC}),
                new PropSizeMinCC(g, sizeMinCC),
                new PropSizeMaxCC(g, sizeMaxCC));
    }

    /**
     * Creates a constraint which ensures that every connected component of g has a minimum number of
     * nodes equal to sizeMinCC.
     *
     * @param g         an undirected graph variable.
     * @param sizeMinCC An IntVar to be equal to the smallest connected component of g.
     * @return A SizeMinCC constraint.
     */
    default Constraint sizeMinConnectedComponents(UndirectedGraphVar g, IntVar sizeMinCC) {
        return new Constraint("SizeMinCC", new PropSizeMinCC(g, sizeMinCC));
    }

    /**
     * Creates a constraint which ensures that every connected component of g has a maximum number of
     * nodes equal to sizeMaxCC.
     *
     * @param g         an undirected graph variable
     * @param sizeMaxCC An IntVar to be equal to the largest connected component of g.
     * @return A SizeMaxCC constraint.
     */
    default Constraint sizeMaxConnectedComponents(UndirectedGraphVar g, IntVar sizeMaxCC) {
        return new Constraint("SizeMaxCC", new PropSizeMaxCC(g, sizeMaxCC));
    }

    /**
     * Creates a strong connectedness constraint which ensures that g has exactly one strongly connected component
     *
     * @param g a directed graph variable
     * @return A strong connectedness constraint which ensures that g is strongly connected
     */
    default Constraint stronglyConnected(DirectedGraphVar g) {
        return nbStronglyConnectedComponents(g, g.getModel().intVar(1));
    }

    /**
     * Creates a strong connectedness constraint which ensures that g has nb strongly connected components
     *
     * @param g  a directed graph variable
     * @param nb an integer variable indicating the expected number of connected components in g
     * @return A strong connectedness constraint which ensures that g has nb strongly connected components
     */
    default Constraint nbStronglyConnectedComponents(DirectedGraphVar g, IntVar nb) {
        return new Constraint("NbSCC", new PropNbSCC(g, nb));
    }


    //***********************************************************************************
    // TREE CONSTRAINTS
    //***********************************************************************************

    /**
     * Creates a tree constraint : g is connected and has no cycle
     *
     * @param g an undirected graph variable
     * @return a tree constraint
     */
    default Constraint tree(UndirectedGraphVar g) {
        return new Constraint("tree", new PropAcyclic(g), new PropConnected(g));
    }

    /**
     * Creates a forest constraint : g has no cycle but may have several connected components
     *
     * @param g an undirected graph variable
     * @return a forest constraint
     */
    default Constraint forest(UndirectedGraphVar g) {
        return new Constraint("forest", new PropAcyclic(g));
    }

    /**
     * Creates a directed tree constraint :
     * g forms an arborescence rooted in vertex 'root'
     * i.e. g has no circuit and a path exists from the root to every node
     *
     * @param g    a directed graph variable
     * @param root the (fixed) root of the tree
     * @return a directed tree constraint
     */
    default Constraint directedTree(DirectedGraphVar g, int root) {
        int n = g.getNbMaxNodes();
        int[] nbPreds = new int[n];
        for (int i = 0; i < n; i++) {
            nbPreds[i] = 1;
        }
        nbPreds[root] = 0;
        return new Constraint("directedTree"
                , new PropArborescence(g, root)
                , new PropNodeDegreeAtMostIncr(g, Orientation.PREDECESSORS, nbPreds)
                , new PropNodeDegreeAtLeastIncr(g, Orientation.PREDECESSORS, nbPreds)
        );
    }

    /**
     * Creates a directed forest constraint :
     * g form is composed of several disjoint (potentially singleton) arborescences
     *
     * @param g a directed graph variable
     * @return a directed forest constraint
     */
    default Constraint directedForest(DirectedGraphVar g) {
        return new Constraint("directedForest",
                new PropArborescences(g),
                new PropNodeDegreeAtMostIncr(g, Orientation.PREDECESSORS, 1)
        );
    }


    //***********************************************************************************
    // PATH and REACHABILITY
    //***********************************************************************************

    // reachability

    /**
     * Creates a constraint which ensures that every vertex in g is reachable by a simple path from the root.
     *
     * @param g    a directed graph variable
     * @param root a vertex reaching every node
     * @return A constraint which ensures that every vertex in g is reachable by a simple path from the root
     */
    default Constraint reachability(DirectedGraphVar g, int root) {
        return new Constraint("reachability_from_" + root, new PropReachability(g, root));
    }


    //***********************************************************************************
    // CLIQUES
    //***********************************************************************************

    /**
     * partition a graph variable into nb cliques
     *
     * @param g  a graph variable
     * @param nb expected number of cliques in g
     * @return a constraint which partitions g into nb cliques
     */
    default Constraint nbCliques(UndirectedGraphVar g, IntVar nb) {
        return new Constraint("NbCliques",
                new PropTransitivity(g),
                new PropNbCC(g, nb),
                new PropNbCliques(g, nb) // redundant propagator
        );
    }


    //***********************************************************************************
    // DIAMETER
    //***********************************************************************************

    /**
     * Creates a constraint which states that d is the diameter of g
     * i.e. d is the length (number of edges) of the largest shortest path among any pair of nodes
     * This constraint implies that g is connected
     *
     * @param g an undirected graph variable
     * @param d an integer variable
     * @return a constraint which states that d is the diameter of g
     */
    default Constraint diameter(UndirectedGraphVar g, IntVar d) {
        return new Constraint("diameter",
                new PropConnected(g),
                new PropDiameter(g, d)
        );
    }

    /**
     * Creates a constraint which states that d is the diameter of g
     * i.e. d is the length (number of edges) of the largest shortest path among any pair of nodes
     * This constraint implies that g is strongly connected
     *
     * @param g a directed graph variable
     * @param d an integer variable
     * @return a constraint which states that d is the diameter of g
     */
    default Constraint diameter(DirectedGraphVar g, IntVar d) {
        return new Constraint("NbCliques",
                new PropNbSCC(g, g.getModel().intVar(1)),
                new PropDiameter(g, d)
        );
    }


    //***********************************************************************************
    // OPTIMIZATION CONSTRAINTS
    //***********************************************************************************


    /**
     * Constraint modeling the Traveling Salesman Problem
     *
     * @param graphVar   graph variable representing a Hamiltonian cycle
     * @param costVar    variable representing the cost of the cycle
     * @param edgeCosts cost matrix (should be symmetric)
     * @param lagrMode  use the Lagrangian relaxation of the tsp
     *                   described by Held and Karp
     *                   {0:no Lagrangian relaxation,
     *                   1:Lagrangian relaxation (since root node),
     *                   2:Lagrangian relaxation but wait a first solution before running it}
     * @return a tsp constraint
     */
    default Constraint tsp(UndirectedGraphVar graphVar, IntVar costVar, int[][] edgeCosts, int lagrMode) {
        Propagator[] props = ArrayUtils.append(cycle(graphVar).getPropagators(),
                new Propagator[]{new PropCycleCostSimple(graphVar, costVar, edgeCosts)});
        if (lagrMode > 0) {
            PropLagrOneTree hk = new PropLagrOneTree(graphVar, costVar, edgeCosts);
            hk.waitFirstSolution(lagrMode == 2);
            props = ArrayUtils.append(props, new Propagator[]{hk});
        }
        return new Constraint("TSP", props);
    }

    /**
     * Creates a degree-constrained minimum spanning tree constraint :
     * GRAPH is a spanning tree of cost COSTVAR and each vertex degree is constrained
     * <p>
     * BEWARE : assumes the channeling between GRAPH and DEGREES is already done
     *
     * @param graphVar      an undirected graph variable
     * @param degrees    the degree of every vertex
     * @param costVar    variable representing the cost of the mst
     * @param edgeCosts cost matrix (should be symmetric)
     * @param lagrMode  use the Lagrangian relaxation of the dcmst
     *                   {0:no Lagrangian relaxation,
     *                   1:Lagrangian relaxation (since root node),
     *                   2:Lagrangian relaxation but wait a first solution before running it}
     * @return a degree-constrained minimum spanning tree constraint
     */
    default Constraint dcmst(UndirectedGraphVar graphVar, IntVar[] degrees,
                             IntVar costVar, int[][] edgeCosts,
                             int lagrMode) {
        Propagator[] props = ArrayUtils.append(
                tree(graphVar).getPropagators()
                , new Propagator[]{
                        new PropTreeCostSimple(graphVar, costVar, edgeCosts)
                        , new PropMaxDegVarTree(graphVar, degrees)
                }
        );
        if (lagrMode > 0) {
            PropGenericLagrDCMST hk = new PropGenericLagrDCMST(graphVar, costVar, degrees, edgeCosts, lagrMode == 2);
            props = ArrayUtils.append(props, new Propagator[]{hk});
        }
        return new Constraint("dcmst", props);
    }

    //***********************************************************************************
    // SYMMETRY BREAKING CONSTRAINTS
    //***********************************************************************************

    /**
     * Post a symmetry breaking constraint. This constraint is a symmetry breaking for
     * class of directed graphs which contain a directed tree with root in node 0.
     * (All nodes must be reachable from node 0)
     * Note, that this method post this constraint directly, so it cannot be reified.
     * <p>
     * This symmetry breaking method based on paper:
     * Ulyantsev V., Zakirzyanov I., Shalyto A.
     * BFS-Based Symmetry Breaking Predicates for DFA Identification
     * //Language and Automata Theory and Applications. – Springer International Publishing, 2015. – С. 611-622.
     *
     * @param graph graph to be constrainted
     */
    default void postSymmetryBreaking(DirectedGraphVar graph) {
        Model m = ref();
        // ---------------------- variables ------------------------
        int n = graph.getNbMaxNodes();
        // t[i, j]
        BoolVar[] t = m.boolVarArray("T[]", n * n);
        // p[i]
        IntVar[] p = new IntVar[n];
        p[0] = m.intVar("P[0]", 0);
        for (int i = 1; i < n; i++) {
            p[i] = m.intVar("P[" + i + "]", 0, i - 1);
        }
        // ---------------------- constraints -----------------------
        // t[i, j] <-> G
        new Constraint("AdjacencyMatrix", new PropIncrementalAdjacencyMatrix(graph, t)).post();

        // (p[j] == i) ⇔ t[i, j] and AND(!t[k, j], 0 ≤ k < j)
        for (int i = 0; i < n - 1; i++) {
            IntVar I = m.intVar(i);
            for (int j = 1; j < n; j++) {
                BoolVar[] clause = new BoolVar[i + 1];
                clause[i] = t[i + j * n];
                for (int k = 0; k < i; k++) {
                    clause[k] = t[k + j * n].not();
                }
                Constraint c = m.and(clause);
                Constraint pij = m.arithm(p[j], "=", I);
                m.ifThen(pij, c);
                m.ifThen(c, pij);
            }
        }

        // p[i] ≤ p[i + 1]
        for (int i = 1; i < n - 1; i++) {
            m.arithm(p[i], "<=", p[i + 1]).post();
        }
    }

    /**
     * Post a symmetry breaking constraint. This constraint is a symmetry breaking for
     * class of undirected connected graphs.
     * Note, that this method post this constraint directly, so it cannot be reified.
     * <p>
     * This symmetry breaking method based on paper:
     * Ulyantsev V., Zakirzyanov I., Shalyto A.
     * BFS-Based Symmetry Breaking Predicates for DFA Identification
     * //Language and Automata Theory and Applications. – Springer International Publishing, 2015. – С. 611-622.
     *
     * @param graph graph to be constrainted
     */
    default void postSymmetryBreaking(UndirectedGraphVar graph) {
        Model m = ref();
        // ---------------------- variables ------------------------
        int n = graph.getNbMaxNodes();

        // t[i, j]
        BoolVar[] t = m.boolVarArray("T[]", n * n);

        // p[i]
        IntVar[] p = new IntVar[n];
        p[0] = m.intVar("P[0]", 0);
        for (int i = 1; i < n; i++) {
            p[i] = m.intVar("P[" + i + "]", 0, i - 1);
        }
        // ---------------------- constraints -----------------------
        // t[i, j] <-> G
        new Constraint("AdjacencyMatrix", new PropIncrementalAdjacencyUndirectedMatrix(graph, t)).post();

        // (p[j] == i) ⇔ t[i, j] and AND(!t[k, j], 0 ≤ k < j)
        for (int i = 0; i < n - 1; i++) {
            IntVar I = m.intVar(i);
            for (int j = 1; j < n; j++) {
                BoolVar[] clause = new BoolVar[i + 1];
                clause[i] = t[i + j * n];
                for (int k = 0; k < i; k++) {
                    clause[k] = t[k + j * n].not();
                }
                Constraint c = m.and(clause);
                Constraint pij = m.arithm(p[j], "=", I);
                m.ifThen(pij, c);
                m.ifThen(c, pij);
            }
        }

        // p[i] ≤ p[i + 1]
        for (int i = 1; i < n - 1; i++) {
            m.arithm(p[i], "<=", p[i + 1]).post();
        }
    }

    /**
     * Creates a symmetry breaking constraint. This constraint is a symmetry breaking for
     * class of undirected connected graphs.
     * <p>
     * This symmetry breaking method based on paper:
     * Codish M. et al.
     * Breaking Symmetries in Graph Representation
     * //IJCAI. – 2013. – С. 3-9.
     *
     * @param graph graph to be constrainted
     */
    default Constraint symmetryBreaking2(UndirectedGraphVar graph) {
        int n = graph.getNbMaxNodes();
        BoolVar[] t = ref().boolVarArray("T[]", n * n);
        return new Constraint("symmBreak",
                new PropIncrementalAdjacencyUndirectedMatrix(graph, t),
                new PropSymmetryBreaking(t)
        );
    }

    /**
     * Creates a symmetry breaking constraint. This constraint is a symmetry breaking for
     * class of undirected connected graphs.
     * <p>
     * This symmetry breaking method based on paper:
     * Codish M. et al.
     * Breaking Symmetries in Graph Representation
     * //IJCAI. – 2013. – С. 3-9.
     *
     * @param graph graph to be constrainted
     */
    default Constraint symmetryBreaking3(UndirectedGraphVar graph) {
        int n = graph.getNbMaxNodes();
        BoolVar[] t = ref().boolVarArray("T[]", n * n);
        return new Constraint("symmBreakEx",
                new PropIncrementalAdjacencyUndirectedMatrix(graph, t),
                new PropSymmetryBreakingEx(t)
        );
    }
}
