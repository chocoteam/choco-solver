# Choco-solver documentation

| Path                  | Audience                            | Published                        |
|-----------------------|-------------------------------------|----------------------------------|
| `content/docs/`       | users: guide and reference          | https://choco-solver.org/docs/   |
| `content/tutos/`      | users: tutorials                    | https://choco-solver.org/tutos/  |
| `static/`             | images used by these pages          |                                  |
| `dev/`                | contributors, humans and AI agents  | not published, read it on GitHub |

`content/` and `static/` are [Hugo](https://gohugo.io/) content, written for the [Docsy](https://www.docsy.dev/) theme.
They are not a site on their own: the website (theme, home page, community page, slides, deployment)
lives in [chocoteam/website](https://github.com/chocoteam/website), which imports these two directories
as a Hugo module, pinned to a release of choco-solver.

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
