%-----------------------------------------------------------------------------%
% Requires that the array 'x' is in decreasing order (duplicates are allowed).
%-----------------------------------------------------------------------------%

predicate decreasing_int(array[int] of var int: x) =
    forall(i in index_set(x) diff { min(index_set(x)) }) (x[i-1] >= x[i]);

