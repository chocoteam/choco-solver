The solver
==========

The object :code:`Solver` is the key component. It is built as following: ::

 Solver solver = new Solver();

or::

 Solver solver = new Solver("my problem");


This should be the first instruction, prior to any other modelling instructions.
The solver stores the declared variables and the posted constraints.
It eases the resolution
