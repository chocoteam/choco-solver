%-----------------------------------------------------------------------------%
% the array of booleans 'b' is a representation of the set 's'
%-----------------------------------------------------------------------------%
predicate link_set_to_booleans(var set of int: s, array[int] of var bool: b) =
    assert(ub(s) subset index_set(b),
        "link_set_to_booleans: the index set of b must be a superset of the possible values of s",
        forall(i in index_set(b)) ( b[i] <-> i in s )
    );

