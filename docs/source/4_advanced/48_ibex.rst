Ibex
====


    "IBEX is a C++ library for constraint processing over real numbers.

    It provides reliable algorithms for handling non-linear constraints.
    In particular, roundoff errors are also taken into account.
    It is based on interval arithmetic and affine arithmetic."
    -- http://www.ibex-lib.org/

To manage continuous constraints with Choco, an interface with Ibex has been done.
It needs Ibex to be installed on your system.
Then, simply declare the following VM options:

.. code-block:: none

    -Djava.library.path=/path/to/Ibex/lib

The path `/path/to/Ibex/lib` points to the `lib` directory of the Ibex installation directory.


Installing Ibex
---------------

See the `installation instructions <http://www.ibex-lib.org/doc/install.html>`_ of Ibex to complied Ibex on your system.
More specially, take a look at `Installation as a dynamic library <http://www.ibex-lib.org/doc/install.html#installation-as-a-dynamic-library>`_
and do not forget to add the ``--with-java-package=solver.constraints.real`` configuration option.

Once the installation is completed, the JVM needs to know where Ibex is installed to fully benefit from the Choco-Ibex bridge and declare real variables and constraints.