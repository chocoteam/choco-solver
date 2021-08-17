% This file contains redefinitions of standard builtins that can be overridden
% by solvers.

/***
  @groupdef flatzinc.two FlatZinc builtins added in MiniZinc 2.0.0.
  
  These functions and predicates define built-in operations of the MiniZinc language
  that have been added in MiniZinc 2.0.0. Solvers that support these natively need
  to include a file called redefinitions-2.0.mzn in their solver library that
  redefines these predicates as builtins.
  
*/

/** @group flatzinc.two Reified clause constraint.
  Constrains \( \a b \leftrightarrow \bigvee_i \a as[i] \lor \bigvee_j \lnot \a bs[j] \)
 */
predicate bool_clause_reif(array[int] of var bool: as,
                           array[int] of var bool: bs,
                           var bool: b);

/** @group flatzinc.two Constrains \a m to be the maximum value in array \a x. */
predicate array_int_maximum(var int: m, array[int] of var int: x);

/** @group flatzinc.two Constrains \a m to be the minimum value in array \a x. */
predicate array_int_minimum(var int: m, array[int] of var int: x);
