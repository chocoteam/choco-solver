%-----------------------------------------------------------------------------%
% Requires that the array 'x' is lexicographically less than or equal to
% array 'y'.  Compares them from first to last element, regardless of indices.
%-----------------------------------------------------------------------------%

include "lex_lesseq_float.mzn";
include "lex_lesseq_int.mzn";
include "lex_lesseq_set.mzn";

predicate lex_lesseq(array[int] of var float: x,
                     array[int] of var float: y) =
    lex_lesseq_float(x, y);

predicate lex_lesseq(array[int] of var int: x,
                     array[int] of var int: y) =
    lex_lesseq_int(x, y);

predicate lex_lesseq(array[int] of var set of int: x,
                     array[int] of var set of int: y) =
    lex_lesseq_set(x, y);

% Alternative names for the above.
%
predicate lex_leq(array[int] of var int: x, array[int] of var int: y) =
	lex_lesseq(x, y);

predicate lex_leq(array[int] of var float: x, array[int] of var float: y) =
	lex_lesseq(x, y);

predicate lex_leq(array[int] of var set of int: x,
                  array[int] of var set of int: y) =
	lex_lesseq(x, y);

