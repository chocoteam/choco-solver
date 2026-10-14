---
title: "Constraints over graph variables"
date: 2020-01-07T16:07:45+01:00
weight: 26
math: "true"
description: >
  Overview of constraints on graph variables.
---

Graph variables represent directed or undirected graphs. They are useful for modeling problems involving networks, routes, connectivity, and structural properties. This section presents the main graph constraints available in Choco-solver.

## Creating graph variables

The domain of a graph variable is defined by two graphs:
the **kernel** (lower bound), whose nodes and edges belong to every solution,
and the **envelope** (upper bound), which contains every node and edge that may belong to a solution.
Bounds are built with `GraphFactory` (`create_undirected_graph` and its variants in Python),
then the variable with `graphVar` (undirected) or `digraphVar` (directed):

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "creation" >}}
{{< /tabpane >}}

`nodeInducedGraphVar` and `nodeInducedDigraphVar` create variables whose values are *node-induced* subgraphs of the envelope:
any two nodes of the solution that are connected in the envelope are also connected in the solution.

In the following examples, `g` is an undirected graph variable and `dg` a directed one,
both on 5 nodes, with an empty kernel and the complete graph as envelope.

## Counting nodes, edges and loops

`nbNodes` and `nbEdges` constrain the number of nodes and edges of a graph:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "nbNodes" >}}
{{< /tabpane >}}

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "nbEdges" >}}
{{< /tabpane >}}

`loopSet` and `nbLoops` deal with loops, i.e. edges $(i,i)$.
Note that the complete graph built by `GraphFactory` has no loop:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "loopsEnvelope" >}}
{{< /tabpane >}}

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "loops" >}}
{{< /tabpane >}}

## Structural constraints

### Symmetry and antisymmetry

`symmetric` ensures that if the edge $(i,j)$ is in a directed graph, then $(j,i)$ is too:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "symmetry" >}}
{{< /tabpane >}}

`antisymmetric` ensures that no edge is in the graph in both directions:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "antisymmetry" >}}
{{< /tabpane >}}

### Transitivity

`transitivity` ensures that if $(i,j)$ and $(j,k)$ are in the graph, then $(i,k)$ is too (loops are not considered).
It can be used to build cliques:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "transitivity" >}}
{{< /tabpane >}}

### Subgraph

`subgraph` ensures that a graph is a subgraph of another one.
Nodes are identified by their index: node 0 of `g1` is node 0 of `g2`.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "subgraph" >}}
{{< /tabpane >}}

## Channeling constraints

### Nodes

`nodesChanneling` links the nodes of a graph to a set variable (or to an array of boolean variables),
and `nodeChanneling` links the presence of one node to a boolean variable:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "nodesChanneling" >}}
{{< /tabpane >}}

### Edges

`edgeChanneling` links the presence of one edge to a boolean variable:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "edgeChanneling" >}}
{{< /tabpane >}}

### Neighbors, successors and predecessors

`neighborsChanneling` links each node of an undirected graph to the set of its neighbors
(set variables, or a matrix of boolean variables; a variant targets a single node):

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "neighborsChanneling" >}}
{{< /tabpane >}}

For directed graphs, `successorsChanneling` and `predecessorsChanneling` play the same role:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "successorsChanneling" >}}
{{< /tabpane >}}

## Degree constraints

`minDegree` and `maxDegree` bound the degree of every node of an undirected graph
(`minDegrees` and `maxDegrees` take one bound per node).
They only hold on the nodes that belong to the graph:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "minMaxDegree" >}}
{{< /tabpane >}}

`degrees` links the degree of each node to an integer variable.
A node that is not in the graph has degree 0, and a node with a positive degree is in the graph:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "degrees" >}}
{{< /tabpane >}}

For directed graphs, the same constraints exist for in-degrees and out-degrees
(`minInDegree`, `maxInDegree`, `inDegrees`, `minOutDegree`, `maxOutDegree`, `outDegrees`, ...):

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "inOutDegrees" >}}
{{< /tabpane >}}

## Connectivity constraints

### Connected components

`nbConnectedComponents` constrains the number of connected components of an undirected graph,
and `sizeConnectedComponents` bounds the number of nodes of each of them
(see also `sizeMinConnectedComponents` and `sizeMaxConnectedComponents`):

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "connectedComponents" >}}
{{< /tabpane >}}

### Connectedness

`connected` ensures that an undirected graph is connected, and `biconnected` that it remains so
when any single node is removed (use it together with `connected`).
An empty graph, or a graph with a single node, is considered connected:
use `nbNodes` to require more nodes.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "connected" >}}
{{< /tabpane >}}

### Directed graphs

`stronglyConnected` ensures that a directed graph has exactly one strongly connected component:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "stronglyConnected" >}}
{{< /tabpane >}}

`nbStronglyConnectedComponents` constrains the number of strongly connected components:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "nbStronglyConnectedComponents" >}}
{{< /tabpane >}}

`reachability` ensures that every node of the graph is reachable from a given root:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "reachability" >}}
{{< /tabpane >}}

## Cycles

`noCycle` prevents an undirected graph from containing a cycle,
and `noCircuit` prevents a directed graph from containing a circuit:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "acyclic" >}}
{{< /tabpane >}}

`cycle` ensures that an undirected graph forms a cycle (the empty graph is accepted).
Combined with a constraint on the number of nodes, it forces a Hamiltonian cycle:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "cycle" >}}
{{< /tabpane >}}

## Trees

`tree` ensures that an undirected graph is connected and has no cycle:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "tree" >}}
{{< /tabpane >}}

`forest` ensures that it has no cycle, but possibly several connected components:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "forest" >}}
{{< /tabpane >}}

For directed graphs, `directedTree` ensures that the graph is an arborescence rooted in a given node
(no circuit and a path from the root to every node):

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "directedTree" >}}
{{< /tabpane >}}

and `directedForest` that it is made of disjoint, possibly singleton, arborescences:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "directedForest" >}}
{{< /tabpane >}}

## Cliques and diameter

`nbCliques` partitions an undirected graph into a given number of cliques:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "nbCliques" >}}
{{< /tabpane >}}

`diameter` states that a variable is the diameter of the graph, i.e. the number of edges of its longest shortest path.
It implies that the graph is connected (strongly connected for a directed graph):

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "diameter" >}}
{{< /tabpane >}}

## Optimization constraints

### Traveling Salesman Problem

`tsp` states that an undirected graph is a Hamiltonian cycle of a given cost.
The last parameter enables the Lagrangian relaxation of Held and Karp
(0: disabled, 1: enabled from the root node, 2: enabled after the first solution):

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "tsp" >}}
{{< /tabpane >}}

### Degree-constrained minimum spanning tree

`dcmst` states that an undirected graph is a spanning tree of a given cost, whose node degrees are constrained.
It assumes that the degree variables are already linked to the graph, e.g. with `degrees`:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "dcmst" >}}
{{< /tabpane >}}

`tsp` and `dcmst` are not yet available in pychoco.

## Views on graph variables

Views give access to parts of a graph variable as set variables:
its nodes, and the neighbors (undirected) or the successors and predecessors (directed) of a node:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/GraphConstraints" "views" >}}
{{< /tabpane >}}

Other views build graph variables from graph variables:
`nodeInducedSubgraphView`, `edgeInducedSubgraphView` and `graphUnionView`.
