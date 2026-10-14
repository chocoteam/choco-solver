# Examples shown in the user documentation, page "Modeling"
# (docs/content/docs/Modeling/_index.md).
# The regions between "tag::name[]" and "end::name[]" are included in the page.
# tag::import[]
from pychoco import Model
# end::import[]


def test_model():
    # tag::model[]
    model = Model()
    # end::model[]
    assert model.get_solver() is not None


def test_named_model():
    # tag::namedModel[]
    model = Model("my problem")
    # end::namedModel[]
    assert model.name == "my problem"
