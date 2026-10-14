# Python examples of the user documentation, run with pytest against the pychoco release on PyPI.
#
# pychoco follows choco-solver releases with some delay. An example of a feature that pychoco
# does not provide yet is marked with the first pychoco version that supports it:
#
#     @pytest.mark.pychoco("0.5.0")
#     def test_new_feature(): ...
#
# and is skipped with older versions.
from importlib.metadata import version

import pytest


def _as_tuple(v):
    return tuple(int(p) for p in v.split(".")[:3] if p.isdigit())


def pytest_configure(config):
    config.addinivalue_line("markers", "pychoco(min_version): first pychoco version supporting the example")


def pytest_collection_modifyitems(config, items):
    installed = _as_tuple(version("pychoco"))
    for item in items:
        marker = item.get_closest_marker("pychoco")
        if marker and installed < _as_tuple(marker.args[0]):
            item.add_marker(pytest.mark.skip(reason=f"requires pychoco >= {marker.args[0]}"))
