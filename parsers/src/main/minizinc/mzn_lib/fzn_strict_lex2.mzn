%-----------------------------------------------------------------------------%
% Require adjacent rows and adjacent columns in the array 'x' to be
% lexicographically ordered.  Adjacent rows and adjacent columns cannot be
% equal.
%-----------------------------------------------------------------------------%

predicate fzn_strict_lex2(array[int, int] of var int: x) =
    lex2Choco(x,true);
