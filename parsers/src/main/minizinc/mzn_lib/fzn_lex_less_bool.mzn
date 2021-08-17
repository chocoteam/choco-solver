%-----------------------------------------------------------------------------%
% Requires that the array 'x' is strictly lexicographically less than array 'y'.
% Compares them from first to last element, regardless of indices
%-----------------------------------------------------------------------------%

predicate choco_fzn_lex_less(array[int] of var bool: x,
                       array[int] of var bool: y,
                       bool: strict);

predicate fzn_lex_less_bool(array[int] of var bool: x,
                       array[int] of var bool: y) =
    choco_fzn_lex_less(x,y,true);


