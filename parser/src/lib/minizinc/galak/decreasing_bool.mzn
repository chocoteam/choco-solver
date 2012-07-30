%-----------------------------------------------------------------------------%
% Requires that the array 'x' is in decreasing order (duplicates are allowed).
%-----------------------------------------------------------------------------%

predicate decreasing_bool(array[int] of var bool: x) =
    forall(i in index_set(x) diff { min(index_set(x)) }) (x[i-1] >= x[i]);
