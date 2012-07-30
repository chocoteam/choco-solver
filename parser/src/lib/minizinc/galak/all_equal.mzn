%-----------------------------------------------------------------------------%
% Constraints the array of objects 'x' to be all equal.
%-----------------------------------------------------------------------------%

include "all_equal_int.mzn";
include "all_equal_set.mzn";

predicate all_equal(array[int] of var int: x) =
	all_equal_int(x);

predicate all_equal(array[int] of var set of int: x) =
	all_equal_set(x);
