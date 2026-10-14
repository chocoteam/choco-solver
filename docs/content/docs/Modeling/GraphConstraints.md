---
title: "Constraints over graph variables"
date: 2020-01-07T16:07:45+01:00
weight: 26
math: "true"
description: >
  Overview of constraints on graph variables.
---

Graph variables represent directed or undirected graphs. They are useful for modeling problems involving networks, routes, connectivity, and structural properties. This section presents the main graph constraints available in Choco-solver.

## Creating Graph Variables

Graph variables have a lower bound (kernel) and an upper bound (envelope) representing the minimum and maximum edges that must or can be included:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
// Create an undirected graph on 5 nodes
UndirectedGraph lb = new UndirectedGraph(5);
UndirectedGraph ub = new UndirectedGraph(5);
ub.addEdges(new int[][]{
    {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 0}  // forms a complete edge set
});
GraphVar gv = model.undirectedGraphVar("graph", lb, ub);

// Create a directed graph
DirectedGraph dlb = new DirectedGraph(5);
DirectedGraph dub = new DirectedGraph(5);
dub.addEdges(new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 4}});
GraphVar dv = model.directedGraphVar("digraph", dlb, dub);
{{< /tab >}}
{{< /tabpane >}}

## Node and Edge Counting

### Node count

The `nbNodes` constraint relates the number of nodes to an integer variable:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
IntVar nbN = model.intVar("nbN", 1, 5);
model.nbNodes(g, nbN).post();
{{< /tab >}}
{{< /tabpane >}}

The `nodeInducedGraphVar` and `nodeInducedDigraphVar` create node-induced subgraphs (where edges exist only between selected nodes):

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar fullGraph = model.undirectedGraphVar("full", lb, ub);
GraphVar subgraph = model.nodeInducedGraphVar("sub", nlb, nub);
// subgraph will be the node-induced subgraph of fullGraph
{{< /tab >}}
{{< /tabpane >}}

### Edge count

The `nbEdges` constraint counts the number of edges:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
IntVar nbE = model.intVar("nbE", 0, 10);
model.nbEdges(g, nbE).post();
{{< /tab >}}
{{< /tabpane >}}

The `nbLoops` constraint counts self-loops (edges from a node to itself):

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
IntVar nbL = model.intVar("nbL", 0, 5);
model.nbLoops(g, nbL).post();
{{< /tab >}}
{{< /tabpane >}}

### Loop set constraint

The `loopSet` constraint retrieves the set of nodes with self-loops:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
SetVar loops = model.setVar("loops", new int[]{}, new int[]{0, 1, 2, 3, 4});
model.loopSet(g, loops).post();
{{< /tab >}}
{{< /tabpane >}}

## Structural Constraints

### Symmetry and antisymmetry

The `symmetric` constraint ensures that if edge $(i,j)$ exists, then edge $(j,i)$ also exists (making a directed graph undirected in structure):

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
model.symmetric(g).post();
{{< /tab >}}
{{< /tabpane >}}

The `antisymmetric` constraint ensures no edge exists in both directions:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
model.antisymmetric(g).post();
{{< /tab >}}
{{< /tabpane >}}

### Transitivity

The `transitivity` constraint ensures that if $(i,j)$ and $(j,k)$ exist, then $(i,k)$ must exist:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
model.transitivity(g).post();
{{< /tab >}}
{{< /tabpane >}}

### Subgraph constraint

The `subgraph` constraint ensures one graph is a subgraph of another (all edges in the first are in the second):

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g1 = model.undirectedGraphVar("g1", lb1, ub1);
GraphVar g2 = model.undirectedGraphVar("g2", lb2, ub2);
model.subgraph(g1, g2).post();  // g1 ⊆ g2
{{< /tab >}}
{{< /tabpane >}}

## Channeling Constraints

### Nodes channeling

The `nodesChanneling` constraint links nodes to a set variable:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
SetVar nodes = model.setVar("nodes", new int[]{}, new int[]{0, 1, 2, 3, 4});
model.nodesChanneling(g, nodes).post();
{{< /tab >}}
{{< /tabpane >}}

The `nodeChanneling` constraint links a specific node to an integer variable:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
IntVar n = model.intVar("node", 0, 4);
model.nodeChanneling(g, 2, n).post();  // relates node 2 to variable n
{{< /tab >}}
{{< /tabpane >}}

### Edge channeling

The `edgeChanneling` constraint relates edges to boolean variables:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
BoolVar[][] edges = new BoolVar[5][5];
// edge[i][j] is true iff edge (i,j) exists in g
model.edgeChanneling(g, edges).post();
{{< /tab >}}
{{< /tabpane >}}

### Neighbors/Successors/Predecessors channeling

The `neighborsChanneling` constraint links each node to its set of neighbors:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
SetVar[] neighbors = model.setVarArray("neighbors", 5, new int[]{}, new int[]{0, 1, 2, 3, 4});
model.neighborsChanneling(g, neighbors).post();  // neighbors[i] = neighbors of node i
{{< /tab >}}
{{< /tabpane >}}

The `successorsChanneling` and `predecessorsChanneling` constraints apply to directed graphs:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
SetVar[] successors = model.setVarArray("succ", 5, new int[]{}, new int[]{0, 1, 2, 3, 4});
model.successorsChanneling(g, successors).post();  // successors[i] = nodes reachable from i
{{< /tab >}}
{{< /tabpane >}}

## Degree Constraints

### Minimum and maximum degree

The `minDegree` and `maxDegree` constraints set bounds on node degrees:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
model.minDegree(g, 2).post();  // all nodes must have degree ≥ 2
model.maxDegree(g, 4).post();  // all nodes must have degree ≤ 4
{{< /tab >}}
{{< /tabpane >}}

