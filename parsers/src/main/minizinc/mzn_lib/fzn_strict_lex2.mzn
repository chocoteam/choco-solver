%-----------------------------------------------------------------------------%
% Require adjacent rows and adjacent columns in the array 'x' to be
% lexicographically ordered.  Adjacent rows and adjacent columns cannot be
% equal.
%-----------------------------------------------------------------------------%
predicate choco_fzn_lex2(array[int] of var int: x, bool: strict);

predicate fzn_strict_lex2(array[int, int] of var int: x) =
    choco_fzn_lex2(array1d(x),true);
