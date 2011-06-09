%-----------------------------------------------------------------------------%
% Requires that the array 'x' is in increasing order (duplicates are allowed).
%-----------------------------------------------------------------------------%

predicate increasing_float(array[int] of var float: x) =
    forall(i in index_set(x) diff { min(index_set(x)) }) (x[i-1] <= x[i]);
