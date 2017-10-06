# Contributing to Choco Solver

The developments are pushed on the master branch, so this is the most up-to-date version of the code.

## Using the issue tracker

The issue tracker is the preferred channel for [bug reports](#bug), [feature requests](#feat)
and [submitting pull requests](#pull).
If you need a personal support request, use our [Gitter chatroom](https://gitter.im/chocoteam/choco-solver?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge) or [google-group](https://groups.google.com/forum/#!forum/choco-solver). Most support requests are answered very fast.


<a name="bug"></a>
## Bug reports

Choco solver is a living library which is frequently modified, sometimes deeply.
We do our best to track bugs in the [tracker](https://github.com/chocoteam/choco-solver/issues).
But, it happens that a modification exhibit a bug or, simply, that we did not test the code enough.
In that case:

1. Search in [tracker](https://github.com/chocoteam/choco-solver/issues) to see if the bug has already been reported
(do not forget to look for closed issues), and/or fixed;

2. Isolate the problem, describe it and provide a [Minimal Working Example](https://en.wikipedia.org/wiki/Minimal_Working_Example).
The [stackoverflow guidelines](http://stackoverflow.com/help/mcve) is a very good starting point.
If possible, try to reproduce the bug on the master branch
and do not forget to indicate which version were used to reproduce the bug (release version or revision number).

Doing so, we will endeavor to reproduce the bug and fix it as soon as possible in the master branch.
If the bug is critical, a release could be done in advance.

<a name="feat"></a>
## Feature requests

Feature requests are welcome, we always appreciate having feedback.
For your ideas to be considered, please give us as much details as possible, some practical cases are bonus.
And if you feel like doing it by yourself, what about submitting a [pull request](#pull) ?



<a name="pull"></a>
## Pull requests

Contributing to Choco solver is easy.

0. Make sure you have the right to send any changes you make. If you do changes at work you may find your employer owns the patch not you.

1. Fork Choco solver on https://github.com/chocoteam/choco-solver,

2. Work with the source: Choco solver is maven-based project, easy to install on any IDEs,

3. Add your features and test them,

4. Send a pull request on Github.


This is about code and documentation!

### Code modification

If you modify a class:

- add your name and email to the list of authors in the file, we will maintain the
global list of authors/contributors,

- comment your modifications,

- always test your changes (if you don't known what/how to test, contact us).


If you create a new class:

- reproduce the licence text like in any other classes already provided,

- add your name and email address, we will maintain the global list of authors/contributors,

- java-document and comment your code as much as possible,

- always test your changes (if you don't known what/how to test, contact us).


##### Tests

We use TestNG as a testing framework.
However, if you prefer JUnit, we will migrate the code in a second phase.

The rules are:

- prefer ten short tests instead of long one,

- make sure the tests you add are deterministic, if not, make sure the seed can be set easily,


### User guide improvement

We use [sphinxdoc](http://sphinx-doc.org) to maintain and generate the documentation.
Any help is appreciated: even if we are the best position to write the documentation,
we need you to stand back and make good (better) choices.
