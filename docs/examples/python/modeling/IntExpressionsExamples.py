# Examples shown in the user documentation, page "Modeling / Building expressions on integer variables"
# (docs/content/docs/Modeling/IntExpressions.md).
# The regions between "tag::name[]" and "end::name[]" are included in the page.
from pychoco import Model


def test_arithmetic():
    model = Model()
    # tag::arithmetic[]
    x = model.intvar(1, 5, "x")
    y = model.intvar(1, 5, "y")
    # z = min((x + 5) % 3, y^2)
    a = model.intvar(0, 2)
    model.mod(x + 5, 3, a).post()
    b = y ** 2
    z = model.intvar(0, 2, "z")
    model.min(z, [a, b]).post()
    # end::arithmetic[]
    sols = model.get_solver().find_all_solutions()
    assert len(sols) == 25
    for s in sols:
        vx = s.get_int_val(x)
        vy = s.get_int_val(y)
        assert s.get_int_val(z) == min((vx + 5) % 3, vy * vy)


def test_bool_var():
    model = Model()
    # tag::boolVar[]
    x = model.intvar(0, 5, "x")
    y = model.intvar(0, 5, "y")
    b = x > y
    # end::boolVar[]
    sols = model.get_solver().find_all_solutions()
    assert len(sols) == 36
    for s in sols:
        assert bool(s.get_int_val(b)) == (s.get_int_val(x) > s.get_int_val(y))


def test_logical():
    model = Model()
    # tag::logical[]
    x = model.intvar(0, 5, "x")
    y = model.intvar(0, 5, "y")
    # (x = y + 1) ==> (x + 2 <= 6)
    model.if_then(x == y + 1, model.arithm(x + 2, "<=", 6))
    # end::logical[]
    sols = model.get_solver().find_all_solutions()
    # only (x, y) = (5, 4) is forbidden
    assert len(sols) == 35
    for s in sols:
        vx = s.get_int_val(x)
        vy = s.get_int_val(y)
        assert vx != vy + 1 or vx + 2 <= 6
