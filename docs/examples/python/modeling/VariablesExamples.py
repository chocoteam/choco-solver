# Examples shown in the user documentation, page "Modeling / Declaring variables"
# (docs/content/docs/Modeling/Variables.md).
# The regions between "tag::name[]" and "end::name[]" are included in the page.
from pychoco import Model, create_directed_graph, create_undirected_graph, create_complete_undirected_graph


def test_int_var():
    model = Model()
    # tag::intVar[]
    # Create a constant variable equal to 42
    v0 = model.intvar(42, name="v0")
    # Create a variable taking its value in [1, 3] (the value is 1, 2 or 3)
    v1 = model.intvar(1, 3, name="v1")
    # Create a variable taking its value in {1, 3} (the value is 1 or 3)
    v2 = model.intvar([1, 3], name="v2")
    # end::intVar[]
    assert v0.get_value() == 42
    assert v2.get_domain_values() == [1, 3]


def test_int_var_array():
    model = Model()
    # tag::intVarArray[]
    # Create an array of 5 variables taking their value in [-1, 1]
    vs = model.intvars(5, -1, 1, name="vs")
    # Create a matrix of 5x6 variables taking their value in [-1, 1]
    ws = model.intvars((5, 6), -1, 1, name="ws")
    # end::intVarArray[]
    assert len(vs) == 5
    assert len(ws) == 5 and len(ws[4]) == 6


def test_bounded():
    model = Model()
    # tag::bounded[]
    v = model.intvar(1, 12, name="v", bounded_domain=True)
    # end::bounded[]
    assert not v.has_enumerated_domain()


def test_enumerated():
    model = Model()
    # tag::enumerated[]
    v = model.intvar(1, 4, name="v", bounded_domain=False)
    w = model.intvar([1, 2, 3, 4], name="w")
    # end::enumerated[]
    assert v.has_enumerated_domain() and w.has_enumerated_domain()


def test_bool_var():
    model = Model()
    # tag::boolVar[]
    b = model.boolvar(name="b")
    # A Boolean variable fixed to True
    t = model.boolvar(True)
    # end::boolVar[]
    assert t.get_value()


def test_set_var():
    model = Model()
    # tag::setVar[]
    # Constant SetVar equal to {2,3,12}, with a list or with a set
    x1 = model.setvar([2, 3, 12], name="x1")
    x2 = model.setvar({2, 3, 12}, name="x2")
    # SetVar representing a subset of {1,2,3,5,12}
    # possible values: {}, {2}, {1,3,5} ...
    y = model.setvar([], {1, 2, 3, 5, 12}, name="y")
    # SetVar representing a superset of {2,3} and a subset of {1,2,3,5,12}
    # possible values: {2,3}, {2,3,5}, {1,2,3,5} ...
    z = model.setvar([2, 3], {1, 2, 3, 5, 12}, name="z")
    # end::setVar[]
    assert model.get_solver().solve()
    assert {2, 3} <= set(z.get_value())


def test_graph_var():
    model = Model()
    # tag::graphVar[]
    # A directed graph
    # first declare the lower bound
    lb = create_directed_graph(model, 3)
    # then declare the upper bound
    ub = create_directed_graph(model, 3, nodes=[0, 1, 2], edges=[[0, 1], [1, 2], [2, 0]])
    # finally declare the graph variable
    dg = model.digraphvar(lb, ub, "dg")

    # An undirected graph
    ulb = create_undirected_graph(model, 3)
    uub = create_complete_undirected_graph(model, 3)
    g = model.graphvar(ulb, uub, "g")
    # end::graphVar[]
    assert model.get_solver().solve()


def test_task():
    model = Model()
    # tag::task[]
    start = model.intvar(0, 10, "start")
    duration = 5
    task = model.task(start, duration)
    # end::task[]
    assert model.get_solver().solve()


def test_arithmetic_views():
    model = Model()
    y = model.intvar(0, 5, "y")
    # tag::offsetView[]
    x = model.int_offset_view(y, 2)
    # end::offsetView[]
    # tag::negView[]
    x2 = model.int_minus_view(y)
    # end::negView[]
    # tag::mulView[]
    x3 = model.int_scale_view(y, 3)
    # end::mulView[]
    # tag::composition[]
    x4 = model.int_affine_view(2, y, 5)
    # end::composition[]
    model.arithm(y, "=", 4).post()
    assert model.get_solver().solve()
    assert (x.get_value(), x2.get_value(), x3.get_value(), x4.get_value()) == (6, -4, 12, 13)


def test_logical_views():
    model = Model()
    x = model.intvar(0, 10, "x")
    # tag::isEq[]
    b = model.int_eq_view(x, 4)
    # end::isEq[]
    # tag::isNeq[]
    b2 = model.int_ne_view(x, 4)
    # end::isNeq[]
    # tag::isLeq[]
    b3 = model.int_le_view(x, 4)
    # end::isLeq[]
    # tag::isGeq[]
    b4 = model.int_ge_view(x, 4)
    # end::isGeq[]
    # tag::boolNot[]
    d = model.bool_not_view(b)
    # end::boolNot[]
    model.arithm(x, "=", 4).post()
    assert model.get_solver().solve()
    assert b.get_value() and not b2.get_value() and not d.get_value()
