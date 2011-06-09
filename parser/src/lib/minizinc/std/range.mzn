%-----------------------------------------------------------------------------%
% Requires that the image of function 'x' (represented as an array) on set
% of values 's' is 't'.  ub(s) must be a subset of index_set(x) otherwise an
% assertion failure will occur.
%-----------------------------------------------------------------------------%
predicate range(array[int] of var int: x, var set of int: s,
                                          var set of int: t) =
    assert(ub(s) subset index_set(x),
        "range: upper bound of 's' must be a subset of the index set of 'x'",
    
        % All values in 's' must map to a value in 't'.
        forall(i in ub(s)) (
            i in s -> x[i] in t
        ) /\
        % All values in 't' must be mapped from a value in 's'.
        forall(i in ub(t)) (
            i in t -> exists(j in ub(s)) ( j in s /\ x[j] == i )
        )
    );

