# Examples shown in the user documentation, page "Modeling / Constraints over graph variables"
# (docs/content/docs/Modeling/GraphConstraints.md).
# The regions between "tag::name[]" and "end::name[]" are included in the page.
# Except in the first example, g is an undirected graph variable and dg a directed one,
# both on 5 nodes, with an empty kernel and the complete graph as envelope.
from pychoco import Model, create_undirected_graph, create_complete_undirected_graph, \
    create_directed_graph, create_complete_directed_graph


def undirected(model):
    return model.graphvar(create_undirected_graph(model, 5), create_complete_undirected_graph(model, 5), "g")


def directed(model):
    return model.digraphvar(create_directed_graph(model, 5), create_complete_directed_graph(model, 5), "dg")


def test_creation():
    model = Model()
    # tag::creation[]
    # Kernel (lower bound): node 0 must belong to the graph
    lb = create_undirected_graph(model, 5, nodes=[0])
    # Envelope (upper bound): the nodes and edges that may belong to the graph
    ub = create_undirected_graph(model, 5, nodes=[0, 1, 2, 3, 4],
                                 edges=[[0, 1], [1, 2], [2, 3], [3, 4], [4, 0]])
    g = model.graphvar(lb, ub, "g")

    # A directed graph variable, whose envelope is the complete directed graph on 5 nodes
    dg = model.digraphvar(create_directed_graph(model, 5), create_complete_directed_graph(model, 5), "dg")
    # end::creation[]
    assert model.get_solver().solve()


def test_nb_nodes():
    model = Model()
    g = undirected(model)
    # tag::nbNodes[]
    nb_nodes = model.intvar(2, 3, "nbNodes")
    model.graph_nb_nodes(g, nb_nodes).post()
    # end::nbNodes[]
    assert model.get_solver().solve()


def test_nb_edges():
    model = Model()
    g = undirected(model)
    # tag::nbEdges[]
    nb_edges = model.intvar(3, 4, "nbEdges")
    model.graph_nb_edges(g, nb_edges).post()
    # end::nbEdges[]
    assert model.get_solver().solve()


def test_loops():
    model = Model()
    lb = create_directed_graph(model, 5)
    ub = create_complete_directed_graph(model, 5)
    # tag::loopsEnvelope[]
    # the complete graph has no loop: add them to the envelope
    for i in range(5):
        ub.add_edge(i, i)
    # end::loopsEnvelope[]
    dg = model.digraphvar(lb, ub, "dg")
    # tag::loops[]
    # loops is the set of nodes i such that (i, i) is in dg
    loops = model.setvar([], [0, 1, 2, 3, 4], "loops")
    model.graph_loop_set(dg, loops).post()
    # nb_loops is the number of such nodes
    nb_loops = model.intvar(1, 2, "nbLoops")
    model.graph_nb_loops(dg, nb_loops).post()
    # end::loops[]
    assert model.get_solver().solve()


def test_symmetry():
    model = Model()
    dg = directed(model)
    # tag::symmetry[]
    model.graph_symmetric(dg).post()       # (i, j) in dg => (j, i) in dg
    # end::symmetry[]
    assert model.get_solver().solve()

    model = Model()
    dg = directed(model)
    # tag::antisymmetry[]
    model.graph_anti_symmetric(dg).post()  # (i, j) in dg => (j, i) not in dg
    # end::antisymmetry[]
    assert model.get_solver().solve()


def test_transitivity():
    model = Model()
    dg = directed(model)
    # tag::transitivity[]
    model.graph_transitivity(dg).post()    # (i, j) and (j, k) in dg => (i, k) in dg
    # end::transitivity[]
    assert model.get_solver().solve()


