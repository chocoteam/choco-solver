Limiting the resolution
=======================

Built-in search limits
~~~~~~~~~~~~~~~~~~~~~~

The exploration of the search tree can be limited in various ways.
Some usual limits are provided in ``SearchMonitorFactory``, or ``SMF`` for short:

- ``limitTime`` stops the search when the given time limit has been reached. This is the most common limit, as many applications have a limited available runtime.
.. note::
    The potential search interruption occurs at the end of a propagation, i.e. it will not interrupt a propagation algorithm, so the overall runtime of the solver might exceed the time limit.

- ``limitSolution`` stops the search when the given solution limit has been reached.
- ``limitNode`` stops the search when the given search node limit has been reached.
- ``limitFail`` stops the search when the given fail limit has been reached.
- ``limitBacktrack`` stops the search when the given backtrack limit has been reached.

Custom search limits
~~~~~~~~~~~~~~~~~~~~

You can decide to interrupt the search process whenever you want with one of the following instructions: ::

 solver.getSearchLoop().reachLimit();
 solver.getSearchLoop().interrupt(String message);

Both options will interrupt the search process but only the first one will inform the solver that the search stops because of a limit. In other words, calling ::

 solver.hasReachedLimit()

will return false if the second option is used.




