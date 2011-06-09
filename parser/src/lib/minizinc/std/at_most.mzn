%-----------------------------------------------------------------------------%
% Requires at most 'n' variables in 'x' to take the value 'v'.
%-----------------------------------------------------------------------------%

include "at_most_int.mzn";
include "at_most_set.mzn";

predicate at_most(int: n, array[int] of var int: x, int: v) =
    at_most_int(n, x, v);

predicate at_most(int: n, array[int] of var set of int: x, set of int: v) =
    at_most_set(n, x, v);

% Synonyms for the above.

predicate atmost(int: n, array[int] of var int: x, int: v) =
    at_most_int(n, x, v);

predicate atmost(int: n, array[int] of var set of int: x, set of int: v) =
    at_most_set(n, x, v);
