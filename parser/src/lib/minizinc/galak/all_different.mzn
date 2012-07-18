%-----------------------------------------------------------------------------%
% Constrains the array of objects 'x' to be all different.
%-----------------------------------------------------------------------------%

include "all_different_int.mzn";
include "all_different_set.mzn";

predicate all_different(array[int] of var int: x) =
    all_different_int(x);

predicate all_different(array[int] of var set of int: x) =
    all_different_set(x);

% Synonyms for the above.

predicate alldifferent(array[int] of var int: x) =
    all_different_int(x);

predicate alldifferent(array[int] of var set of int: x) =
    all_different_set(x);

