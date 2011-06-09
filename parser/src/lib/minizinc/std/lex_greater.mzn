%-----------------------------------------------------------------------------%
% Requires that the array 'x' is strictly lexicographically greater than
% array 'y'.
% Compares them from first to last element, regardless of indices
%-----------------------------------------------------------------------------%

include "lex_less.mzn";

predicate lex_greater(array[int] of var int: x,
                      array[int] of var int: y) =
    lex_less(y, x);
 
predicate lex_greater(array[int] of var float: x,
                      array[int] of var float: y) =
    lex_less(y, x);

predicate lex_greater(array[int] of var set of int: x,
                      array[int] of var set of int: y) =
    lex_less(y, x);