The `degrees` constraint links each node to an integer variable representing its degree:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
IntVar[] degrees = model.intVarArray("deg", 5, 0, 4);
model.degrees(g, degrees).post();  // degrees[i] = degree of node i
{{< /tab >}}
{{< /tabpane >}}

### In-degree and out-degree (directed graphs)

For directed graphs:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
IntVar[] inDegrees = model.intVarArray("inDeg", 5, 0, 4);
IntVar[] outDegrees = model.intVarArray("outDeg", 5, 0, 4);
model.inDegrees(g, inDegrees).post();      // in-degree constraints
model.outDegrees(g, outDegrees).post();    // out-degree constraints
model.minInDegree(g, 1).post();            // all nodes must have in-degree ≥ 1
model.maxOutDegree(g, 3).post();           // all nodes must have out-degree ≤ 3
{{< /tab >}}
{{< /tabpane >}}

## Connectivity Constraints

### Connected components

The `nbConnectedComponents` constraint counts the number of connected components:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
IntVar nbCC = model.intVar("nbCC", 1, 5);
model.nbConnectedComponents(g, nbCC).post();
{{< /tab >}}
{{< /tabpane >}}

The `sizeConnectedComponents` constraint retrieves the size of each connected component:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
SetVar[] ccSizes = model.setVarArray("ccSize", 5, new int[]{}, new int[]{0, 1, 2, 3, 4});
model.sizeConnectedComponents(g, ccSizes).post();
{{< /tab >}}
{{< /tabpane >}}

### Full connectivity

The `connected` constraint ensures the graph is fully connected:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
model.connected(g).post();  // graph must be connected
{{< /tab >}}
{{< /tabpane >}}

The `biconnected` constraint ensures the graph is biconnected (no cut vertices):

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
model.biconnected(g).post();
{{< /tab >}}
{{< /tabpane >}}

### Directed connectivity

The `stronglyConnected` constraint ensures all nodes are mutually reachable:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
model.stronglyConnected(g).post();
{{< /tab >}}
{{< /tabpane >}}

The `nbStronglyConnectedComponents` counts strongly connected components:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
IntVar nbSCC = model.intVar("nbSCC", 1, 5);
model.nbStronglyConnectedComponents(g, nbSCC).post();
{{< /tab >}}
{{< /tabpane >}}

### Reachability

The `reachability` constraint ensures that every node is reachable from a source node:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
model.reachability(g, 0).post();  // all nodes must be reachable from node 0
{{< /tab >}}
{{< /tabpane >}}

## Cycles and Paths

### Acyclic constraints

The `noCycle` constraint ensures the graph is acyclic:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
model.noCycle(g).post();
{{< /tab >}}
{{< /tabpane >}}

The `noCircuit` constraint (undirected) ensures no cycles exist:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
model.noCircuit(g).post();
{{< /tab >}}
{{< /tabpane >}}

### Cycle existence

The `cycle` constraint forces a Hamiltonian cycle (a cycle visiting each node exactly once):

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
model.cycle(g).post();
{{< /tab >}}
{{< /tabpane >}}

## Tree Constraints

### Tree and forest

The `tree` constraint ensures the graph is a tree (connected, acyclic, with n-1 edges for n nodes):

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
model.tree(g).post();
{{< /tab >}}
{{< /tabpane >}}

The `forest` constraint ensures the graph is a forest (acyclic, can have multiple connected components):

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
model.forest(g).post();
{{< /tab >}}
{{< /tabpane >}}

### Directed trees and forests

For directed graphs:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
model.directedTree(g).post();   // a directed tree rooted at node 0
model.directedForest(g).post();  // a directed forest
{{< /tab >}}
{{< /tabpane >}}

## Advanced Structural Constraints

### Cliques

The `nbCliques` constraint counts the number of maximal cliques:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
IntVar nbC = model.intVar("nbCliques", 0, 5);
model.nbCliques(g, nbC).post();
{{< /tab >}}
{{< /tabpane >}}

### Diameter

The `diameter` constraint limits the graph diameter (maximum shortest path between any two nodes):

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);
model.diameter(g, 3).post();  // maximum diameter is 3
{{< /tab >}}
{{< /tabpane >}}

## Optimization Constraints

### Traveling Salesman Problem (TSP)

The `tsp` constraint models the Traveling Salesman Problem with edge weights:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
int[][] distances = {{0, 10, 15}, {10, 0, 35}, {15, 35, 0}};
IntVar cost = model.intVar("tspCost", 0, 1000);
model.tsp(g, distances, cost).post();
{{< /tab >}}
{{< /tabpane >}}

### Directed Cost Minimum Spanning Tree (DCMST)

The `dcmst` constraint models a minimum spanning tree for directed graphs:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.directedGraphVar("g", lb, ub);
int[][] costs = {{0, 5, 10}, {5, 0, 15}, {10, 15, 0}};
IntVar cost = model.intVar("dcmstCost", 0, 1000);
model.dcmst(g, costs, cost).post();
{{< /tab >}}
{{< /tabpane >}}

## Views on Graph Variables

Graph variables support several views for working with their components:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
GraphVar g = model.undirectedGraphVar("g", lb, ub);

// Get the set of all nodes
SetVar nodes = model.graphNodeSetView(g);

// Get the set of successors for a specific node (directed graphs)
SetVar succ = model.graphSuccessorsSetView((DirectedGraphVar) g, 2);

// Get subgraph induced by a set of nodes
SetVar nodeSet = model.setVar("nodes", new int[]{}, new int[]{0, 1, 2, 3, 4});
GraphVar subgraph = model.nodeInducedSubgraphView(g, nodeSet, false);
{{< /tab >}}
{{< /tabpane >}}
