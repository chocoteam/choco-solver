%-----------------------------------------------------------------------------%
% Requires that 'x[i] in t' for all 'i in s'
%-----------------------------------------------------------------------------%
predicate roots(array[int] of var int: x, var set of int: s,
                                          var set of int: t) =
    forall(i in ub(s)) ( i in s -> x[i] in t );

