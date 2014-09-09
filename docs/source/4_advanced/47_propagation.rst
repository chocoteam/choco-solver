Propagation
===========

One may want to propagate each constraint manually.
This can be achieved by calling ``solver.propagate()``.
This method runs, in turn, the domain reduction algorithms of the constraints until it reaches a fix point.
It may throw a ``ContradictionException`` if a contradiction occurs.
In that case, the propagation engine must be flushed calling ``solver.getEngine().flush()``
to ensure there is no pending events.

.. warning::

 If there are still pending events in the propagation engine, the propagation may results in unexpected results.