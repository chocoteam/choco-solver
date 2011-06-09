%-----------------------------------------------------------------------------%
% Requires that the array 'x' is in increasing order (duplicates are allowed).
%-----------------------------------------------------------------------------%

include "increasing_bool.mzn";
include "increasing_float.mzn";
include "increasing_int.mzn";
include "increasing_set.mzn";

predicate increasing(array[int] of var bool: x) =
    increasing_bool(x);

predicate increasing(array[int] of var float: x) =
    increasing_float(x);

predicate increasing(array[int] of var int: x) =
    increasing_int(x);

predicate increasing(array[int] of var set of int: x) =
    increasing_set(x);

