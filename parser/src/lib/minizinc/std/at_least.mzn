%-----------------------------------------------------------------------------%
% Requires at least 'n' variables in 'x' to take the value 'v'.
%-----------------------------------------------------------------------------%

include "at_least_int.mzn";
include "at_least_set.mzn";

predicate at_least(int: n, array[int] of var int: x, int: v) =
    at_least_int(n, x, v);

predicate at_least(int: n, array[int] of var set of int: x, set of int: v) =
    at_least_set(n, x, v);

% Synonyms for the above.

predicate atleast(int: n, array[int] of var int: x, int: v) =
    at_least_int(n, x, v);

predicate atleast(int: n, array[int] of var set of int: x, set of int: v) =
    at_least_set(n, x, v);
