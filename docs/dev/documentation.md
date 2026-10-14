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
| release notes                                | `CHANGES.md` (root)                           |
| API details                                  | Javadoc, published by [javadoc.io](https://javadoc.io/doc/org.choco-solver/choco-solver) from Maven Central |
| internals, conventions, pitfalls             | `docs/dev/` (plain Markdown, not published)   |
| home page, community page, slides, theme     | [chocoteam/website](https://github.com/chocoteam/website) |

A page starts with a front matter giving at least `title`, `weight` (order in the menu) and `description`.
Add `math: "true"` to render LaTeX formulas.

## Hugo conventions in use

- **Code samples** use Docsy tabs, one tab per language (Java, and Python for
  [pychoco](https://github.com/chocoteam/pychoco) when relevant):

  ```
  {{< tabpane langEqualsHeader=true >}}
  {{< tab header="Java" >}}
  IntVar x = model.intVar("x", 1, 3);
  {{< /tab >}}
  {{< /tabpane >}}
  ```

- **Notes and warnings**: `{{% alert title="Info" color="primary" %}} ... {{% /alert %}}`.
- **Current version**: write `{{< param "choco_version" >}}` instead of a hard-coded version number
  (the parameter is set in the website configuration).
- **Internal links**: `{{< ref "/docs/Modeling/Variables.md" >}}`, so that the build fails on a broken link.
- **Moved pages**: keep the old URL working with `aliases` in the front matter.

## Check and preview

The workflow `.github/workflows/docs.yml` builds the website with the `docs/` of the pull request,
so a page that breaks the build fails the pull request.
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
