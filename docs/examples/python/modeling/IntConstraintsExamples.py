# Examples shown in the user documentation, page "Modeling / Constraints over integer variables"
# (docs/content/docs/Modeling/IntConstraints.md).
# The regions between "tag::name[]" and "end::name[]" are included in the page.
from pychoco import Model


def test_arithm():
    model = Model()
    x = model.intvar(0, 5, "x")
    y = model.intvar(0, 5, "y")
    z = model.intvar(0, 10, "z")
    # tag::arithm[]
    model.arithm(x, ">=", 3).post()
    # end::arithm[]
    # tag::arithm2[]
    op = ">="  # among ">=", ">", "<=", "<", "=" and "!="
    model.arithm(x, op, y).post()
    # with x an IntVar and y either an IntVar or an int

    op2 = "+"  # either "+" or "-"
    model.arithm(x, op2, y, "=", z).post()
    # with x and y IntVar, and z either an int or an IntVar
    # end::arithm2[]
    assert model.get_solver().solve()
    assert x.get_value() + y.get_value() == z.get_value()


def test_mathematical():
    model = Model()
    x = model.intvar(-5, 5, "x")
    y = model.intvar(-5, 5, "y")
    z = model.intvar(-25, 25, "z")
    a, b = 2, 1
    # tag::times[]
    model.times(x, a, z).post()  # x * a = z
    model.times(x, y, 6).post()  # x * y = 6
    model.times(x, y, z).post()  # x * y = z
    # end::times[]
    assert model.get_solver().solve()

    model = Model()
    x = model.intvar(-5, 5, "x")
    y = model.intvar(-5, 5, "y")
    z = model.intvar(-25, 25, "z")
    # tag::div[]
    model.div(x, y, z).post()  # x / y = z
    # it ensures that y != 0, and z is rounded towards 0
    # end::div[]
    # tag::mod[]
    model.mod(x, a, b).post()  # x % a = b
    model.mod(x, a, z).post()  # x % a = z
    model.mod(x, y, z).post()  # x % y = z
    # end::mod[]
    assert model.get_solver().solve()

    model = Model()
    x = model.intvar(-5, 5, "x")
    y = model.intvar(-5, 5, "y")
    # tag::absolute[]
    model.absolute(x, y).post()  # x = |y|
    # end::absolute[]
    assert model.get_solver().solve()

    model = Model()
    base = model.intvar(1, 3, "base")
    x2 = model.intvar(0, 30, "x2")
    # tag::pow[]
    model.pow(base, 2, x2).post()  # x2 = base ^ 2 (the exponent is a constant in pychoco)
    # end::pow[]
    assert model.get_solver().solve()


def test_min_max_sum():
    model = Model()
    vars = model.intvars(4, 0, 5, "v")
    min_var = model.intvar(0, 5, "min")
    max_var = model.intvar(0, 5, "max")
    x = model.intvar(0, 20, "x")
    coefs = [1, 2, 3, 4]
    # tag::minMax[]
    model.min(min_var, vars).post()
    model.max(max_var, vars).post()
    # end::minMax[]
    # tag::sum[]
    op = ">="  # among ">=", ">", "<=", "<", "=" and "!="
    model.sum(vars, op, x).post()  # sum(vars) >= x
    # end::sum[]
    # tag::scalar[]
    model.scalar(vars, coefs, "=", x).post()  # sum(vars[i] * coefs[i]) = x
    # end::scalar[]
    assert model.get_solver().solve()


def test_all_different():
    model = Model()
    # tag::allDifferent[]
    vars = model.intvars(4, 1, 5, "X")
    model.all_different(vars).post()
    # end::allDifferent[]
    assert model.get_solver().solve()


def test_count():
    model = Model()
    # tag::count[]
    occ0 = model.intvar(0, 5, "occ0")
    vars = model.intvars(7, 0, 5, "X")
    model.count(0, vars, occ0).post()  # occ0 is the number of variables of vars equal to 0
    # end::count[]
    assert model.get_solver().solve()


def test_global_cardinality():
    model = Model()
    # tag::globalCardinality[]
    vars = model.intvars(7, 0, 5, "X")
    values = [1, 3, 5]
    occs = model.intvars(3, 0, 3, "O")
    model.global_cardinality(vars, values, occs, False).post()
    # end::globalCardinality[]
    assert model.get_solver().solve()


def test_element():
    model = Model()
    # tag::element[]
    idx = model.intvar(0, 5, "I")
    rst = model.intvar(0, 10, "R")
    model.element(rst, [0, 2, 4, 6, 7], idx).post()  # rst = table[idx]
    # end::element[]
    model.arithm(idx, "=", 3).post()
    assert model.get_solver().solve()
    assert rst.get_value() == 6


def test_tasks():
    model = Model()
    start = model.intvar(0, 10, "start")
    duration = 3
    # tag::task[]
    task = model.task(start, duration)
    # end::task[]
    assert model.get_solver().solve()


def test_cumulative():
    model = Model()
    starts = model.intvars(4, 0, 20, "start")
    durations = [2, 3, 1, 4]
    # tag::cumulative[]
    tasks = [model.task(starts[i], durations[i]) for i in range(4)]
    heights = [model.intvar(1) for _ in range(4)]
    capacity = model.intvar(1)
    model.cumulative(tasks, heights, capacity).post()
    # end::cumulative[]
    assert model.get_solver().solve()


def test_tables():
    model = Model()
    X = model.intvars(4, 0, 3, "X")
    # tag::tableAllowed[]
    all_equal = [[0, 0, 0, 0], [1, 1, 1, 1], [2, 2, 2, 2], [3, 3, 3, 3]]
    model.table(X, all_equal, True, "CT+").post()  # True stands for 'allowed' combinations
    # end::tableAllowed[]
    assert model.get_solver().solve()

    model = Model()
    X = model.intvars(4, 0, 3, "X")
    # tag::tableForbidden[]
    not_all_equal = [[0, 0, 0, 0], [1, 1, 1, 1], [2, 2, 2, 2], [3, 3, 3, 3]]
    # False stands for 'forbidden' combinations (pychoco then uses the CT algorithm)
    model.table(X, not_all_equal, False).post()
    # end::tableForbidden[]
    assert model.get_solver().solve()


def test_universal_value():
    model = Model()
    X = model.intvars(3, 0, 99, "X")
    # tag::universal[]
    STAR = -1
    rel = [[0, 0, STAR], [3, 2, 1]]
    model.table(X, rel, True, uvalue=STAR).post()
    # end::universal[]
    model.arithm(X[2], "=", 42).post()
    assert model.get_solver().solve()
    assert X[0].get_value() == 0
