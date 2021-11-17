% This file contains redefinitions of standard builtins for version 2.5.2
% that can be overridden by solvers.

predicate choco_array_int_element2d_nonshifted(var int: idx1, int:os1, var int: idx2, int: os2, array[int] of var int: x, int: d1, int: d2, var int: c);
predicate choco_array_var_int_element2d_nonshifted(var int: idx1, int:os1, var int: idx2, int: os2, array[int] of var int: x, int: d1, int: d2, var int: c);
predicate choco_array_bool_element2d_nonshifted(var int: idx1, int:os1, var int: idx2, int: os2, array[int] of var bool: x, int: d1, int: d2, var bool: c);
predicate choco_array_var_bool_element2d_nonshifted(var int: idx1, int:os1, var int: idx2, int: os2, array[int] of var bool: x, int: d1, int: d2, var bool: c);

predicate array_int_element2d_nonshifted(var int: idx1, var int: idx2, array[int,int] of int: x, var int: c) =
  let {
    int: o1 = min(index_set_1of2(x));
    int: d1 = card(index_set_1of2(x));
    int: o2 = min(index_set_2of2(x));
    int: d2 = card(index_set_2of2(x));
  } in choco_array_int_element2d_nonshifted(idx1,o1,idx2, o2, array1d(x), d1, d2, c);

predicate array_var_int_element2d_nonshifted(var int: idx1, var int: idx2, array[int,int] of var int: x, var int: c) =
  let {
    int: o1 = min(index_set_1of2(x));
    int: d1 = card(index_set_1of2(x));
    int: o2 = min(index_set_2of2(x));
    int: d2 = card(index_set_2of2(x));
  } in choco_array_var_int_element2d_nonshifted(idx1,o1,idx2, o2, array1d(x), d1, d2, c);

predicate array_bool_element2d_nonshifted(var int: idx1, var int: idx2, array[int,int] of bool: x, var bool: c) =
  let {
    int: o1 = min(index_set_1of2(x));
    int: d1 = card(index_set_1of2(x));
    int: o2 = min(index_set_2of2(x));
    int: d2 = card(index_set_2of2(x));
  } in choco_array_var_bool_element2d_nonshifted(idx1,o1,idx2, o2, array1d(x), d1, d2, c);

predicate array_var_bool_element2d_nonshifted(var int: idx1, var int: idx2, array[int,int] of var bool: x, var bool: c) =
  let {
    int: o1 = min(index_set_1of2(x));
    int: d1 = card(index_set_1of2(x));
    int: o2 = min(index_set_2of2(x));
    int: d2 = card(index_set_2of2(x));
  } in choco_array_var_bool_element2d_nonshifted(idx1,o1,idx2, o2, array1d(x), d1, d2, c);

