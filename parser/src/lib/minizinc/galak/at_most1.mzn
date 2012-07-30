%-----------------------------------------------------------------------------%
% Requires that each pair of sets in s overlap in at most one element.
%-----------------------------------------------------------------------------%
predicate at_most1(array[int] of var set of int: s) =
    forall(i,j in index_set(s) where i < j) (
         card(s[i] intersect s[j]) <= 1 );

% Synonym for the above.

predicate atmost1(array[int] of var set of int: s) = at_most1(s);