def test_subgraph():
    model = Model()
    lb, ub = create_undirected_graph(model, 5), create_complete_undirected_graph(model, 5)
    lb2, ub2 = create_undirected_graph(model, 5), create_complete_undirected_graph(model, 5)
    # tag::subgraph[]
    g1 = model.graphvar(lb, ub, "g1")
    g2 = model.graphvar(lb2, ub2, "g2")
    model.graph_subgraph(g1, g2).post()    # every node and edge of g1 is in g2
    # end::subgraph[]
    assert model.get_solver().solve()


def test_nodes_channeling():
    model = Model()
    g = undirected(model)
    # tag::nodesChanneling[]
    # i in nodes <=> node i in g
    nodes = model.setvar([], [0, 1, 2, 3, 4], "nodes")
    model.graph_nodes_channeling(g, nodes).post()
    # is_in_2 = 1 <=> node 2 in g
    is_in_2 = model.boolvar(name="isIn2")
    model.graph_node_channeling(g, is_in_2, 2).post()
    # end::nodesChanneling[]
    assert model.get_solver().solve()


def test_edge_channeling():
    model = Model()
    g = undirected(model)
    # tag::edgeChanneling[]
    # is_edge = 1 <=> edge (0, 1) in g
    is_edge = model.boolvar(name="isEdge")
    model.graph_edge_channeling(g, is_edge, 0, 1).post()
    # end::edgeChanneling[]
    assert model.get_solver().solve()


def test_neighbors_channeling():
    model = Model()
    g = undirected(model)
    # tag::neighborsChanneling[]
    # j in neighbors[i] <=> edge (i, j) in g
    neighbors = [model.setvar([], [0, 1, 2, 3, 4], f"neighbors[{i}]") for i in range(5)]
    model.graph_neighbors_channeling(g, neighbors).post()
    # end::neighborsChanneling[]
    assert model.get_solver().solve()


def test_successors_channeling():
    model = Model()
    dg = directed(model)
    # tag::successorsChanneling[]
    # j in successors[i] <=> edge (i, j) in dg
    successors = [model.setvar([], [0, 1, 2, 3, 4], f"successors[{i}]") for i in range(5)]
    model.graph_successors_channeling(dg, successors).post()
    # j in predecessors_of_2 <=> edge (j, 2) in dg
    predecessors_of_2 = model.setvar([], [0, 1, 2, 3, 4], "predecessorsOf2")
    model.graph_node_predecessors_channeling(dg, predecessors_of_2, 2).post()
    # end::successorsChanneling[]
    assert model.get_solver().solve()


def test_degrees():
    model = Model()
    g = undirected(model)
    # tag::minMaxDegree[]
    model.graph_min_degree(g, 2).post()    # every node of g has at least 2 neighbors
    model.graph_max_degree(g, 3).post()    # and at most 3
    # end::minMaxDegree[]
    # tag::degrees[]
    # degrees[i] is the degree of node i (0 if i is not in g)
    degrees = model.intvars(5, 0, 4, "degrees")
    model.graph_degrees(g, degrees).post()
    # end::degrees[]
    assert model.get_solver().solve()


def test_in_out_degrees():
    model = Model()
    dg = directed(model)
    # tag::inOutDegrees[]
    in_degrees = model.intvars(5, 0, 4, "inDegrees")
    out_degrees = model.intvars(5, 0, 4, "outDegrees")
    model.graph_in_degrees(dg, in_degrees).post()
    model.graph_out_degrees(dg, out_degrees).post()
    model.graph_min_in_degree(dg, 1).post()   # every node of dg has at least 1 predecessor
    model.graph_max_out_degree(dg, 2).post()  # and at most 2 successors
    # end::inOutDegrees[]
    assert model.get_solver().solve()


def test_connected_components():
    model = Model()
    g = undirected(model)
    # tag::connectedComponents[]
    nb_cc = model.intvar(2, name="nbCC")
    model.graph_nb_connected_components(g, nb_cc).post()
    # every connected component has between min_size and max_size nodes
    min_size = model.intvar(2, 5, "minSize")
    max_size = model.intvar(2, 5, "maxSize")
    model.graph_size_connected_components(g, min_size, max_size).post()
    # end::connectedComponents[]
    assert model.get_solver().solve()


