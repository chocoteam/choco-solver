%-----------------------------------------------------------------------------%
% Requires exactly 'n' variables in 'x' to take the value 'v'.
%-----------------------------------------------------------------------------%

include "exactly_int.mzn";
include "exactly_set.mzn";

predicate exactly(int: n, array[int] of var int: x, int: v) =
    exactly_int(n, x, v);

predicate exactly(int: n, array[int] of var set of int: x, set of int: v) =
    exactly_set(n, x, v);

