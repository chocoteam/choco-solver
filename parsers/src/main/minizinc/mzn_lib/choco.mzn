% This file contains predicate definitions for additional constraints available
% in the Choco FlatZinc interpreter.

%-----------------------------------------------------------------------------%
% Stable keysort
%-----------------------------------------------------------------------------%
predicate choco_stable_keysort(array[int] of var int: unsorted,
                         array[int] of var int: permutations,
                         array[int] of var int: sorted,
                         int : keys);


predicate stable_keysort(array[int,int] of var int: unsorted,
                         array[int] of var int: permutations,
                         array[int,int] of var int: sorted,
                         int : keys) = 
choco_stable_keysort(array1d(unsorted), permutations, array1d(sorted), keys);

predicate stable_keysort(array[int,int] of var int: unsorted,
                         array[int,int] of var int: sorted,
                         int : keys) =
let {
    int: n = card(index_set_1of2(unsorted));
    array[1..n] of var 1..n: permutations;
} in (
    stable_keysort(unsorted, permutations, sorted, keys)
);                         