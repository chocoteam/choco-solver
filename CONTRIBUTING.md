# Contributing to Choco-solver

Contributions are welcome: bug reports, feature requests, code and documentation.
For questions about using the library, read the [documentation](https://choco-solver.org/docs/)
or ask on the [google group](https://groups.google.com/forum/#!forum/choco-solver).

## Branches

- `develop` receives all changes: **open your pull requests against `develop`**.
- `master` only receives releases.

## Bug reports

1. Search the [tracker](https://github.com/chocoteam/choco-solver/issues), closed issues included,
   to see whether the bug is already known or fixed.
2. Reproduce it on `develop` if you can, and give the version you used.
3. Provide a [minimal reproducible example](https://stackoverflow.com/help/minimal-reproducible-example).

## Feature requests

Give as much detail as possible; practical use cases help a lot.
If you want to implement it yourself, open an issue first to discuss the design.

## Pull requests

0. Make sure you have the right to submit the change (your employer may own your work).
1. Fork the repository and branch from `develop`.
2. Read the [developer guide](docs/dev/README.md), in particular the
   [best practices](docs/dev/best-practices.md) for constraints, propagators and tests.
3. Test your change (see [building and testing](docs/dev/building-and-testing.md)):
   tests are deterministic and tagged with a TestNG group.
4. Respect the style (`mvn checkstyle:check`) and the license header (`etc/header.txt`);
   a new class has class-level Javadoc with an `@author` tag.
5. Update the documentation if users can see the change: website pages in `docs/content/`
   (see [writing documentation](docs/dev/documentation.md)) and an entry in `CHANGES.md`.
6. Open the pull request against `develop`.

Changes limited to the documentation follow the same path: edit `docs/` and open a pull request.
Every website page has an "Edit this page" link that leads there.
