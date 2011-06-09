%-----------------------------------------------------------------------------%
% Requires that the array 'x' is lexicographically greater than or equal to
% array 'y'.  Compares them from first to last element, regardless of indices.
%-----------------------------------------------------------------------------%

include "lex_lesseq.mzn";

predicate lex_greatereq(array[int] of var int: x,
                      array[int] of var int: y) =
    lex_lesseq(y, x);
 
predicate lex_greatereq(array[int] of var float: x,
                      array[int] of var float: y) =
    lex_lesseq(y, x);

predicate lex_greatereq(array[int] of var set of int: x,
                      array[int] of var set of int: y) =
    lex_lesseq(y, x);
