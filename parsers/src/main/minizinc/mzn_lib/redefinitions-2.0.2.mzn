% This file contains redefinitions of standard builtins for version 2.0.2
% that can be overridden by solvers.

predicate symmetry_breaking_constraint(var bool: b) = b;

predicate redundant_constraint(var bool: b) = b;

predicate choco_array_int_element_nonshifted(var int: idx, int: os, array[int] of int: x, var int: c);
predicate choco_array_var_int_element_nonshifted(var int: idx, int: os, array[int] of var int: x, var int: c);
predicate choco_array_bool_element_nonshifted(var int: idx, int: os, array[int] of bool: x, var bool: c);
predicate choco_array_var_bool_element_nonshifted(var int: idx, int: os, array[int] of var bool: x, var bool: c);
predicate choco_array_var_set_element_nonshifted(var int: idx, int: os, array[int] of var set of int: x, var set of int: c);

predicate array_int_element_nonshifted(var int: idx, array[int] of int: x, var int: c) =
  choco_array_int_element_nonshifted(idx,min(index_set(x)),array1d(x),c);

predicate array_var_int_element_nonshifted(var int: idx, array[int] of var int: x, var int: c) =
  choco_array_var_int_element_nonshifted(idx,min(index_set(x)),array1d(x),c);

predicate array_bool_element_nonshifted(var int: idx, array[int] of bool: x, var bool: c) =
  choco_array_bool_element_nonshifted(idx,min(index_set(x)),array1d(x),c);

predicate array_var_bool_element_nonshifted(var int: idx, array[int] of var bool: x, var bool: c) =
  choco_array_var_bool_element_nonshifted(idx,min(index_set(x)),array1d(x),c);

predicate array_var_set_element_nonshifted(var int: idx, array[int] of var set of int: x, var set of int: c) =
  choco_array_var_set_element_nonshifted(idx, min(index_set(x)),array1d(x),c);