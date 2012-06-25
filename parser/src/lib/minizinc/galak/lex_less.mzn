%-----------------------------------------------------------------------------%
% Requires that the array 'x' is strictly lexicographically less than array 'y'.
% Compares them from first to last element, regardless of indices.
%-----------------------------------------------------------------------------%

include "lex_less_float.mzn";
include "lex_less_int.mzn";
include "lex_less_set.mzn";

predicate lex_less(array[int] of var int: x,
                   array[int] of var int: y) =
    lex_less_int(x, y);

predicate lex_less(array[int] of var float: x,
                   array[int] of var float: y) =
    lex_less_float(x, y);

predicate lex_less(array[int] of var set of int: x,
                   array[int] of var set of int: y) =
    lex_less_set(x, y);

% Alternative names for the above.
%
predicate lex_lt(array[int] of var int: x, array[int] of var int: y) =
	lex_less(x, y);

predicate lex_lt(array[int] of var float: x, array[int] of var float: y) =
	lex_less(x, y);

predicate lex_lt(array[int] of var set of int: x,
                  array[int] of var set of int: y) =
	lex_less(x, y);

