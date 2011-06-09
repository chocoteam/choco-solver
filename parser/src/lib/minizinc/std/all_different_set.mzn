%-----------------------------------------------------------------------------%
% Constrains the array of objects 'x' to be all different.
%-----------------------------------------------------------------------------%

predicate all_different_set(array[int] of var set of int: x) =
    forall(i,j in index_set(x) where i < j) ( x[i] != x[j] );
