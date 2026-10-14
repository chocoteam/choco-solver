# Choco-solver documentation

| Path                  | Audience                            | Published                        |
|-----------------------|-------------------------------------|----------------------------------|
| `content/docs/`       | users: guide and reference          | https://choco-solver.org/docs/   |
| `content/tutos/`      | users: tutorials                    | https://choco-solver.org/tutos/  |
| `static/`             | images used by these pages          |                                  |
| `layouts/`            | Hugo shortcodes (`example`)         |                                  |
| `examples/python/`    | tested Python examples of the pages | run with pytest                  |
| `dev/`                | contributors, humans and AI agents  | not published, read it on GitHub |

Tested Java examples shown in the pages are TestNG tests in
`examples/src/test/java/org/chocosolver/docs/`.

`content/`, `static/` and `layouts/` are [Hugo](https://gohugo.io/) content, written for the [Docsy](https://www.docsy.dev/) theme.
They are not a site on their own: the website (theme, home page, community page, slides, deployment)
lives in [chocoteam/website](https://github.com/chocoteam/website), which imports this repository
as a Hugo module, pinned to a release of choco-solver. What the module brings to the site is declared in
[`hugo.toml`](../hugo.toml), at the root of the repository.

## Preview

Requirements: Hugo **extended** (version in `.github/workflows/docs.yml`), Go (Hugo modules) and Node.js (PostCSS).

```bash
git clone https://github.com/chocoteam/website.git
cd website
npm ci
HUGO_MODULE_REPLACEMENTS="github.com/chocoteam/choco-solver -> /path/to/choco-solver" hugo server
```

## Write documentation

See [dev/documentation.md](dev/documentation.md).
