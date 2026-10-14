# Examples shown in the user documentation, page "Modeling / Constraints over set variables"
# (docs/content/docs/Modeling/SetConstraints.md).
# The regions between "tag::name[]" and "end::name[]" are included in the page.
from pychoco import Model


def test_member():
    model = Model()
    # tag::member[]
    s = model.setvar([], [0, 1, 2, 3, 4], "s")
    model.set_member_int(2, s).post()      # 2 is in s
    model.set_not_member_int(3, s).post()  # 3 is not in s
    x = model.intvar(0, 4, "x")
    model.set_member_int(x, s).post()      # the value of x is in s
    # end::member[]
    assert model.get_solver().solve()
    assert 2 in s.get_value() and 3 not in s.get_value()


def test_subset_eq():
    model = Model()
    # tag::subsetEq[]
    s1 = model.setvar([], [0, 1, 2, 3], "s1")
    s2 = model.setvar([0, 1], [0, 1, 2, 3, 4], "s2")
    model.set_subset_eq([s1, s2]).post()   # s1 ⊆ s2
    # end::subsetEq[]
    assert model.get_solver().solve()


def test_set_le():
    model = Model()
    # tag::setLe[]
    s1 = model.setvar([], [0, 1, 2], "s1")
    s2 = model.setvar([], [0, 1, 2], "s2")
    model.set_le(s1, s2).post()            # s1 ≤lex s2
    # end::setLe[]
    assert model.get_solver().solve()


def test_nb_empty():
    model = Model()
    # tag::nbEmpty[]
    sets = [model.setvar([], [0, 1, 2, 3, 4], f"S[{i}]") for i in range(5)]
    nb_empty = model.intvar(2, name="nbEmpty")
    model.set_nb_empty(sets, nb_empty).post()  # exactly 2 sets are empty
    model.set_not_empty(sets[0]).post()    # sets[0] has at least one element
    # end::nbEmpty[]
    assert model.get_solver().solve()


def test_offset():
    model = Model()
    # tag::offSet[]
    s1 = model.setvar([0, 2], [0, 1, 2], "s1")
    s2 = model.setvar([], [0, 1, 2, 3, 4, 5], "s2")
    model.set_offset(s1, s2, 3).post()     # x in s1 <=> x + 3 in s2
    # end::offSet[]
    assert model.get_solver().solve()
    assert {3, 5} <= set(s2.get_value())


def test_sum():
    model = Model()
    # tag::sum[]
    s = model.setvar([], [0, 1, 2, 3, 4], "s")
    total = model.intvar(0, 10, "sum")
    model.set_sum(s, total).post()         # total = Σ_{i ∈ s} i
    weights = [1, 2, 3, 4, 5]
    weighted_sum = model.intvar(0, 15, "weightedSum")
    model.set_sum_element(s, weights, weighted_sum).post()  # weighted_sum = Σ_{i ∈ s} weights[i]
    # end::sum[]
    assert model.get_solver().solve()


def test_union_intersection():
    model = Model()
    # tag::union[]
    s1 = model.setvar([], [0, 1, 2], "s1")
    s2 = model.setvar([], [0, 1, 3], "s2")
    union = model.setvar([], [0, 1, 2, 3], "union")
    model.set_union([s1, s2], union).post()         # union = s1 ∪ s2
    inter = model.setvar([], [0, 1, 2, 3], "inter")
    model.set_intersection([s1, s2], inter).post()  # inter = s1 ∩ s2
    # end::union[]
    assert model.get_solver().solve()


def test_min_max():
    model = Model()
    # tag::minMax[]
    s = model.setvar([], [1, 2, 3, 4, 5], "s")
    smallest = model.intvar(1, 5, "min")
    largest = model.intvar(1, 5, "max")
    # True: s cannot be empty
    model.set_min(s, smallest, True).post()  # smallest = smallest element of s
    model.set_max(s, largest, True).post()   # largest = largest element of s
    # end::minMax[]
    assert model.get_solver().solve()


def test_disjoint():
    model = Model()
    # tag::disjoint[]
    s1 = model.setvar([], [0, 1, 2, 3], "s1")
    s2 = model.setvar([3], [2, 3, 4, 5], "s2")
    model.set_disjoint(s1, s2).post()      # s1 ∩ s2 = ∅
    sets = [model.setvar([], [0, 1, 2, 3, 4, 5], f"S[{i}]") for i in range(3)]
    model.set_all_disjoint(sets).post()    # the sets are pairwise disjoint
    # end::disjoint[]
    assert model.get_solver().solve()


def test_partition():
    model = Model()
    # tag::partition[]
    parts = [model.setvar([], [0, 1, 2, 3, 4], f"parts[{i}]") for i in range(3)]
    universe = model.setvar([0, 1, 2, 3, 4], name="universe")  # a constant set
    model.set_partition(parts, universe).post()  # parts are disjoint and their union is universe
    # end::partition[]
    assert model.get_solver().solve()


def test_all_equal():
    model = Model()
    # tag::allEqual[]
    sets = [model.setvar([], [0, 1, 2], f"S[{i}]") for i in range(3)]
    model.set_all_different(sets).post()   # the sets are pairwise different (but not necessarily disjoint)
    others = [model.setvar([], [0, 1, 2], f"T[{i}]") for i in range(3)]
    model.set_all_equal(others).post()     # the sets are all equal
    # end::allEqual[]
    assert model.get_solver().solve()


def test_set_bools_channeling():
    model = Model()
    # tag::setBoolsChanneling[]
    s = model.setvar([], [0, 1, 2, 3], "s")
    bools = [model.boolvar(name=f"b[{i}]") for i in range(4)]
    model.set_bools_channeling(bools, s).post()  # bools[i] = 1 <=> i ∈ s
    # end::setBoolsChanneling[]
    assert model.get_solver().solve()


def test_sets_ints_channeling():
    model = Model()
    # tag::setsIntsChanneling[]
    x = model.intvars(5, 0, 2, "x")
    s = [model.setvar([], [0, 1, 2, 3, 4], f"s[{j}]") for j in range(3)]
    model.set_ints_channeling(s, x).post()       # i ∈ s[j] <=> x[i] = j
    # end::setsIntsChanneling[]
    assert model.get_solver().solve()


def test_symmetric():
    model = Model()
    # tag::symmetric[]
    sets = [model.setvar([], [0, 1, 2], f"S[{i}]") for i in range(3)]
    model.set_symmetric(sets).post()       # i ∈ sets[j] <=> j ∈ sets[i]
    # end::symmetric[]
    assert model.get_solver().solve()


def test_inverse_set():
    model = Model()
    # tag::inverseSet[]
    sets = [model.setvar([], [0, 1, 2], f"S[{i}]") for i in range(3)]
    inverses = [model.setvar([], [0, 1, 2], f"I[{i}]") for i in range(3)]
    model.set_inverse_set(sets, inverses).post()  # i ∈ sets[j] <=> j ∈ inverses[i]
    # end::inverseSet[]
    assert model.get_solver().solve()


def test_element():
    model = Model()
    # tag::element[]
    sets = [model.setvar([], [0, 1, 2, 3], f"sets[{i}]") for i in range(4)]
    index = model.intvar(0, 3, "index")
    result = model.setvar([], [0, 1, 2, 3], "result")
    model.set_element(index, sets, result).post()  # sets[index] = result
    # end::element[]
    assert model.get_solver().solve()
