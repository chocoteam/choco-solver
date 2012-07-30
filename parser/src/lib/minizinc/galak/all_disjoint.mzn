%-----------------------------------------------------------------------------%
% Ensures that every pair of sets in the array 'S' is disjoint.
%-----------------------------------------------------------------------------%

predicate all_disjoint(array[int] of var set of int: S) =
    forall(i,j in index_set(S) where i < j) ( disjoint(S[i], S[j]) );

include "disjoint.mzn";
