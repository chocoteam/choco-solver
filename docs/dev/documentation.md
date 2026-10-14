# Writing documentation

## Code and documentation change together

If a pull request changes something users can see (a new constraint, a new option, a changed signature
or behaviour), it updates the user documentation in the same pull request.
The pull request template has a checkbox for it.

## Where things go

| What                                         | Where                                         |
|----------------------------------------------|-----------------------------------------------|
| user guide and reference                     | `docs/content/docs/<section>/`                |
| tutorials                                    | `docs/content/tutos/`                         |
| images                                       | `docs/static/images/`, linked as `/images/...` |
| code examples                                | see [Code examples](#code-examples)           |
| release notes                                | `CHANGES.md` (root)                           |
| API details                                  | Javadoc, published by [javadoc.io](https://javadoc.io/doc/org.choco-solver/choco-solver) from Maven Central |
| internals, conventions, pitfalls             | `docs/dev/` (plain Markdown, not published)   |
| home page, community page, slides, theme     | [chocoteam/website](https://github.com/chocoteam/website) |

A page starts with a front matter giving at least `title`, `weight` (order in the menu) and `description`.
Add `math: "true"` to render LaTeX formulas.

## Code examples

Code shown in the pages is **tested**: it lives in example files, and the page includes regions of them.

- Java: a TestNG test class in `examples/src/test/java/org/chocosolver/docs/<section>/<Page>Examples.java`,
  group `1s` (`10s` for long examples, `ibex` for examples that require Ibex), run by the Maven workflow;
- Python ([pychoco](https://github.com/chocoteam/pychoco)): `docs/examples/python/<section>/<Page>Examples.py`,
  run with `pytest` (in `docs/examples/python`) against the pychoco release on PyPI. Optional.

In an example file, delimit the region to show with comments, and check something after it:

```java
@Test(groups = "1s")
public void globalCardinality() {
    Model model = new Model();
    // tag::globalCardinality[]
    IntVar[] vars = model.intVarArray("X", 7, 0, 5);
    ...
    // end::globalCardinality[]
    Assert.assertTrue(model.getSolver().solve());
}
```

and include it in the page (the Python tab appears only if the Python file has the same region):

```
{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "globalCardinality" >}}
{{< /tabpane >}}
```

pychoco follows choco-solver releases with some delay: a Python example of a feature that pychoco does not
provide yet is marked `@pytest.mark.pychoco("<first pychoco version supporting it>")` and skipped until then.

Code written directly in a page (`{{< tab header="Java" >}}`) is not tested: keep it for short
illustrations that are not meant to run (signatures, pseudo-code), and migrate the others.

## Hugo conventions in use

- **Notes and warnings**: `{{% alert title="Info" color="primary" %}} ... {{% /alert %}}`.
- **Current version**: write `{{< param "choco_version" >}}` instead of a hard-coded version number
  (the parameter is set in the website configuration).
- **Internal links**: `{{< ref "/docs/Modeling/Variables.md" >}}`, so that the build fails on a broken link.
- **Moved pages**: keep the old URL working with `aliases` in the front matter.

## Check and preview

The workflow `.github/workflows/docs.yml` builds the website with the `docs/` of the pull request
and runs the Python examples, so a page that breaks the build or a failing example fails the pull request.
The Java examples run with the other tests.
To preview locally, see [../README.md](../README.md).

## Publication

The website shows the documentation of the **latest release**: `develop` contains unreleased features,
and documenting them there is expected, they go online with the next release.

After a release `vX.Y.Z`, in [chocoteam/website](https://github.com/chocoteam/website):

```bash
hugo mod get github.com/chocoteam/choco-solver@vX.Y.Z
# update choco_version in config.toml, then build and deploy as usual (deploy.sh)
```

For an urgent fix between two releases, pin the website to the commit that carries the fix
(`hugo mod get github.com/chocoteam/choco-solver@<commit>`), typically on a branch made from the latest tag,
and merge the fix into `develop` too.
