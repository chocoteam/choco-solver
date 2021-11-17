% This file contains redefinitions of standard builtins for version 2.2.1
% that can be overridden by solvers.

/** @group flatzinc.int Constrains \a z = \(\a x ^ {\a y}\) */
predicate int_pow_fixed(var int: x, int: y, var int: z);

