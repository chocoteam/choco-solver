%-----------------------------------------------------------------------------%
% Require that the value_precede constraint is true for every pair of adjacent
% integers in 'c' in the array 'x'.
%-----------------------------------------------------------------------------%

include "value_precede_chain_int.mzn";
include "value_precede_chain_set.mzn";

predicate value_precede_chain(array[int] of int: c, array[int] of var int: x) =
    value_precede_chain_int(c, x);
predicate value_precede_chain(array[int] of int: c, array[int] of var set of int: x) =
    value_precede_chain_set(c, x);
