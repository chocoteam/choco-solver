# Developer guide

This guide is for people who change choco-solver itself, humans or AI agents.
It assumes you know constraint programming and Java, but not the internals of choco-solver.
User documentation (how to *use* the library) is in the website sources, `docs/content/`.

The pages are plain Markdown (no Hugo shortcodes) so that they read well on GitHub, in an IDE
and by a coding agent. They are not published on the website.

| Page                                             | Read it when                                                    |
|--------------------------------------------------|-----------------------------------------------------------------|
| [architecture.md](architecture.md)               | you need to find your way in the modules and packages           |
| [building-and-testing.md](building-and-testing.md) | you build, test, or check the style of a change              |
| [best-practices.md](best-practices.md)           | you write a constraint factory, a propagator or a test          |
| [documentation.md](documentation.md)             | your change is visible to users, or you edit the website        |
| [../../scripts/RELEASE.md](../../scripts/RELEASE.md) | you make a release                                          |
| [../../CONTRIBUTING.md](../../CONTRIBUTING.md)   | you open an issue or a pull request                             |

## Keeping this guide alive

- A fix that reveals a non-obvious pitfall gets an entry in [best-practices.md](best-practices.md).
- A change that moves or renames a subsystem updates [architecture.md](architecture.md) in the same pull request.
- Agent instruction files (`AGENTS.md`, `CLAUDE.md`) stay short and point here rather than duplicating this guide.
