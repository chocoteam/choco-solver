%-----------------------------------------------------------------------------%
% Require adjacent rows and adjacent columns in the array 'x' to be
% lexicographically ordered.  Adjacent rows and adjacent columns may be equal.
%-----------------------------------------------------------------------------%
predicate choco_fzn_lex2(array[int, int] of var int: x, bool: strict);

predicate fzn_lex2(array[int, int] of var int: x) =
    choco_fzn_lex2(x,false);

