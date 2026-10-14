# Examples shown in the user documentation, page "Modeling / Constraints"
# (docs/content/docs/Modeling/Constraints.md).
# The regions between "tag::name[]" and "end::name[]" are included in the page.
from pychoco import Model


def test_post():
    model = Model()
    vars = model.intvars(3, 0, 2, "x")
    # tag::post[]
    model.all_different(vars).post()
    # end::post[]
    assert model.get_solver().solve()


def test_reify():
    model = Model()
    x = model.intvar(0, 5, "x")
    constraint = model.arithm(x, ">", 2)
    # tag::reify[]
    b = constraint.reify()
    # end::reify[]
    assert model.get_solver().solve()
    assert bool(b.get_value()) == (x.get_value() > 2)