def test_connected():
    model = Model()
    g = undirected(model)
    # tag::connected[]
    model.graph_nb_nodes(g, model.intvar(5)).post()
    model.graph_connected(g).post()
    model.graph_biconnected(g).post()      # to use with graph_connected(g)
    # end::connected[]
    assert model.get_solver().solve()


def test_strongly_connected():
    model = Model()
    dg = directed(model)
    # tag::stronglyConnected[]
    model.graph_strongly_connected(dg).post()
    # end::stronglyConnected[]
    assert model.get_solver().solve()

    model = Model()
    dg = directed(model)
    # tag::nbStronglyConnectedComponents[]
    nb_scc = model.intvar(2, 3, "nbSCC")
    model.graph_nb_strongly_connected_components(dg, nb_scc).post()
    # end::nbStronglyConnectedComponents[]
    assert model.get_solver().solve()


def test_reachability():
    model = Model()
    dg = directed(model)
    # tag::reachability[]
    model.graph_reachability(dg, 0).post()  # every node of dg is reachable from node 0
    # end::reachability[]
    assert model.get_solver().solve()


def test_acyclic():
    model = Model()
    g = undirected(model)
    dg = directed(model)
    # tag::acyclic[]
    model.graph_no_cycle(g).post()         # undirected: no cycle
    model.graph_no_circuit(dg).post()      # directed: no circuit
    # end::acyclic[]
    assert model.get_solver().solve()


def test_cycle():
    model = Model()
    g = undirected(model)
    # tag::cycle[]
    model.graph_nb_nodes(g, model.intvar(5)).post()
    model.graph_cycle(g).post()            # with all 5 nodes, a Hamiltonian cycle
    # end::cycle[]
    assert model.get_solver().solve()


def test_trees():
    model = Model()
    g = undirected(model)
    # tag::tree[]
    model.graph_tree(g).post()             # connected and without cycle
    # end::tree[]
    assert model.get_solver().solve()

    model = Model()
    g = undirected(model)
    # tag::forest[]
    model.graph_forest(g).post()           # without cycle, possibly several components
    # end::forest[]
    assert model.get_solver().solve()


def test_directed_trees():
    model = Model()
    dg = directed(model)
    # tag::directedTree[]
    model.graph_directed_tree(dg, 0).post()  # an arborescence rooted in node 0
    # end::directedTree[]
    assert model.get_solver().solve()

    model = Model()
    dg = directed(model)
    # tag::directedForest[]
    model.graph_directed_forest(dg).post()   # disjoint arborescences
    # end::directedForest[]
    assert model.get_solver().solve()


def test_nb_cliques():
    model = Model()
    g = undirected(model)
    # tag::nbCliques[]
    # g is partitioned into nb_cliques cliques
    nb_cliques = model.intvar(2, name="nbCliques")
    model.graph_nb_cliques(g, nb_cliques).post()
    # end::nbCliques[]
    assert model.get_solver().solve()


def test_diameter():
    model = Model()
    g = undirected(model)
    # tag::diameter[]
    # the longest shortest path of g has diameter edges (g is connected)
    diameter = model.intvar(2, 3, "diameter")
    model.graph_diameter(g, diameter).post()
    # end::diameter[]
    assert model.get_solver().solve()


def test_views():
    model = Model()
    g = undirected(model)
    dg = directed(model)
    # tag::views[]
    # the set of nodes of g
    nodes = model.graph_node_set_view(g)
    # the neighbors of node 2 in g, the successors and predecessors of node 2 in dg
    neighbors_of_2 = model.graph_neighbors_set_view(g, 2)
    successors_of_2 = model.graph_successors_set_view(dg, 2)
    predecessors_of_2 = model.graph_predecessors_set_view(dg, 2)
    # end::views[]
    assert model.get_solver().solve()
